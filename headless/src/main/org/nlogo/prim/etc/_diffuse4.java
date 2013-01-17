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

public final strictfp class _diffuse4
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.ReferenceType(), Syntax.NumberType()},
            "O---", true);
  }

  @Override
  public String toString() {
    if (world != null && reference != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(reference.vn());
    } else {
      return super.toString() + ":" + reference.vn();
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
      world.diffuse4(diffuseparam, reference.vn());
    } catch (AgentException e) {
      throw new EngineException(context, this, e.getMessage());
    } catch (PatchException e) {
      Object value = e.patch().getPatchVariable(reference.vn());
      throw new EngineException
          (context, this, e.patch() + " should contain a number in the " +
              world.patchesOwnNameAt(reference.vn()) +
              " variable, but contains " +
              (value == org.nlogo.api.Nobody$.MODULE$
                  ? "NOBODY"
                  : "the " + TypeNames.name(value) + " " + Dump.logoObject(value)) +
              " instead");
    }
    context.ip = next;
  }
}
