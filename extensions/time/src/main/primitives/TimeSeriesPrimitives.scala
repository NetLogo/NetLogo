package org.nlogo.extensions.time.primitives

import java.util.ArrayList
import org.nlogo.api.Argument
import org.nlogo.api.Command
import org.nlogo.api.Context
import org.nlogo.api.Dump
import org.nlogo.api.ExtensionException
import org.nlogo.api.Reporter
import org.nlogo.core.LogoList
import org.nlogo.core.Syntax
import org.nlogo.nvm.ExtensionContext
import org.nlogo.extensions.time._
import org.nlogo.extensions.time.datatypes._

object TimeSeriesPrimitives {
  class TimeSeriesAddRow extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(right = List(Syntax.WildcardType, Syntax.WildcardType))
    def perform(args: Array[Argument], context: Context): Unit = {
      val ts: LogoTimeSeries = TimeUtils.getTimeSeriesFromArgument(args, 0)
      val list: LogoList = TimeUtils.getListFromArgument(args, 1)
      val timeObj: AnyRef = list.get(0)
      var time: LogoTime = null
      timeObj match {
        case tObj: String =>
          time = new LogoTime(tObj)
        case tObj: LogoTime =>
          time = new LogoTime(tObj)
        case tObj =>
        throw new ExtensionException(
          "time: was expecting a LogoTime object as the first item in the list passed as argument 2, found this instead: " +
            Dump.logoObject(tObj))
      }
      if (list.size != (ts.getNumColumns() + 1))
        throw new ExtensionException(
          "time: cannot add " + (list.size - 1) + " values to a time series with " +
            ts.getNumColumns() +
            " columns.")
      ts.add(time, list.logoSublist(1, list.size))
    }

  }

  class TimeSeriesCreate extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      var columnList: LogoList = null
      try columnList = TimeUtils.getListFromArgument(args, 0)
      catch {
        case e: ExtensionException => {
          val cols: ArrayList[String] = new ArrayList[String]()
          val colName: String = TimeUtils.getStringFromArgument(args, 0)
          cols.add(colName)
          columnList = LogoList.fromJava(cols)
        }
      }
      new LogoTimeSeries(columnList)
    }
  }

  class TimeSeriesGet extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType,
                                         Syntax.WildcardType,
                                         Syntax.StringType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val ts: LogoTimeSeries = TimeUtils.getTimeSeriesFromArgument(args, 0)
      val time: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      ts.ensureDateTypeConsistent(time)
      var columnName: String = TimeUtils.getStringFromArgument(args, 2)
      if (columnName.==("ALL") || columnName.==("all")) {
        columnName = "ALL_-_COLUMNS"
      }
      ts.getByTime(time, columnName, Nearest)
    }
  }

  class TimeSeriesGetExact extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType,
                                         Syntax.WildcardType,
                                         Syntax.StringType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val ts: LogoTimeSeries = TimeUtils.getTimeSeriesFromArgument(args, 0)
      val time: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      ts.ensureDateTypeConsistent(time)
      var columnName: String = TimeUtils.getStringFromArgument(args, 2)
      if (columnName.==("ALL") || columnName.==("all")) {
        columnName = "ALL_-_COLUMNS"
      }
      ts.getByTime(time, columnName, Exact)
    }

  }

  class TimeSeriesGetInterp extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType,
                                         Syntax.WildcardType,
                                         Syntax.StringType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val ts: LogoTimeSeries = TimeUtils.getTimeSeriesFromArgument(args, 0)
      val time: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      ts.ensureDateTypeConsistent(time)
      var columnName: String = TimeUtils.getStringFromArgument(args, 2)
      if (columnName.==("ALL") || columnName.==("all")) {
        columnName = "ALL_-_COLUMNS"
      }
      ts.getByTime(time, columnName, LinearInterp)
    }
  }

  class TimeSeriesGetRange extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.WildcardType, Syntax.WildcardType,
                                         Syntax.WildcardType, Syntax.StringType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val ts: LogoTimeSeries = TimeUtils.getTimeSeriesFromArgument(args, 0)
      val timeA: LogoTime = TimeUtils.getTimeFromArgument(args, 1)
      ts.ensureDateTypeConsistent(timeA)
      val timeB: LogoTime = TimeUtils.getTimeFromArgument(args, 2)
      ts.ensureDateTypeConsistent(timeB)
      var columnName: String = TimeUtils.getStringFromArgument(args, 3)
      if (columnName.==("logotime")) {
        columnName = "LOGOTIME"
      }
      if (columnName.==("ALL") || columnName.==("all")) {
        columnName = "ALL_-_COLUMNS"
      }
      ts.getRangeByTime(timeA, timeB, columnName)
    }
  }

  class TimeSeriesLoad extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.StringType), ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val filename: String = TimeUtils.getStringFromArgument(args, 0)
       new LogoTimeSeries(filename, context.asInstanceOf[ExtensionContext])
    }
  }

  class TimeSeriesLoadWithFormat extends Reporter {
    def getSyntax: Syntax =
      Syntax.reporterSyntax(right = List(Syntax.StringType, Syntax.StringType),
                            ret = Syntax.WildcardType)
    def report(args: Array[Argument], context: Context): AnyRef = {
      val filename: String = TimeUtils.getStringFromArgument(args, 0)
      val format: Option[String] =
        TimeUtils.getStringFromArgument(args, 1) match {
          case null => None
          case x => Some(x) }
      new LogoTimeSeries(filename,format,context.asInstanceOf[ExtensionContext])
    }
  }

  class TimeSeriesWrite extends Command {
    def getSyntax: Syntax =
      Syntax.commandSyntax(right = List(Syntax.WildcardType, Syntax.StringType))
    def perform(args: Array[Argument], context: Context): Unit = {
      val ts: LogoTimeSeries = TimeUtils.getTimeSeriesFromArgument(args, 0)
      val filename: String = TimeUtils.getStringFromArgument(args, 1)
      ts.write(filename, context.asInstanceOf[ExtensionContext])
    }
  }
}
