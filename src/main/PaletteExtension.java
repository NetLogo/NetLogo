import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;

public class PaletteExtension extends DefaultClassManager {
    public void load(PrimitiveManager primitiveManager) {
        primitiveManager.addPrimitive ("scale-gradient", new ScaleGradient());
        primitiveManager.addPrimitive ("scale-gradient-hsb", new ScaleGradientHSB());
        primitiveManager.addPrimitive ("scale-scheme", new ScaleScheme());
        primitiveManager.addPrimitive ("scheme-colors", new SchemeColors());
        primitiveManager.addPrimitive ("scheme-dialog", new SchemeDialog());
        //alpha
        primitiveManager.addPrimitive ("alpha-of", new ExtractAlpha());
        primitiveManager.addPrimitive ("alpha", new GetAgentAlpha());
        primitiveManager.addPrimitive ("set-alpha", new SetAgentAlpha());
        primitiveManager.addPrimitive ("with-alpha", new WithAlpha());
        primitiveManager.addPrimitive ("transparency-of", new ExtractTransparency());
        primitiveManager.addPrimitive ("transparency", new GetAgentTransparency());
        primitiveManager.addPrimitive ("set-transparency", new SetAgentTransparency());
        primitiveManager.addPrimitive ("with-transparency", new WithTransparency());
        //HSB
        primitiveManager.addPrimitive ("hue", new GetAgentHSB(0));
        primitiveManager.addPrimitive ("saturation", new GetAgentHSB(1));
        primitiveManager.addPrimitive ("brightness", new GetAgentHSB(2));

        primitiveManager.addPrimitive ("set-hue", new SetAgentHSB(0));
        primitiveManager.addPrimitive ("set-saturation", new SetAgentHSB(1));
        primitiveManager.addPrimitive ("set-brightness", new SetAgentHSB(2));

        primitiveManager.addPrimitive ("hue-of", new ExtractHSB(0));
        primitiveManager.addPrimitive ("saturation-of", new ExtractHSB(1));
        primitiveManager.addPrimitive ("brightness-of", new ExtractHSB(2));

        primitiveManager.addPrimitive ("with-hue", new WithHSB(0));
        primitiveManager.addPrimitive ("with-saturation", new WithHSB(1));
        primitiveManager.addPrimitive ("with-brightness", new WithHSB(2));
        //RGB
        primitiveManager.addPrimitive ("R", new GetAgentRGB(0));
        primitiveManager.addPrimitive ("G", new GetAgentRGB(1));
        primitiveManager.addPrimitive ("B", new GetAgentRGB(2));

        primitiveManager.addPrimitive ("set-R", new SetAgentRGB(0));
        primitiveManager.addPrimitive ("set-G", new SetAgentRGB(1));
        primitiveManager.addPrimitive ("set-B", new SetAgentRGB(2));

        primitiveManager.addPrimitive ("R-of", new ExtractRGB(0));
        primitiveManager.addPrimitive ("G-of", new ExtractRGB(1));
        primitiveManager.addPrimitive ("B-of", new ExtractRGB(2));

        primitiveManager.addPrimitive ("with-R", new WithRGB(0));
        primitiveManager.addPrimitive ("with-G", new WithRGB(1));
        primitiveManager.addPrimitive ("with-B", new WithRGB(2));
    }
}
