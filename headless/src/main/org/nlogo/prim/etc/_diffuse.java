// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.PatchException;
import org.nlogo.api.AgentException;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.Syntax;
import org.nlogo.api.TypeNames;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _diffuse
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.ReferenceType(), Syntax.NumberType()},
            "O---", true);
  }

  @Override
  public String toString() {
    if (reference != null && world != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(reference.vn());
    } else {
      return super.toString();
    }
  }

  @Override
  public void perform(final Context context) {
    double diffuseparam = argEvalDoubleValue(context, 0);
    if (diffuseparam < 0.0 || diffuseparam > 1.0) {
      throw new EngineException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.$common.paramOutOfBounds", diffuseparam));
    }
    try {
      world.diffuse(diffuseparam, reference.vn());
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    } catch (PatchException ex) {
      Object value = ex.patch().getPatchVariable(reference.vn());
      throw new EngineException
          (context, this,
              ex.patch() + " should contain a number in the " + world.patchesOwnNameAt(reference.vn()) +
                  " variable, but contains " +
                  (value == org.nlogo.api.Nobody$.MODULE$
                      ? "NOBODY"
                      : "the " + TypeNames.name(value) + " " + Dump.logoObject(value)) +
                  " instead");
    }
    context.ip = next;
  }
}
