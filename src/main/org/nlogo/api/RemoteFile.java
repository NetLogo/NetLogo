package org.nlogo.api;

// we shouldn't need "File." lampsvn.epfl.ch/trac/scala/ticket/1409 - ST 3/6/11

public strictfp class RemoteFile extends File {

  private final String filepath;
  private String suffix = null;
  private File.Mode mode = File.Mode.NONE;
  private java.io.BufferedReader buffReader;

  public RemoteFile(String filepath) {
    this.filepath = filepath;
  }

  public RemoteFile(String filepath, String suffix) {
    if (filepath != null && suffix != null) {
      String tmpf = filepath.toLowerCase();
      String tmps = suffix.toLowerCase();
      if (tmpf.endsWith(tmps)) {
        this.filepath = filepath.substring(0, tmpf.lastIndexOf(tmps));
      } else {
        this.filepath = filepath;
      }
    } else {
      this.filepath = filepath;
    }
    this.suffix = suffix;
  }

  @Override
  public File.Mode getMode() {
    return mode;
  }


  @Override
  public java.io.PrintWriter getPrintWriter() {
    return null;
  }

  @Override
  public java.io.BufferedReader getBufferedReader() {
    return buffReader;
  }

  @Override
  public java.io.InputStream getInputStream()
      throws java.io.IOException {
    return new java.io.BufferedInputStream
        (new java.net.URL
            (org.nlogo.util.Utils.escapeSpacesInURL(getPath()))
            .openStream());
  }

  public static boolean exists(String path) {
    String url = org.nlogo.util.Utils.escapeSpacesInURL(path);
    try {
      new java.net.URL(url).openStream();
      return true;
    } catch (java.io.IOException ex) {
      return false;
    }

  }

  @Override
  public void open(File.Mode mode)
      throws java.io.IOException {
    if (buffReader != null) {
      throw new java.io.IOException("Attempted to open an already open file");
    }
    // Comment out this line and switch lines below to enable renaming code
    String fullpath = suffix == null ? filepath : filepath + suffix;
    switch (mode) {
      case READ:
        pos = 0;
        eof = false;
        buffReader = new java.io.BufferedReader(
            new java.io.InputStreamReader(
                new java.io.BufferedInputStream(
                    new java.net.URL(org.nlogo.util.Utils.escapeSpacesInURL(fullpath)).openStream())));
        this.mode = mode;
        break;
      case WRITE:
        throw new java.io.IOException("Cannot write to remote files.");
      case APPEND:
        throw new java.io.IOException("Cannot append to remote files.");
      default:
        break;
    }
  }

  @Override
  public void print(String str)
      throws java.io.IOException {
    throw new java.io.IOException("Attempted to print to an unopened File");
  }

  @Override
  public void println(String line)
      throws java.io.IOException {
    throw new java.io.IOException("Attempted to println to an unopened File");
  }

  @Override
  public void println()
      throws java.io.IOException {
    throw new java.io.IOException("Attempted to println to an unopened File");
  }

  @Override
  public void flush() {
  }

  @Override
  public void close(boolean ok)
      throws java.io.IOException {
    if (buffReader == null) {
      return;  // not an error
    }
    switch (mode) {
      case WRITE:
      case APPEND:
        break;
      case READ:
        buffReader.close();
        buffReader = null;
        break;
      default:
        break;
    }
    mode = File.Mode.NONE;

  }

  @Override
  public String getAbsolutePath() {
    return filepath;
  }

  @Override
  public String getPath() {
    return filepath;
  }

}
