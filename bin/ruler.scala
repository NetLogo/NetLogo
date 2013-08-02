#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon -Dfile.encoding=UTF-8 "$0" "$@"
!#

/// count lines of code in NetLogo source tree

import sys.process.Process

val format = "%25s: %6d unique, %6d total  (%3d%% Scala)\n"
var tj, tuj, ts, tus = 0 // totals. u = unique, j = Java, s = Scala

def percent(n: Int, d: Int) =
  math.ceil(100 * n / d).toInt
// for extensions we don't want to break it out into packages, we just
// want to "flatten" all the packages into one entry
def names(dir: String, extension: String, flatten: Boolean) =
  Process(Seq("find", dir, "-name", "*." + extension) ++
          (if (flatten) Seq() else Seq("-maxdepth", "1")))
    .lines
def lines(dir: String, extension: String, flatten: Boolean): List[String] =
  names(dir, extension, flatten)
    .flatMap(io.Source.fromFile(_).getLines)
    .toList
def dirs(root: String) =
  Process(Seq("find", root, "-type", "d"))
    .lines
    .filterNot(_.containsSlice("/.git/"))
    .filterNot(_.containsSlice("/build/"))
    .filterNot(_.matches("extensions/.*/src/.*/.*"))

def firstNumber(s: String) =
  s.split(' ').find(_.matches("\\d+")).get.toInt

def sortAndPrint(root: String) {
  println(root + ":")
  var subtj, subtuj, subts, subtus = 0
  val entries =
    for{dir <- dirs(root)
        flatten = dir.matches("extensions/.*/src/.*")
        j = lines(dir, "java", flatten)
        uj = j.distinct
        s = lines(dir, "scala", flatten)
        us = s.distinct
        if j.nonEmpty || s.nonEmpty}
    yield {
      subtj += j.size; subtuj += uj.size; subts += s.size; subtus += us.size
      format.format(dir.replaceAll(root + "/org/nlogo/", "")
                       .replaceAll(".src.org", "")
                       .replaceAll(root + "/", "")
                       .replaceFirst(".src$", "")
                       .replaceAll("/", "."),
                    uj.size + us.size, j.size + s.size,
                    percent(us.size, uj.size + us.size))
    }
  entries.toList.sortBy(firstNumber).reverse.foreach(print)
  println(format.format("SUBTOTAL",
                        subtuj + subtus, subtj + subts,
                        percent(subtus, subtuj + subtus)))
  tj += subtj; tuj += subtuj; ts += subts; tus += subtus
  println()
}

sortAndPrint("src/main")
sortAndPrint("src/test")
sortAndPrint("extensions")
sortAndPrint("project")
sortAndPrint("bin")

println(format.format("TOTAL",
                      tuj + tus, tj + ts,
                      percent(tus, tuj + tus)))

// Local Variables:
// mode: scala
// End:
