import sys.process._

object Scripting {

  implicit val codec: io.Codec = io.Codec.UTF8

  // run shell command (discarding output)
  def shellDo(cmd: String, requireZeroExitStatus: Boolean = true) {
    val status = exec(cmd).!
    require(!requireZeroExitStatus || status == 0,
            "exit status " + status + ": " + cmd)
  }
  
  // pipe a string through a shell command
  def pipe(input: String, cmd: String): Iterator[String] = {
    val inputStream =
      new java.io.ByteArrayInputStream(input.getBytes("UTF-8"))
    exec(cmd).#<(inputStream).lines.iterator
  }

  // run shell command, get iterator over lines of output
  def shell(cmd: String, requireZeroExitStatus: Boolean = true): Iterator[String] =
    if(requireZeroExitStatus)
      exec(cmd).lines.iterator
    else
      exec(cmd).lines_!.iterator

  // run a shell command
  private def exec(cmd: String): ProcessBuilder = {
    // We drop down to j.l.P here in order to merge the output and error streams.  (Why?
    // I forget.  Maybe it was useful in benches.scala or something? - ST 9/14/11)
    val builder = new java.lang.ProcessBuilder("/bin/sh", "-c", cmd)
    builder.redirectErrorStream(true)
    builder
  }

  // read a whole file into a string
  def slurp(name: String): String =
    io.Source.fromFile(name).mkString

  // get iterator over lines in a text file
  def read(name: String): Iterator[String] =
    io.Source.fromFile(name).getLines

}
