// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// Importer is too big a class to convert to Scala all at once, so
// we'll convert it at method at a time, as needed, by relocating
// methods from ImporterJ to here. - ST 7/11/12

import org.nlogo.api.{ AgentKind, AgentVariables, Breed, ImporterUser }
import collection.immutable.ListMap
import collection.JavaConverters._

class Importer(_errorHandler: ImporterJ.ErrorHandler,
               _world: World,
               _importerUser: ImporterUser,
               _stringReader: ImporterJ.StringReader)
extends ImporterJ(_errorHandler, _world, _importerUser, _stringReader) {

  override def getImplicitVariables(kind: AgentKind): Array[String] =
    kind match {
      case AgentKind.Observer =>
        AgentVariables.getImplicitObserverVariables.toArray
      case AgentKind.Turtle =>
        AgentVariables.getImplicitTurtleVariables.toArray
      case AgentKind.Patch =>
        AgentVariables.getImplicitPatchVariables.toArray
      case AgentKind.Link =>
        AgentVariables.getImplicitLinkVariables.toArray
    }

  def getSpecialObserverVariables: Array[String] = {
    import ImporterJ._
    Array(
      MIN_PXCOR_HEADER, MAX_PXCOR_HEADER, MIN_PYCOR_HEADER, MAX_PYCOR_HEADER,
      SCREEN_EDGE_X_HEADER, SCREEN_EDGE_Y_HEADER,
      PERSPECTIVE_HEADER, SUBJECT_HEADER,
      NEXT_INDEX_HEADER, DIRECTED_LINKS_HEADER, TICKS_HEADER)
  }

  def getSpecialTurtleVariables: Array[String] = {
    val vars = AgentVariables.getImplicitTurtleVariables
    Array(vars(Turtle.VAR_WHO), vars(Turtle.VAR_BREED),
          vars(Turtle.VAR_LABEL), vars(Turtle.VAR_SHAPE))
  }

  def getSpecialPatchVariables: Array[String] = {
    val vars = AgentVariables.getImplicitPatchVariables
    Array(vars(Patch.VAR_PXCOR), vars(Patch.VAR_PYCOR),
          vars(Patch.VAR_PLABEL))
  }

  def getSpecialLinkVariables: Array[String] = {
    val vars = AgentVariables.getImplicitLinkVariables
    Array(vars(Link.VAR_BREED), vars(Link.VAR_LABEL),
          vars(Link.VAR_END1), vars(Link.VAR_END2))
  }

  def getAllVars(breeds: ListMap[String, Breed]): java.util.List[String] =
    breeds.values.flatMap(_.owns).toSeq.asJava

}
