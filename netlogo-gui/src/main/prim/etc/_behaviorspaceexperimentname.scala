// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter }

class _behaviorspaceexperimentname extends Reporter {

  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    workspace.behaviorSpaceExperimentName
}
