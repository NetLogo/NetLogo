// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import
  org.nlogo.{ agent, core, nvm },
    agent.{ Agent, AgentSet },
    core.{ AgentKind, I18N },
    nvm.{ Context, RuntimePrimitiveException, Reporter }

final class _inradius extends Reporter {

  override def report(context: Context) = {
    val sourceSet = argEvalAgentSet(context, 0)
    val radius    = argEvalDoubleValue(context, 1)

    if (sourceSet.kind == AgentKind.Link)
      throw new RuntimePrimitiveException(context, this, I18N.errorsJ.get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"))

    if (radius < 0)
      throw new RuntimePrimitiveException(context, this, I18N.errorsJ.getN("org.nlogo.prim.etc.$common.noNegativeRadius", displayName))

    val result = world.inRadiusOrCone.inRadius(context.agent, sourceSet, radius, true)
    AgentSet.fromArray(sourceSet.kind, result.toArray(new Array[Agent](result.size)))
  }

}
