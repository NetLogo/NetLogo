#!/bin/sh
exec scala -classpath bin -deprecation -Dfile.encoding=UTF-8 "$0" "$@"
!#

// The purpose of all this is to make sure that every class and interface is declared strictfp, and
// that we never ever use Math, only StrictMath.

import sys.process.Process

def withoutComments(lines: Seq[String]): Seq[String] = {
  def helper(lines: Seq[String]): Seq[String] =
    if(lines.isEmpty)
      lines
    else {
      val (nonComment, rest) = lines.span(!_.containsSlice("/*"))
      nonComment ++ helper(rest.dropWhile(!_.containsSlice("*/")).drop(1))
    }
  helper(lines.filter(!_.matches("""^//.*""")))
}

// first do the strictfp check

val okDeclarations =
  List("strictfp class","public strictfp class","public abstract strictfp","public final strictfp",
       "public strictfp class", "final strictfp class", "strictfp class", "strictfp final class",
       "abstract strictfp class", "strictfp abstract class", "public strictfp final class",
       "public enum","interface", "public interface",
       "class AbstractEditorArea", "class TokenLexer", "class ImportLexer") // let's not bother making JFlex emit "strictfp"
for {
  path <- Process("find netlogo-core/src netlogo-gui/src -name *.java").lazyLines
  if !path.containsSlice("/gl/render/")  // we don't care if OpenGL stuff is strictfp
} {
  val lines = for{line <- withoutComments(io.Source.fromFile(path).getLines.toSeq)
                  if !line.matches("""\s*""")
                  if !line.matches("""package.*""")
                  if !line.matches("""import.*""")}
              yield line
  if(!lines.exists(line => okDeclarations.exists(ok => line.startsWith(ok + " ") ||
                                                       line.endsWith(ok))))
    println("needs strictfp: " + path)
}

// now do the StrictMath check

for{path <- Process("find netlogo-core/src netlogo-gui/src -name *.java").lazyLines
    if !path.containsSlice("/gl/render/")  // we don't care if OpenGL stuff is strictfp
    if !path.containsSlice("Activation.java") // Activation uses Math.max, on "safe" data
    if path != "src/org/nlogo/headless/TestCommands.java"}
  // this isn't the absolutely correct check to be doing, but it seems like a good enough heuristic
  // for now - ST 5/8/03
  if(io.Source.fromFile(path).getLines
     .filter(!_.containsSlice("Mathematica"))
     .filter(!_.containsSlice("DummyMath"))
     .exists(_.matches(""".*[^t]Math.*""")))
    println("needs StrictMath: " + path)

// Local Variables:
// mode: scala
// End:
