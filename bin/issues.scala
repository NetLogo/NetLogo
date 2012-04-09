#!/usr/bin/env scalas
!#

// This generates Markdown suitable for pasting into
// https://github.com/NetLogo/NetLogo/wiki/Release-notes

// running this is tricky because the sbt script mode stuff isn't currently maintained in sync with
// conscript so you have fiddle with it or it doesn't work. instructions:
// - install conscript if you don't have it already:
//   curl https://raw.github.com/n8han/conscript/master/setup.sh | sh
// - install sbt (and the scalas script) through conscript:
//   cs mharrah/xsbt --branch v0.11.2
// - edit ~/.conscript/harrah/xsbt/scalas/launchconfig
//    and ~/.conscript/harrah/xsbt/sbt/launchconfig
//   and remove the entire [organization] section
//   from both files

/***
scalaVersion := "2.9.1"

onLoadMessage := ""

libraryDependencies ~= { seq =>
    val vers = "0.8.5"
    seq ++ Seq("net.databinder" %% "dispatch-core" % vers,
               "net.databinder" %% "dispatch-http" % vers,
               "net.databinder" %% "dispatch-lift-json" % vers)
}
*/

import dispatch._
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._

object Issue {
  def fromJson(j: JValue): Issue = {
    val JInt(n) = j \ "number"
    val JString(title) = j \ "title"
    Issue(n.toInt, title)
  }
}
case class Issue(number: Int, title: String)

val base = "https://api.github.com/repos/NetLogo/NetLogo/issues"
val u = url(base) <<? Map("milestone" -> "10",
                          "state" -> "closed",
                          "per_page" -> "1000")
val http = new Http with NoLogging
val issues: List[Issue] =
  http(u ># { json =>
    for {
      JArray(objs) <- json
      obj <- objs
    } yield Issue.fromJson(obj)})

println(issues.size + " issues fixed!")
for(Issue(n, title) <- issues.sortBy(_.number))
  println(" * " + title + " ([#" + n + "]" +
          "(https://github.com/NetLogo/NetLogo/issues/" + n + "))")
