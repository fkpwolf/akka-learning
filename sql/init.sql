-- Initialize database for Akka Persistence JDBC
-- Schema compatible with akka-persistence-jdbc 5.2.1

-- Journal table for storing events  
CREATE TABLE IF NOT EXISTS event_journal (
  ordering BIGSERIAL,
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  deleted BOOLEAN DEFAULT FALSE NOT NULL,
  writer VARCHAR(255) NOT NULL,
  write_timestamp BIGINT,
  adapter_manifest VARCHAR(255),
  event_ser_id INTEGER NOT NULL,
  event_ser_manifest VARCHAR(255) NOT NULL,
  event_payload BYTEA NOT NULL,
  meta_ser_id INTEGER,
  meta_ser_manifest VARCHAR(255),
  meta_payload BYTEA,
  PRIMARY KEY(persistence_id, sequence_number)
);

CREATE UNIQUE INDEX IF NOT EXISTS event_journal_ordering_idx ON event_journal(ordering);

-- Snapshot table for storing snapshots (with serialization metadata)
CREATE TABLE IF NOT EXISTS snapshot (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  created BIGINT NOT NULL,
  snapshot_ser_id INTEGER NOT NULL,
  snapshot_ser_manifest VARCHAR(255) NOT NULL,
  snapshot_payload BYTEA NOT NULL,
  meta_ser_id INTEGER,
  meta_ser_manifest VARCHAR(255),
  meta_payload BYTEA,
  PRIMARY KEY(persistence_id, sequence_number)
);

COMMENT ON TABLE event_journal IS 'Stores all events for event sourcing';
COMMENT ON TABLE snapshot IS 'Stores snapshots for faster recovery';
