import sys.process._

object Scripting {

  // run shell command (discarding output)
  def shellDo(cmd: String, requireZeroExitStatus: Boolean = true) {
    val result = Seq("/bin/sh", "-c", cmd).!
    require(!requireZeroExitStatus || result == 0,
            "exit status " + result + ": " + cmd)
  }
  
  // pipe a string through a shell command
  def pipe(input: String, cmd: String): Iterator[String] = {
    val process = exec(cmd)
    val out = new java.io.PrintWriter(process.getOutputStream)
    out.print(input)
    out.close(); process.getOutputStream.close() // both necessary? not sure
    io.Source.fromInputStream(process.getInputStream)("UTF-8").getLines
  }

  // run shell command, get iterator over lines of output
  def shell(cmd: String, requireZeroExitStatus: Boolean = true): Iterator[String] =
    new Iterator[String] {
      val process = exec(cmd)
      val source = io.Source.fromInputStream(process.getInputStream)("UTF-8")
      val iter = source.getLines
      def next() = iter.next()
      def hasNext = iter.hasNext || {
        process.waitFor()
        if (requireZeroExitStatus)
          require(process.exitValue == 0, "exit status " + process.exitValue + ": " + cmd)
        false
      }
    }

  // run a shell command
  private def exec(cmd: String): java.lang.Process = {
    val builder = new java.lang.ProcessBuilder("/bin/sh", "-c", cmd)
    builder.redirectErrorStream(true)
    builder.start()
  }

  // read a whole file into a string
  def slurp(name: String): String =
    io.Source.fromFile(name)("UTF-8").mkString

  // get iterator over lines in a text file
  def read: Iterator[String] =
    io.Source.fromInputStream(System.in)("UTF-8").getLines
  def read(name: String): Iterator[String] =
    io.Source.fromFile(name)("UTF-8").getLines

}
