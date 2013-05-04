// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

public abstract strictfp class AbstractWorkspaceJ {

  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings("deprecation")
  public static java.net.URL toURL(java.io.File file)
      throws java.net.MalformedURLException {
    return file.toURL();
  }

  /// misc

  private org.nlogo.api.UpdateMode updateMode = org.nlogo.api.UpdateModeJ.CONTINUOUS();

  public org.nlogo.api.UpdateMode updateMode() {
    return updateMode;
  }

  public void updateMode(org.nlogo.api.UpdateMode updateMode) {
    this.updateMode = updateMode;
  }

  // called from an "other" thread (neither event thread nor job thread)
  public abstract void open(String path)
      throws java.io.IOException, org.nlogo.api.LogoException;

  public abstract void openString(String modelContents);

  // called by _display from job thread
  public abstract void requestDisplayUpdate(org.nlogo.nvm.Context context, boolean force);

  // called when the engine comes up for air
  public abstract void breathe(org.nlogo.nvm.Context context);

  public abstract void clearAll();
  public abstract void clearDrawing();

  public abstract org.nlogo.agent.World world();
  public abstract org.nlogo.nvm.CompilerInterface compiler();
  public abstract org.nlogo.nvm.ParserInterface parser();
  public abstract scala.collection.immutable.ListMap<String, org.nlogo.nvm.Procedure> procedures();
  public abstract org.nlogo.nvm.FileManager fileManager();
  public abstract String getModelPath();
  public abstract String getModelFileName();
  public abstract ExtensionManager getExtensionManager();
  public abstract boolean profilingEnabled();
  public abstract String getModelDir();
  public abstract void setProfilingTracer(org.nlogo.nvm.Tracer tracer);

}
