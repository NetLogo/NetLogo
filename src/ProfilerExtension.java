package org.nlogo.extensions.profiler;

import java.util.Arrays;
import java.util.stream.Stream;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.Context;
import org.nlogo.api.Command;
import org.nlogo.api.Reporter;
import org.nlogo.api.Argument;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.Tracer;
import org.nlogo.nvm.Workspace;

public class ProfilerExtension extends org.nlogo.api.DefaultClassManager {
  public void load(org.nlogo.api.PrimitiveManager primManager) {
    primManager.addPrimitive("start", new ProfilerStart());
    primManager.addPrimitive("stop", new ProfilerStop());
    primManager.addPrimitive("reset", new ProfilerReset());
    primManager.addPrimitive("report", new ProfilerReport());
    primManager.addPrimitive("data", new ProfilerData());
    primManager.addPrimitive("calls", new ProfilerProcedureCalls());
    primManager.addPrimitive("exclusive-time", new ProfilerProcedureExclusiveTime());
    primManager.addPrimitive("inclusive-time", new ProfilerProcedureInclusiveTime());
  }

  public void runOnce(org.nlogo.api.ExtensionManager em)
      throws org.nlogo.api.ExtensionException {
    Tracer tracer = new QuickTracer();
    ((org.nlogo.workspace.ExtensionManager) em).workspace().setProfilingTracer(tracer);
    // we disable it once it is installed, so that
    // we don't start collecting profiling data until
    // ProfilerStart is called.  -- CLB
    tracer.disable();
  }

  public void unload(org.nlogo.api.ExtensionManager em) {
    ((org.nlogo.workspace.ExtensionManager) em).workspace().setProfilingTracer(null);
  }

  public static class ProfilerStart implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    public void perform(Argument args[], Context context) throws ExtensionException {

      if (!org.nlogo.api.Version$.MODULE$.useGenerator()) {
        throw new ExtensionException
            ("The profiler extension requires the NetLogo bytecode generator, "
                + "which is currently turned off. See the org.nlogo.noGenerator property.");
      }
      ((ExtensionContext) context).workspace().profilingTracer().enable();
    }
  }

  public static class ProfilerStop implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    public void perform(Argument args[], Context context) throws ExtensionException {
      Tracer tracer = ((ExtensionContext) context).workspace().profilingTracer();
      if (tracer != null) {
        tracer.disable();
      }
    }
  }

  public static class ProfilerReset implements Command {
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    public void perform(Argument args[], Context context) throws ExtensionException {
      Tracer tracer = ((ExtensionContext) context).workspace().profilingTracer();
      if (tracer != null) {
        tracer.reset();
      }
    }

  }

  public static class ProfilerReport implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.StringType());
    }

    public Object report(Argument args[], Context context) throws ExtensionException {
      Tracer tracer = ((ExtensionContext) context).workspace().profilingTracer();
      if (tracer != null) {
        java.io.ByteArrayOutputStream outArray = new java.io.ByteArrayOutputStream();
        java.io.PrintStream out = new java.io.PrintStream(outArray);
        tracer.dump(out);
        return outArray.toString();
      }
      return "";
    }
  }

  public static class ProfilerData implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.ListType());
    }

    public Object report(Argument args[], Context context) {
      final Workspace workspace = ((ExtensionContext) context).workspace();
      final QuickTracer tracer = ((QuickTracer) workspace.profilingTracer());
      return tracer == null
        ? LogoList.Empty()
        : LogoList.fromJava(Stream.concat(
          Stream.of(LogoList.fromJava(Arrays.asList(
            "procedure", "calls", "inclusive_time", "exclusive_time"
          ))),
          tracer.procedureNames().stream().map(procedureName ->
            (Object) LogoList.fromJava(Arrays.asList(
              procedureName,
              Double.valueOf(tracer.calls(procedureName)),
              Double.valueOf(tracer.inclusiveTime(procedureName) / 1E6),
              Double.valueOf(tracer.exclusiveTime(procedureName) / 1E6)
            ))
          )
        )::iterator);
    }
  }

  public static class ProfilerProcedureCalls implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.StringType()},
              Syntax.NumberType());
    }

    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      Tracer tracer = ((ExtensionContext) context).workspace().profilingTracer();
      if (tracer != null) {
        String arg0 = args[0].getString().toUpperCase();
        return Double.valueOf(tracer.calls(arg0));
      }
      return Double.valueOf(0);
    }
  }

  public static class ProfilerProcedureExclusiveTime implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.StringType()},
              Syntax.NumberType());
    }

    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      Tracer tracer = ((ExtensionContext) context).workspace().profilingTracer();
      if (tracer != null) {
        String arg0 = args[0].getString().toUpperCase();
        return Double.valueOf(tracer.exclusiveTime(arg0) / 1000000.0);
      }
      return Double.valueOf(0);
    }
  }

  public static class ProfilerProcedureInclusiveTime implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.StringType()},
              Syntax.NumberType());
    }

    public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
      Tracer tracer = ((ExtensionContext) context).workspace().profilingTracer();
      if (tracer != null) {
        String arg0 = args[0].getString().toUpperCase();
        return Double.valueOf(tracer.inclusiveTime(arg0) / 1000000.0);
      }
      return Double.valueOf(0);
    }
  }
}
