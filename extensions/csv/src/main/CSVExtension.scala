package org.nlogo.extensions.csv

import java.io

import org.nlogo.nvm.ExtensionContext

import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.reflect.Selectable.reflectiveSelectable

import org.nlogo.core.LogoList
import org.nlogo.api._
import org.nlogo.core.{ NumberParser, Syntax }
import org.nlogo.api.Reporter
import org.nlogo.api.Command
import org.nlogo.core.Syntax._

import org.apache.commons.csv._
import java.io._

class CSVExtension extends DefaultClassManager {
  def csvFormat(delimiter: Option[String]) =
    (delimiter foldLeft CSVFormat.DEFAULT)(_ `withDelimiter` _(0))

  def parse(str: String, format: CSVFormat) =
    format.parse(new StringReader(str)).iterator.asScala map (_.iterator.asScala)

  def write(row: Iterator[String], format: CSVFormat) = format.format(row.toSeq*)

  def parseValue(entry: String): AnyRef = NumberParser.parse(entry) getOrElse (entry.toUpperCase match {
    case "TRUE"  => true:  java.lang.Boolean
    case "FALSE" => false: java.lang.Boolean
    case _       => entry
  })

  def lift[T](parseItem: T => AnyRef)(row: Iterator[T]): LogoList =
    LogoList.fromIterator(row map parseItem)

  case class ParserPrimitive(process: Iterator[Iterator[String]] => LogoList) extends Reporter {
    override def getSyntax = reporterSyntax(right = List(StringType | RepeatableType), ret = ListType, defaultOption = Some(1))
    override def report(args: Array[Argument], context: Context) =
      process(parse(args(0).getString, csvFormat(args.lift(1).map(_.getString))))
  }

  case class FileParserPrimitive(process: Iterator[Iterator[String]] => LogoList) extends Reporter {
    override def getSyntax = reporterSyntax(right = List(StringType | RepeatableType), ret = ListType, defaultOption = Some(1))
    override def report(args: Array[Argument], context: Context) = {
      val path = context.asInstanceOf[ExtensionContext].workspace.fileManager.attachPrefix(args(0).getString)
      val parserFormat = csvFormat(args.lift(1).map(_.getString))
      try {
        using(new FileReader(new io.File(path))) { reader =>
          process(parserFormat.parse(reader).iterator.asScala.map(_.iterator.asScala))
        }
      } catch {
        case e: FileNotFoundException => throw new ExtensionException("Couldn't find file: " + path, e)
        case e: IOException => throw new ExtensionException("Couldn't open file: " + path, e)
      }
    }
  }

  def rowParser(parseItem: String => AnyRef) = ParserPrimitive { rows =>
    try {
      lift(parseItem)(rows.next())
    } catch {
      case (e: NoSuchElementException) => LogoList("")
    }
  }

  def fullParser(parseItem: String => AnyRef) = ParserPrimitive(lift(lift(parseItem)))

  def toListOfRows(dump: AnyRef => String, rows: LogoList): Iterator[Iterator[String]] = rows.iterator.map {
    case l: LogoList => l.iterator.map(dump)
    case x           => throw new ExtensionException(s"Expected a list of lists, but ${Dump.logoObject(x)} was one of the elements.")
  }


  case class ToLine(dump: AnyRef => String) extends Reporter {
    override def getSyntax = reporterSyntax(
      right = List(ListType, StringType | RepeatableType),
      ret = StringType,
      defaultOption = Some(1))
    override def report(args: Array[Argument], context: Context) =
      write(args(0).getList.iterator map dump, csvFormat(args.lift(1) map (_.getString)))
  }

  case class ToString(dump: AnyRef => String) extends Reporter {
    override def getSyntax = reporterSyntax(right = List(ListType, StringType | RepeatableType), ret = StringType, defaultOption = Some(1))
    override def report(args: Array[Argument], context: Context) = {
      val format = csvFormat(args.lift(1).map(_.getString))
      toListOfRows(dump, args(0).getList).map(write(_, format)).mkString("\n")
    }
  }

  case class ToFile(dump: AnyRef => String) extends Command {
    override def getSyntax = commandSyntax(right = List(StringType, ListType, StringType | RepeatableType), defaultOption = Some(2))
    override def perform(args: Array[Argument], context: Context) = {
      val path = context.asInstanceOf[ExtensionContext].workspace.fileManager.attachPrefix(args(0).getString)
      val format = csvFormat(args.lift(2).map(_.getString))
      try {
        using(new PrintWriter(new io.File(path))) { writer =>
          toListOfRows(dump, args(1).getList).foreach {
            r => writer.println(write(r, format))
          }
        }
      } catch {
        case e: FileNotFoundException => throw new ExtensionException("Couldn't create file: " + path, e)
        case e: IOException => throw new ExtensionException("Couldn't write to file: " + path, e)
      }
    }
  }

  override def load(primManager: PrimitiveManager) = {
    val add = primManager.addPrimitive
    add("from-row", rowParser(parseValue))
    add("from-string", fullParser(parseValue))
    add("from-file", FileParserPrimitive(lift(lift(parseValue))))
    add("to-row", ToLine(Dump.logoObject))
    add("to-string", ToString(Dump.logoObject))
    add("to-file", ToFile(Dump.logoObject))
  }

  def using[A, B <: {def close(): Unit}] (closeable: B) (f: B => A): A =
    try { f(closeable) } finally { closeable.close() }
}
