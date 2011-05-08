// Scala 2.9 includes equivalents for this stuff under sys.process, so once we upgrade,
// we should be able to get rid of most or all of this. - ST 4/11/11

object Scripting {
  // pipe a string through a shell command
  def pipe(input: String, cmd: String): Iterator[String] = {
    val process = exec(cmd)
    val out = new java.io.PrintWriter(process.getOutputStream)
    out.print(input)
    out.close(); process.getOutputStream.close() // both necessary? not sure
    lines(io.Source.fromInputStream(process.getInputStream)("UTF-8"))
  }
  // get result code from unix shell command
  def exitValue(cmd: String): Int = {
    val process = exec(cmd)
    process.waitFor()
    process.exitValue
  }
  // get iterator over lines of output from a unix shell command
  // Note I don't use io.Source. I was having weird problems with
  // bin/benches.scala hanging and I thought io.Source might be
  // the culprit.  If this new code is found to be reliable, maybe
  // the other uses of io.Source in this file should be replaced
  // too. - ST 2/13/09
  def shell(cmd: String): Iterator[String] = shell(cmd, true)
  def shell(cmd: String, requireZeroExitStatus: Boolean): Iterator[String] = {
    val process = exec(cmd)
    val reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream, "UTF-8"))
    new Iterator[String] {
      var line = nextLine()
      def next = { val result = line; line = null; result }
      def hasNext = {
        if (line == null) line = nextLine()
        line != null
      }
      private def nextLine() = {
        val result = reader.readLine()
        if (result == null) {
          process.waitFor()
          if (requireZeroExitStatus)
            require(process.exitValue == 0, "exit status " + process.exitValue + ": " + cmd)
        }
        result
      }
    }
  }
  // run a shell command
  val sh =
    if (System.getProperty("os.name").startsWith("Windows"))
      "c:/cygwin/bin/sh.exe"
    else "/bin/sh"
  def exec(cmd: String): Process = {
    val builder = new ProcessBuilder(sh, "-c", cmd)
    builder.redirectErrorStream(true)
    builder.start()
  }
  // get an iterator of lines from an IO source
  def lines(source: io.Source) = source.getLines
  // get iterator over lines in a text file
  def read: Iterator[String] = lines(io.Source.fromInputStream(System.in)("UTF-8"))
  def read(name: String): Iterator[String] = lines(readChars(name))
  def readChars(name: String): io.Source = io.Source.fromFile(name)("UTF-8")
}
