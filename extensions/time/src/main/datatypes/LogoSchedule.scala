package org.nlogo.extensions.time.datatypes

import java.util.{ArrayList, TreeSet}
import org.nlogo.agent.{Agent, AgentIterator, ArrayAgentSet, TickCounter, AgentSet}
import org.nlogo.nvm.AnonymousCommand
import org.nlogo.api.{Argument, Context, ExtensionException}
import org.nlogo.core.{AgentKindJ, ExtensionObject}
import org.nlogo.nvm.ExtensionContext
import org.nlogo.extensions.time._

import scala.jdk.CollectionConverters.ListHasAsScala

class LogoSchedule extends ExtensionObject {
  // This is the data structure that contains all the scheduled events
  var scheduleTree: TreeSet[LogoEvent] = new TreeSet[LogoEvent](LogoEventComparator)
  var tickCounter: TickCounter = null
  var timeAnchor: LogoTime = null
  var tickType: PeriodType = null
  var tickValue: java.lang.Double = null
  override def equals(obj: Any): Boolean = this == obj
  def isAnchored(): java.lang.Boolean = timeAnchor != null

  def anchorSchedule(time:LogoTime, tickValue:java.lang.Double, tickType:PeriodType): Unit =
    try {
      this.timeAnchor = new LogoTime(time)
      this.tickType = tickType
      this.tickValue = tickValue
    } catch {
      case e: ExtensionException => e.printStackTrace()
    }

  @throws[ExtensionException]
  def timeToTick(time: LogoTime): java.lang.Double = {
    if (this.timeAnchor.dateType != time.dateType)
      throw new ExtensionException(s"Cannot schedule event to occur at a LogoTime of type ${time.dateType.toString} because the schedule is anchored to a LogoTime of type ${this.timeAnchor.dateType.toString}.  Types must be consistent.")
    this.timeAnchor.getDifferenceBetween(this.tickType, time) / this.tickValue
  }

