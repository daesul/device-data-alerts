package de.cboerner.device.alerts.consumer

import akka.actor.{Actor, Props}
import de.cboerner.device.alerts.consumer.BaseConsumer.RawAlert
import de.cboerner.device.alerts.consumer.HeatAlertConsumer.HeatAlert
import io.circe.parser.parse


object HeatAlertParser {
  val Name = "hear-alert-parser"

  def props(): Props = Props(new HeatAlertParser)

}

final class HeatAlertParser extends Actor {
  override def receive: Receive = {
    case rawAlert: RawAlert =>
      parse(rawAlert.value) match {
        case Left(failure) => throw new IllegalArgumentException(failure)
        case Right(json) =>
          val cursor = json.hcursor
          val deviceId = cursor.downField("deviceId").as[Int].getOrElse(0)
          val temperature = cursor.downField("temperature").as[Double].getOrElse(0.0)
          val timestamp = cursor.downField("timestamp").as[Long].getOrElse(0L)
          sender() ! HeatAlert(deviceId, temperature, timestamp)
      }
  }
}



