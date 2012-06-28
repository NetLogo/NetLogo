// At one time, io.Source was flaky and sys.process wasn't in the standard library, so this class
// was important to have, because it replaced flaky io.Source and wrapped the low-level
// java.lang.Process/ProcessBuilder stuff and made it easy to use from Scala.  But Scala 2.9 has
// sys.process and an io.Source that seems reliable, so I reimplemented the methods here to just
// call those APIs.  What's left is just some thin wrappers that aren't strictly needed anymore.
// But I find them handy. - ST 9/14/11

object Scripting extends sys.process.ProcessImplicits {  // adds methods such as !

  implicit val codec: io.Codec = io.Codec.UTF8

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
    // This could maybe be done instead using sys.process.ProcessIO?
    val builder = new java.lang.ProcessBuilder("/bin/sh", "-c", cmd)
    builder.redirectErrorStream(true)
    builder
  }

}
