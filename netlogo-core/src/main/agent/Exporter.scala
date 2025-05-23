// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.io.PrintWriter
import java.util.{ ArrayList, Collections }
import java.lang.{ Long => JLong }
import org.nlogo.{ core, api },
  api.Dump,
    Dump.csv,
  core.Nobody
import scala.jdk.CollectionConverters.{ CollectionHasAsScala, SeqHasAsJava }

// I converted this from Java without (for now, at least) making any effort to clean it up and make
// it more Scalatastic. - ST 4/12/11

private[agent] class Exporter(world: World, writer: PrintWriter) {

  import writer.{ print, println }

  def exportWorld(full: Boolean): Unit = {
    exportRandomState()
    exportGlobals()
    if(full) {
      exportTurtles()
      exportPatches()
      exportLinks()
    }
  }

  private def exportLinks(): Unit = {
    println(csv.encode("LINKS"))
    val allLinkVars = new ArrayList[String]
    for(v <- world.program.linksOwn)
      allLinkVars.add(v)
    val linkVarSize = world.program.linksOwn.size
    // this next hashtable is keyed by the breed variable names and holds the index of where that
    // var is positioned
    val breedVarIndices = collection.mutable.Map[String, Int]()
    for{current <- world.program.linkBreeds.values
        breedVarName <- current.owns}
      if (!breedVarIndices.contains(breedVarName)) {
        allLinkVars.add(breedVarName)
        breedVarIndices(breedVarName) = allLinkVars.size - 1
      }
    println(csv.variableNameRow(allLinkVars))
    // when we get the list it's sorted and I think it's cool to export in who number order rather
    // than what ever the HashMap deems to return, in fact essential in order to get consistent
    // checksums.
    val it = world.links.iterator
    while(it.hasNext) {
      val link = it.next().asInstanceOf[Link]
      val breed = link.getLinkVariable(Link.VAR_BREED).asInstanceOf[AgentSet]
      val key = breed.printName
      var breedOwns: Seq[String] = null
      var thisBreedVarIndices: Array[Int] = null
      var sortedBreedOwns: Array[String] = null
      if(key != "LINKS") {
        breedOwns = world.program.linkBreeds(key).owns
        thisBreedVarIndices = Array.fill(breedOwns.size)(0)
        sortedBreedOwns = Array.fill(breedOwns.size)(null: String)
        for(j <- 0 until breedOwns.size) {
          sortedBreedOwns(j) = breedOwns(j)
          thisBreedVarIndices(j) = breedVarIndices(breedOwns.apply(j))
        }
        sortIndicesAndVars(sortedBreedOwns, thisBreedVarIndices)
      }
      else
        thisBreedVarIndices = Array()
      var index = 0
      for(j <- 0 until allLinkVars.size) {
        if (j > 0)
          print(",")
        if (j < linkVarSize)
          print(csv.data(link.getLinkVariable(j)))
        else if (index < thisBreedVarIndices.length && j == thisBreedVarIndices(index)) {
          print(csv.data(link.getLinkVariable(
            world.linkBreedsOwnIndexOf(breed, sortedBreedOwns(index)))))
          index += 1
        }
      }
      println()
    }
    println()
  }

  private def exportRandomState(): Unit = {
    println(csv.encode("RANDOM STATE"))
    println(csv.encode(world.mainRNG.save()))
    println()
  }

  protected def exportGlobals(): Unit = {
    println(csv.encode("GLOBALS"))
    print(csv.encode("min-pxcor") + ","
          + csv.encode("max-pxcor") + ","
          + csv.encode("min-pycor") + ","
          + csv.encode("max-pycor") + ","
          + csv.encode("perspective") + ","
          + csv.encode("subject") + ","
          + csv.encode("nextIndex") + ","
          + csv.encode("directed-links") + ","
          + csv.encode("ticks"))
    val globals = world.program.globals
    val sortedGlobals = new ArrayList[String](globals.size)
    val globalVarIndices = collection.mutable.Map[String, Int]()
    for((g, i) <- globals.zipWithIndex) {
      globalVarIndices(g) = i
      sortedGlobals.add(g)
    }
    // we want to make sure to export the globals in alphabetical order so that the world files are
    // exactly the same everytime which is important for checksums in particular.  ev 6/15/05
    Collections.sort(sortedGlobals)
    val subject: Any = Option(world.observer.targetAgent).getOrElse(Nobody)
    if (! sortedGlobals.isEmpty) {
      print("," + csv.variableNameRow(sortedGlobals))
    }
    println()
    print(csv.encode(world.minPxcor.toString) + ","
          + csv.encode(world.maxPxcor.toString) + ","
          + csv.encode(world.minPycor.toString) + ","
          + csv.encode(world.maxPycor.toString) + ","
          + csv.encode(world.observer.perspective.`export`.toString) + ","
          + csv.data(subject) + ","
          + csv.encode(JLong.toString(world.nextTurtleIndex)) + ","
          + csv.data(if (world.links.isDirected) "DIRECTED" else
                     if (world.links.isUndirected) "UNDIRECTED" else "NEITHER") + ","
          + csv.encode(Dump.number(world.tickCounter.ticks)))
    for(g <- sortedGlobals.asScala) {
      print(",")
      print(csv.data(world.observer.getVariable(
        globalVarIndices(g))))
    }
    println()
    println()
  }

  protected def exportTurtles(): Unit = {
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
    // when we get the array list it's sorted and I think it's cool to export in who number order
    // rather than what ever the HashMap deems to return, in fact essential for consistent
    // checksums.
    val it = world.turtles.iterator
    while(it.hasNext) {
      val turtle = it.next().asInstanceOf[Turtle]
      print(csv.data(turtle.getTurtleVariable(Turtle.VAR_WHO)))
      val breed = turtle.getTurtleVariable(Turtle.VAR_BREED).asInstanceOf[AgentSet]
      val key = breed.printName
      var breedOwns: Seq[String] = null
      var thisBreedVarIndices: Array[Int] = null
      var sortedBreedOwns: Array[String] = null
      if (key != "TURTLES") {
        breedOwns = world.program.breeds(key).owns
        thisBreedVarIndices = Array.fill(breedOwns.size)(0)
        sortedBreedOwns = Array.fill(breedOwns.size)(null: String)
        for(j <- 0 until breedOwns.size) {
          sortedBreedOwns(j) = breedOwns(j)
          thisBreedVarIndices(j) = breedVarIndices(breedOwns(j))
        }
        sortIndicesAndVars(sortedBreedOwns, thisBreedVarIndices)
      }
      else
        thisBreedVarIndices = Array()
      var index = 0
      for(j <- 1 until allTurtleVars.size) {
        print(",")
        if (j < turtlesVarSize)
          print(csv.data(turtle.getTurtleVariable(j)))
        else if(index < thisBreedVarIndices.length && j == thisBreedVarIndices(index)) {
          print(csv.data(turtle.getTurtleVariable(world.breedsOwnIndexOf(
            breed, sortedBreedOwns(index)))))
          index += 1
        }
      }
      println()
    }
    println()
  }

  private def exportPatches(): Unit = {
    println(csv.encode("PATCHES"))
    val vars = world.program.patchesOwn
    println(csv.variableNameRow(vars))
    val it = world.patches.iterator
    while(it.hasNext) {
      val patch = it.next().asInstanceOf[Patch]
      for(j <- 0 until vars.size) {
        if(j > 0)
          print(",")
        print(csv.data(patch.getPatchVariable(j)))
      }
      println()
    }
    println()
  }

  protected def sortIndicesAndVars(vars: Array[String], indices: Array[Int]): Unit = {
    val (sortedVars, sortedIndices) = (vars zip indices).sortBy(_._2).unzip
    sortedVars.copyToArray(vars)
    sortedIndices.copyToArray(indices)
  }

}
