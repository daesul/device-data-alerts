package de.cboerner.device.alerts

import akka.Done
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, Terminated}
import de.cboerner.device.alerts.consumer.{HeatAlertConsumer, RpmAlertConsumer}

import scala.concurrent.duration._


object AlertCoordinator {

  val Name = "alert-coordinator"

  def props():Props = Props(new AlertCoordinator)

}

final class AlertCoordinator extends Actor with ActorLogging{

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30.seconds) {
    case _: Exception => Restart
  }

  val heatAlertCoordinator = context.actorOf(HeatAlertCoordinator.props(), HeatAlertCoordinator.Name)
  val rpmAlertCoordinator = context.actorOf(RpmAlertCoordinator.props(), RpmAlertCoordinator.Name)

  context.watch(heatAlertCoordinator)
  context.watch(rpmAlertCoordinator)

  override def receive: Receive = {
    case terminated:Terminated => log.info(s"${terminated.actor.path.name}")
    case heatAlert:HeatAlertConsumer.HeatAlert =>
      heatAlertCoordinator ! HeatAlert.Alert(heatAlert.deviceId, heatAlert.temperature)
      sender() ! Done
    case rpmAlert:RpmAlertConsumer.RpmAlert =>
      rpmAlertCoordinator ! RpmAlert.Alert(rpmAlert.deviceId, rpmAlert.rpm)
      sender() ! Done
  }
}

