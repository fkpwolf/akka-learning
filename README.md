# Akka Actor Model Learning Workspace

A hands-on workspace for learning the **Actor Model** using **Akka** with **PostgreSQL** state persistence.

## 🎯 What You'll Learn

- **Actor Model Fundamentals**: Message passing, isolation, and concurrency
- **Basic Actors**: Stateful and stateless actor patterns
- **Event Sourcing**: Building actors that persist events
- **Akka Persistence**: Using PostgreSQL to store actor state
- **Snapshots**: Optimizing recovery with snapshots
- **Real-world Patterns**: Banking and shopping cart examples

## 📁 Project Structure

```
akka-learning/
├── build.sbt                          # SBT build configuration
├── project/
│   ├── build.properties               # SBT version
│   └── plugins.sbt                    # SBT plugins
├── docker-compose.yml                 # PostgreSQL setup
├── sql/
│   └── init.sql                       # Database schema
├── src/main/
│   ├── resources/
│   │   ├── application.conf           # Akka configuration
│   │   └── logback.xml               # Logging configuration
│   └── scala/com/akkalearning/
│       ├── CborSerializable.scala    # Serialization marker
│       ├── basic/                     # Basic actor examples
│       │   ├── HelloWorldActor.scala
│       │   ├── CounterActor.scala
│       │   └── WorkerActor.scala
│       ├── persistence/               # Persistent actors
│       │   ├── BankAccountActor.scala
│       │   └── ShoppingCartActor.scala
│       └── examples/                  # Runnable examples
│           ├── BasicActorExample.scala
│           ├── PersistenceExample.scala
│           └── ShoppingCartExample.scala
```

## 🚀 Getting Started

### Prerequisites

- **Java** 11 or higher (check with `java -version`)
- **SBT** 1.9+ (will be downloaded automatically by SBT launcher)
- **Docker** & **Docker Compose** (for PostgreSQL)

### 1. Start PostgreSQL

```bash
# Start PostgreSQL container
docker-compose up -d

# Verify it's running
docker-compose ps

# View logs if needed
docker-compose logs postgres
```

The database will be initialized with the required tables for Akka Persistence.

### 2. Compile the Project

```bash
# Compile all source files
sbt compile
```

### 3. Run the Examples

#### Example 1: Basic Actors

Learn fundamental actor patterns with simple examples:

```bash
sbt "runMain com.akkalearning.examples.BasicActorExample"
```

This demonstrates:
- ✓ Message passing between actors
- ✓ Stateful actors (Counter)
- ✓ Actor communication patterns

#### Example 2: Event Sourcing with Bank Account

See how persistent actors store and recover state:

```bash
sbt "runMain com.akkalearning.examples.PersistenceExample"
```

This demonstrates:
- ✓ Event sourcing pattern
- ✓ Persisting events to PostgreSQL
- ✓ State recovery from events
- ✓ Command validation

**Try this**: Run it multiple times with the same account ID to see state recovery!

#### Example 3: Shopping Cart with Snapshots

Complex state management with snapshots:

```bash
sbt "runMain com.akkalearning.examples.ShoppingCartExample"
```

This demonstrates:
- ✓ Multiple event types
- ✓ Complex state with collections
- ✓ Snapshot optimization (every 20 events)
- ✓ State machines (cart → checkout)

## 🔍 Exploring the Code

### Basic Actors (`src/main/scala/com/akkalearning/basic/`)

1. **HelloWorldActor.scala**: Simple message passing
2. **CounterActor.scala**: Stateful actor maintaining a count
3. **WorkerActor.scala**: Manager-worker pattern with round-robin

### Persistent Actors (`src/main/scala/com/akkalearning/persistence/`)

1. **BankAccountActor.scala**: 
   - Event sourcing with deposits/withdrawals
   - Demonstrates command → event → state flow
   - Business logic validation

2. **ShoppingCartActor.scala**:
   - Complex state with item collections
   - Snapshot configuration
   - Multiple event types
   - Lifecycle management (open → checkout)

## 🗄️ Database Exploration

