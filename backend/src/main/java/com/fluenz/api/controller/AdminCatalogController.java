package com.fluenz.api.controller;

import com.fluenz.api.dto.request.CreateDefaultSituationRequest;
import com.fluenz.api.dto.request.CreateDefaultChunkRequest;
import com.fluenz.api.dto.request.CreateDefaultSubPhraseRequest;
import com.fluenz.api.dto.request.CreateDefaultTopicRequest;
import com.fluenz.api.dto.request.UpdateDefaultSituationRequest;
import com.fluenz.api.dto.request.UpdateDefaultChunkRequest;
import com.fluenz.api.dto.request.UpdateDefaultSubPhraseRequest;
import com.fluenz.api.dto.request.UpdateDefaultTopicRequest;
import com.fluenz.api.dto.response.AdminCatalogVersionResponse;
import com.fluenz.api.dto.response.LearningPathResponse;
import com.fluenz.api.dto.response.CsvImportResponse;
import com.fluenz.api.dto.response.UploadedMediaResponse;
import com.fluenz.api.service.CsvCatalogImportService;
import com.fluenz.api.service.DefaultCatalogAdminService;
import com.fluenz.api.service.LocalMediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/catalog")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final DefaultCatalogAdminService defaultCatalogAdminService;
    private final LocalMediaService localMediaService;
    private final CsvCatalogImportService csvCatalogImportService;

    @GetMapping("/versions")
    public ResponseEntity<List<AdminCatalogVersionResponse>> listVersions() {
        return ResponseEntity.ok(defaultCatalogAdminService.listVersions());
    }

    @PostMapping("/versions")
    public ResponseEntity<AdminCatalogVersionResponse> createDraftVersion() {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultCatalogAdminService.createDraftVersion());
    }

    @GetMapping("/versions/{versionId}/preview")
    public ResponseEntity<LearningPathResponse> previewVersion(@PathVariable UUID versionId) {
        return ResponseEntity.ok(defaultCatalogAdminService.previewVersion(versionId));
    }

    @PostMapping("/versions/{versionId}/publish")
    public ResponseEntity<AdminCatalogVersionResponse> publishVersion(@PathVariable UUID versionId) {
        return ResponseEntity.ok(defaultCatalogAdminService.publishVersion(versionId));
    }

    @PostMapping("/versions/{versionId}/import-csv")
    public ResponseEntity<CsvImportResponse> importCsv(
            @PathVariable UUID versionId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(csvCatalogImportService.importCsv(versionId, file));
    }

    @PostMapping("/versions/{versionId}/topics")
    public ResponseEntity<Map<String, Object>> createTopic(
            @PathVariable UUID versionId,
            @Valid @RequestBody CreateDefaultTopicRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultCatalogAdminService.createTopic(versionId, request));
    }

    @PutMapping("/topics/{topicId}")
    public ResponseEntity<Map<String, Object>> updateTopic(
            @PathVariable UUID topicId,
            @Valid @RequestBody UpdateDefaultTopicRequest request
    ) {
        return ResponseEntity.ok(defaultCatalogAdminService.updateTopic(topicId, request));
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID topicId) {
        defaultCatalogAdminService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/topics/{topicId}/situations")
    public ResponseEntity<Map<String, Object>> createSituation(
            @PathVariable UUID topicId,
            @Valid @RequestBody CreateDefaultSituationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultCatalogAdminService.createSituation(topicId, request));
    }

    @PutMapping("/situations/{situationId}")
    public ResponseEntity<Map<String, Object>> updateSituation(
            @PathVariable UUID situationId,
            @Valid @RequestBody UpdateDefaultSituationRequest request
    ) {
        return ResponseEntity.ok(defaultCatalogAdminService.updateSituation(situationId, request));
    }

    @DeleteMapping("/situations/{situationId}")
    public ResponseEntity<Void> deleteSituation(@PathVariable UUID situationId) {
        defaultCatalogAdminService.deleteSituation(situationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/situations/{situationId}/chunks")
    public ResponseEntity<Map<String, Object>> createChunk(
            @PathVariable UUID situationId,
            @Valid @RequestBody CreateDefaultChunkRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultCatalogAdminService.createChunk(situationId, request));
    }

    @PutMapping("/chunks/{chunkId}")
    public ResponseEntity<Map<String, Object>> updateChunk(
            @PathVariable UUID chunkId,
            @Valid @RequestBody UpdateDefaultChunkRequest request
    ) {
        return ResponseEntity.ok(defaultCatalogAdminService.updateChunk(chunkId, request));
    }

    @DeleteMapping("/chunks/{chunkId}")
    public ResponseEntity<Void> deleteChunk(@PathVariable UUID chunkId) {
        defaultCatalogAdminService.deleteChunk(chunkId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chunks/{chunkId}/sub-phrases")
    public ResponseEntity<Map<String, Object>> createSubPhrase(
            @PathVariable UUID chunkId,
            @Valid @RequestBody CreateDefaultSubPhraseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultCatalogAdminService.createSubPhrase(chunkId, request));
    }

    @PutMapping("/sub-phrases/{subPhraseId}")
    public ResponseEntity<Map<String, Object>> updateSubPhrase(
            @PathVariable UUID subPhraseId,
            @Valid @RequestBody UpdateDefaultSubPhraseRequest request
    ) {
        return ResponseEntity.ok(defaultCatalogAdminService.updateSubPhrase(subPhraseId, request));
    }

    @DeleteMapping("/sub-phrases/{subPhraseId}")
    public ResponseEntity<Void> deleteSubPhrase(@PathVariable UUID subPhraseId) {
        defaultCatalogAdminService.deleteSubPhrase(subPhraseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/media/upload")
    public ResponseEntity<UploadedMediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "folder", defaultValue = "catalog") String folder
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(localMediaService.upload(file, folder));
    }
}
