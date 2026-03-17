package com.fluenz.api.config;

import com.fluenz.api.entity.Profession;
import com.fluenz.api.repository.ProfessionRepository;
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

    @Override
    public void run(String... args) {
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
    }

    private Profession createProfession(String name, String description) {
        return Profession.builder()
                .name(name)
                .description(description)
                .build();
    }
}
