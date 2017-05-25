// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.io.PrintWriter
import java.util.{ ArrayList, Collections, HashMap => JHashMap, List => JList, Map => JMap }
import java.lang.{ Double => JDouble, Integer => JInteger, Long => JLong }
import org.nlogo.api.Dump
import org.nlogo.core.Nobody
import Dump.csv
import collection.JavaConverters._

// This is just a bunch of copy-and-pasted code from the 2D version, with little 3D tweaks embedded
// in it.  It really ought to be redone to eliminate the copy-and-paste. - ST 4/11/11

// I converted this from Java without (for now, at least) making any effort to clean it up and make
// it more Scalatastic. - ST 4/11/11

private[agent] class Exporter3D(world: World3D, writer: PrintWriter) extends Exporter(world, writer) {

  import writer.{ print, println }

  override def exportWorld(full: Boolean) {
    super.exportWorld(full)
    if(full)
      exportDrawing()
  }

  def exportDrawing() {
    println(csv.encode("DRAWING"))
    println(csv.encode("x0")  + ","
            + csv.encode("y0")  + ","
            + csv.encode("z0")  + ","
            + csv.encode("x1")  + ","
            + csv.encode("y1")  + ","
            + csv.encode("z1")  + ","
            + csv.encode("width")  + ","
            + csv.encode("color"))
    val drawing  = world.drawing
    for(line <- drawing.lines.asScala) {
      print(csv.encode(JDouble.toString(line.x0)))
      print("," + csv.encode(JDouble.toString(line.y0)))
      print("," + csv.encode(JDouble.toString(line.z0)))
      print("," + csv.encode(JDouble.toString(line.x1)))
      print("," + csv.encode(JDouble.toString(line.y1)))
      print("," + csv.encode(JDouble.toString(line.z1)))
      print("," + csv.encode(JDouble.toString(line.width)))
      print("," + csv.data(line.color))
      println()
    }
    println(csv.encode("shape")  + ","
            + csv.encode("xcor")  + ","
            + csv.encode("ycor")  + ","
            + csv.encode("zcor")  + ","
            + csv.encode("size")  + ","
            + csv.encode("heading")  + ","
            + csv.encode("pitch")  + ","
            + csv.encode("roll")  + ","
            + csv.encode("color")  + ","
            + csv.encode("lineThickness"))

    for(stamp <- drawing.turtleStamps.asScala) {
      print(csv.encode(stamp.shape))
      print("," + csv.encode(JDouble.toString(stamp.xcor)))
      print("," + csv.encode(JDouble.toString(stamp.ycor)))
      print("," + csv.encode(JDouble.toString(stamp.zcor)))
      print("," + csv.encode(JDouble.toString(stamp.size)))
      print("," + csv.encode(JDouble.toString(stamp.heading)))
      print("," + csv.encode(JDouble.toString(stamp.pitch)))
      print("," + csv.encode(JDouble.toString(stamp.roll)))
      print("," + csv.data(stamp.color))
      print("," + csv.encode(JDouble.toString(stamp.lineThickness)))
      println()
    }
    println(csv.encode("shape")  + ","
            + csv.encode("x1")  + ","
            + csv.encode("y1")  + ","
            + csv.encode("z1")  + ","
            + csv.encode("x2")  + ","
            + csv.encode("y2")  + ","
            + csv.encode("z2")  + ","
            + csv.encode("color")  + ","
            + csv.encode("lineThickness")  + ","
            + csv.encode("directedLink")  + ","
            + csv.encode("destSize") + ","
            + csv.encode("heading")  + ","
            + csv.encode("pitch"))
    for(stamp <- drawing.linkStamps.asScala) {
      print(csv.encode(stamp.shape))
      print("," + csv.encode(JDouble.toString(stamp.x1)))
      print("," + csv.encode(JDouble.toString(stamp.y1)))
      print("," + csv.encode(JDouble.toString(stamp.z1)))
      print("," + csv.encode(JDouble.toString(stamp.x2)))
      print("," + csv.encode(JDouble.toString(stamp.y2)))
      print("," + csv.encode(JDouble.toString(stamp.z2)))
      print("," + csv.data(stamp.color))
      print("," + csv.encode(JDouble.toString(stamp.lineThickness)))
      print("," + csv.encode(java.lang.Boolean.toString(stamp.isDirectedLink)))
      print("," + csv.encode(JDouble.toString(stamp.linkDestinationSize)))
      print("," + csv.encode(JDouble.toString(stamp.heading)))
      print("," + csv.encode(JDouble.toString(stamp.pitch)))
      println()
    }
    println()
  }

  override def exportGlobals() {
    println(csv.encode("GLOBALS"))
    print(csv.encode("min-pxcor") + ","
                  + csv.encode("max-pxcor") + ","
                  + csv.encode("min-pycor") + ","
                  + csv.encode("max-pycor") + ","
                  + csv.encode("min-pzcor") + ","
                  + csv.encode("max-pzcor") + ","
                  + csv.encode("perspective") + ","
                  + csv.encode("subject") + ","
                  + csv.encode("nextIndex") + ","
                  + csv.encode("directed-links") + ","
                  + csv.encode("ticks"))
    val globals = world.program.globals
    val sortedGlobals = new ArrayList[String](globals.size)
    val globalVarIndices = new JHashMap[String, JInteger]
    for((g, i) <- globals.zipWithIndex) {
      globalVarIndices.put(globals(i), Int.box(i))
      sortedGlobals.add(globals(i))
    }
    // we want to make sure to export the globals in alphabetical order so that the world files are
    // exactly the same everytime which is important for checksums in particular.  ev 6/15/05
    Collections.sort(sortedGlobals)
    val subject = Option(world.observer.targetAgent).getOrElse(Nobody)
    print("," + csv.variableNameRow(sortedGlobals))
    println()
    print(csv.encode(Integer.toString(world.minPxcor)) + ","
                  + csv.encode(Integer.toString(world.maxPxcor)) + ","
                  + csv.encode(Integer.toString(world.minPycor)) + ","
                  + csv.encode(Integer.toString(world.maxPycor)) + ","
                  + csv.encode(Integer.toString(world.minPzcor)) + ","
                  + csv.encode(Integer.toString(world.maxPzcor)) + ","
                  + csv.encode(Integer.toString(world.observer.perspective.export)) + ","
                  + csv.data(subject) + ","
                  + csv.encode(JLong.toString(world.nextTurtleIndex)) + ","
                  + csv.data(if(world.links.isDirected) "DIRECTED" else
                                   if(world.links.isUndirected) "UNDIRECTED" else "NEITHER") + ","
                  + csv.encode(Dump.number(world.tickCounter.ticks)))
    for((g, i) <- globals.zipWithIndex) {
      print(",")
      print(csv.data
                   (world.observer.getVariable
                    (globalVarIndices.get(sortedGlobals.get(i)).intValue())
                  ))
    }
    println()
    println()
  }

  override def exportTurtles() {
    println(csv.encode("TURTLES"))
    val allTurtleVars = new ArrayList[String](world.program.turtlesOwn.asJava)
    val turtlesVarSize = world.program.turtlesOwn.size
    // this next hashtable is keyed by the breed variable names and holds the index of where that var is positioned
    val breedVarIndices = collection.mutable.Map[String, Int]()
    for {
      current <- world.program.breeds.values
      breedVarName <- current.owns
    } if (!breedVarIndices.contains(breedVarName)) {
      allTurtleVars.add(breedVarName)
      breedVarIndices(breedVarName) = allTurtleVars.size - 1
    }
    println(csv.variableNameRow(allTurtleVars))
    val it = world.turtles.iterator
    while(it.hasNext) {
      val turtle = it.next.asInstanceOf[Turtle]
      print(csv.data(turtle.getTurtleVariable(Turtle.VAR_WHO)))
      val breed = turtle.getTurtleVariable(Turtle3D.VAR_BREED3D).asInstanceOf[AgentSet]
      val key = breed.printName
      var breedOwns: Seq[String] = null
      var thisBreedVarIndices: Array[Int] = null
      var sortedBreedOwns: Array[String] = null
      if (!key.equals("TURTLES")) {
        breedOwns = world.program.breeds(key).owns
        thisBreedVarIndices = Array.fill(breedOwns.size)(0)
        sortedBreedOwns = Array.fill(breedOwns.size)(null: String)
        for(j <- 0 until breedOwns.size) {
          sortedBreedOwns(j) = breedOwns(j)
          thisBreedVarIndices(j) = breedVarIndices(breedOwns(j))
        }
        sortIndicesAndVars(sortedBreedOwns, thisBreedVarIndices)
      }
      else thisBreedVarIndices = Array[Int](0)
      var index = 0
      for(j <- 1 until allTurtleVars.size()) {
        print(",")
        if (j < turtlesVarSize)
          print(csv.data(turtle.getTurtleVariable(j)))
        else if(index < thisBreedVarIndices.length && j == thisBreedVarIndices(index)) {
          print(csv.data(turtle.getTurtleVariable(
            world.breedsOwnIndexOf(breed, sortedBreedOwns(index)))))
          index += 1
        }
      }
      println()
    }
    println()
  }

}
