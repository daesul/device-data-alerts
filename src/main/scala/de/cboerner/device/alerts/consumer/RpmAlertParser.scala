package de.cboerner.device.alerts.consumer

import akka.actor.{Actor, Props}
import de.cboerner.device.alerts.consumer.BaseConsumer.RawAlert
import de.cboerner.device.alerts.consumer.RpmAlertConsumer.RpmAlert
import io.circe.parser.parse


object RpmAlertParser {
  val Name = "rpm-alert-parser"

  def props(): Props = Props(new RpmAlertParser)

}

final class RpmAlertParser extends Actor {
  override def receive: Receive = {
    case rawAlert: RawAlert =>
      parse(rawAlert.value) match {
        case Left(failure) => throw new IllegalArgumentException(failure)
        case Right(json) =>
          val cursor = json.hcursor
          val deviceId = cursor.downField("deviceId").as[Int].getOrElse(0)
          val temperature = cursor.downField("rpm").as[Double].getOrElse(0.0)
          val timestamp = cursor.downField("timestamp").as[Long].getOrElse(0L)
          sender() ! RpmAlert(deviceId, temperature, timestamp)
      }
  }
}



