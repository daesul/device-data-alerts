package de.cboerner.device.alerts.consumer

import java.time.Instant

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object HeatAlertConsumer {

  val Name = "heat-alert-consumer"

  def props(alertCoordinator: ActorRef): Props = Props(new HeatAlertConsumer(alertCoordinator))

  final case class HeatAlert(deviceId: Int, temperature: Double, timestamp: Long = Instant.now().toEpochMilli)
}

final class HeatAlertConsumer(alertCoordinator: ActorRef) extends BaseConsumer(alertCoordinator) {
  override def receive: Receive = ???
}

