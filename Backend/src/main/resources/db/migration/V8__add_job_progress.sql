ALTER TABLE jobs
    ADD COLUMN progress INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN progress_note VARCHAR(500),
    ADD COLUMN progress_updated_at TIMESTAMP;

ALTER TABLE jobs ADD CONSTRAINT chk_jobs_progress CHECK (progress BETWEEN 0 AND 100);
