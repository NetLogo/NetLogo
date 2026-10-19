package org.nlogo.extensions.time.datatypes

import org.nlogo.api.Agent
import org.nlogo.nvm.AnonymousCommand
import org.nlogo.core.ExtensionObject
import org.nlogo.extensions.time._

import scala.jdk.CollectionConverters.IterableHasAsScala

object LogoEventComparator extends Ordering[LogoEvent] {
  def compare(a: LogoEvent, b: LogoEvent): Int =
    if (a.tick < b.tick) {
      -1
    } else if (a.tick > b.tick) {
      1
    } else if (a.id < b.id) {
      -1
    } else if (a.id > b.id) {
      1
    } else {
      0
    }
}

class LogoEvent(var agents: org.nlogo.agent.AgentSet,
                var task: AnonymousCommand,
                var tick: java.lang.Double,
                var repeatInterval: java.lang.Double,
                var repeatIntervalPeriodType: PeriodType,
                var shuffleAgentSet: java.lang.Boolean)
    extends ExtensionObject {

  val id: Long = { TimeExtension.nextEvent += 1; TimeExtension.nextEvent - 1 }
  def replaceData(agent: Agent, task: AnonymousCommand, tick: java.lang.Double): Unit = {
    this.agents = agents
    this.task = task
    this.tick = tick
  }
  /*
   * If a repeatInterval is set, this method uses it to update it's tick field and then adds itself to the
   * schedule argument.  The return value indicates whether the event was added to the schedule again.
   */

  def reschedule(callingSchedule: LogoSchedule): java.lang.Boolean =
    repeatInterval match {
      case null => false
      case x if repeatIntervalPeriodType == null => {
        this.tick = this.tick + repeatInterval
        TimeExtension.schedule.scheduleTree.add(this)
      }
      case _ => {
        val currentTime: LogoTime = callingSchedule.getCurrentTime()
        this.tick = this.tick + currentTime.getDifferenceBetween(callingSchedule.tickType, currentTime.plus(repeatIntervalPeriodType, repeatInterval)) / callingSchedule.tickValue
        TimeExtension.schedule.scheduleTree.add(this)
      }
    }

  override def equals(obj: Any): Boolean = this == obj

  def getExtensionName: String = "time"
  def getNLTypeName: String = "event"
  def recursivelyEqual(arg0: AnyRef): Boolean = equals(arg0)
  def dump(arg0: Boolean, arg1: Boolean, arg2: Boolean): String = {
    var result: String = tick.toString + ": "
    if (agents != null) {
      val listOfAgents: Iterable[org.nlogo.api.Agent] = agents.agents.asScala
      for (agent <- listOfAgents) {result += agent.toString + ";"}
      result = result.substring(0, result.length - 1)
    }
    val splitArr: Array[String] = task.procedure.nameToken.toString.split(":")
    result = result +
    (if(task==null) ""
     else
       ((if(splitArr.length > 1){
           splitArr(1).substring(0, splitArr(1).length()-1) + " "
         } else {
           ""
         }
       ) + task.procedure.displayName))
    result
  }
}
