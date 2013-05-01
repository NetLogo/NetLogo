// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

import org.nlogo.agent.Agent;
import org.nlogo.api.*;
import org.nlogo.agent.Importer;
import org.nlogo.agent.ImporterJ;
import org.nlogo.agent.World;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.CompilerInterface;
import org.nlogo.nvm.ParserInterface;
import org.nlogo.nvm.FileManager;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Workspace;

public abstract strictfp class AbstractWorkspace
  implements org.nlogo.api.LogoThunkFactory, org.nlogo.api.ParserServices {

  /// startup

  protected AbstractWorkspace() {
    world().parser_$eq(this);
  }

  /// model name utilities

  /**
   * converts a model's filename to an externally displayable model name.
   * The argument may be null, the return value will never be.
   * <p/>
   * Package protected for unit testing.
   */
  static String makeModelNameForDisplay(String str) {
    if (str == null) {
      return "Untitled";
    }
    int suffixIndex = str.lastIndexOf(".nlogo");
    if (suffixIndex > 0 && suffixIndex == str.length() - 6) {
      str = str.substring(0, str.length() - 6);
    }
    suffixIndex = str.lastIndexOf(".nlogo3d");
    if (suffixIndex > 0 && suffixIndex == str.length() - 8) {
      str = str.substring(0, str.length() - 8);
    }
    return str;
  }

  /**
   * attaches the current model directory to a relative path, if necessary.
   * If filePath is an absolute path, this method simply returns it.
   * If it's a relative path, then the current model directory is prepended
   * to it. If this is a new model, the user's platform-dependent home
   * directory is prepended instead.
   */
  public String attachModelDir(String filePath)
      throws java.net.MalformedURLException {
    if (new java.io.File(filePath).isAbsolute()) {
      return filePath;
    }
    String path = getModelPath();
    if (path == null) {
      path = System.getProperty("user.home")
          + java.io.File.separatorChar + "dummy.txt";
    }

    java.net.URL urlForm =
        new java.net.URL
            (toURL(new java.io.File(path)), filePath);

    return new java.io.File(urlForm.getFile()).getAbsolutePath();
  }

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

  private UpdateMode updateMode = UpdateModeJ.CONTINUOUS();

  public UpdateMode updateMode() {
    return updateMode;
  }

  public void updateMode(UpdateMode updateMode) {
    this.updateMode = updateMode;
  }

  // called from an "other" thread (neither event thread nor job thread)
  public abstract void open(String path)
      throws java.io.IOException, LogoException;

  public abstract void openString(String modelContents);

  // called by _display from job thread
  public abstract void requestDisplayUpdate(org.nlogo.nvm.Context context, boolean force);

  // called when the engine comes up for air
  public abstract void breathe(org.nlogo.nvm.Context context);

  public abstract void clearDrawing();

  protected abstract class FileImporter {
    public String filename;

    FileImporter(String filename) {
      this.filename = filename;
    }

    public abstract void doImport(org.nlogo.api.File reader)
        throws java.io.IOException;
  }

  public abstract void clearAll();

  protected abstract org.nlogo.agent.ImporterJ.ErrorHandler importerErrorHandler();

  public void importWorld(String filename)
      throws java.io.IOException {
    // we need to clearAll before we import in case
    // extensions are hanging on to old data. ev 4/10/09
    clearAll();
    doImport
        (new BufferedReaderImporter(filename) {
          @Override
          public void doImport(java.io.BufferedReader reader)
              throws java.io.IOException {
            world().importWorld
              (importerErrorHandler(), (Workspace) AbstractWorkspace.this,
                    stringReader(), reader);
          }
        });
  }

  public void importWorld(java.io.Reader reader)
      throws java.io.IOException {
    // we need to clearAll before we import in case
    // extensions are hanging on to old data. ev 4/10/09
    clearAll();
    world().importWorld
        (importerErrorHandler(), (Workspace) AbstractWorkspace.this,
            stringReader(), new java.io.BufferedReader(reader));
  }

  private final ImporterJ.StringReader stringReader() {
    return new ImporterJ.StringReader() {
      public Object readFromString(String s)
          throws Importer.StringReaderException {
        try {
          return compiler().readFromString
            (s, world(), getExtensionManager());
        } catch (CompilerException ex) {
          throw new Importer.StringReaderException
              (ex.getMessage());
        }
      }
    };
  }

  public void importDrawing(String filename)
      throws java.io.IOException {
    doImport
        (new FileImporter(filename) {
          @Override
          public void doImport(org.nlogo.api.File file)
              throws java.io.IOException {

            importDrawing(file);
          }
        });
  }

  protected abstract void importDrawing(org.nlogo.api.File file)
      throws java.io.IOException;

  // overridden in subclasses - ST 9/8/03, 3/1/11
  public void doImport(BufferedReaderImporter importer)
      throws java.io.IOException {
    org.nlogo.api.File file = new org.nlogo.api.LocalFile(importer.filename());
    try {
      file.open(org.nlogo.api.FileModeJ.READ());
      importer.doImport(file.reader());
    } finally {
      try {
        file.close(false);
      } catch (java.io.IOException ex2) {
        org.nlogo.util.Exceptions.ignore(ex2);
      }
    }
  }


  // protected because GUIWorkspace will override - ST 9/8/03
  protected void doImport(FileImporter importer)
      throws java.io.IOException {
    importer.doImport(new org.nlogo.api.LocalFile(importer.filename));
  }

  public String getSource(String filename)
      throws java.io.IOException {
    // when we stick a string into a JTextComponent, \r\n sequences
    // on Windows will get translated to just \n.  This is a problem
    // because when an error occurs we want to highlight the location
    // using the token location information recorded by the tokenizer,
    // but the removal of the \r characters will throw off that information.
    // So we do the stripping of \r here, *before* we run the tokenizer,
    // and that avoids the problem. - ST 9/14/04
    return new org.nlogo.api.LocalFile(filename).readFile().replaceAll("\r\n", "\n");
  }

  public abstract World world();
  public abstract CompilerInterface compiler();
  public abstract ParserInterface parser();
  public abstract scala.collection.immutable.ListMap<String, Procedure> procedures();
  public abstract FileManager fileManager();
  public abstract String getModelPath();
  public abstract String getModelFileName();
  public abstract ExtensionManager getExtensionManager();
  public abstract boolean profilingEnabled();
  public abstract String getModelDir();
  public abstract void setProfilingTracer(org.nlogo.nvm.Tracer tracer);

}
