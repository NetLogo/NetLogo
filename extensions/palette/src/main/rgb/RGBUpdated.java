import org.nlogo.api.Color;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.LogoList;

public class RGBUpdated {
  public static LogoList updateRGB(LogoList rgb, double newVal, int ind) {
    LogoListBuilder newRGB = new LogoListBuilder();
    for (int i = 0; i < rgb.length(); i++) {
      if (i == ind) { newRGB.add(newVal); }
      else { newRGB.add(rgb.get(i)); }
    }
    return newRGB.toLogoList();
  }
}
