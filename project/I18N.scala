import com.github.javaparser.{ ParserConfiguration, StaticJavaParser }
import com.github.javaparser.ast.expr.{ MethodCallExpr, StringLiteralExpr, TextBlockLiteralExpr }

import java.nio.file.{ Files, Path, StandardOpenOption }

import sbt.{ Compile, taskKey }
import sbt.Keys.{ baseDirectory, sources }

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.meta.{ Input, Source, Term, XtensionCollectionLikeUI, XtensionDialectApply, XtensionParseDialectInput,
                    XtensionSyntax }
import scala.meta.dialects.Scala37

object I18N {
  private val extractStrings = taskKey[Unit]("extract localized strings into a PO template file")

  val settings = {
    extractStrings := {
      val base: Path = baseDirectory.value.toPath.toAbsolutePath
      val root: Path = base.getParent

      StaticJavaParser.setConfiguration(new ParserConfiguration {
        setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
      })

      val javaEntries: Seq[Entry] = (Compile / sources).value.filter(_.getName.endsWith(".java")).flatMap { file =>
        val relative: String = root.relativize(file.toPath.toAbsolutePath).toString.replace("\\\\", "/")
        val result = StaticJavaParser.parse(file)

        result.findAll(classOf[MethodCallExpr]).asScala.collect {
          case expr: MethodCallExpr if expr.getName.asString == "loc" =>
            expr.getArguments.getFirst.get match {
              case arg: StringLiteralExpr =>
                Entry(relative, expr.getBegin.get.line, arg.asString.replace("\"", "\\\""))

              case arg: TextBlockLiteralExpr =>
                Entry(relative, expr.getBegin.get.line, formatMessage(arg.asString))
            }
        }
      }

      val scalaEntries: Seq[Entry] = (Compile / sources).value.filter(_.getName.endsWith(".scala")).flatMap { file =>
        val path: Path = file.toPath.toAbsolutePath
        val relative: String = root.relativize(path).toString.replace("\\\\", "/")
        val input: Input = Input.VirtualFile(file.getAbsolutePath, Files.readString(path))
        val source: Source = Scala37(input).parse[Source].get

        source.collect {
          case term @ Term.Interpolate(Term.Name("loc"), parts, args) =>
            val message: String = parts.zip(args.map(_ => s"{}") :+ "").map {
              case (part, arg) =>
                part.syntax + arg
            }.mkString

            Entry(relative, term.pos.startLine + 1, formatMessage(message))
        }
      }

      val entries: Seq[Entry] = javaEntries ++ scalaEntries

      val outDir: Path = base.resolve("resources/i18n")

      Files.createDirectories(outDir)
      Files.writeString(outDir.resolve("netlogo.pot"), formatEntries(entries, false), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)
      Files.writeString(outDir.resolve("en.po"), formatEntries(entries, true), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)
    }
  }

  private def formatMessage(message: String): String =
    message.replace("\"", "\\\"").split('\n').map("\\s*\\|".r.replaceFirstIn(_, "")).mkString(" ")

  private def formatEntries(entries: Seq[Entry], copyStr: Boolean): String = {
    entries.groupBy(_.msgid).toSeq.sortBy(_._1).map {
      case (msgid, entries) =>
        val header: String = entries.map {
          case Entry(path, line, _) =>
            s"#: $path:$line"
        }.mkString("\n")

        val msgstr: String = {
          if (copyStr) {
            msgid
          } else {
            ""
          }
        }

        s"""$header\nmsgid "$msgid"\nmsgstr "$msgstr""""
    }.mkString("", "\n\n", "\n")
  }

  private case class Entry(path: String, line: Int, msgid: String)
}
