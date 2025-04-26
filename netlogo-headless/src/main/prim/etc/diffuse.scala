// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.PatchException
import org.nlogo.core.{ Nobody, Reference }
import org.nlogo.{ api, core, nvm }

class _diffuse extends DiffuseCommand {
  override def diffuse(amount: Double) =
    world.diffuse(amount, reference.vn)
}
class _diffuse4 extends DiffuseCommand {
  override def diffuse(amount: Double) =
    world.diffuse4(amount, reference.vn)
}

abstract class DiffuseCommand extends nvm.Command with nvm.Referencer {
  switches = true

  override def referenceIndex: Int = 0

  override def applyReference(ref: Reference): nvm.Command = {
    reference = ref
    this
  }

  protected var reference: Reference = null

  override def toString =
    super.toString +
      (if (reference != null && world != null)
         ":" + world.patchesOwnNameAt(reference.vn)
       else
         "")

  def diffuse(amount: Double): Unit // abstract

  override def perform(context: nvm.Context): Unit = {
    val amount = argEvalDoubleValue(context, 0)
    if (amount < 0.0 || amount > 1.0)
      throw new nvm.RuntimePrimitiveException(
        context, this, core.I18N.errors.getN(
          "org.nlogo.prim.$common.paramOutOfBounds", Double.box(amount)))
    try diffuse(amount)
    catch {
      case ex: api.AgentException =>
        throw new nvm.RuntimePrimitiveException(context, this, ex.getMessage)
      case ex: PatchException =>
        val value: AnyRef = ex.patch.getPatchVariable(reference.vn)
        val bad =
          if (value == Nobody)
            "NOBODY"
          else
            "the " + api.TypeNames.name(value) + " " + api.Dump.logoObject(value)
        throw new nvm.RuntimePrimitiveException(
          context, this,
          s"${ex.patch} should contain a number in the ${world.patchesOwnNameAt(reference.vn)} " +
            s" variable, but contains $bad instead"
        )
    }
    context.ip = next
  }

}
