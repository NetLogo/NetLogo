// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.PatchException
import org.nlogo.core.Nobody
import org.nlogo.{ api, core, nvm }

class _diffuse extends DiffuseCommand {
  override def diffuse(amount: Double) =
    world.diffuse(amount, reference.vn)
}
class _diffuse4 extends DiffuseCommand {
  override def diffuse(amount: Double) =
    world.diffuse4(amount, reference.vn)
}

abstract class DiffuseCommand extends nvm.Command {

  switches = true

  override def toString =
    super.toString +
      (if (reference != null && world != null)
         ":" + world.patchesOwnNameAt(reference.vn)
       else
         "")

  def diffuse(amount: Double) // abstract

  override def perform(context: nvm.Context) {
    val amount = argEvalDoubleValue(context, 0)
    if (amount < 0.0 || amount > 1.0)
      throw new nvm.EngineException(
        context, this, core.I18N.errors.getN(
          "org.nlogo.prim.$common.paramOutOfBounds", Double.box(amount)))
    try diffuse(amount)
    catch {
      case ex: api.AgentException =>
        throw new nvm.EngineException(context, this, ex.getMessage)
      case ex: PatchException =>
        val value: AnyRef = ex.patch.getPatchVariable(reference.vn)
        val bad =
          if (value == Nobody)
            "NOBODY"
          else
            "the " + api.TypeNames.name(value) + " " + api.Dump.logoObject(value)
        throw new nvm.EngineException(
          context, this,
          s"${ex.patch} should contain a number in the ${world.patchesOwnNameAt(reference.vn)} " +
            s" variable, but contains $bad instead"
        )
    }
    context.ip = next
  }

}
