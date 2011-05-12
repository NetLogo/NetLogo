#!/bin/sh
exec bin/scala -classpath bin -deprecation -nocompdaemon "$0" "$@" 
!# 
// Local Variables:
// mode: scala
// End:

/// print and histogram the lengths of the Code tabs of all Sample Models
import Scripting.{shell,read}
println("Counting lines containing something besides a bracket or paren and/or a comment.")
println("Of the GasLab suite, only Circular Particles and Gas in a Box are included.")
println
def hasActualCode(line:String):Boolean =
  line.matches(""".*\S.*""") &&               // ignore blank lines
  !line.matches("""\s*[\[\]\(\)]\s*""") &&    // ignore if nothing but a bracket/paren
  !line.matches("""\s*;.*""") &&              // ignore if nothing but a comment
  !line.matches("""\s*[\[\]\(\)]\s*;.*""")    // ignore if nothing but a bracket/paren and a comment
val hash = collection.mutable.HashMap[String,Int]()
for{path <- shell("""find models/Sample\ Models -name \*.nlogo -o -name \*.nlogo3d""")
    name = path.split("/").last.dropRight(6)
    if !path.containsSlice("/System Dynamics/")
    if !path.containsSlice("/GasLab/") || List("GasLab Circular Particles","GasLab Gas in a Box").contains(name)}
  hash += name -> read(path).takeWhile(_ != "@#$#@#$#@").count(hasActualCode)
println(hash.size + " models total")
println
for(bin <- hash.values.max / 25 to 0 by -1) {
  val (min,max) = (bin * 25,bin * 25 + 24)
  val count = hash.values.count((min to max).contains)
  printf("%3d-%3d = %s (%d)\n",min,max,List.fill(count)('*').mkString,count)
}
println
for(name <- hash.keys.toList.sortBy(hash).reverse)
  printf("%4d %s\n",hash(name),name)

