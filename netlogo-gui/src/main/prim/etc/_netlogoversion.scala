// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.TwoDVersion
import org.nlogo.nvm.{ Context, Reporter }

// NOTE: this primitive applies to the two-D version of NetLogo
// The ThreeD dialect compiles a different prim (threed._netlogoversion)
// to display the appropriate version in 3D mode. - RG 10/25/17
class _netlogoversion extends Reporter {

  override def report(context: Context) =
    TwoDVersion.versionNumberOnly
}
