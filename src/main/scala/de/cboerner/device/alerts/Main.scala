package de.cboerner.device.alerts

import akka.actor.ActorSystem

object Main {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("device-data-alerts")
    val root = system.actorOf(Root.props(), "root")
  }

}
