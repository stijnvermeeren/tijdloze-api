package util

import akka.actor.{Actor, ActorRef, Props}

class ChatActor(out: ActorRef) extends Actor {
  def receive: Receive = {
    case message: String =>
      out ! ("I received your message: " + message)
  }
}

object ChatActor {
  def props(out: ActorRef) = Props(new ChatActor(out))
}
