package com.damon.implicittest

import java.util.Calendar
import java.util.Date
/**
 * 功能：
 *
 * Created by ZhouJW on 2015/7/3 15:44.
 */
class DateHelper private(number: Int) {

  def days(when: String): Date = {
    var date = Calendar.getInstance()
    when match {
      case DateHelper.ago => date.add(Calendar.DAY_OF_MONTH, -number)
      case DateHelper.from_now => date.add(Calendar.DAY_OF_MONTH, number)
      case _ => date
    }
    date.getTime()
  }
}

object DateHelper {
  val ago = "ago"
  val from_now = "from_now"
  implicit def convertInt2Date(number: Int) = new DateHelper(number)
}
