package com.damon

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

/**
 * 功能：
 *
 * Created by ZhouJW on 2015/6/25 17:31.
 */
object Demo {
  def main(args: Array[String]) {

    val system = ActorSystem("MySystem")
    val greeter = system.actorOf(Props[GreetingActor], name = "greeter")
    greeter ! Greeting("Charlie Parker")
  }
}

case class Greeting(who: String)

class GreetingActor extends Actor with ActorLogging {
  def receive = {
    case Greeting(who) ⇒ log.info("Hello " + who)
  }
}
