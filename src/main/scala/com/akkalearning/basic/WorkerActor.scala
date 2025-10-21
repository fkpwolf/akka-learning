package com.akkalearning.basic

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.akkalearning.CborSerializable
import scala.concurrent.duration._

/**
 * Worker actor that processes tasks
 */
object WorkerActor {

  sealed trait Command extends CborSerializable
  final case class ProcessTask(taskId: String, data: String, replyTo: ActorRef[TaskResult]) extends Command
  
  final case class TaskResult(taskId: String, result: String, success: Boolean) extends CborSerializable

  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case ProcessTask(taskId, data, replyTo) =>
        context.log.info("Processing task {} with data: {}", taskId, data)
        
        // Simulate some processing work
        Thread.sleep(100)
        
        val result = s"Processed: ${data.toUpperCase}"
        replyTo ! TaskResult(taskId, result, success = true)
        
        Behaviors.same
    }
  }
}

/**
 * Manager actor that delegates work to worker actors
 */
object ManagerActor {

  sealed trait Command extends CborSerializable
  final case class SubmitTask(taskId: String, data: String, replyTo: ActorRef[WorkerActor.TaskResult]) extends Command
  private final case class WorkerResponse(result: WorkerActor.TaskResult) extends Command

  def apply(numberOfWorkers: Int): Behavior[Command] = Behaviors.setup { context =>
    // Create worker pool
    val workers = (1 to numberOfWorkers).map { i =>
      context.spawn(WorkerActor(), s"worker-$i")
    }.toVector

    def withWorkerIndex(currentIndex: Int): Behavior[Command] = {
      Behaviors.receiveMessage {
        case SubmitTask(taskId, data, replyTo) =>
          val worker = workers(currentIndex)
          context.log.info("Delegating task {} to {}", taskId, worker.path.name)
          
          // Use adapter to handle worker response
          val responseAdapter = context.messageAdapter[WorkerActor.TaskResult](WorkerResponse)
          worker ! WorkerActor.ProcessTask(taskId, data, responseAdapter)
          
          // Store the original replyTo (in real app, use a Map)
          context.self ! WorkerResponse(WorkerActor.TaskResult(taskId, "", success = true))
          
          // Round-robin to next worker
          val nextIndex = (currentIndex + 1) % workers.size
          withWorkerIndex(nextIndex)

        case WorkerResponse(result) =>
          context.log.info("Received result for task {}: {}", result.taskId, result.result)
          Behaviors.same
      }
    }

    withWorkerIndex(0)
  }
}
