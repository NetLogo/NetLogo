// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.ThreeDVersion
import org.nlogo.nvm.{ Context, Reporter }

class _netlogoversion extends Reporter {

  override def report(context: Context) =
    ThreeDVersion.versionNumberOnly
}
