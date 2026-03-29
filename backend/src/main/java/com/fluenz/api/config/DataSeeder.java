package com.fluenz.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluenz.api.entity.DefaultCatalogVersion;
import com.fluenz.api.entity.DefaultChunk;
import com.fluenz.api.entity.DefaultSituation;
import com.fluenz.api.entity.DefaultSubPhrase;
import com.fluenz.api.entity.DefaultTopic;
import com.fluenz.api.entity.User;
import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.Profession;
import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import com.fluenz.api.entity.enums.Level;
import com.fluenz.api.entity.enums.UserRole;
import com.fluenz.api.repository.DefaultCatalogVersionRepository;
import com.fluenz.api.repository.ProfessionRepository;
import com.fluenz.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProfessionRepository professionRepository;
    private final DefaultCatalogVersionRepository defaultCatalogVersionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) {
        log.info("DataSeeder starting: professions={}, defaultCatalogVersions={}",
                professionRepository.count(),
                defaultCatalogVersionRepository.count());

        if (professionRepository.count() == 0) {
            List<Profession> professions = List.of(
                    createProfession("Software Engineer", "Phát triển phần mềm, code review, standup, technical discussions"),
                    createProfession("Marketing", "Chiến lược marketing, content, campaigns, client presentations"),
                    createProfession("Finance & Accounting", "Báo cáo tài chính, analysis, auditing, investor relations"),
                    createProfession("Human Resources", "Tuyển dụng, onboarding, employee relations, performance reviews"),
                    createProfession("Sales", "Pitching, negotiation, client relationship, closing deals"),
                    createProfession("Customer Service", "Hỗ trợ khách hàng, complaint handling, feedback management"),
                    createProfession("Healthcare", "Patient communication, medical discussions, team coordination"),
                    createProfession("Education", "Giảng dạy, student communication, academic discussions"),
                    createProfession("Design", "Design reviews, client feedback, creative briefs, presentations"),
                    createProfession("F&B / Hospitality", "Phục vụ khách hàng, order management, team coordination")
            );
            professionRepository.saveAll(professions);
            log.info("Seeded {} professions", professions.size());
        }

        ensurePublishedDefaultCatalogExists();
        backfillExistingUsers();
    }

    private Profession createProfession(String name, String description) {
        return Profession.builder()
                .name(name)
                .description(description)
                .build();
    }

    private DefaultCatalogVersion buildStarterDefaultCatalog() {
        Integer nextVersion = defaultCatalogVersionRepository.findAllByOrderByVersionNumberDesc().stream()
                .findFirst()
                .map(DefaultCatalogVersion::getVersionNumber)
                .orElse(0) + 1;

        DefaultCatalogVersion version = DefaultCatalogVersion.builder()
                .versionNumber(nextVersion)
                .title("FluenZ Default Path v" + nextVersion)
                .status(DefaultCatalogVersionStatus.PUBLISHED)
                .published(true)
                .build();

        DefaultTopic topic = DefaultTopic.builder()
                .name("Giao tiep hang ngay")
                .orderIndex(0)
                .catalogVersion(version)
                .build();

        DefaultSituation situation = DefaultSituation.builder()
                .title("Goi mon tai nha hang")
                .description("Luyen cac cum tu co ban khi goi mon, hoi mon dac biet, va thanh toan.")
                .level(Level.BEGINNER)
                .orderIndex(0)
                .topic(topic)
                .build();

        DefaultChunk chunk = DefaultChunk.builder()
                .contextQuestion("What would you like to order?")
                .contextTranslation("Ban muon goi mon gi?")
                .rootSentence("I'd like to order ___, please.")
                .rootTranslation("Toi muon goi ___, vui long.")
                .rootIpa("/aɪd laɪk tuː ˈɔːrdər ___ pliːz/")
                .orderIndex(0)
                .situation(situation)
                .build();

        chunk.getSubPhrases().add(DefaultSubPhrase.builder()
                .text("a bowl of pho")
                .translation("mot to pho")
                .ipa("/ə boʊl əv fʌ/")
                .distractors(toJson(List.of("a glass of water", "the bill right now")))
                .orderIndex(0)
                .chunk(chunk)
                .build());
        chunk.getSubPhrases().add(DefaultSubPhrase.builder()
                .text("without onions")
                .translation("khong hanh")
                .ipa("/wɪˈðaʊt ˈʌnjənz/")
                .distractors(toJson(List.of("with extra sugar", "for tomorrow morning")))
                .orderIndex(1)
                .chunk(chunk)
                .build());
        chunk.getSubPhrases().add(DefaultSubPhrase.builder()
                .text("for here")
                .translation("dung tai quan")
                .ipa("/fɔːr hɪr/")
                .distractors(toJson(List.of("to the airport", "after lunch")))
                .orderIndex(2)
                .chunk(chunk)
                .build());

        situation.getChunks().add(chunk);
        topic.getSituations().add(situation);
        version.getTopics().add(topic);
        return version;
    }

    private void ensurePublishedDefaultCatalogExists() {
        boolean hasPublishedVersion = defaultCatalogVersionRepository.findFirstByPublishedTrueOrderByVersionNumberDesc().isPresent();
        if (hasPublishedVersion) {
            log.info("Default catalog startup check: published version already exists");
            return;
        }

        DefaultCatalogVersion defaultVersion = buildStarterDefaultCatalog();
        defaultCatalogVersionRepository.save(defaultVersion);
        log.warn("No published default catalog found at startup. Seeded fallback default catalog version {}", defaultVersion.getVersionNumber());
    }

    private void backfillExistingUsers() {
        DefaultCatalogVersion publishedVersion = defaultCatalogVersionRepository.findFirstByPublishedTrueOrderByVersionNumberDesc()
                .orElse(null);

        int updatedUsers = 0;
        for (User user : userRepository.findAll()) {
            boolean changed = false;

            if (user.getRole() == null) {
                user.setRole(UserRole.USER);
                changed = true;
            }

            if (user.getPreferredLearningMode() == null) {
                user.setPreferredLearningMode(LearningMode.DEFAULT);
                changed = true;
            }

            if (user.getActiveDefaultCatalogVersion() == null && publishedVersion != null) {
                user.setActiveDefaultCatalogVersion(publishedVersion);
                changed = true;
            }

            if (changed) {
                userRepository.save(user);
                updatedUsers++;
            }
        }

        if (updatedUsers > 0) {
            log.warn("Backfilled {} existing users with missing role/mode/default version", updatedUsers);
        } else {
            log.info("User backfill check: no existing users needed updates");
        }
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
