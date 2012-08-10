// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;

/*
to go
  ask patches
    [ set live-neighbors count neighbors with [living?] ] ]
  ask patches
    [ ifelse live-neighbors = 3
        [ set living? true
          set pcolor fgcolor ]
        [ if live-neighbors != 2
            [ set living? false
              set pcolor bgcolor ] ] ] ]
*/

public final strictfp class _life
    extends Command {
  private static final int LIVING_VAR = Patch.LAST_PREDEFINED_VAR + 1;
  private static final int LIVE_NEIGHBORS_VAR = Patch.LAST_PREDEFINED_VAR + 2;

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    int patchCount = world.patches().count();
    double fgcolor =
        ((Double) world.observer().variables
            [world.program().globals().indexOf("FGCOLOR")])
            .doubleValue();
    double bgcolor =
        ((Double) world.observer().variables
            [world.program().globals().indexOf("BGCOLOR")])
            .doubleValue();
    for (int i = 0; i < patchCount; i++) {
      Patch patch = world.getPatch(i);
      int liveNeighbors = 0;
      for (AgentIterator iter = patch.getNeighbors().iterator(); iter.hasNext();) {
        if (((Boolean) (((Patch) iter.next()).variables[LIVING_VAR]))
            .booleanValue()) {
          liveNeighbors++;
        }
      }
      patch.variables[LIVE_NEIGHBORS_VAR] = Double.valueOf(liveNeighbors);
    }
    for (int i = 0; i < patchCount; i++) {
      Patch patch = world.getPatch(i);
      int liveNeighbors =
          ((Double) patch.variables[LIVE_NEIGHBORS_VAR]).intValue();
      if (liveNeighbors == 3) {
        patch.variables[LIVING_VAR] = Boolean.TRUE;
        patch.pcolor(fgcolor);
      } else if (liveNeighbors != 2) {
        patch.variables[LIVING_VAR] = Boolean.FALSE;
        patch.pcolor(bgcolor);
      }
    }
    world.tickCounter.tick(1);
    context.ip = next;
  }
}
