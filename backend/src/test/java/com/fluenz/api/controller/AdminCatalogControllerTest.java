package com.fluenz.api.controller;

import com.fluenz.api.entity.DefaultCatalogVersion;
import com.fluenz.api.entity.User;
import com.fluenz.api.entity.enums.DefaultCatalogVersionStatus;
import com.fluenz.api.entity.enums.LearningMode;
import com.fluenz.api.entity.enums.Level;
import com.fluenz.api.entity.enums.UserRole;
import com.fluenz.api.repository.DefaultCatalogVersionRepository;
import com.fluenz.api.repository.UserRepository;
import com.fluenz.api.service.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminCatalogControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private DefaultCatalogVersionRepository versionRepository;
    @Autowired private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Create admin user if not exists
        if (userRepository.findByEmail("admin@test.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@test.com")
                    .username("admin_test")
                    .passwordHash("$2a$10$dummy")
                    .currentLevel(Level.BEGINNER)
                    .role(UserRole.ADMIN)
                    .preferredLearningMode(LearningMode.DEFAULT)
                    .build();
            userRepository.save(admin);
        }

        // Create regular user if not exists
        if (userRepository.findByEmail("user@test.com").isEmpty()) {
            User regularUser = User.builder()
                    .email("user@test.com")
                    .username("user_test")
                    .passwordHash("$2a$10$dummy")
                    .currentLevel(Level.BEGINNER)
                    .role(UserRole.USER)
                    .preferredLearningMode(LearningMode.DEFAULT)
                    .build();
            userRepository.save(regularUser);
        }

        adminToken = jwtService.generateAccessToken("admin@test.com");
        userToken = jwtService.generateAccessToken("user@test.com");
    }

    // ===================== Role Protection Tests =====================

    @Test
    @Order(1)
    void regularUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    void adminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void unauthenticatedUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/catalog/versions"))
                .andExpect(status().isForbidden());
    }

    // ===================== Draft Creation =====================

    @Test
    @Order(10)
    void adminCanCreateDraftVersion() throws Exception {
        mockMvc.perform(post("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.published").value(false));
    }

    // ===================== Publish Safeguards =====================

    @Test
    @Order(20)
    void cannotPublishEmptyVersion() throws Exception {
        // Create a draft
        String result = mockMvc.perform(post("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String versionId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(result).get("id").asText();

        // Try to publish empty version — should fail with error (400 from GlobalExceptionHandler)
        mockMvc.perform(post("/api/admin/catalog/versions/" + versionId + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("topic")));
    }

    // ===================== CSV Import =====================

    @Test
    @Order(30)
    void csvImportCreatesFullContentTree() throws Exception {
        // Create a draft version
        String result = mockMvc.perform(post("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String versionId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(result).get("id").asText();

        // Valid CSV content
        String csv = "topic_name,situation_title,situation_description,situation_level,"
                + "chunk_context_question,chunk_context_translation,chunk_root_sentence,"
                + "chunk_root_translation,chunk_root_ipa,sub_phrase_text,sub_phrase_translation,"
                + "sub_phrase_ipa,sub_phrase_distractors\n"
                + "Greetings,Meeting someone,\"Meeting a new person\",BEGINNER,"
                + "How do you greet?,Ban chao nhu the nao?,\"Nice to meet you\","
                + "Rat vui duoc gap ban,/naɪs tə miːt juː/,nice to meet,rat vui duoc gap,"
                + "/naɪs tə miːt/,nice to see|glad to meet\n"
                + "Greetings,Meeting someone,\"Meeting a new person\",BEGINNER,"
                + "How do you greet?,Ban chao nhu the nao?,\"Nice to meet you\","
                + "Rat vui duoc gap ban,/naɪs tə miːt juː/,you,ban,"
                + "/juː/,your|yours";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/admin/catalog/versions/" + versionId + "/import-csv")
                        .file(csvFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.importedRows").value(2))
                .andExpect(jsonPath("$.skippedRows").value(0))
                .andExpect(jsonPath("$.errors", hasSize(0)));

        // Verify the tree was created via preview
        mockMvc.perform(get("/api/admin/catalog/versions/" + versionId + "/preview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topicCount").value(1))
                .andExpect(jsonPath("$.topics[0].name").value("Greetings"))
                .andExpect(jsonPath("$.topics[0].situations[0].title").value("Meeting someone"));
    }

    @Test
    @Order(31)
    void csvImportReportsInvalidRows() throws Exception {
        // Create a draft version
        String result = mockMvc.perform(post("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String versionId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(result).get("id").asText();

        // CSV with a valid row and an invalid row (missing sub_phrase_text)
        String csv = "topic_name,situation_title,situation_description,situation_level,"
                + "chunk_context_question,chunk_context_translation,chunk_root_sentence,"
                + "chunk_root_translation,chunk_root_ipa,sub_phrase_text,sub_phrase_translation,"
                + "sub_phrase_ipa,sub_phrase_distractors\n"
                + "Topic1,Sit1,Desc,BEGINNER,Q,T,Root,RootT,/ipa/,phrase,tran,/ipa/,d1|d2\n"
                + "Topic2,Sit2,Desc,BEGINNER,Q,T,Root2,RootT2,/ipa/,,tran2,/ipa2/,d3\n";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/admin/catalog/versions/" + versionId + "/import-csv")
                        .file(csvFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.importedRows").value(1))
                .andExpect(jsonPath("$.skippedRows").value(1))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].row").value(3))
                .andExpect(jsonPath("$.errors[0].field").value("sub_phrase_text"));
    }

    // ===================== Publish + Version Assignment =====================

    @Test
    @Order(40)
    void publishVersionAssignsToAllUsers() throws Exception {
        // Create a draft version with content via CSV
        String result = mockMvc.perform(post("/api/admin/catalog/versions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String versionId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(result).get("id").asText();

        // Import content so it's not empty
        String csv = "topic_name,situation_title,situation_description,situation_level,"
                + "chunk_context_question,chunk_context_translation,chunk_root_sentence,"
                + "chunk_root_translation,chunk_root_ipa,sub_phrase_text,sub_phrase_translation,"
                + "sub_phrase_ipa,sub_phrase_distractors\n"
                + "TestTopic,TestSit,Desc,BEGINNER,Q,T,TestRoot,TestRootT,/t/,TestPhrase,TestTran,/t/,d1\n";

        MockMultipartFile csvFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/admin/catalog/versions/" + versionId + "/import-csv")
                        .file(csvFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Publish
        mockMvc.perform(post("/api/admin/catalog/versions/" + versionId + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        // Verify all users now have this version assigned
        java.util.UUID parsedVersionId = java.util.UUID.fromString(versionId);
        userRepository.findAll().forEach(user -> {
            Assertions.assertNotNull(user.getActiveDefaultCatalogVersion(),
                    "User " + user.getEmail() + " should have an assigned version");
            Assertions.assertEquals(parsedVersionId, user.getActiveDefaultCatalogVersion().getId(),
                    "User " + user.getEmail() + " should be assigned to the newly published version");
        });
    }
}
