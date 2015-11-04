import sbt._

object RunProcess {
  def apply(args: Seq[String], taskName: String): Unit = {
    apply(args, None, taskName)
  }

  def apply(args: Seq[String], workingDirectory: File, taskName: String): Unit = {
    apply(args, Some(workingDirectory), taskName)
  }

  def apply(args: Seq[String], workingDirectory: Option[File], taskName: String): Unit = {
    val res = Process(args, workingDirectory).!
    if (res != 0) {
      sys.error(s"$taskName failed!\n" +
        args.map(_.replaceAllLiterally(" ", "\\ ")).mkString(" "))
    }
  }
}

