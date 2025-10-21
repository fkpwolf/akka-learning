-- Initialize database for Akka Persistence JDBC
-- Schema compatible with akka-persistence-jdbc 5.2.1

-- Journal table for storing events  
CREATE TABLE IF NOT EXISTS journal (
  ordering BIGSERIAL,
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  deleted BOOLEAN DEFAULT FALSE NOT NULL,
  tags VARCHAR(255) DEFAULT NULL,
  message BYTEA NOT NULL,
  PRIMARY KEY(persistence_id, sequence_number)
);

CREATE UNIQUE INDEX IF NOT EXISTS journal_ordering_idx ON journal(ordering);

-- Snapshot table for storing snapshots (with serialization metadata)
CREATE TABLE IF NOT EXISTS snapshot (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  created BIGINT NOT NULL,
  snapshot BYTEA NOT NULL,
  ser_id INTEGER DEFAULT 0 NOT NULL,
  ser_manifest VARCHAR(255),
  PRIMARY KEY(persistence_id, sequence_number)
);

COMMENT ON TABLE journal IS 'Stores all events for event sourcing';
COMMENT ON TABLE snapshot IS 'Stores snapshots for faster recovery';
