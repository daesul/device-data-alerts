package de.cboerner.device.alerts.consumer

import java.time.Instant

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object RpmAlertConsumer {

  val Name = "rpm-alert-consumer"

  def props(alertCoordinator: ActorRef): Props = Props(new RpmAlertConsumer(alertCoordinator))
  final case class RpmAlert(deviceId: Int, rpm: Double, timestamp: Long = Instant.now().toEpochMilli)

}

class RpmAlertConsumer(alertCoordinator: ActorRef) extends BaseConsumer(alertCoordinator) {
  override def receive: Receive = ???
}

