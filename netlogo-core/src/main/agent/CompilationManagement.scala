// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.util.{ HashMap => JHashMap, Map => JMap }

import org.nlogo.{ core, api },
  core.{ AgentKind, Breed, Dialect, Program },
  api.CompilerServices

// This trait supports the current (mid-2017) model of interaction between the World and
// compiler. I think the current model is stateful and gross. That this is a trait
// should indicate that keeping track of compilation is not an intrinsic responsibility
// of "World". A better solution would be to simply generate a new World after each compile,
// although that seems somewhat unrealistic. - RG 6/14/17
trait CompilationManagement extends CoreWorld { this: AgentManagement =>
  protected def defaultDialect: Dialect

  // These are used to cache old values while recompiling...
  private var _oldProgram: Program = null
  private var _compiler: CompilerServices = null
  private var _program: Program = newProgram


  def program: Program = _program
  def oldProgram: Program = _oldProgram

  def program_=(p: Program): Unit = {
    program(p)
  }

  def program(program: Program): Unit = {
    if (program == null) {
      throw new IllegalArgumentException("World.program cannot be set to null")
    }
    _program = program

    createBreeds(_program.breeds, breeds)
    createBreeds(_program.linkBreeds, linkBreeds)
  }

  def newProgram: Program = Program.fromDialect(defaultDialect)

  def newProgram(interfaceGlobals: scala.collection.Seq[String]): Program =
    newProgram.copy(interfaceGlobals = interfaceGlobals)

  def rememberOldProgram(): Unit = {
    _oldProgram = _program
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
    Realloc.realloc(this, _oldProgram, program)
    setUpShapes(false)
    buildBreedCaches()
    _oldProgram = null
  }


  protected def recreateAllBreeds(): Unit = {
    breeds.clear()
    createBreeds(program.breeds, breeds)
    linkBreeds.clear()
    createBreeds(program.linkBreeds, linkBreeds)
  }

  private def createBreeds(
      programBreeds: scala.collection.Map[String, Breed],
      worldBreeds: JMap[String, TreeAgentSet]): Unit = {

    programBreeds
      .filterNot {
        case (name: String, _) => worldBreeds.containsKey(name.toUpperCase)
      }
      .foreach {
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

    def addBreedCache(breeds: Map[String, Breed], offset: Int): Unit = {
      for {
        (_, b)       <- breeds
        (varName, i) <- b.owns.zipWithIndex
        key = b.name + "~" + varName
      } { breedsOwnCache.put(key, new Integer(offset + i)) }
    }

    addBreedCache(program.breeds,     program.turtlesOwn.size)
    addBreedCache(program.linkBreeds, program.linksOwn.size)
  }
}
