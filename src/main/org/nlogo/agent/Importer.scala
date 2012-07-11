// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// Importer is too big a class to convert to Scala all at once, so
// we'll convert it at method at a time, as needed, by relocating
// methods from ImporterJ to here. - ST 7/11/12

import org.nlogo.api.{ AgentVariables, ImporterUser }

class Importer(_errorHandler: ImporterJ.ErrorHandler,
               _world: World,
               _importerUser: ImporterUser,
               _stringReader: ImporterJ.StringReader)
extends ImporterJ(_errorHandler, _world, _importerUser, _stringReader) {

  override def getImplicitVariables(agentClass: Class[_ <: Agent]): Array[String] =
    if (agentClass == classOf[Observer])
      AgentVariables.getImplicitObserverVariables.toArray
    else if (agentClass == classOf[Turtle])
      AgentVariables.getImplicitTurtleVariables(world.program.is3D).toArray
    else if (agentClass == classOf[Patch])
      AgentVariables.getImplicitPatchVariables(world.program.is3D).toArray
    else if (agentClass == classOf[Link])
      AgentVariables.getImplicitLinkVariables.toArray
    else
      throw new IllegalArgumentException(agentClass.toString)

}
