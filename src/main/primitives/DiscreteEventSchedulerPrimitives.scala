package org.nlogo.extensions.time.primitives
import org.nlogo.api.Argument
import org.nlogo.api.Context
import org.nlogo.api.Command
import org.nlogo.api.ExtensionException
import org.nlogo.api.Reporter
import org.nlogo.core.Syntax
import org.nlogo.extensions.time._
import org.nlogo.extensions.time.datatypes._

object DiscreteEventSchedulerPrimitives {

  class AddEvent extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.WildcardType, Syntax.WildcardType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.addEvent(args, context, Default)
    }
  }

  class AddEventShuffled extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.WildcardType, Syntax.WildcardType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.addEvent(args, context, Shuffle)
    }
  }

  class AnchorSchedule extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.NumberType, Syntax.StringType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.anchorSchedule(
        TimeUtils.getTimeFromArgument(args, 0),
        TimeUtils.getDoubleFromArgument(args, 1),
        TimeUtils.stringToPeriodType(TimeUtils.getStringFromArgument(args, 2)))
    }
  }

  class ClearSchedule extends Command {
    def getSyntax: Syntax = Syntax.commandSyntax()
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.clear()
    }
  }

  class GetSize extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(ret = Syntax.NumberType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      if (TimeExtension.debug)
        TimeUtils.printToConsole(context, "size of schedule: " + TimeExtension.schedule.scheduleTree.size)
      Double.box(TimeExtension.schedule.scheduleTree.size)
    }
  }

  class Go extends Command {
    def getSyntax: Syntax = Syntax.commandSyntax()
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.performScheduledTasks(args, context)
    }
  }

  class GoUntil extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType))
    def perform(args: Array[Argument], context: Context): Unit = {
      var untilTime: LogoTime = null
      var untilTick: java.lang.Double = null
      try untilTime = TimeUtils.getTimeFromArgument(args, 0)
      catch {
        case e: ExtensionException =>
          untilTick = TimeUtils.getDoubleFromArgument(args, 0)
      }
      if (untilTime == null) {
        TimeExtension.schedule.performScheduledTasks(args, context, untilTick)
      } else {
        TimeExtension.schedule.performScheduledTasks(args, context, untilTime)
      }
    }
  }

  class RepeatEvent extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.WildcardType, Syntax.WildcardType, Syntax.NumberType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.addEvent(args, context, Repeat)
    }

  }

  class RepeatEventShuffled extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.WildcardType, Syntax.WildcardType, Syntax.NumberType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.addEvent(args, context, RepeatShuffled)
    }

  }

  class RepeatEventShuffledWithPeriod extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.WildcardType, Syntax.WildcardType, Syntax.NumberType,
                                Syntax.StringType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.addEvent(args, context, RepeatShuffled)
    }

  }

  class RepeatEventWithPeriod extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(List(Syntax.WildcardType, Syntax.WildcardType, Syntax.WildcardType, Syntax.NumberType,
                                Syntax.StringType))
    def perform(args: Array[Argument], context: Context): Unit = {
      TimeExtension.schedule.addEvent(args, context, Repeat)
    }
  }

  class ShowSchedule extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(ret = Syntax.StringType)
    def report(args: Array[Argument], context: Context): AnyRef =
      TimeExtension.schedule.dump(false, false, false)
  }
}
