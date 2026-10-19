import org.nlogo.api.*;
import org.nlogo.api.Reporter;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

public class MyList implements Reporter {
  public Syntax getSyntax() {
    return SyntaxJ.reporterSyntax
        (new int[]{Syntax.WildcardType() | Syntax.RepeatableType()}, Syntax.ListType(), 2);
  }

  public Object report(Argument args[], Context context)
      throws ExtensionException, LogoException {
    LogoListBuilder list = new LogoListBuilder();
    for (int i = 0; i < args.length; i++) {
      list.add(args[i].get());
    }
    return list.toLogoList();
  }
}
