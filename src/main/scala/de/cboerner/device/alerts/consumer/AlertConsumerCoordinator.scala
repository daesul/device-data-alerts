package de.cboerner.device.alerts.consumer

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, Terminated}
import scala.concurrent.duration._

object AlertConsumerCoordinator {

  def props(alertCoordinator: ActorRef): Props = Props(new AlertConsumerCoordinator(alertCoordinator))

  val Name = "alert-consumer-coordinator"
}

final class AlertConsumerCoordinator(alertCoordinator: ActorRef) extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30.seconds) {
    case _: Exception => Restart
  }

  val heatAlertConsumer = context.actorOf(HeatAlertConsumer.props(alertCoordinator), HeatAlertConsumer.Name)
  val rpmAlertConsumer = context.actorOf(RpmAlertConsumer.props(alertCoordinator), RpmAlertConsumer.Name)

  context.watch(heatAlertConsumer)
  context.watch(rpmAlertConsumer)

  override def receive: Receive = {
    case terminated:Terminated => log.info(s"${terminated.actor.path.name}")
  }
}

