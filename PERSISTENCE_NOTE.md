# Akka Learning Workspace - Current Status

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

## ⚠️ PostgreSQL Persistence Issue

The persistence examples have a schema compatibility issue between `akka-persistence-jdbc` v5.2.1 and our PostgreSQL setup. The plugin expects different column names than what we configured.

### Temporary Workaround

Focus on learning the Actor Model fundamentals first:
1. Message passing and actor communication  
2. Stateful vs stateless actors
3. Actor lifecycle and supervision
4. Ask pattern vs tell pattern

### To Fix Persistence Later

Options:
1. **Use H2 in-memory database** (simpler for learning)
2. **Investigate the exact schema** required by akka-persistence-jdbc 5.2.1
3. **Try a different persistence plugin** or version

## 🎓 Learning Path

**Current Focus** (Working Now):
- ✅ Run Basic Actor Example multiple times
- ✅ Modify actor behaviors
- ✅ Experiment with message types
- ✅ Add your own actors

**Next Steps** (After fixing persistence):
- Event Sourcing patterns
- Snapshot optimization
- State recovery from database

## 📁 Project Structure

All code is ready and waiting:
- `src/main/scala/com/akkalearning/basic/` - Basic actors ✅
- `src/main/scala/com/akkalearning/persistence/` - Persistent actors (needs schema fix)
- `src/main/scala/com/akkalearning/examples/` - Runnable examples

## 💡 Quick Start

```bash
# Make sure SBT is in PATH
export PATH="$HOME/.local/share/coursier/bin:$PATH"

# Run the working example
sbt "runMain com.akkalearning.examples.BasicActorExample"

# Explore the code
code src/main/scala/com/akkalearning/basic/

# Modify and re-run!
```

The workspace is fully functional for learning Akka's Actor Model! 🎉
