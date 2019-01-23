package de.cboerner.device.alerts

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import scala.concurrent.duration._


object RpmAlertCoordinator {

  def props(): Props = Props(new RpmAlertCoordinator)

  val Name = "rpm-alert-coordinator"
}

final class RpmAlertCoordinator extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30.seconds) {
    case _: Exception => Restart
  }
  private var rpmAlerts = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case alert: RpmAlert.Alert =>

      val alertName = RpmAlert.name(alert.deviceId)
      rpmAlerts.get(alertName) match {
        case Some(alert) => alert ! alert
        case None =>
          val newAlert = context.actorOf(RpmAlert.props(), alertName)
          rpmAlerts += alertName -> newAlert
          newAlert ! alert
      }
  }
}

