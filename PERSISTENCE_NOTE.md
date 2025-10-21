# Akka Learning Workspace - Fully Functional! 🎉

## ✅ What's Working

### Basic Actor Examples (Fully Functional!)
```bash
export PATH="$HOME/.local/share/coursier/bin:$PATH"
sbt "runMain com.akkalearning.examples.BasicActorExample"
```

This demonstrates core Actor Model concepts:
- **HelloWorldActor**: Message passing between actors
- **CounterActor**: Stateful actor with increment/decrement
- **WorkerActor**: Manager/worker pattern with round-robin

**Result**: ✅ All working perfectly!

## ✅ PostgreSQL Persistence (Fixed!)

The persistence examples are now fully working with PostgreSQL! The schema compatibility issue has been resolved.

### Persistence Examples (All Working!)

**Bank Account with Event Sourcing:**
```bash
sbt "runMain com.akkalearning.examples.PersistenceExample"
```

Demonstrates:
- ✅ Event sourcing with deposits/withdrawals
- ✅ State recovery from persisted events
- ✅ PostgreSQL event storage
- ✅ Command validation

**Shopping Cart with Snapshots:**
```bash
sbt "runMain com.akkalearning.examples.ShoppingCartExample"
```

Demonstrates:
- ✅ Complex state with item collections
- ✅ Multiple event types (add, remove, update, checkout)
- ✅ Snapshot optimization (every 20 events)
- ✅ State machine (open → checked out)

## 🔧 What Was Fixed

The issue was a **schema mismatch** between akka-persistence-jdbc v5.2.1 and our PostgreSQL setup:

### Problem
- Our schema had columns: `ser_id`, `ser_manifest`
- Plugin expected: `snapshot_ser_id`, `snapshot_ser_manifest`
- Table name: `journal` vs expected `event_journal`

### Solution
Updated both SQL schema and configuration to match the official akka-persistence-jdbc v5.5.3 schema:

**Fixed SQL Schema:**
```sql
-- Journal table with proper columns
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

-- Snapshot table with correct column names
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
```

**Fixed Configuration:**
```hocon
jdbc-journal {
  slick = ${slick}
  tables {
    event_journal {
      tableName = "event_journal"
      schemaName = "public"
    }
  }
}

jdbc-snapshot-store {
  slick = ${slick}
  tables {
    snapshot {
      tableName = "snapshot"
      schemaName = "public"
      columnNames {
        persistenceId = "persistence_id"
        sequenceNumber = "sequence_number"
        created = "created"
        snapshot = "snapshot_payload"
        serializerId = "snapshot_ser_id"
        serializerManifest = "snapshot_ser_manifest"
      }
    }
  }
}
```

## 🎓 Complete Learning Path

**Phase 1: Actor Model Fundamentals**
- ✅ Run Basic Actor Example multiple times
- ✅ Modify actor behaviors
- ✅ Experiment with message types
- ✅ Add your own actors

**Phase 2: Event Sourcing & Persistence**
- ✅ Run Bank Account Persistence Example
- ✅ See state recovery by running multiple times
- ✅ Inspect database events with SQL queries
- ✅ Run Shopping Cart with Snapshots

**Phase 3: Advanced Exploration**
- ✅ Check PostgreSQL tables for stored events
- ✅ Modify actors and see behavior changes
- ✅ Create custom persistent actors

## 📁 Project Structure

All code is ready and working:
- `src/main/scala/com/akkalearning/basic/` - Basic actors ✅
- `src/main/scala/com/akkalearning/persistence/` - Persistent actors ✅
- `src/main/scala/com/akkalearning/examples/` - Runnable examples ✅
- `sql/init.sql` - Correct PostgreSQL schema ✅

## 💡 Quick Start Guide

```bash
# Start PostgreSQL
docker-compose up -d

# Make sure SBT is in PATH
export PATH="$HOME/.local/share/coursier/bin:$PATH"

# Run basic actors (no DB required)
sbt "runMain com.akkalearning.examples.BasicActorExample"

# Run persistence with PostgreSQL
sbt "runMain com.akkalearning.examples.PersistenceExample"

# Run shopping cart with snapshots
sbt "runMain com.akkalearning.examples.ShoppingCartExample"

# Inspect stored events
docker exec akka-postgres psql -U akka -d akka_learning -c "SELECT persistence_id, sequence_number FROM event_journal ORDER BY ordering DESC LIMIT 10;"
```

## 🗄️ Database Exploration

```bash
# Connect to PostgreSQL
docker exec -it akka-postgres psql -U akka -d akka_learning

# View stored events
SELECT persistence_id, sequence_number, deleted 
FROM event_journal 
ORDER BY ordering DESC 
LIMIT 10;

# View snapshots (will be created after 20 events)
SELECT persistence_id, sequence_number, created 
FROM snapshot 
ORDER BY created DESC;
```

The workspace is now **100% functional** for learning Akka's Actor Model with full PostgreSQL persistence! 🚀