  @throws[ExtensionException]
  def addEvent(args: Array[Argument], context: Context, addType: AddType): Unit = {
    /* It should be mentioned that this match statement
      is for recording the type of primitive that is
      calling this function. It only serves for debugging
      and not much else in the function */
    val primName = addType match { // initial check
      case Default =>
        if (args.length < 3)
          throw new ExtensionException(
            "time:add must have 3 arguments: schedule agent task tick/time")
        "add"
      case Shuffle =>
        if (args.length < 3)
          throw new ExtensionException(
            "time:add-shuffled must have 3 arguments: schedule agent task tick/time")
        "add-shuffled"
      case Repeat =>
        if (args.length < 4)
          throw new ExtensionException("""time:repeat must have 4 or 5 arguments:
            schedule agent task tick/time number (period-type)""")
        "repeat"
      case RepeatShuffled =>
        if (args.length < 4)
          throw new ExtensionException("time:repeat-shuffled must have 4 or 5 arguments: schedule agent task tick/time number (period-type)")
        "repeat-shuffled"
    }

    // Throw exception on conditions for agent, agentsets and observer
    val isInvalidEventArgument = !(args(0).get.isInstanceOf[Agent]) &&
        !(args(0).get.isInstanceOf[AgentSet]) &&
        !((args(0).get.isInstanceOf[String]) && args(0).get.toString.toLowerCase().==("observer"))

    if (isInvalidEventArgument)
      throw new ExtensionException(
        s"time: $primName expected an agent, agentset, or the string \'observer\' as the first argument")
    else if (!(args(1).get.isInstanceOf[AnonymousCommand]))
      throw new ExtensionException(
        s"time: $primName expecting a command task as the second argument")
    else if (args(1).get.asInstanceOf[AnonymousCommand].formals.length > 0)
      throw new ExtensionException(
       s"""time: $primName expecting as the second argument a command task that takes no arguments of
       its own, but found a task which expects its own arguments, this kind of task is unsupported by
       the time extension.""")

      /* --------------------------------------------------------------------------------
         Determine the type of the entered input for addEvent: number, logotime, observer
         -------------------------------------------------------------------------------- */
    val eventTick: java.lang.Double = args(2).get.getClass match {
      case clazz if clazz == classOf[java.lang.Double] =>
        args(2).getDoubleValue
      case clazz if clazz == classOf[LogoTime] =>
        if (!this.isAnchored())
            throw new ExtensionException(s"""A LogoEvent can only be scheduled to occur at a LogoTime
            if the discrete event schedule has been anchored to a LogoTime, see time:anchor-schedule""")
        this.timeToTick(TimeUtils.getTimeFromArgument(args, 2))
      case _ =>
        throw new ExtensionException(
          s"""time: $primName expecting a number or logotime as the third argument""")
    }

    if (eventTick < context.asInstanceOf[ExtensionContext].workspace.world.ticks)
      throw new ExtensionException(s"""Attempted to schedule an event for tick $eventTick which is
        before the present 'moment' of ${context.asInstanceOf[ExtensionContext].workspace.world.ticks}""")
    var repeatIntervalPeriodType: PeriodType = null
    var repeatInterval: java.lang.Double = null

    if (addType == Repeat || addType == RepeatShuffled) {
      if (args(3).get.getClass != classOf[java.lang.Double])
          throw new ExtensionException("time repeat expecting a number as the fourth argument")
      repeatInterval = args(3).getDoubleValue
      if (repeatInterval <= 0)
          throw new ExtensionException("time repeat the repeat interval must be a positive number")
      if (args.length == 5) {
        if (!this.isAnchored())
           throw new ExtensionException(
           """A LogoEvent can only be scheduled to repeat using a period type if the discrete
              event schedule has been anchored to a LogoTime, see time:anchor-schedule""")
        repeatIntervalPeriodType = TimeUtils.stringToPeriodType(TimeUtils.getStringFromArgument(args, 4))
        if (repeatIntervalPeriodType != Month && repeatIntervalPeriodType != Year) {
          repeatInterval = this.timeAnchor
            .getDifferenceBetween(this.tickType,
              this.timeAnchor.plus(repeatIntervalPeriodType, repeatInterval)) / this.tickValue
          repeatIntervalPeriodType = null
        }
      }
    }

    val shuffleAgentSet: Boolean = addType == Shuffle || addType == RepeatShuffled
    val agentSet = args(0).get match {
      case agent: Agent =>
        val theAgent: Agent = args(0).getAgent.asInstanceOf[org.nlogo.agent.Agent]
        new ArrayAgentSet(AgentKindJ.Turtle, theAgent.toString, Array(theAgent))
      case agentset: AgentSet => args(0).getAgentSet.asInstanceOf[AgentSet]
      case _ => null
    }
    val event: LogoEvent =
      new LogoEvent(agentSet,
        args(1).getCommand.asInstanceOf[AnonymousCommand],
        eventTick,repeatInterval,
        repeatIntervalPeriodType,
        shuffleAgentSet)
    scheduleTree.add(event)
  }

  def getTickCounter(): TickCounter =
    tickCounter match {
      case null =>
        throw new ExtensionException("Tick counter has not been initialized in time extension.")
      case tc => tc
    }

  def getTickCounter(context: ExtensionContext): TickCounter =
    tickCounter match {
      case null =>
        tickCounter = context.workspace.world.tickCounter
        tickCounter
      case tc => tc
    }

  def updateTickCounter(context: ExtensionContext): Unit = {
    context.workspace.requestDisplayUpdate(false)
  }

  /* ----------------------------------------------------------------------------
     performScheduledTasks are three functions that are meant to allow overloaded
     use of performing a task by the LogoSchedule object [CBR 01/24/2019]
     ---------------------------------------------------------------------------- */

