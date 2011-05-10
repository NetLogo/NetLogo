package org.nlogo.api;

public abstract strictfp class File {

  public enum Mode {NONE, READ, WRITE, APPEND}

  // private static final String TMPSUFFIX = ".tmp";

  public static final String PROG_DELIM_START = "\\";
  public static final String PROG_DELIM_REST = "n";
  public static final String LINE_BREAK = "\n";

  public boolean eof = false;
  public long pos = 0;

  private java.io.BufferedReader buffReader;

  public Mode getMode() {
    return Mode.NONE;
  }

  public abstract java.io.PrintWriter getPrintWriter();

  public java.io.BufferedReader getBufferedReader() {
    return buffReader;
  }

  public abstract void open(Mode mode)
      throws java.io.IOException;

  public abstract void print(String str)
      throws java.io.IOException;

  public abstract void println(String line)
      throws java.io.IOException;

  public abstract void println()
      throws java.io.IOException;

  public abstract void flush();

  public abstract void close(boolean ok)
      throws java.io.IOException;

  public abstract java.io.InputStream getInputStream()
      throws java.io.IOException;

  public abstract String getAbsolutePath();

  public abstract String getPath();

  public static String stripLines(String st) {
    if (st == null || st.equals("")) {
      // why null? this is bad old Chuck-ness, but I'm worried if I change it
      // it might break something, god knows what... - ST 6/8/04
      return null;
    }
    StringBuilder buff = new StringBuilder();

    for (int i = 0; i < st.length(); i++) {
      char ch = st.charAt(i);
      switch (ch) {
        case '\n':
          buff.append("\\n");
          break;
        case '\\':
          buff.append("\\\\");
          break;
        case '\"':
          buff.append("\\\"");
          break;
        default:
          buff.append(ch);
      }
    }
    return buff.toString();
  }

  public static String restoreLines(String s) {
    if (s == null || s.equals("")) {
      // why null? this is bad old Chuck-ness, but I'm worried if I change it
      // it might break something, god knows what... - ST 6/8/04
      return null;
    }
    // now handle escape sequences
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '\\' && i != s.length() - 1) {
        i++;
        switch (s.charAt(i)) {
          case 'n':
            buf.append('\n');
            break;
          case '\\':
            buf.append('\\');
            break;
          case '\"':
            buf.append('\"');
            break;
          default:
            throw new IllegalArgumentException("invalid escape sequence in \"" + s + "\"");
        }
      } else {
        buf.append(s.charAt(i));
      }
    }
    return buf.toString();
  }

  public static boolean validName(String filename) {
    return (filename != null) && (filename.length() > 0);
  }

  // Read in a file

  public String readFile()
      throws java.io.IOException {
    if (getBufferedReader() == null) {
      open(Mode.READ);
    }

    return org.nlogo.util.Utils.reader2String(getBufferedReader());
  }

}
