package arduino

import scala.util.{ Either, Left, Try }
import scala.annotation.tailrec

object MessageParser {
  type ResultSeq = Seq[Either[ErrorRecord,ValuePair]]
  private val EmptyResults = Seq.empty[Either[ErrorRecord,ValuePair]]

  final def parseStream(streamContent: String): (String, ResultSeq) = {
    val (s, r) = parseStream(streamContent, EmptyResults)
    (s, r.reverse)
  }

  @tailrec
  final def parseStream(streamContent: String, acc: ResultSeq): (String, ResultSeq) = {
    val semiIndex = streamContent.indexOf(';')
    if (semiIndex == 0)
      parseStream(streamContent.substring(semiIndex + 1, streamContent.length), acc)
    else if (semiIndex != -1) {
      parseStream(streamContent.substring(semiIndex + 1, streamContent.length),
        parseEntry(streamContent.substring(0, semiIndex)) +: acc)
    } else
      (streamContent, acc)
  }

  def parseEntry(entry: String): Either[ErrorRecord, ValuePair] = {
    val fields = entry.split(",")
    val valueField =
      if (fields.length == 3) Right(fields(2))
      else {
        val accumulatedContent = fields.drop(2).foldLeft(List.empty[String]) {
          case (hd::tl, s) if hd.endsWith("\\") => (hd.dropRight(1) + "," + s) :: tl
          case (acc, s) => s :: acc
        }
        if (accumulatedContent.length == 1)
          Right(accumulatedContent.head)
        else
          Left(ErrorRecord(entry, "Arduino values must have three comma-separated fields", None))
      }

    valueField.map(content => (fields(0).toLowerCase, fields(1).toUpperCase.headOption, content))
      .flatMap {
        case (name, Some('S'), rawValue) =>
          Right(ValuePair(name, rawValue))
        case (name, Some('D'), rawValue) =>
          Try(rawValue.toDouble)
            .fold(
            {
              case e: Exception => Left(ErrorRecord(entry, "Cannot parse number", Some(e)))
              case t: Throwable => throw t
            },
            d => Right(ValuePair(name, Double.box(d))))
         case (name, tpe, rawValue) =>
           Left(ErrorRecord(entry, s"Unknown type '${tpe.getOrElse("")}' for value '$name'", None))
      }
  }
}
