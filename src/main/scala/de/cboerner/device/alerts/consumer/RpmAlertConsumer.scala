package de.cboerner.device.alerts.consumer

import java.time.Instant

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.util.Timeout
import de.cboerner.device.alerts.consumer.BaseConsumer.RawAlert
import de.cboerner.device.alerts.consumer.HeatAlertConsumer.HeatAlert
import de.cboerner.device.alerts.consumer.RpmAlertConsumer.RpmAlert
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._

object RpmAlertConsumer {

  val Name = "rpm-alert-consumer"

  def props(alertCoordinator: ActorRef): Props = Props(new RpmAlertConsumer(alertCoordinator))
  final case class RpmAlert(deviceId: Int, rpm: Double, timestamp: Long = Instant.now().toEpochMilli)

}

class RpmAlertConsumer(alertCoordinator: ActorRef) extends BaseConsumer(alertCoordinator) {
  implicit val timeout: Timeout = 2.seconds

  implicit val mat = ActorMaterializer()

  import context.dispatcher

  val rpmAlertParser = context.system.actorOf(RpmAlertParser.props(), RpmAlertParser.Name)
  val config = context.system.settings.config.getConfig("akka.kafka.consumer")
  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("rpm-alert-group")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val committerSettings = CommitterSettings(context.system)

  log.info("Start consuming topic")

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics("rpm-alert"))
    .mapAsync(1) { msg =>
      log.info("Receiving raw message: {}", msg.record.value())
      (rpmAlertParser ? RawAlert(msg.record.value()))
        .mapTo[RpmAlert]
        .map(rpmAlert => (msg.committableOffset, rpmAlert))
    }
    .mapAsync(1) {
      offsetAndData => (alertCoordinator ? offsetAndData._2)
        .mapTo[Done]
        .map(_ => offsetAndData._1)
    }
    .via(Committer.flow(committerSettings))
    .toMat(Sink.seq)(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()


  override def receive: Receive = Actor.emptyBehavior
}