  def performScheduledTasks(args: Array[Argument], context: Context): Unit = {
    performScheduledTasks(args, context, java.lang.Double.MAX_VALUE)
  }
  def performScheduledTasks(args: Array[Argument], context: Context, untilTime: LogoTime): Unit = {
    if (!this.isAnchored()) throw new ExtensionException(
      """time:go-until can only accept a LogoTime as a stopping time if the schedule is anchored
         using time:anchor-schedule""")
    // Calculate the untilTick and pass it to the main function
    val untilTick: java.lang.Double = this.timeAnchor.getDifferenceBetween(this.tickType, untilTime) / this.tickValue
    performScheduledTasks(args, context, untilTick)
  }
  def performScheduledTasks(args: Array[Argument], context: Context, untilTick: java.lang.Double): Unit = {
    // This extension is only for CommandTasks, so we know there aren't any args to pass in
    val extcontext: ExtensionContext = context.asInstanceOf[ExtensionContext]
    val emptyArgs: Array[Any] = Array.ofDim[Any](1)
    var event: LogoEvent = if (scheduleTree.isEmpty) null else scheduleTree.first()

    /* --------------------------------------------------------------------------
       While Loop: Recursive but can end up in a recursive infinite loop
       Queue Afterwards: Remove the current event from the logo queue, reschedule
         the event, then grab the next event from the schedule [CBR 01/24/2019]
       -------------------------------------------------------------------------- */
    while (event != null && event.tick <= untilTick) { // iterates through scheduleTree
      getTickCounter(extcontext).tick(event.tick-getTickCounter(extcontext).ticks)
      event.agents match {
        case null => // observer context
          val nvmContext: org.nlogo.nvm.Context = new org.nlogo.nvm.Context(
            extcontext.nvmContext.job,
            extcontext.getAgent.world.observer.asInstanceOf[org.nlogo.agent.Agent],
            extcontext.nvmContext.ip,
            extcontext.nvmContext.activation,
            extcontext.workspace)
          event.task.perform(nvmContext, emptyArgs.asInstanceOf[Array[AnyRef]])
        case _ =>
          val iter: AgentIterator =
            if (event.shuffleAgentSet)
              event.agents.shufflerator(extcontext.nvmContext.job.random)
            else event.agents.iterator
          val copy: ArrayList[Agent] = new ArrayList[Agent]()

          while (iter.hasNext) copy.add(iter.next())
          copy.asScala.dropWhile { agent =>
            if (agent == null || agent.id == -1) {
              false
            } else {
              val nvmContext: org.nlogo.nvm.Context = new org.nlogo.nvm.Context(
                extcontext.nvmContext.job, agent, extcontext.nvmContext.ip,
                extcontext.nvmContext.activation,
                extcontext.workspace)
              if (extcontext.nvmContext.stopping) {
                false
              } else {
                event.task.perform(nvmContext, emptyArgs.asInstanceOf[Array[AnyRef]])
                !nvmContext.stopping
              }
            }
          }
      }
      scheduleTree.remove(event)
      event.reschedule(this)
      event = if (scheduleTree.isEmpty) null else scheduleTree.first()
    }
    if (untilTick != null && untilTick < java.lang.Double.MAX_VALUE && untilTick > getTickCounter(extcontext).ticks) {
      getTickCounter(extcontext).tick(untilTick - getTickCounter(extcontext).ticks)
    }
    updateTickCounter(extcontext)
  }

  def getCurrentTime(): LogoTime =
    if (!this.isAnchored()) null
    else this.timeAnchor.plus(this.tickType, getTickCounter().ticks / this.tickValue)

  def dump(readable: Boolean, exporting: Boolean, reference: Boolean): String = {
    val buf: StringBuilder = new StringBuilder()
    if (exporting) {
      buf.append("LogoSchedule")
      if (!reference)
        buf.append(":")
    }
    if (!(reference && exporting)) {
      buf.append(" [ ")
      val iter: java.util.Iterator[LogoEvent] = scheduleTree.iterator()
      while (iter.hasNext) {
        buf.append(iter.next().asInstanceOf[LogoEvent].dump(true, true, true) + " ")
      }
      buf.append("]")
    }
    buf.toString
  }

  def getExtensionName: String = "time"
  def getNLTypeName: String = "schedule"
  def recursivelyEqual(arg0: AnyRef): Boolean = equals(arg0)
  def clear(): Unit = scheduleTree.clear()
}
