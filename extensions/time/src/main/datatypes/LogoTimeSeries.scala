package org.nlogo.extensions.time.datatypes

import java.io.{BufferedWriter, File, FileWriter}
import java.util.{ArrayList, Arrays}
import scala.collection.immutable.TreeMap
import scala.collection.mutable.LinkedHashMap
import scala.io.Source

import org.nlogo.api.ExtensionException
import org.nlogo.core.{ExtensionObject, LogoList}
import org.nlogo.nvm.ExtensionContext
import org.nlogo.extensions.time._
import scala.util.{Try, Success, Failure}
import util.control.Breaks.{breakable, break}

import scala.jdk.CollectionConverters.{ ListHasAsScala, SeqHasAsJava, SetHasAsJava }

class LogoTimeSeries extends ExtensionObject {
  implicit val logoComparator: org.nlogo.extensions.time.datatypes.LogoTimeComparator.type = LogoTimeComparator
  var times: TreeMap[LogoTime, TimeSeriesRecord] =
    new TreeMap[LogoTime, TimeSeriesRecord]()
  var scacolumns: LinkedHashMap[String, TimeSeriesColumn] =
    new LinkedHashMap[String, TimeSeriesColumn]()
  var numRows = 0

  def this(colNames: LogoList) = {
    this()
    for (colName <- colNames) {
      scacolumns += (colName.toString -> new TimeSeriesColumn())
    }
  }

  def this(filename: String, customFormat: Option[String], context: ExtensionContext) = {
    this()
    parseTimeSeriesFile(filename, customFormat, context)
  }

  def this(filename: String, context: ExtensionContext) = {
    this()
    parseTimeSeriesFile(filename, context)
  }

  def add(time: LogoTime, list: LogoList): Unit = {
    val index: Int = times.size
    val record = new TimeSeriesRecord(time, index)
    scacolumns.keySet.zipWithIndex.foreach{ case (colName, index) =>
      scacolumns
        .getOrElse(colName, throw new ExtensionException("columns section doesn't exist")).add(list.get(index).toString)
    }
    try times = times + (time -> record)
    catch {
      case e: NullPointerException =>
        if (time.dateType != times.keySet.toArray(using scala.reflect.classTag[LogoTime])(0).dateType) {
          throw new ExtensionException(s"date type mismatch ${time.dateType}")
        } else {
          throw e
        }
    }
  }

  def getNumColumns(): Integer = scacolumns.size

  def write(filename: String, context: ExtensionContext): Unit = {
    val dataFile = filename.charAt(0) match {
      case '/' => new File(filename)
      case '\\' => new File(filename)
      case _ if context.workspace.getModelDir == null =>
        new File("./" + filename)
      case _ =>
        new File(context.workspace.getModelDir + "/" + filename)
    }

    val fw = new FileWriter(dataFile.getAbsoluteFile)
    val bw: BufferedWriter = new BufferedWriter(fw)
    bw.write("TIMESTAMP")
    scacolumns.keySet.foreach(colName => bw.write("," + colName))
    bw.write("\n")
    for (logoTime <- times.keySet) {
      val time: TimeSeriesRecord = times.getOrElse(logoTime, throw new ExtensionException(s"TimeSeriesRecord uninitialized for $logoTime"))
      bw.write(time.time.dump(false, false, false))
      scacolumns.keySet
        .foreach( colName =>
          bw.write("," + scacolumns
            .getOrElse(colName, throw new ExtensionException(s"colName doesn't exist: $colName")).data(time.dataIndex)))
      bw.write("\n")
    }
    bw.flush()
    bw.close()
  }

  def parseTimeSeriesFile(filename: String, context: ExtensionContext): Unit = {
    parseTimeSeriesFile(filename, None, context)
  }

  /*  parseTimeSeriesFile parses the files and adds them into the LogoTimeSeries
   *  object. There are a couple global variables: columns and times
   */
  def parseTimeSeriesFile(filename: String, customFormat: Option[String], context: ExtensionContext): Unit =
    Try(Source.fromFile(context.attachCurrentDirectory(filename))) match {
      case Success(bufferedSource) =>
        var ind = 0
        var columnNames = Array[String]()
        for( line <- bufferedSource.getLines())
          breakable {
            line match {
              case linedata if linedata.charAt(0) == ';' => break()
              case _ if ind == 0 =>
                ind = ind + 1
                columnNames = line.split(',').map(_.trim)
                for ( columnName <- Arrays.copyOfRange(columnNames, 1, columnNames.length)){
                  scacolumns += (columnName -> new TimeSeriesColumn())
                }
              case linedata =>
                val lineData = linedata.split(",").map(_.trim)
                val newTime = new LogoTime(lineData(0), customFormat)
                times = times + (newTime -> new TimeSeriesRecord(newTime, numRows))
                numRows = numRows + 1
                var colInd: Int = 1
                while (colInd <= scacolumns.size) {
                  scacolumns
                    .getOrElse(columnNames(colInd), throw new ExtensionException(s"Failed term: ${columnNames(colInd)} and keyset ${scacolumns.keySet}"))
                    .add(lineData(colInd))
                  colInd += 1
                }
            }
          }
      case Failure(_) => throw new ExtensionException("File cannot be saved without loading/saving a model")
    }

