import org.nlogo.api.*;

public class IntegerList extends DefaultReporter {
  // take one number as input, report a list
  public Syntax getSyntax() {
    return Syntax.reporterSyntax(
        new int[]{Syntax.TYPE_NUMBER}, Syntax.TYPE_LIST
    );
  }

  public Object report(Argument args[], Context context)
      throws ExtensionException {
    // create a NetLogo list for the result
    LogoListBuilder list = new LogoListBuilder();

    int n;
    // use typesafe helper method from
    // org.nlogo.api.Argument to access argument
    try {
      n = args[0].getIntValue();
    } catch (LogoException e) {
      throw new ExtensionException(e.getMessage());
    }

    if (n < 0) {
      // signals a NetLogo runtime error to the modeler
      throw new ExtensionException
          ("input must be positive");
    }

    // populate the list
    // note that we use Double objects; NetLogo numbers
    // are always doubles
    for (int i = 0; i < n; i++) {
      list.add(Double.valueOf(i));
    }
    return list.toLogoList();
  }
}
