CREATE SCHEMA IF NOT EXISTS osint;

CREATE TABLE IF NOT EXISTS osint.scans
(
    id              UUID PRIMARY KEY,
    domain          VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    result          TEXT,
    timeout_minutes BIGINT,
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

ALTER TABLE osint.scans
    ADD CONSTRAINT scan_status_check
        CHECK (status IN ('QUEUED', 'IN_PROGRESS', 'SUCCESS', 'EMPTY_RESULT', 'FAILED', 'TIMEOUT'));

CREATE INDEX IF NOT EXISTS idx_scan_results_domain
    ON osint.scans (domain);

CREATE INDEX IF NOT EXISTS idx_scan_results_status
    ON osint.scans (status);

CREATE UNIQUE INDEX uq_active_scan_per_domain
    ON osint.scans (domain)
    WHERE status IN ('IN_PROGRESS', 'QUEUED');
