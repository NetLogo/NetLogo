// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Procedure;

public final strictfp class _makepreview
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", false);
  }

  @Override
  public void perform(Context context) throws LogoException {
    try {
      // based on _run.perform - ST 1/25/11
      String modelPath = workspace.getModelPath();
      if(modelPath == null) {
        throw new EngineException(context, this, "no model loaded");
      }
      String previewPath = modelPath.substring(0, modelPath.lastIndexOf(".nlogo")) + ".png";
      String escaped = org.nlogo.api.StringUtils.escapeString(previewPath);
      Procedure procedure =
          workspace.compileForRun
              ("random-seed 0 " + workspace.previewCommands() +
                  "\nexport-view \"" + escaped + "\"" +
                  "\nprint \"GENERATED: " + escaped + "\"",
                  context, false);
      context.activation =
          new org.nlogo.nvm.Activation
              (procedure, context.activation, next);
      context.activation.setUpArgsForRunOrRunresult();
      context.ip = 0;
    } catch (CompilerException error) {
      throw new EngineException
          (context, this, "syntax error: " + error.getMessage());
    }
  }
}
