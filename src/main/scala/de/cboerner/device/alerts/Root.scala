package de.cboerner.device.alerts

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, AllForOneStrategy, OneForOneStrategy, Props, Terminated}
import de.cboerner.device.alerts.consumer.AlertConsumerCoordinator
import scala.concurrent.duration._

object Root {

  def props(): Props = Props(new Root)
}

final class Root extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30.seconds) {
    case _: Exception => Restart
  }

  val alertCoordinator = context.actorOf(AlertCoordinator.props(), AlertCoordinator.Name)
  val alertConsumerCoordinator = context.actorOf(AlertConsumerCoordinator.props(alertCoordinator), AlertConsumerCoordinator.Name)

  context.watch(alertCoordinator)
  context.watch(alertConsumerCoordinator)

  override def receive: Receive = {
    case terminated: Terminated => log.info(s"terminated:${terminated.actor.path.name}")
  }
}

