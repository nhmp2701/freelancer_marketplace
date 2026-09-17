CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    reviewee_id BIGINT NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_review_job_reviewer UNIQUE (job_id, reviewer_id),
    CONSTRAINT ck_review_parties_differ CHECK (reviewer_id <> reviewee_id)
);

CREATE INDEX idx_reviews_reviewee ON reviews(reviewee_id, created_at DESC);
