package org.nlogo.extensions.time

import java.io.IOException
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.util.logging.{FileHandler, Logger, SimpleFormatter}
import org.nlogo.api.{Argument, Context, Dump, ExtensionException, OutputDestinationJ}
import org.nlogo.core.LogoList
import org.nlogo.nvm.ExtensionContext
import org.nlogo.extensions.time.datatypes.{LogoTimeSeries, LogoTime, LogoSchedule}

/* This object holds convenient methods for time and types */
object TimeUtils {
  def dToL(d: java.lang.Double): java.lang.Long =
    d.longValue()

  def stringToPeriodType(typeString: String): PeriodType = {
    val typ = typeString.trim().toLowerCase() match {
      case s if s.substring(s.length - 1).== ("s") => s.substring(0, s.length - 1)
      case s => s
    }
    typ match {
      case "milli"  => Milli
      case "second" => Second
      case "minute" => Minute
      case "hour" => Hour
      case stype
        if stype == "day" || stype == "dayofmonth" || stype == "dom"
          => Day
      case stype
        if stype == "dayofyear" || stype == "doy" || stype == "julianday" || stype == "jday"
          => DayOfYear
      case stype
        if stype == "dayofweek" || stype == "dow" || stype == "weekday" || stype == "wday"
          => DayOfWeek
      case "week" => Week
      case "month" => Month
      case "year" => Year
      case _ => throw new Exception(s"Unexpected type: $typ")
    }
  }

  def getTimeFromArgument(args: Array[Argument], argIndex: java.lang.Integer): LogoTime = {
    val time = args(argIndex).get match {
      case str: String => new LogoTime(args(argIndex).getString)
      case logoTime: LogoTime => logoTime
      case obj =>
        throw new ExtensionException(
          s"time was expecting a LogoTime object as argument ${argIndex + 1}, found this instead: $obj")
    }
    time.updateFromTick()
    time
  }

  def getDoubleFromArgument(args: Array[Argument], argIndex: java.lang.Integer): java.lang.Double =
    args(argIndex).get match {
      case double: java.lang.Double => double
      case obj =>
       throw new ExtensionException(
        s"time: was expecting a number as argument ${argIndex + 1}, found this instead: $obj")
    }

  def getListFromArgument(args: Array[Argument], argIndex: java.lang.Integer): LogoList =
    args(argIndex).get match {
      case logolist: LogoList => logolist
      case logolist =>
        throw new ExtensionException(
          "time: was expecting a list as argument " + (argIndex + 1))
    }

  def getIntFromArgument(args: Array[Argument], argIndex: java.lang.Integer): java.lang.Double =
    args(argIndex).get match {
      case double: java.lang.Double => roundDouble(double).toDouble
      case integer: java.lang.Integer => integer.toDouble
      case obj =>
        throw new ExtensionException(
          "time: was expecting a number as argument " + (argIndex + 1) +
            ", found this instead: " +
            Dump.logoObject(0.0.asInstanceOf[AnyRef]))
    }


  def getLongFromArgument(args: Array[Argument], argIndex: java.lang.Integer): java.lang.Long =
    args(argIndex).get match {
      case double: java.lang.Double => double.longValue()
      case integer: java.lang.Integer => integer.longValue()
      case obj =>
        throw new ExtensionException(
          s"time: was expecting a number as argument ${argIndex + 1}, found this instead: $obj")
    }

  def getStringFromArgument(args: Array[Argument], argIndex: java.lang.Integer): String =
    args(argIndex).get match {
      case string: String => string
      case obj =>
        throw new ExtensionException(
          s"time: was expecting a string as argument ${argIndex + 1}, found this instead: $obj")
    }

  def getTimeSeriesFromArgument(args: Array[Argument], argIndex: java.lang.Integer): LogoTimeSeries =
    args(argIndex).get match {
      case lts: LogoTimeSeries => lts
      case obj =>
        throw new ExtensionException(
          s"time: was expecting a LogoTimeSeries object as argument ${argIndex + 1}, found this instead: $obj")
    }

  def roundDouble(d: java.lang.Double): java.lang.Integer =
    Math.round(d).longValue().intValue()

  def intToDouble(i: Int): java.lang.Double =
    java.lang.Double.valueOf(i)

  def printToLogfile(msg: String): Unit = {
    val logger: Logger = Logger.getLogger("MyLog")
    var fh: FileHandler = null
    try {// This block configure the logger with handler and formatter
      fh = new FileHandler("logfile.txt", true)
      logger.addHandler(fh) //logger.setLevel(Level.ALL);
      val formatter: SimpleFormatter = new SimpleFormatter()
      fh.setFormatter(formatter) // the following statement is used to log any messages
      logger.info(msg)
      fh.close()
    } catch {
      case e: SecurityException => e.printStackTrace()
      case e: IOException => e.printStackTrace()
    }
  }

  // Convenience method, to extract a schedule object from an Argument.
  def getScheduleFromArguments(args: Array[Argument], index: Int): LogoSchedule =
    args(index).get match {
      case lschedule: LogoSchedule => lschedule
      case obj =>
        throw new ExtensionException(
          s"Was expecting a LogoSchedule as argument ${index + 1} found this instead: $obj")
    }

  def printToConsole(context: Context, msg: String): Unit = {
    val extcontext: ExtensionContext = context.asInstanceOf[ExtensionContext]
    extcontext
      .workspace
      .outputObject(msg, null, true, true, OutputDestinationJ.OUTPUT_AREA)
    Files.write(
      Paths.get("./log.txt"),
      (msg + "\n").getBytes,
      StandardOpenOption.APPEND)
  }

  def setContext(context: Context): Unit =
    TimeExtension.context = context
}
