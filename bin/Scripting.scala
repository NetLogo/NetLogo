// Scala 2.9 includes equivalents for this stuff under sys.process, so once we upgrade,
// we should be able to get rid of most or all of this. - ST 4/11/11

object Scripting {

  // pipe a string through a shell command
  def pipe(input: String, cmd: String): Iterator[String] = {
    val process = exec(cmd)
    val out = new java.io.PrintWriter(process.getOutputStream)
    out.print(input)
    out.close(); process.getOutputStream.close() // both necessary? not sure
    io.Source.fromInputStream(process.getInputStream)("UTF-8").getLines
  }

  // get result code from unix shell command
  def exitValue(cmd: String): Int = {
    val process = exec(cmd)
    process.waitFor()
    process.exitValue
  }

  // get iterator over lines of output from a unix shell command
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
  def exec(cmd: String): Process = {
    val builder = new ProcessBuilder("/bin/sh", "-c", cmd)
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
