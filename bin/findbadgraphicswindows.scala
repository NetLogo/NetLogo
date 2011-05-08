#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

// finds models whose graphics windows' saved sizes don't match the size you should get if you
// compute from the saved patch size and screen-edge-x/y

import Scripting.{ shell, read }

for{path <- shell("find models -name \\*.nlogo")
    lines = read(path).toSeq}
{
  val version = lines.find(_.matches("""NetLogo [0-9]\..*""")).get.drop("NetLogo ".size)
  val graphics = lines.dropWhile(_ != "GRAPHICS-WINDOW").takeWhile(_ != "")
  val (x1, y1, x2, y2) = (graphics(1).toInt, graphics(2).toInt, graphics(3).toInt, graphics(4).toInt)
  val patchSize = graphics(7).toDouble
  val maxx = if(graphics.size > 18) graphics(18).toInt else graphics(5).toInt
  val maxy = if(graphics.size > 20) graphics(20).toInt else graphics(6).toInt
  val minx = if(graphics.size > 17) graphics(17).toInt else -maxx
  val miny = if(graphics.size > 19) graphics(19).toInt else -maxy
  val (worldWidth, worldHeight) = (maxx - minx + 1, maxy - miny + 1)
  val (extraWidth, extraHeight) =
    // take control strip and gray border into account
    if(List("1.3", "2.", "3.", "4.", "5.").exists(version.startsWith(_))) (10, 31) else (0, 0)
  val computedWidth  = extraWidth  + patchSize * worldWidth
  val computedHeight = extraHeight + patchSize * worldHeight
  if(maxx < 0 || minx > 0 || maxy < 0 || miny > 0)
    println(path + " (" + version + "): bad world dimensions: " + (maxx, minx, maxy, miny))
  if(computedWidth != x2 - x1)
    println(path + " (" + version + "): computed width " + computedWidth + ", actual width " + (x2 - x1))
  if(computedHeight != y2 - y1)
    println(path + " (" + version + "): computed height " + computedHeight + ", actual height " + (y2 - y1))
}
