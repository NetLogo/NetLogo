package org.nlogo.extensions.time.primitives

import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle.STRICT

import org.nlogo.api.{ Argument, Context, Reporter }
import org.nlogo.core.{ I18N, Syntax }
import org.nlogo.nvm.ExtensionContext

import org.nlogo.extensions.time._
import org.nlogo.extensions.time.datatypes._

object TimePrimitives {

  class NewLogoTime extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.StringType), ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      TimeExtension.context = context
      new LogoTime(TimeUtils.getStringFromArgument(args, 0))
    }
  }

  class Anchor extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(
        right = List(Syntax.WildcardType, Syntax.NumberType, Syntax.StringType),
        ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      /* The anchor primitives have experienced some changes with the use of the
         Java time library. Originally, the Joda library would create copies of
         LocalDateTime, LocalDate, and MonthDay. That is no longer the case with
         the Java 8 time library, instead preferring references. This has caused
         some headaches with scheduler and timeseries functionality.
         Since two LogoTimes can be referencing the same LocalDateTime/LocalDate/
         MonthDay, this can be a source of inconsistencies.
         CBR [ 04/26/2019 ]
       */
      val newTime: LogoTime = new LogoTime(TimeUtils.getTimeFromArgument(args, 0))
      newTime.setAnchor(
        TimeUtils.getDoubleFromArgument(args, 1),
        TimeUtils.stringToPeriodType(TimeUtils.getStringFromArgument(args, 2)),
        context.asInstanceOf[ExtensionContext].workspace.world
      )
      newTime
    }
  }

  class Show extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType, Syntax.StringType),
                            ret = Syntax.StringType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val time: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      // There is an implicit LogoTime initialization
      val fmtString: String = TimeUtils.getStringFromArgument(args, 1)
      val fmt =
        if (fmtString.trim().==(""))
          DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS", I18N.shared.defaultLocale).withResolverStyle(STRICT)
        else DateTimeFormatter.ofPattern(fmtString.replace('y','u'), I18N.shared.defaultLocale).withResolverStyle(STRICT)
      time.show(fmt)
    }
  }

  class Copy extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val time: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      new LogoTime(time)
    }
  }

  class CreateWithFormat extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.StringType, Syntax.StringType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      TimeExtension.context = context
      val customFormat = TimeUtils.getStringFromArgument(args, 1) match {
        case null => None
        case x => Some(x)
      }
      new LogoTime(TimeUtils.getStringFromArgument(args, 0), customFormat)
    }
  }

  class DifferenceBetween extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType,
                                         Syntax.WildcardType,
                                         Syntax.StringType),
                            ret = Syntax.NumberType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val startTime: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      val endTime: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      val pType: PeriodType =
        TimeUtils.stringToPeriodType(TimeUtils.getStringFromArgument(args, 2))
      startTime.getDifferenceBetween(pType, endTime)
    }
  }

  class Get extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.StringType, Syntax.WildcardType), ret = Syntax.NumberType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val periodType: PeriodType =
        TimeUtils.stringToPeriodType(TimeUtils.getStringFromArgument(args, 0))
      val time: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      time.get(periodType).doubleValue().asInstanceOf[AnyRef]
    }
  }

  class IsAfter extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(
        right = List(Syntax.WildcardType, Syntax.WildcardType),
        ret = Syntax.BooleanType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val timeA: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      val timeB: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      (!(timeA.isBefore(timeB) || timeA.isEqual(timeB))).asInstanceOf[AnyRef]
    }
  }

  class IsBefore extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(
        right = List(Syntax.WildcardType, Syntax.WildcardType),
        ret = Syntax.BooleanType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val timeA: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      val timeB: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      timeA.isBefore(timeB).asInstanceOf[AnyRef]
    }
  }

  class IsBetween extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType,
                                         Syntax.WildcardType,
                                         Syntax.WildcardType),
                            ret = Syntax.BooleanType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val timeA: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      val timeB: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      val timeC: LogoTime = TimeUtils.getTimeFromArgument(args, 2)
      timeA.isBetween(timeB, timeC).asInstanceOf[AnyRef]
    }
  }

  class IsEqual extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(
        right = List(Syntax.WildcardType, Syntax.WildcardType),
        ret = Syntax.BooleanType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val timeA: LogoTime = TimeUtils.getTimeFromArgument(args, 0)
      val timeB: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      timeA.isEqual(timeB).asInstanceOf[AnyRef]
    }
  }

  class Plus extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(
        right = List(Syntax.WildcardType, Syntax.NumberType, Syntax.StringType),
        ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val time: LogoTime = new LogoTime(TimeUtils.getTimeFromArgument(args, 0))
      time.plus(
        TimeUtils.stringToPeriodType(TimeUtils.getStringFromArgument(args, 2)),
        TimeUtils.getDoubleFromArgument(args, 1))
    }
  }
}
