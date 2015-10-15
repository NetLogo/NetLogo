// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import
  java.util.{ List => JList }

import
  scala.annotation.strictfp

import
  org.nlogo.{ agent, core, nvm },
    agent.{ Agent, AgentSet },
    core.{ AgentKind, I18N },
    nvm.{ Context, EngineException, Instruction, Reporter }

// These classes are purposely written to delegate, rather than inherit, for the sake of performance --JAB (6/20/14)

@strictfp
final class _inradius extends Reporter {

  private lazy val reportFunc = InRadiusOps.report(world.inRadiusOrCone.inRadiusSimple, this) _

  override def report(context: Context) = reportFunc(context)

}

@strictfp
final class _inradiusboundingbox extends Reporter {

  private lazy val reportFunc = InRadiusOps.report(world.inRadiusOrCone.inRadius, this) _

  override def report(context: Context) = reportFunc(context)

}

private object InRadiusOps {

  type InRadiusFinder = (Agent, AgentSet, Double, Boolean) => JList[Agent]

  def report(findAgentsInRadius: InRadiusFinder, instr: Instruction)(context: Context): AnyRef = {
    val sourceSet = instr.argEvalAgentSet(context, 0)
    val radius    = instr.argEvalDoubleValue(context, 1)

    if (sourceSet.kind == AgentKind.Link)
      throw new EngineException(context, instr, I18N.errorsJ.get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"))

    if (radius < 0)
      throw new EngineException(context, instr, I18N.errorsJ.getN("org.nlogo.prim.etc.$common.noNegativeRadius", instr.displayName))

    val result = findAgentsInRadius(context.agent, sourceSet, radius, true)
    AgentSet.fromArray(sourceSet.kind, result.toArray(new Array[Agent](result.size)))
  }

}
