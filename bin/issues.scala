#!/usr/bin/env scalas
!#

// This generates Markdown suitable for pasting into
// https://github.com/NetLogo/NetLogo/wiki/Release-notes

// running this is tricky because the sbt script mode stuff isn't currently maintained in sync with
// conscript so you have fiddle with it or it doesn't work. instructions:
// - install conscript if you don't have it already:
//   curl https://raw.github.com/n8han/conscript/master/setup.sh | sh
// - install sbt (and the scalas script) through conscript:
//   cs harrah/xsbt --branch v0.11.3
// - edit ~/.conscript/harrah/xsbt/scalas/launchconfig
//    and ~/.conscript/harrah/xsbt/sbt/launchconfig
//   and remove the entire [organization] section
//   from both files
// - ensure that ~/bin is included in your `PATH` environment variable
//   (this is where Conscript places the scripts that it manages)
// - note: if you run the script for the first time and it appears non-responsive, do not fear;
//         wait at least a few minutes for it to try downloading the dependencies before panicking!

// We're using Dispatch Reboot here, even though it's still in alpha, instead of just using the old,
// stable Dispatch.  It's not for any pragmatic reason -- just wanted to give the reboot version
// a try. - ST 4/25/12

/***
scalaVersion := "2.9.2"

onLoadMessage := ""

libraryDependencies ~= { seq =>
    val vers = "0.9.0-beta1"
    seq ++ Seq("net.databinder.dispatch" %% "core" % vers,
               "net.liftweb" % "lift-json_2.9.1" % "2.4",
               "org.slf4j" % "slf4j-nop" % "1.6.0")
}
*/

import dispatch._
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._

object Issue {
  def fromJson(j: JValue): Issue = {
    val JInt(n) = j \ "number"
    val JString(title) = j \ "title"
    Issue(n.toInt, title)
  }
}
case class Issue(number: Int, title: String)

val host = :/("api.github.com").secure
val base = host / "repos" / "NetLogo" / "NetLogo" / "issues"
val req = base <<? Map("milestone" -> "11",
                       "state" -> "closed",
                       "per_page" -> "1000")
val stream = Http(req > As(_.getResponseBodyAsStream)).apply
val parsed = JsonParser.parse(new java.io.InputStreamReader(stream))
val issues: List[Issue] =
  (for {
    JArray(objs) <- parsed
    obj <- objs
  } yield Issue.fromJson(obj)).toList

println(issues.size + " issues fixed!")
for(Issue(n, title) <- issues.sortBy(_.number))
  println(" * " + title + " ([#" + n + "]" +
          "(https://github.com/NetLogo/NetLogo/issues/" + n + "))")

Http.shutdown()
