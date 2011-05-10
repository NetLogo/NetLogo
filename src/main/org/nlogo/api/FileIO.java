package org.nlogo.api;

public final strictfp class FileIO {

  // this class is not instantiable
  private FileIO() {
    throw new IllegalStateException();
  }

  public static String file2String(String path)
      throws java.io.IOException {
    java.io.FileReader fr = new java.io.FileReader(path);
    return org.nlogo.util.Utils.reader2String(fr);
  }

  public static void writeFile(String path, String text)
      throws java.io.IOException {
    writeFile(path, text, false);
  }

  public static void writeFile
      (String path, String text, boolean convertToPlatformLineBreaks)
      throws java.io.IOException {
    org.nlogo.api.File file = null;
    try {
      file = new org.nlogo.api.LocalFile(path);
      file.open(org.nlogo.api.File.Mode.WRITE);
      if (!convertToPlatformLineBreaks) {
        file.print(text);
      } else {
        java.io.BufferedReader lineReader = new java.io.BufferedReader(
            new java.io.StringReader(text));
        String line = lineReader.readLine();
        while (line != null) {
          file.println(line);
          line = lineReader.readLine();
        }
      }
      file.close(true);
    } finally {
      if (file != null) {
        file.close(false);
      }
    }
  }
}
