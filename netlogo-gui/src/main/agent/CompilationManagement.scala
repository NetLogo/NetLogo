// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, CompilerServices, Version }

import org.nlogo.core.{ AgentKind, Breed, Program }

import java.util.{ ArrayList => JArrayList, Arrays,
  HashMap => JHashMap, Iterator => JIterator, List => JList,
  Map => JMap }

// TODO: I'm creating this trait to support current models of interaction between the compiler
// and the World. This does not mean I think the current model is actually good in any
// way. I think it's stateful and gross. The name "CompilationManagement" should hopefully
// remind the reader that this is not an intrinsic responsibility of "World".
// My preffered solution would be to simply generate a new World after each compile,
// although I'm not sure that's realistic. - RG 3/24/17
trait CompilationManagement extends CoreWorld { this: AgentManagement =>
  // These are used to cache old values while recompiling...
  private var _oldProgram: Program = null
  private var _compiler: CompilerServices = null
  private var _program: Program = newProgram

  private var oldBreeds: JMap[String, AgentSet] = new JHashMap[String, AgentSet]()
  private var oldLinkBreeds: JMap[String, AgentSet] = new JHashMap[String, AgentSet]()

  def program: Program = _program
  def oldProgram: Program = _oldProgram

  def program(program: Program): Unit = {
    if (program == null) {
      throw new IllegalArgumentException("World.program cannot be set to null")
    }
    _program = program

    breeds.clear()
    linkBreeds.clear()
    createBreeds(_program.breeds, breeds)
    createBreeds(_program.linkBreeds, linkBreeds)
  }

  def newProgram: Program = {
    val dialect =
      if (Version.is3D) org.nlogo.api.NetLogoThreeDDialect
      else              org.nlogo.api.NetLogoLegacyDialect

    Program.fromDialect(dialect)
  }

  def newProgram(interfaceGlobals: scala.collection.Seq[String]): Program =
    newProgram.copy(interfaceGlobals = interfaceGlobals)

  def rememberOldProgram(): Unit = {
    _oldProgram = _program
    oldBreeds = new JHashMap[String, AgentSet](breeds)
    oldLinkBreeds = new JHashMap[String, AgentSet](linkBreeds)
  }

  /// accessing observer variables by name;

  def compiler_=(compiler: CompilerServices): Unit = {
    _compiler = compiler
  }

  def compiler: CompilerServices = _compiler

  // this exists to support recompiling a model without causing
  // agent state information to be lost.  it is called after a
  // successful recompilation.
  def realloc(): Unit = {
    // copy the breed agentsets from the old Program object from
    // the previous compile to the new Program object that was
    // created when we recompiled.  any new breeds that were created,
    // we create new agentsets for.  (if this is a first compile, all
    // the breeds will be created.)  any breeds that no longer exist
    // are dropped.
    if (_program.breeds.nonEmpty) {
      _program.breeds.keys.foreach { breedName =>
        val upcaseName = breedName.toUpperCase
        val breedSet =
          Option(oldBreeds.get(upcaseName)).getOrElse(new TreeAgentSet(AgentKind.Turtle, upcaseName))
        breeds.put(upcaseName, breedSet)
      }
    } else {
      breeds.clear()
    }

    if (_program.linkBreeds.nonEmpty) {
      _program.linkBreeds.foreach {
        case (_, linkBreed) =>
          val breedName = linkBreed.name.toUpperCase
          val directed = linkBreed.isDirected
          val breedSet = Option(oldLinkBreeds.get(breedName)).getOrElse(new TreeAgentSet(AgentKind.Link, breedName))
          breedSet.setDirected(directed)
          linkBreeds.put(breedName, breedSet)
      }
    } else {
      linkBreeds.clear()
    }

    val doomedAgents: JList[Agent] = new JArrayList[Agent]()

    val compiling = _oldProgram != null

    // call Agent.realloc() on all the turtles
    try {
      if (turtles != null) {
        val iter = turtles.iterator
        while (iter.hasNext) {
          Option(iter.next().realloc(oldProgram, program)).foreach(doomedAgents.add)
        }
        val doomedIter = doomedAgents.iterator
        while (doomedIter.hasNext) {
          doomedIter.next().asInstanceOf[Turtle].die()
        }
        doomedAgents.clear()
      }
    } catch {
      case ex: AgentException => throw new IllegalStateException(ex)
    }
    // call Agent.realloc() on all links
    try {
      if (links != null) {
        val iter = links.iterator
        while (iter.hasNext) {
          Option(iter.next().realloc(oldProgram, program)).foreach(doomedAgents.add)
        }
        val doomedIter = doomedAgents.iterator
        while (doomedIter.hasNext) {
          doomedIter.next().asInstanceOf[Link].die()
        }
        doomedAgents.clear()
      }
    } catch {
      case ex: AgentException => throw new IllegalStateException(ex)
    }
    // call Agent.realloc() on all the patches
    try {
      // Note: we only need to realloc() if the patch variables have changed.
      //  ~Forrest ( 5/2/2007)
      if (_patches != null && ((! compiling) || _program.patchesOwn != _oldProgram.patchesOwn)) {
        val iter = _patches.iterator
        while (iter.hasNext) {
          iter.next().realloc(oldProgram, program)
        }
      }
    } catch {
      case ex: AgentException => throw new IllegalStateException(ex)
    }
    // call Agent.realloc() on the observer
    observer.realloc(oldProgram, program)
    // and finally...
    setUpShapes(false)
    buildBreedCaches()
    _oldProgram = null
  }

  //TODO: We can remove these if we pass _oldProgram to realloc
  def oldTurtlesOwnIndexOf(name: String): Int = _oldProgram.turtlesOwn.indexOf(name)
  def oldLinksOwnIndexOf(name: String): Int = _oldProgram.linksOwn.indexOf(name)

  protected def recreateAllBreeds(): Unit = {
    breeds.clear()
    createBreeds(program.breeds, breeds)

    linkBreeds.clear()
    createBreeds(program.linkBreeds, linkBreeds)
  }

  private def createBreeds(
      programBreeds: scala.collection.Map[String, Breed],
      worldBreeds: JMap[String, AgentSet]): Unit = {

    programBreeds.foreach {
      case (name: String, breed: Breed) =>
        val agentKind = if (breed.isLinkBreed) AgentKind.Link else AgentKind.Turtle
        val agentset = new TreeAgentSet(agentKind, breed.name)
        if (breed.isLinkBreed) {
          agentset.setDirected(breed.isDirected)
        }
        worldBreeds.put(name.toUpperCase, agentset)
    }
  }

  protected def buildBreedCaches(): Unit = {
    breedsOwnCache = new JHashMap[String, Integer](16, 0.5f);

    val breedIter = breeds.values.iterator
    while (breedIter.hasNext) {
      val breed = breedIter.next().asInstanceOf[AgentSet]
      val offset = program.turtlesOwn.size
      for {
        b            <- program.breeds.get(breed.printName)
        (varName, i) <- b.owns.zipWithIndex
      } {
        val key = breed.printName + "~" + varName
        breedsOwnCache.put(key, new Integer(offset + i))
      }
    }

    val linkBreedIter = linkBreeds.values.iterator
    while (linkBreedIter.hasNext) {
      val linkBreed = linkBreedIter.next().asInstanceOf[AgentSet]
      val offset = program.linksOwn.size
      for {
        b            <- program.linkBreeds.get(linkBreed.printName)
        (varName, i) <- b.owns.zipWithIndex
      } {
        val key = linkBreed.printName + "~" + varName
        breedsOwnCache.put(key, new Integer(offset + i))
      }
    }
  }
}
