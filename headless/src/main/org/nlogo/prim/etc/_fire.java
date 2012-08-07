// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Patch;
import org.nlogo.agent.World;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

// Note: this is for the old patch-based Fire Benchmark model, not the
// new turtle-based Fire model. - ST 11/5/03

/*
to go
  if not any patches with [burning?]
    [ stop ]
  ask patches with [pcolor = green]
    [ if any neighbors4 with [burned?]
        [ set pcolor red
          set burned-trees burned-trees + 1 ] ]
  ask patches with [burning?]
    [ set burned? true
      set pcolor pcolor - 0.3 ]
end

to-report burning? ;; patch procedure
  report (pcolor > (red - 4)) and (pcolor < (red + 1))
end

how to speed it up further:
- burning? is purely functional
- subtracting 0.3 always gives a Double result
- (any (with ...)) doesn't need to actually construct
  an agentset as an intermediate value
- ...?

*/

public final strictfp class _fire
    extends Command {
  private static final double RED = 15.0;
  private static final double GREEN = 55.0;
  private static final Double BOXED_RED = Double.valueOf(RED);
  private static final double RED_MINUS_FOUR = RED - 4;
  private static final double RED_PLUS_ONE = RED + 1;
  private static final int FIRE_VAR = Patch.LAST_PREDEFINED_VAR + 1;
  private static final int COUNTS_VAR = Patch.LAST_PREDEFINED_VAR + 2;

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", true);
  }

  @Override
  public void perform(final Context context) {
    int BURNED_TREES_VAR = world.program().interfaceGlobals().size() + 1;
    int patchCount = world.patches().count();
    boolean any = false;
    for (int i = 0; i < patchCount; i++) {
      Patch patch = world.getPatch(i);
      double pcolor = patch.pcolorDouble();
      if (pcolor > RED_MINUS_FOUR && pcolor < RED_PLUS_ONE) {
        any = true;
        break;  // in the Logo code, the compiler uses _anywith to get this optimization
      }
    }
    if (!any) {
      context.finished = true;
      return;
    }
    for (int i = 0; i < patchCount; i++) {
      Patch patch = world.getPatch(i);
      // if pcolor = green
      if (patch.pcolorDouble() == GREEN) {
        // set counts (nsum4 fire)
        double counts =
            ((Double) patch.getPatchNorth().variables[FIRE_VAR]).doubleValue() +
                ((Double) patch.getPatchSouth().variables[FIRE_VAR]).doubleValue() +
                ((Double) patch.getPatchEast().variables[FIRE_VAR]).doubleValue() +
                ((Double) patch.getPatchWest().variables[FIRE_VAR]).doubleValue();
        patch.variables[COUNTS_VAR] = Double.valueOf(counts);
      }
    }
    for (int i = 0; i < patchCount; i++) {
      Patch patch = world.getPatch(i);
      double pcolor = patch.pcolorDouble();
      if (pcolor == GREEN) {
        if (((Double) patch.variables[COUNTS_VAR]).doubleValue() > 0) {
          // set fire 1
          patch.variables[FIRE_VAR] = World.ONE;
          // set pcolor red
          patch.pcolorDoubleUnchecked(BOXED_RED);
          // set burned-trees burned-trees + 1
          world.observer().variables[BURNED_TREES_VAR] =
              Double.valueOf
                  (((Double) world.observer().variables
                      [world.program().interfaceGlobals().size()])
                      .doubleValue() + 1);
        }
      } else {
        if (pcolor > RED_MINUS_FOUR && pcolor < RED_PLUS_ONE) {
          // set pcolor pcolor - 0.3
          patch.pcolor(pcolor - 0.3);
        }
      }
    }
    context.ip = next;
  }
}
