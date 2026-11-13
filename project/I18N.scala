import java.nio.file.{ Files, Path, Paths, StandardOpenOption }

import sbt.{ Compile, taskKey }
import sbt.Keys.{ baseDirectory, resourceDirectory, sources }

import scala.meta.{ Input, Source, Term, XtensionCollectionLikeUI, XtensionDialectApply, XtensionParseDialectInput,
                    XtensionSyntax }
import scala.meta.dialects.Scala37

object I18N {
  private val extractStrings = taskKey[Unit]("extract localized strings into a PO template file")

  val settings = {
    extractStrings := {
      val root: Path = baseDirectory.value.toPath.toAbsolutePath.getParent

      val output: String = (Compile / sources).value.foldLeft("") {
        case (acc, file) if file.getName.endsWith(".scala") =>
          val path: Path = file.toPath.toAbsolutePath
          val input: Input = Input.VirtualFile(file.getAbsolutePath, Files.readString(path))
          val source: Source = Scala37(input).parse[Source].get

          val locs: Seq[String] = source.collect {
            case term @ Term.Interpolate(Term.Name("loc"), parts, args) =>
              val relative: String = root.relativize(path).toString.replace("\\\\", "/")
              val header = s"#: $relative:${term.pos.startLine + 1}"

              val message: String = parts.zip(args.map(_ => s"{}") :+ "").map {
                case (part, arg) =>
                  part.syntax + arg
              }.mkString.replace("\"", "\\\"")

              s"""$header\nmsgid "$message"\nmsgstr """""
          }

          if (locs.nonEmpty) {
            s"$acc\n${locs.mkString("\n")}\n"
          } else {
            acc
          }

        case (acc, _) =>
          acc
      }

      Files.writeString(root.resolve("shared/resources/main/i18n/netlogo.pot"), s"${output.trim}\n",
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
  }
}
