package pl.newicom.dddd.monitoring

import akka.actor.ActorRef
import akka.contrib.pattern.ReceivePipeline.Inner
import kamon.Kamon
import kamon.trace.Tracer
import pl.newicom.dddd.aggregate.AggregateRootBase
import pl.newicom.dddd.eventhandling.EventHandler
import pl.newicom.dddd.messaging.command.CommandMessage
import pl.newicom.dddd.messaging.event.OfficeEventMessage

import scala.util.control.NonFatal

trait AggregateRootMonitoring extends EventHandler {
  this: AggregateRootBase =>

  override abstract def handle(senderRef: ActorRef, event: OfficeEventMessage): Unit = {
    super.handle(senderRef, event)
    Option(Tracer.currentContext).foreach(_.finish())
    log.debug("Event stored: {}", event.payload)
  }


  pipelineOuter {
    case cm: CommandMessage =>
      log.debug("Received: {}", cm)
      try {
        Tracer.setCurrentContext(Kamon.tracer.newContext("CommandMessage"))
      } catch {
        case NonFatal(e) => // Kamon not initialized, ignore
      }
      Inner(cm)
  }


}
