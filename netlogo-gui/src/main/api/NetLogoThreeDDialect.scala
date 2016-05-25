// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ AgentVariableSet, Dialect, Instantiator, Instruction }

import scala.collection.immutable.ListMap

object NetLogoThreeDDialect extends Dialect {
  val is3D = true;
  val agentVariables = new AgentVariableSet {
    val implicitObserverVariableTypeMap: ListMap[String, Int] = ListMap()
    val implicitTurtleVariableTypeMap: ListMap[String, Int]   = AgentVariables.implicitTurtleVariableTypeMap(true)
    val implicitPatchVariableTypeMap: ListMap[String, Int]    = AgentVariables.implicitPatchVariableTypeMap(true)
    val implicitLinkVariableTypeMap: ListMap[String, Int]     = AgentVariables.implicitLinkVariableTypeMap
  }
  val tokenMapper = ThreeDTokenMapper
}

object ThreeDTokenMapper extends DelegatingMapper {
  val defaultMapper = NetLogoLegacyDialectTokenMapper
  val path = "/system/tokens-threed.txt"
  val pkgName = "org.nlogo.compiler.prim"
  override def overrideBreedInstruction(primName: String, breedName: String): Option[Instruction] =
    primName match {
      case "etc._breedat" =>
        try {
          Some(Instantiator.newInstance[Instruction](
            Class.forName("org.nlogo.compiler.prim.threed._breedat"), breedName))
        } catch {
          case e: ClassNotFoundException => None
        }
      case _ => None
    }
}
