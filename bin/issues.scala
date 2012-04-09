#!/usr/bin/env scalas
!#

// This generates Markdown suitable for pasting into
// https://github.com/NetLogo/NetLogo/wiki/Release-notes

// Getting this script to work on your own machine might be difficult since it seems that sbt script
// mode isn't currently being maintained.  If you get an error like "bad section: organization", try
// hand-editing ~/.conscript/harrah/xsbt/scalas and just removing the whole organization section.
// I'd like to find a way to get this working seamlessly for everyone without having to struggle
// with installation issues, but I'm not sure if/when I'll get to it. - ST 4/9/12

// I'd like to be using dispatch 0.8.8 which is the latest at present, but binary dispatch-lift-json
// artifacts don't seem to be published past 0.8.5.  I think that's because the dispatch manual
// recommends having dispatch-lift-json as a source dependency, not a binary dependency, but I
// don't think we have that option since we're using sbt script mode. - ST 4/8/12

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
