import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;

public class SampleExtension extends DefaultClassManager {
  public void load(PrimitiveManager primitiveManager) {
    primitiveManager.addPrimitive("first-n-integers", new IntegerList());
    primitiveManager.addPrimitive("my-list", new MyList());
  }
}
