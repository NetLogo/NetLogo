package org.nlogo.tools

import java.io.{ File, PrintWriter }
import java.nio.file.Files

import org.nlogo.core.TokenType
import org.nlogo.api.XMLReader
import org.nlogo.headless.HeadlessWorkspace

import scala.io.Source
import scala.sys.process.Process
import scala.util.hashing.MurmurHash3
import scala.util.Try

import ujson.{ Arr, Obj }

object PrimitiveScraper {
  def main(args: Array[String]): Unit = {
    val workspace = HeadlessWorkspace.newInstance
    val root = Files.createTempDirectory(null)

    root.toFile.deleteOnExit()

    val maps = args.map { tag =>
      println(s"Scraping models at $tag...")

      val path = root.resolve(tag)

      if (Process(Seq("curl", "-sL", s"https://api.github.com/repos/NetLogo/models/tarball/$tag",
                      "--output", s"$path.tar.gz")).! == 0) {
        path.toFile.mkdirs()

        if (Process(Seq("tar", "-xf", s"$path.tar.gz", "-C", path.toString)).! == 0) {
          println("\tExtracting primitives...")

          val prims = traverseModels(path.toFile, workspace)

          println("\tDone.")

          Some(Obj(tag -> prims))
        } else {
          println("\tFailed to scrape models.")

          None
        }
      } else {
        println("\tFailed to scrape models.")

        None
      }
    }.flatten

    workspace.dispose()

    val writer = new PrintWriter("prims.json")

    writer.println(ujson.reformat(Arr(maps*), 2))
    writer.close()
  }

  private def traverseModels(file: File, workspace: HeadlessWorkspace): Seq[Obj] = {
    if (file.isFile) {
      val name = file.getName

      if (name.endsWith(".nlogox") || name.endsWith(".nlogo") ||
          name.endsWith(".nlogox3d") || name.endsWith(".nlogo3d")) {
        scrapeModel(file, workspace).toSeq
      } else {
        Seq()
      }
    } else if (!Seq("bin", "project", "src", "test").contains(file.getName.toLowerCase)) {
      file.listFiles.toSeq.map(traverseModels(_, workspace)).flatten
    } else {
      Seq()
    }
  }

  private def scrapeModel(modelPath: File, workspace: HeadlessWorkspace): Option[Obj] = {
    val source = Source.fromFile(modelPath, "UTF-8")
    val text = source.getLines.mkString("\n")

    source.close()

    XMLReader.read(text).map(_.getChild("code").text).orElse {
      Try(text.substring(0, text.indexOf("@#$#@#$#@")))
    }.toOption.map { code =>
      val prims = workspace.tokenizeForColorizationIterator(code).foldLeft(Map[String, Int]()) {
        case (map, token) =>
          if (token.tpe == TokenType.Command || token.tpe == TokenType.Reporter) {
            val name = token.text.toLowerCase

            map + ((name, map.getOrElse(name, 0) + 1))
          } else {
            map
          }
      }

      Obj(
        "name" -> modelPath.getName,
        "hash" -> MurmurHash3.stringHash(code),
        "prims" -> prims
      )
    }
  }
}
