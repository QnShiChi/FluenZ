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

        jdbcTemplate.execute("""
                ALTER TABLE learning_paths
                ADD COLUMN IF NOT EXISTS published_topic_count integer
                """);
        jdbcTemplate.execute("""
                ALTER TABLE learning_paths
                ADD COLUMN IF NOT EXISTS generated_topic_count integer
                """);
        jdbcTemplate.execute("""
                ALTER TABLE learning_paths
                ADD COLUMN IF NOT EXISTS total_topic_count integer
                """);

        jdbcTemplate.execute("""
                UPDATE learning_paths
                SET published_topic_count = COALESCE(published_topic_count, 0),
                    generated_topic_count = COALESCE(generated_topic_count, 0),
                    total_topic_count = COALESCE(total_topic_count, 0)
                """);

        jdbcTemplate.execute("ALTER TABLE learning_paths ALTER COLUMN published_topic_count SET DEFAULT 0");
        jdbcTemplate.execute("ALTER TABLE learning_paths ALTER COLUMN generated_topic_count SET DEFAULT 0");
        jdbcTemplate.execute("ALTER TABLE learning_paths ALTER COLUMN total_topic_count SET DEFAULT 0");
        jdbcTemplate.execute("ALTER TABLE learning_paths ALTER COLUMN published_topic_count SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE learning_paths ALTER COLUMN generated_topic_count SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE learning_paths ALTER COLUMN total_topic_count SET NOT NULL");

        log.info("SchemaConstraintBackfill ensured learning_paths generation metadata columns and status constraint are up to date");
    }
}
