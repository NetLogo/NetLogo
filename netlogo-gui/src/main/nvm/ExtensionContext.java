// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import scala.collection.Seq;
import scala.Tuple2;

import org.nlogo.api.MersenneTwisterFast;

public strictfp class ExtensionContext
    implements org.nlogo.api.Context {
  private final Workspace workspace;
  private final Context context;

  public ExtensionContext(Workspace workspace, Context context) {
    this.workspace = workspace;
    this.context = context;
  }

  // These were made public so that extensions can access the
  // runtime and implement things like curry etc...  access to them
  // violates the org.nlogo.api abstraction -- CLB
  public Context nvmContext() {
    return context;
  }

  public Workspace workspace() {
    return workspace;
  }

  public org.nlogo.api.Agent getAgent() {
    return context.agent;
  }

  public String attachModelDir(String filePath)
      throws java.net.MalformedURLException {
    return workspace.attachModelDir(filePath);
  }

  public String attachCurrentDirectory(String path)
      throws java.net.MalformedURLException {
    return workspace.fileManager().attachPrefix(path);
  }

  public MersenneTwisterFast getRNG() {
    return context.job.random;
  }

  public java.awt.image.BufferedImage getDrawing() {
    return workspace.getAndCreateDrawing();
  }

  public void importPcolors(java.awt.image.BufferedImage image, boolean asNetLogoColors) {
    org.nlogo.agent.ImportPatchColors.doImport(image, workspace.world(), asNetLogoColors);
  }

  @Override
  public void logCustomMessage(String msg) {
    workspace.logCustomMessage(msg);
  }

  @Override
  public void logCustomGlobals(Seq<Tuple2<String, String>> nameValuePairs) {
    workspace.logCustomGlobals(nameValuePairs);
  }

}
