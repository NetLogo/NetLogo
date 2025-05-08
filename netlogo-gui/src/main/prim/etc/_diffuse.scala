// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.PatchException
import org.nlogo.api.{ AgentException, Dump, LogoException, TypeNames }
import org.nlogo.core.{ I18N, Nobody, Reference }
import org.nlogo.nvm.{ Command, Context, Referencer, RuntimePrimitiveException }

class _diffuse extends Command with Referencer {
  this.switches = true

  override def referenceIndex: Int = 0

  override def applyReference(ref: Reference): Command = {
    reference = ref
    this
  }

  private var reference: Reference = null

  override def toString: String = {
    if (world != null && reference != null)
      super.toString + ":" + world.patchesOwnNameAt(reference.vn)
    else
      super.toString + ":" + reference.vn
  }


  @throws(classOf[LogoException])
  override def perform(context: Context): Unit = {
    val diffuseparam = argEvalDoubleValue(context, 0)
    if (diffuseparam < 0.0 || diffuseparam > 1.0) {
      throw new RuntimePrimitiveException(context, this, I18N.errors.getN("org.nlogo.prim.$common.paramOutOfBounds", Double.box(diffuseparam)))
    }
    try {
      world.diffuse(diffuseparam, reference.vn)
    } catch {
      case e: AgentException                => throw new RuntimePrimitiveException(context, this, e.getMessage())
      case e: UnsupportedOperationException => throw new RuntimePrimitiveException(context, this, "Diffuse4 is not supported in 3D")
      case e: PatchException                =>
        val value = e.patch.getPatchVariable(reference.vn);
        val fieldName = world.patchesOwnNameAt(reference.vn)
        val valueName = if (value == Nobody) "NOBODY" else s"the ${TypeNames.name(value)} ${Dump.logoObject(value)}"
        val message = s"${e.patch} should contain a number in the ${fieldName} variable, but contains $valueName instead"
        throw new RuntimePrimitiveException(context, this, message)
    }
    context.ip = next;
  }
}
