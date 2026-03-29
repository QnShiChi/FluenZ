package com.fluenz.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.dto.response.CsvImportResponse;
import com.fluenz.api.entity.*;
import com.fluenz.api.entity.enums.Level;
import com.fluenz.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvCatalogImportService {

    private final DefaultCatalogVersionRepository versionRepository;
    private final DefaultTopicRepository topicRepository;
    private final DefaultSituationRepository situationRepository;
    private final DefaultChunkRepository chunkRepository;
    private final DefaultSubPhraseRepository subPhraseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Expected CSV columns
    private static final String[] EXPECTED_HEADERS = {
            "topic_name",
            "situation_title", "situation_description", "situation_level",
            "chunk_context_question", "chunk_context_translation",
            "chunk_root_sentence", "chunk_root_translation", "chunk_root_ipa",
            "sub_phrase_text", "sub_phrase_translation", "sub_phrase_ipa",
            "sub_phrase_distractors"
    };

    @Transactional
    public CsvImportResponse importCsv(UUID versionId, MultipartFile file) {
        DefaultCatalogVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Catalog version not found"));

        List<CsvImportResponse.RowError> errors = new ArrayList<>();
        int totalRows = 0;
        int importedRows = 0;

        // Caches to reuse existing/created entities within this import
        Map<String, DefaultTopic> topicCache = new LinkedHashMap<>();
        Map<String, DefaultSituation> situationCache = new LinkedHashMap<>();
        Map<String, DefaultChunk> chunkCache = new LinkedHashMap<>();

        // Pre-populate caches with existing entities in this version
        for (DefaultTopic t : version.getTopics()) {
            topicCache.put(t.getName().trim().toLowerCase(), t);
            for (DefaultSituation s : t.getSituations()) {
                String sitKey = t.getName().trim().toLowerCase() + "|" + s.getTitle().trim().toLowerCase();
                situationCache.put(sitKey, s);
                for (DefaultChunk c : s.getChunks()) {
                    String chunkKey = sitKey + "|" + c.getRootSentence().trim().toLowerCase();
                    chunkCache.put(chunkKey, c);
                }
            }
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                errors.add(CsvImportResponse.RowError.builder()
                        .row(0).field("file").message("File is empty").build());
                return buildResponse(0, 0, errors);
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim().toLowerCase(), i);
            }

            // Validate required headers
            for (String expected : EXPECTED_HEADERS) {
                if (!headerIndex.containsKey(expected)) {
                    errors.add(CsvImportResponse.RowError.builder()
                            .row(0).field(expected).message("Missing required column: " + expected).build());
                }
            }
            if (!errors.isEmpty()) {
                return buildResponse(0, 0, errors);
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                totalRows++;

                if (line.trim().isEmpty()) continue;

                String[] values = parseCsvLine(line);
                try {
                    importRow(version, values, headerIndex, rowNum, errors,
                            topicCache, situationCache, chunkCache);
                    importedRows++;
                } catch (RowSkippedException e) {
                    // Error already added in importRow
                }
            }
        } catch (Exception e) {
            log.error("CSV import failed", e);
            errors.add(CsvImportResponse.RowError.builder()
                    .row(0).field("file").message("Failed to read CSV file: " + e.getMessage()).build());
        }

        return buildResponse(totalRows, importedRows, errors);
    }

    private void importRow(DefaultCatalogVersion version, String[] values, Map<String, Integer> headerIndex,
                           int rowNum, List<CsvImportResponse.RowError> errors,
                           Map<String, DefaultTopic> topicCache,
                           Map<String, DefaultSituation> situationCache,
                           Map<String, DefaultChunk> chunkCache) {

        String topicName = getCell(values, headerIndex, "topic_name");
        String sitTitle = getCell(values, headerIndex, "situation_title");
        String sitDesc = getCell(values, headerIndex, "situation_description");
        String sitLevel = getCell(values, headerIndex, "situation_level");
        String chunkQuestion = getCell(values, headerIndex, "chunk_context_question");
        String chunkTranslation = getCell(values, headerIndex, "chunk_context_translation");
        String chunkRoot = getCell(values, headerIndex, "chunk_root_sentence");
        String chunkRootTranslation = getCell(values, headerIndex, "chunk_root_translation");
        String chunkRootIpa = getCell(values, headerIndex, "chunk_root_ipa");
        String spText = getCell(values, headerIndex, "sub_phrase_text");
        String spTranslation = getCell(values, headerIndex, "sub_phrase_translation");
        String spIpa = getCell(values, headerIndex, "sub_phrase_ipa");
        String spDistractors = getCell(values, headerIndex, "sub_phrase_distractors");

        // Validate required fields
        if (isBlank(topicName)) {
            errors.add(error(rowNum, "topic_name", "Topic name is required"));
            throw new RowSkippedException();
        }
        if (isBlank(sitTitle)) {
            errors.add(error(rowNum, "situation_title", "Situation title is required"));
            throw new RowSkippedException();
        }
        if (isBlank(chunkRoot)) {
            errors.add(error(rowNum, "chunk_root_sentence", "Root sentence is required"));
            throw new RowSkippedException();
        }
        if (isBlank(spText)) {
            errors.add(error(rowNum, "sub_phrase_text", "Sub phrase text is required"));
            throw new RowSkippedException();
        }

        Level level;
        try {
            level = isBlank(sitLevel) ? Level.BEGINNER : Level.valueOf(sitLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.add(error(rowNum, "situation_level", "Invalid level: " + sitLevel + ". Must be BEGINNER, INTERMEDIATE, or ADVANCED."));
            throw new RowSkippedException();
        }

        // Resolve or create topic
        String topicKey = topicName.trim().toLowerCase();
        DefaultTopic topic = topicCache.computeIfAbsent(topicKey, k -> {
            int nextOrder = version.getTopics().stream()
                    .map(DefaultTopic::getOrderIndex).max(Integer::compareTo).orElse(-1) + 1;
            DefaultTopic t = DefaultTopic.builder()
                    .name(topicName.trim())
                    .orderIndex(nextOrder)
                    .catalogVersion(version)
                    .build();
            DefaultTopic saved = topicRepository.save(t);
            version.getTopics().add(saved);
            return saved;
        });

        // Resolve or create situation
        String sitKey = topicKey + "|" + sitTitle.trim().toLowerCase();
        DefaultSituation situation = situationCache.computeIfAbsent(sitKey, k -> {
            int nextOrder = topic.getSituations().stream()
                    .map(DefaultSituation::getOrderIndex).max(Integer::compareTo).orElse(-1) + 1;
            DefaultSituation s = DefaultSituation.builder()
                    .title(sitTitle.trim())
                    .description(sitDesc != null ? sitDesc.trim() : "")
                    .level(level)
                    .orderIndex(nextOrder)
                    .topic(topic)
                    .build();
            DefaultSituation saved = situationRepository.save(s);
            topic.getSituations().add(saved);
            return saved;
        });

        // Resolve or create chunk
        String chunkKey = sitKey + "|" + chunkRoot.trim().toLowerCase();
        DefaultChunk chunk = chunkCache.computeIfAbsent(chunkKey, k -> {
            int nextOrder = situation.getChunks().stream()
                    .map(DefaultChunk::getOrderIndex).max(Integer::compareTo).orElse(-1) + 1;
            DefaultChunk c = DefaultChunk.builder()
                    .contextQuestion(chunkQuestion != null ? chunkQuestion.trim() : "")
                    .contextTranslation(chunkTranslation != null ? chunkTranslation.trim() : "")
                    .rootSentence(chunkRoot.trim())
                    .rootTranslation(chunkRootTranslation != null ? chunkRootTranslation.trim() : "")
                    .rootIpa(chunkRootIpa != null ? chunkRootIpa.trim() : "")
                    .orderIndex(nextOrder)
                    .situation(situation)
                    .build();
            DefaultChunk saved = chunkRepository.save(c);
            situation.getChunks().add(saved);
            return saved;
        });

        // Always create a new sub-phrase
        int nextSpOrder = chunk.getSubPhrases().stream()
                .map(DefaultSubPhrase::getOrderIndex).max(Integer::compareTo).orElse(-1) + 1;

        List<String> distractors = new ArrayList<>();
        if (!isBlank(spDistractors)) {
            distractors = Arrays.stream(spDistractors.split("\\|"))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
        }

        DefaultSubPhrase sp = DefaultSubPhrase.builder()
                .text(spText.trim())
                .translation(spTranslation != null ? spTranslation.trim() : "")
                .ipa(spIpa != null ? spIpa.trim() : "")
                .distractors(toJson(distractors))
                .orderIndex(nextSpOrder)
                .chunk(chunk)
                .build();
        DefaultSubPhrase saved = subPhraseRepository.save(sp);
        chunk.getSubPhrases().add(saved);
    }

    private String getCell(String[] values, Map<String, Integer> headerIndex, String column) {
        Integer idx = headerIndex.get(column);
        if (idx == null || idx >= values.length) return null;
        String val = values[idx].trim();
        return val.isEmpty() ? null : val;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private CsvImportResponse.RowError error(int row, String field, String message) {
        return CsvImportResponse.RowError.builder().row(row).field(field).message(message).build();
    }

    private CsvImportResponse buildResponse(int total, int imported, List<CsvImportResponse.RowError> errors) {
        return CsvImportResponse.builder()
                .totalRows(total)
                .importedRows(imported)
                .skippedRows(total - imported)
                .errors(errors)
                .build();
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * Simple CSV line parser that handles quoted fields with commas inside.
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result.toArray(new String[0]);
    }

    private static class RowSkippedException extends RuntimeException {
    }
}
