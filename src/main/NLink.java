import com.wolfram.jlink.*;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.core.CompilerException;
import org.nlogo.core.LogoList;
import org.nlogo.nvm.EngineException;
import org.nlogo.api.LogoException;
import org.nlogo.app.App;
import java.awt.EventQueue;

public class NLink {
  private String modelLocation;
  private org.nlogo.workspace.Controllable workspace = null;
  private java.io.IOException caughtEx = null;
  private boolean isGUIworkspace;

  public NLink(boolean isGUImode)
  {
    isGUIworkspace = isGUImode;
    if( isGUImode ) {
      App.main( new String[] { } ) ;
      workspace = App.app();
      org.nlogo.api.Exceptions.setHandler
        ( new org.nlogo.api.Exceptions.Handler() {
            public void handle( Throwable t ) {
              throw new RuntimeException(t.getMessage());
            } } );
    }
    else
      workspace = HeadlessWorkspace.newInstance() ;
  }

  public void loadModel(final String path)
    throws java.io.IOException, LogoException, CompilerException, InterruptedException
  {
    caughtEx = null;
    if ( isGUIworkspace ) {
      try {
        EventQueue.invokeAndWait (
          new Runnable() {
            public void run() {
              try
              { App.app().open(path, true); }
              catch( java.io.IOException ex)
              { caughtEx = ex; }
            } } );
      }
      catch( java.lang.reflect.InvocationTargetException ex ) {
        throw new RuntimeException(ex.getMessage());
      }
      if( caughtEx != null ) {
        throw caughtEx;
      }
    }
    else {
      try {
        if (workspace != null)
          ((HeadlessWorkspace)workspace).dispose();
        workspace = HeadlessWorkspace.newInstance() ;
        workspace.open(path, true);
      }
      // if we cannot open a NetLogo model for some reason, throw an exception
      // and start a new workspace
      catch( java.io.IOException ex) {
        if (workspace != null)
          ((HeadlessWorkspace)workspace).dispose();
        workspace = HeadlessWorkspace.newInstance() ;
        throw ex;
      }
    }
  }



/* returns the value of a reporter.  if it is a LogoList, it will be
   recursively converted to an array of Objects */
  public Expr report(final String s)
    throws LogoException, CompilerException
  {
    return logoToExpr(workspace.report(s));
  }

/* returns an evaluated list of reporters */
  public Expr report(final String s[])
    throws LogoException, CompilerException
  {
    Expr[] results = new Expr[s.length];
    for( int i = 0; i < s.length; i++ )
      results[i] = logoToExpr(workspace.report(s[i]));
    return (new Expr(new Expr(Expr.SYMBOL, "List"), results));
  }

  public void command(final String s)
    throws LogoException, CompilerException
  {
    workspace.command(s);
  }

  public void command(final String[] s)
    throws LogoException, CompilerException
  {
    for(int i = 0; i < s.length; i++)
      workspace.command(s[i]);
  }


/* Repeats a command and returns a single reporter for n interations */
  public Expr doReport(final String s, final String var, final int repeats)
    throws LogoException, CompilerException
  {
    Expr[] results = new Expr[repeats];
    for(int i=0; i < repeats; i++) {
      workspace.command(s);
      results[i] = report(var);
    }
    return (new Expr(new Expr(Expr.SYMBOL, "List"), results));
  }

/* Repeats a command and returns a list of reporters for n interations */
  public Expr doReport(final String s, final String[] vars, final int repeats)
    throws LogoException, CompilerException
  {
    int i;
    Expr[] results = new Expr[repeats];
    for(i=0; i < repeats; i++) {
      workspace.command(s);
      results[i] = report(vars);
    }
    return (new Expr(new Expr(Expr.SYMBOL, "List"), results));
  }



/* Repeats a command and returns a reporter until a condition is met */
  public Expr doReportWhile(final String s, final String var, final String condition)
    throws LogoException, CompilerException
  {
    java.util.ArrayList<Expr> varList = new java.util.ArrayList<Expr>();
    for(int i=0; ((Boolean)workspace.report(condition)).booleanValue(); i++) {
      workspace.command(s);
      varList.add(report(var));
    }
    Object[] objArray = varList.toArray();
    Expr[] exprArray = new Expr[objArray.length];
    for (int j=0; j < exprArray.length; j++)
      exprArray[j] = (Expr)objArray[j];
    return (new Expr(new Expr(Expr.SYMBOL, "List"), exprArray));
  }

/* Repeats a command and returns a list of reporters until a condition is met */
  public Expr doReportWhile(final String s, final String[] vars, final String condition)
    throws LogoException, CompilerException
  {
    java.util.ArrayList<Expr> varList = new java.util.ArrayList<Expr>();
    Expr[] reports;
    for(int i=0; ((Boolean)workspace.report(condition)).booleanValue(); i++) {
      workspace.command(s);
      varList.add(report(vars));
    }
    Object[] objArray = varList.toArray();
    Expr[] exprArray = new Expr[objArray.length];
    for (int j=0; j < exprArray.length; j++)
      exprArray[j] = (Expr)objArray[j];
    return (new Expr(new Expr(Expr.SYMBOL, "List"), exprArray));
  }

/* Recursively converts LogoList into an Expr object representing a Mathematica List*/
    private Expr NLArray( LogoList l ) {
    Expr[] exprArray = new Expr[l.size()];
    for(int i = 0; i < l.size(); i++)
      exprArray[i] = logoToExpr(l.get(i));
    return (new Expr(new Expr(Expr.SYMBOL, "List"), exprArray));
    }

/* Converts datatypes returned from NetLogo controlling API into Mathematica
   Expression objects
*/
  private Expr logoToExpr( Object o ) {
    if(o instanceof LogoList)
      return NLArray((LogoList)o);
    else if (o instanceof String)
      return new Expr((String)o);
    else if (o instanceof Integer)
      return new Expr(((Integer)o).intValue());
    else if (o instanceof Double)
      return new Expr(((Double)o).doubleValue());
    else if (o instanceof Boolean) {
      if ( ((Boolean)o).booleanValue() )
        return new Expr(Expr.SYMBOL, "True");
      else
        return new Expr(Expr.SYMBOL, "False");
    }
    else
      return new Expr("Unknown data type");
  }
}
