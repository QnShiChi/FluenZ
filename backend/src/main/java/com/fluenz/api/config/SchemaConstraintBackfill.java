package com.fluenz.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaConstraintBackfill implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1
                        FROM pg_constraint
                        WHERE conname = 'learning_paths_status_check'
                    ) THEN
                        ALTER TABLE learning_paths DROP CONSTRAINT learning_paths_status_check;
                    END IF;

                    ALTER TABLE learning_paths
                    ADD CONSTRAINT learning_paths_status_check
                    CHECK (status IN ('GENERATING', 'ACTIVE', 'FAILED', 'ARCHIVED'));
                EXCEPTION
                    WHEN duplicate_object THEN
                        NULL;
                END $$;
                """);

        log.info("SchemaConstraintBackfill ensured learning_paths_status_check allows GENERATING/FAILED statuses");
    }
}
