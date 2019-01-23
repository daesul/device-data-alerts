package de.cboerner.device.alerts

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import scala.concurrent.duration._


object HeatAlertCoordinator {

  val Name = "heat-alert-coordinator"

  def props():Props = Props(new HeatAlertCoordinator)
}

final class HeatAlertCoordinator extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30.seconds) {
    case _: Exception => Restart
  }

  private var heatAlerts = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case alert: HeatAlert.Alert =>

      val alertName = HeatAlert.name(alert.deviceId)
      heatAlerts.get(alertName) match {
        case Some(alert) => alert ! alert
        case None =>
          val newAlert = context.actorOf(HeatAlert.props(), alertName)
          heatAlerts += alertName -> newAlert
          newAlert ! alert
      }
  }
}
