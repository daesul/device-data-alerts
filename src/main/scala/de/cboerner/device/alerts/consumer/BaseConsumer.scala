package de.cboerner.device.alerts.consumer

import akka.actor.{Actor, ActorLogging, ActorRef}

abstract class BaseConsumer(alertCoordinator: ActorRef) extends Actor with ActorLogging