Connect to PostgreSQL to inspect persisted data:

```bash
# Connect to database
docker exec -it akka-postgres psql -U akka -d akka_learning

# View stored events
SELECT persistence_id, sequence_number, deleted 
FROM journal 
ORDER BY ordering DESC 
LIMIT 10;

# View snapshots
SELECT persistence_id, sequence_number, created 
FROM snapshot 
ORDER BY created DESC;

# Exit
\q
```

## 🧪 Experimentation Ideas

### Modify Examples

1. **Bank Account**: 
   - Add interest calculation
   - Implement transaction history query
   - Add account types (checking, savings)

2. **Shopping Cart**:
   - Add discount codes
   - Implement cart expiration
   - Add item recommendations

3. **Create Your Own**:
   - Chat room actor
   - Task queue system
   - Game state manager

### Test State Recovery

```scala
// 1. Run persistence example with a specific ID
val account = context.spawn(BankAccountActor("test-account-123"), "test-account")

// 2. Perform operations and note the final balance

// 3. Restart the application

// 4. Create actor with same ID - state will recover!
```

## 📚 Actor Model Concepts

### Key Principles

- **Isolation**: Each actor has its own state, no shared memory
- **Message Passing**: Actors communicate via asynchronous messages
- **Location Transparency**: Actors can be local or remote
- **Supervision**: Parent actors supervise children

### Event Sourcing

Instead of storing current state, store events:

```
Events:      Deposited($100) → Withdrawn($30) → Deposited($50)
State:       $0 → $100 → $70 → $120
```

Benefits:
- Complete audit trail
- Time travel debugging
- Easy to add new projections
- Natural fit with domain events

### Snapshots

For faster recovery, take periodic snapshots:

```
Snapshot at event 20: {balance: $500}
Then replay: Event 21, 22, 23... 
Final state: Snapshot + recent events
```

## ⚙️ Configuration

### Akka Settings (`src/main/resources/application.conf`)

- Actor system configuration
- Persistence plugin settings
- PostgreSQL connection details

### Database Connection

Default credentials (change for production):
- Host: `localhost:5432`
- Database: `akka_learning`
- User: `akka`
- Password: `akka123`

## 🛠️ Useful Commands

```bash
# Clean build
sbt clean

# Compile
sbt compile

# Run with auto-reload (for development)
sbt ~compile

# Format code
sbt scalafmt

# Interactive SBT console
sbt

# Stop PostgreSQL
docker-compose down

# Stop and remove volumes (reset database)
docker-compose down -v
```

## 📖 Learning Resources

### Official Documentation
- [Akka Documentation](https://doc.akka.io/docs/akka/current/)
- [Akka Typed Actors](https://doc.akka.io/docs/akka/current/typed/index.html)
- [Akka Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)

### Recommended Reading
- "Akka in Action" by Raymond Roestenburg
- "Reactive Design Patterns" by Roland Kuhn
- "The Actor Model" by Carl Hewitt (original paper)

### Video Tutorials
- Lightbend Akka YouTube channel
- "Actor Model Explained" talks

## 🐛 Troubleshooting

### PostgreSQL Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Restart
docker-compose restart postgres
```

### Compilation Errors

```bash
# Clear ivy cache
rm -rf ~/.ivy2/cache

# Clean and recompile
sbt clean compile
```

### Actor Not Recovering State

- Verify database tables exist: `docker exec -it akka-postgres psql -U akka -d akka_learning -c "\dt"`
- Check `persistence_id` is consistent between runs
- Review logs for errors

## 🎓 Next Steps

1. **Read the code**: Start with `BasicActorExample.scala`
2. **Run examples**: Execute each example and observe behavior
3. **Modify actors**: Change behavior and see effects
4. **Check database**: Inspect persisted events
5. **Build your own**: Create a new actor solving a problem
6. **Explore clustering**: Look into Akka Cluster for distributed actors

## 📝 License

This is a learning project. Feel free to modify and experiment!

---

**Happy Learning!** 🚀 Dive into the actor model and build resilient, concurrent systems!
