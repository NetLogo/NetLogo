// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import
  java.nio.file.{ Files, Path, Paths }

import
  java.io.StringWriter

import
  javax.xml.transform.{ stream, OutputKeys, Source, TransformerFactory },
    stream.{ StreamResult, StreamSource }

import
  org.nlogo.util.PathTools

object XmlTool extends App {
  import XmlToolProcessor._

  lazy val HelpInformation =
    """|XmlTool performs common tasks on XML")
       |
       |Usage: <invocation> <operation> <args>
       |
       |Operations:
       |
       |  indent <files>
       |
       |  Indents each xml file using a 2-space indent. Resaves each file.
       |
       |  xslt [-r][-v] <xsl-file> <targets>
       |
       |  Applies the given xslt file to each of the target files (recursively if -r supplied). Resaves the transformed files.""".stripMargin

  if (args.length == 0 || args(0) == "help")
    Console.println(HelpInformation)
  else {
    args(0).toLowerCase match {
      case "indent" =>
        warnIfEmpty(parseFiles(args.tail), indentFile _)
      case "xslt"   =>
        val (xsltFile, isVerbose, targetFiles) = {
          val (isRecursive, isVerbose, argStart) = {
            val r = args.contains("-r")
            val v = args.contains("-v")
            val argStart = 1 + (if (r) 1 else 0) + (if (v) 1 else 0)
            (r, v, argStart)
          }
          val processArgumentFiles: Path => Seq[Path] =
            if (isRecursive) recursiveChildren _
            else ((p: Path) => Seq(p))
          (parseFile(args(argStart)),
            isVerbose,
            parseFiles(args.drop(argStart + 1)).flatMap(processArgumentFiles))
        }
        if (xsltFile.isEmpty)
          Console.println("[warn] Unable to locate xslt sheet, exiting...")
        else
          xsltFile.foreach(xslt => warnIfEmpty(targetFiles, applyStylesheet(xslt, isVerbose)))
      case _ => Console.println(HelpInformation)
    }
  }

  def warnIfEmpty(paths: Seq[Path], op: Path => Unit): Unit = {
    if (paths.isEmpty)
      Console.println("[warn] No files found to operate on, exiting...")
    else
      paths.foreach(op)
  }

  def parseFile(s: String): Option[Path] = {
    val p = Paths.get(s)
    if (Files.exists(p))
      Some(p)
    else {
      Console.println(s"[warn] File not found $s")
      None
    }
  }

  def parseFiles(fileStrings: Seq[String]): Seq[Path] =
    fileStrings.flatMap(parseFile _)

  def recursiveChildren(path: Path): Seq[Path] =
    PathTools.findChildrenRecursive(path)
      .filter(p => p.getFileName.toString.endsWith(".nlogox"))
}

object XmlToolProcessor {
  val NLogoXPrettyIndent = 2

  val transformerFactory = TransformerFactory.newInstance

  def newTransformer(sourceOption: Option[Source] = None) = {
    val transformer =
      sourceOption.map(transformerFactory.newTransformer _)
        .getOrElse(transformerFactory.newTransformer)
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", NLogoXPrettyIndent.toString)
    transformer
  }

  def applyStylesheet(stylesheetPath: Path, isVerbose: Boolean)(path: Path): Unit = {
    val (res, writer) = stringResult
    if (isVerbose) {
      Console.println(s"transforming $path")
    }
    newTransformer(Some(stylesheetPath.toSource)).transform(path.toSource, res)
    path.replaceContentsWith(writer.toString)
  }

  def indentFile(path: Path): Unit = {
    val (res, writer) = stringResult
    newTransformer().transform(path.toSource, res)
    path.replaceContentsWith(writer.toString)
  }

  implicit class RichPath(p: Path) {
    def toSource: StreamSource = {
      new StreamSource(Files.newBufferedReader(p))
    }

    def replaceContentsWith(s: String): Unit = {
      Files.write(p, s.getBytes("UTF-8"))
    }
  }

  def stringResult: (StreamResult, StringWriter) = {
    val w = new StringWriter()
    val r = new StreamResult(w)
    w.append("<?xml version='1.0' encoding='UTF-8'?>\n")
    (r, w)
  }
}
