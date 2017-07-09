// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

// only display the mainRNG state. the auxiliary shouldn't matter since it doesn't affect the
// outcome of the model.
import org.nlogo.nvm.{ Context, Reporter }

class _randomstate extends Reporter {

  override def report(context: Context) =
    world.mainRNG.save
}
