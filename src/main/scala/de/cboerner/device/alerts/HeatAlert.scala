package de.cboerner.device.alerts

import java.time.Instant

import akka.actor.{Actor, ActorLogging, Props}


object HeatAlert {

  def props(): Props = Props(new HeatAlert)

  def name(deviceId: Int): String = s"heat-alert-$deviceId"

  final case class Alert(deviceId: Int, temperature: Double, timestamp: Long = Instant.now().toEpochMilli)

  final case object Solve

  final case class State(alert: Option[Alert] = None) {

    def +(alert: Alert): State = copy(alert = Some(alert))

  }



}

final class HeatAlert extends Actor with ActorLogging{
  import de.cboerner.device.alerts.HeatAlert._

  private var state = State()
  override def receive: Receive = {
    case alert:Alert => state = state + alert
    case Solve => state = State()
  }
}