  def getByTime(time: LogoTime, columnName: String, getMethod: GetTSMethod): AnyRef = {
    val columnList: ArrayList[String] = new ArrayList[String](scacolumns.size)
    val resultList: ArrayList[AnyRef] = new ArrayList[AnyRef](scacolumns.size)
    if (columnName.==("ALL_-_COLUMNS")) {
      columnList.addAll(scacolumns.keySet.asJava)
    } else if (!scacolumns.contains(columnName)) {
      throw new ExtensionException(s"The LogoTimeSeries does not contain the column $columnName")
    } else {
      columnList.add(columnName)
    }
    var finalKey: LogoTime  = null
    var lowerKey: LogoTime  = null
    var higherKey: LogoTime = null

    finalKey = times.get(time) match {
      case None =>
        (Try(times.keySet.filter(stime => !stime.isBefore(time) || stime.isEqual(time)).max),
         Try(times.keySet.filter(stime =>  stime.isBefore(time) || stime.isEqual(time)).min)) match {
          case (Failure(_), Failure(_)) => time
          case (Failure(_), Success(lowerKey)) => lowerKey
          case (Success(higherKey), Failure(_)) => higherKey
          case (Success(higherKey), Success(lowerKey)) => getMethod match {
            case Exact =>
              throw new ExtensionException(
                s"The LogoTime ${time.dump(false,false,false)} does not exist in the time series.")
            case Nearest =>
              if (time.isCloserToAThanB(lowerKey, higherKey)) lowerKey
              else higherKey
            case LinearInterp =>
              if (time.isCloserToAThanB(lowerKey, higherKey)) lowerKey
              else higherKey
          }
        }
      case Some(record) => time
    }

    if(columnName == ("ALL_-_COLUMNS")) resultList.add(finalKey)
    for (colName <- columnList.asScala) {
      getMethod match {
        case LinearInterp =>
          if(lowerKey == null &&  higherKey == null) {
              resultList.add(scacolumns.getOrElse(colName, throw new ExtensionException(s"colName doesn't exist in columns: $colName"))
                .data.map(_.toDouble).apply(times.getOrElse(finalKey, throw new ExtensionException(s"finalKey doesn't exist in data")).dataIndex).asInstanceOf[AnyRef])
          } else {
            if(lowerKey == null)
              lowerKey = time
            else if (higherKey == null)
              higherKey = time
            val lowerKeyCol =
              scacolumns.getOrElse(colName, throw new ExtensionException("lowerKeyColumn is not provided"))
                .data.apply(times.getOrElse(lowerKey, throw new ExtensionException("lowerKey doesn't exist in data")).dataIndex)
            val higherKeyCol =
              scacolumns.getOrElse(colName, throw new ExtensionException("colName is not provided"))
                .data.map(_.toDouble).apply(times.getOrElse(higherKey, throw new ExtensionException("higherKey doesn't exist in data")).dataIndex)
            val lowerKeyCol2 =
              scacolumns.getOrElse(colName, throw new ExtensionException("colName is not provided"))
                .data.map(_.toDouble).apply(times.getOrElse(lowerKey, throw new ExtensionException("lowerKey doesn't exist in data")).dataIndex)
            val keyDiv =
              lowerKey.getDifferenceBetween(Milli, time) / lowerKey.getDifferenceBetween(Milli, higherKey)
            resultList.add((lowerKeyCol + (higherKeyCol - lowerKeyCol2) * keyDiv).toString)
          }
        case _ =>
          resultList.add(scacolumns.getOrElse(colName, throw new ExtensionException("colName doesn't exist in columns"))
            .data.map(_.toDouble).apply(times.getOrElse(finalKey, throw new ExtensionException("finalKey doesn't exist in times")).dataIndex).asInstanceOf[AnyRef])
      }
    }
    if (resultList.size == 1)
      resultList.get(0)
    else
      LogoList.fromJava(resultList)
  }

