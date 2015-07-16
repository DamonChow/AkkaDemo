package com.damon.implicittest


/**
 * 功能：
 *
 * Created by ZhouJW on 2015/7/3 15:47.
 */
object Demo {
  def main(args: Array[String]) {
    import com.damon.implicittest.DateHelper._

    val twoDayAgo = 2 days ago
    val twoDayAfter = 2 days from_now
    println("two days ago is " + twoDayAgo)
    println("two days after is " + twoDayAfter)

    import scala.concurrent.duration._
    val d = 5.seconds
    val deadline = 10 seconds fromNow
    println("time:" + d)
    println("time:" + deadline)
    //val array(3) = "ssss"
  }
}
