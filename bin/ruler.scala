#!/bin/sh
exec scala -classpath bin -deprecation -Dfile.encoding=UTF-8 "$0" "$@"
!#

/// count lines of code in NetLogo source tree

import scala.io.Source
import scala.sys.process.Process

val format = "%25s: %6d unique, %6d total  (%3d%% Scala)\n"
var tj, tuj, ts, tus = 0 // totals. u = unique, j = Java, s = Scala

def percent(n: Int, d: Int) =
  math.ceil(100 * n / d).toInt
// for extensions we don't want to break it out into packages, we just
// want to "flatten" all the packages into one entry
def names(dir: String, extension: String, flatten: Boolean) =
  Process(Seq("find", dir, "-name", "*." + extension) ++
          (if (flatten) Seq() else Seq("-maxdepth", "1")))
    .lazyLines

def lines(dir: String, extension: String, flatten: Boolean): List[String] =
  names(dir, extension, flatten)
    .flatMap(Source.fromFile(_).getLines)
    .toList
def dirs(root: String) =
  Process(Seq("find", root, "-type", "d"))
    .lazyLines
    .filterNot(_.containsSlice("/.git/"))
    .filterNot(_.containsSlice("/build/"))
    .filterNot(_.matches("extensions/.*/src/.*/.*"))

def outputLines(root: String) =
  for{dir <- dirs(root)
      flatten = dir.matches("extensions/.*/src/.*")
      j = lines(dir, "java", flatten)
      uj = j.distinct
      s = lines(dir, "scala", flatten)
      us = s.distinct
      if j.nonEmpty || s.nonEmpty}
  yield {
    tj += j.size; tuj += uj.size; ts += s.size; tus += us.size
    format.format(dir.replaceAll(root + "/org/nlogo/", "")
                     .replaceAll(".src.org", "")
                     .replaceAll(root + "/", "")
                     .replaceFirst(".src$", "")
                     .replaceAll("/", "."),
                  uj.size + us.size, j.size + s.size,
                  percent(us.size, uj.size + us.size))
  }
def firstNumber(s: String) =
  s.dropWhile(!_.isDigit).takeWhile(_.isDigit).mkString.toInt
def sortAndPrint(root: String) = {
  println(root + ":")
  outputLines(root).toList.sortBy(firstNumber).reverse.foreach(print)
  println()
}

sortAndPrint("netlogo-gui/src/main")
sortAndPrint("netlogo-gui/src/test")
sortAndPrint("netlogo-gui/src/tools")
sortAndPrint("netlogo-core/src/main")
sortAndPrint("netlogo-core/src/test")
sortAndPrint("netlogo-headless/src/main")
sortAndPrint("netlogo-headless/src/test")
sortAndPrint("parser-core")
sortAndPrint("parser-js")
sortAndPrint("parser-jvm")
sortAndPrint("extensions")
sortAndPrint("project")
sortAndPrint("bin")
println(format.format("TOTAL",
                      tuj + tus, tj + ts,
                      percent(tus, tuj + tus)))

// Local Variables:
// mode: scala
// End:
