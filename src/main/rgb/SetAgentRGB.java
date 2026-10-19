import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Command;
import org.nlogo.api.Color;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.ExtensionException;

public class SetAgentRGB implements Command {

  private int index;

  public SetAgentRGB(int ind) {
    index = ind;
  }

  public Syntax getSyntax() {
    int values[] = { Syntax.NumberType() };
    return SyntaxJ.commandSyntax(values, "-TPL");
  }

  public void perform(Argument args[], Context context) throws ExtensionException {
    double newVal = 255;
    try {
      newVal = args[0].getDoubleValue();
    }
    catch (ExtensionException e) {
      throw new ExtensionException(e.getMessage());
    }
    if (newVal < 0 || newVal > 255) {
      throw new ExtensionException("Value must be in the range from 0 to 255.");
    }
    ColorManager.setAgentColor(context, RGBUpdated.updateRGB(ColorManager.getAgentColor(context), newVal, index));
  }
}