  def getRangeByTime(timeLowArg: LogoTime, timeHighArg: LogoTime, columnName: String): AnyRef = {
    if(columnName == null){ throw new ExtensionException(s"$columnName is null") }
    var timeLow = timeLowArg
    var timeHigh = timeHighArg
    if (!timeLow.isBefore(timeHigh)) {
      val timeTemp: LogoTime = timeLow
      timeLow = timeHigh
      timeHigh = timeTemp
    }

    val columnList: ArrayList[String] = new ArrayList[String](scacolumns.size)
    val resultList: ArrayList[LogoList] = new ArrayList[LogoList](scacolumns.size)
    columnName match {
        case "ALL_-_COLUMNS" => columnList.addAll(scacolumns.keySet.asJava)
        case "LOGOTIME" =>
        case str if !scacolumns.contains(columnName) =>
          throw new ExtensionException(s"The LogoTimeSeries does not contain the column $columnName")
        case _ => columnList.add(columnName)
    }

    var lowerKey: LogoTime = timeLow
    var higherKey: LogoTime = timeHigh
    if (times.get(lowerKey) == None) {  // NOTE: I think we don't need this. It's fine if the list is empty, just return an empty list.
      val timelist = times.keySet.filter(stime => !stime.isBefore(timeLow) || stime.isEqual(timeLow))
      if(!timelist.isEmpty)
        lowerKey = timelist.min
      else throw new ExtensionException("List is empty (Low)")
    }
    if(times.get(higherKey) == None) {  // NOTE: I think we don't need this. It's fine if the list is empty, just return an empty list.
      val timelist = times.keySet.filter(stime => stime.isBefore(timeHigh) || stime.isEqual(timeHigh))
      if(!timelist.isEmpty) higherKey = timelist.max
      else throw new ExtensionException("List is empty (High)")
    }
    if (lowerKey == null || higherKey == null) {
      if (columnName == "ALL_-_COLUMNS" || columnName == "LOGOTIME") {
        resultList.add(LogoList.fromVector(scala.collection.immutable.Vector[Any]().asInstanceOf[Vector[AnyRef]]))
      }
      for (colName <- columnList.asScala) {
        resultList.add(
          LogoList.fromVector(scala.collection.immutable.Vector[Any]()
            .asInstanceOf[Vector[AnyRef]]))
      }
    } else {
      if (columnName.==("ALL_-_COLUMNS") || columnName.==("LOGOTIME")) {
        resultList.add(LogoList.fromJava(times.range(lowerKey, higherKey).keySet.asJava))
      }
      for (colName <- columnList.asScala) {
        resultList.add(LogoList.fromJava(scacolumns.getOrElse(colName, throw new ExtensionException("colName doesn't exist in columns")).data
          .slice(from = times.getOrElse(lowerKey, throw new ExtensionException("colName doesn't exist in columns")).dataIndex,
            until = times.getOrElse(higherKey, throw new ExtensionException("higherKey doesn't exist in times")).dataIndex + 1).asJava))
      }
    }
    resultList.size match {
      case 1 =>
        val allDoubles = resultList.get(0).toVector
          .forall(x =>
            try {
              x.asInstanceOf[String].toDouble.asInstanceOf[AnyRef]; true
            } catch {
              case _: Throwable => false
            }
          )
        if(allDoubles)
          LogoList.fromVector(resultList.get(0).toVector
            .map(x => x.asInstanceOf[String].toDouble.asInstanceOf[AnyRef]))
        else
          resultList.get(0)
      case n =>
        LogoList.fromJava(resultList)
    }
  }

  def dump(arg0: Boolean, arg1: Boolean, arg2: Boolean): String = {
    var result: String = "TIMESTAMP"
    scacolumns.keySet.foreach{ case (colName) => result += "," + colName }
    result += "\n"
    times.keySet.foreach{
      case (logoTime) =>
        val time: TimeSeriesRecord = times.getOrElse(logoTime, throw new ExtensionException(s"times doesn't contain LogoTime: ${logoTime}"))
        result += time.time.dump(false, false, false)
        for (colName <- scacolumns.keySet) {
          result = result + "," + scacolumns.getOrElse(colName, throw new ExtensionException(s"columns doesn't contain colName: ${colName}")).data.apply(time.dataIndex)
        }
        result += "\n"
    }
    result
  }

  def ensureDateTypeConsistent(time: LogoTime): Unit = {
    if (times.size > 0) {
      if (times.firstKey.dateType != time.dateType) {
        throw (new ExtensionException(
          "The LogoTimeSeries contains LogoTimes of type " + times
            .firstKey
            .dateType
            .toString +
            " while the LogoTime " +
            time.toString +
            " used in the search is of type " +
            time.dateType.toString))
      }
    }
  }
  def getExtensionName: String = "time"
  def getNLTypeName: String = "LogoTimeSeries"
  def recursivelyEqual(arg0: AnyRef): Boolean = false
}
