package de.cboerner.device.alerts

import java.time.Instant

import akka.actor.{Actor, ActorLogging, Props}

object RpmAlert {


  def props(): Props = Props(new RpmAlert)

  def name(deviceId: Int): String = s"rpm-alert-$deviceId"

  final case class Alert(deviceId: Int, temperature: Double, timestamp: Long = Instant.now().toEpochMilli)

  final case object Solve

  final case class State(alert: Option[Alert] = None) {

    def +(alert: Alert): State = copy(alert = Some(alert))

  }
}

final class RpmAlert extends Actor with ActorLogging{
  import de.cboerner.device.alerts.RpmAlert._

  private var state = State()
  override def receive: Receive = {
    case alert:Alert => state = state + alert
    case Solve => state = State()
  }
}

