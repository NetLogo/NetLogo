package org.nlogo.api;

import org.nlogo.util.SysInfo;

public final strictfp class Version {

  private static final String PATH = "/system/version.txt";
  private static final String NO_VERSION;

  // this class is not instantiable
  private Version() {
    throw new IllegalStateException();
  }

  private static final String version;
  private static final String buildDate;
  private static final String[] knownVersions;

  static {
    String[] lines =
        org.nlogo.util.Utils.getResourceAsStringArray(PATH);
    buildDate = lines[1];
    String[] lines2 = new String[]{"NetLogo 3D Preview 5",
        "NetLogo 3D Preview 4",
        "NetLogo 3D Preview 3",
        "NetLogo 3-D Preview 2",
        "NetLogo 3-D Preview 1"};
    if (is3D()) {
      version = lines[0].replaceFirst("NetLogo", "NetLogo 3D");
      knownVersions = new String[(lines.length * 2) + lines2.length - 2];
    } else {
      version = lines[0];
      knownVersions = new String[lines.length];
    }
    knownVersions[0] = version;
    int j = 1;
    for (int i = 2; i < lines.length; i++) {
      knownVersions[j] = lines[i];
      j++;
    }
    if (is3D()) {
      for (int i = 2; i < lines.length; i++) {
        knownVersions[j] = "NetLogo 3D" + lines[i].substring("NetLogo".length());
        j++;
      }
      for (int i = 0; i < lines2.length; i++) {
        knownVersions[j] = lines2[i];
        j++;
      }
    }
    NO_VERSION = is3D() ? "NetLogo 3D (no version)" : "NetLogo (no version)";
    knownVersions[knownVersions.length - 1] = NO_VERSION;
  }

  public static boolean is3D(String version) {
    if (version != null) {
      return version.indexOf("3D") != -1 ||
          version.indexOf("3-D") != -1;
    } else {
      return false;
    }
  }

  public static boolean is3D() {
    try {
      return Boolean.getBoolean("org.nlogo.is3d");
    }
    // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
    catch (java.security.AccessControlException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }
    return false;
  }

  // it's gruesome this is a static global, but can't really
  // do anything about it for 4.1.x - ST 11/11/10

  private static boolean loggingEnabled = false;

  public static boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  public static void startLogging() {
    loggingEnabled = true;
  }

  public static void stopLogging() {
    loggingEnabled = false;
  }

  // Turning the optimizer off may be useful when testing or
  // modifying the compiler.  This flag is public so we can
  // conditionalize tests on it, since the results of some tests are
  // affected by whether the optimizer is enabled or not.  The
  // results are no less correct either way, just different, since
  // the optimizer is free to make changes that cause floating point
  // operations to happen in a different order or use a different
  // amount of random numbers and thus leave the RNG in a different
  // state. - ST 3/9/06
  public static boolean useOptimizer() {
    try {
      return !Boolean.getBoolean("org.nlogo.noOptimizer");
    }
    // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
    catch (java.security.AccessControlException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
      return false;
    }
  }

  public static boolean useGenerator() {
    try {
      if (Boolean.getBoolean("org.nlogo.noGenerator")) {
        return false;
      }
      Class.forName("org.nlogo.generator.Generator");
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
    // can't check arbitrary properties from applets... - ST 10/4/04, 1/31/05
    catch (java.security.AccessControlException ex) {
      // generator doesn't work in applet yet - ST 7/18/06
      // don't use the generator in the applet because it
      // requires CustomClass loading which is not allowed
      // in the applet.
      return false;
    }
  }

  public static boolean knownVersion(String version) {
    version = removeRev(version.trim());
    for (int i = 0; i < knownVersions.length; i++) {
      if (version.startsWith(knownVersions[i])) {
        return true;
      }
    }
    return false;
  }

  public static String removeRev(String version) {
    if (version.lastIndexOf(" (Rev ") == version.length() - 8) {
      return version.substring(0, version.length() - 8);
    } else {
      return version;
    }
  }

  public static String version() {
    return version;
  }

  public static String noVersion() {
    return NO_VERSION;
  }

  public static String versionNumberOnly() {
    return version().substring("NetLogo ".length());
  }

  public static boolean compatibleVersion(String modelVersion) {
    return compareVersions(version, modelVersion);
  }

  static boolean compareVersions(String appVersion, String modelVersion) {
    return modelVersion.equals(noVersion()) ||
        versionNumber(modelVersion).startsWith(versionNumber(appVersion));
  }

  static String versionNumber(String v) {
    if (v.startsWith("NetLogo 3D Preview")) {
      return v.substring("NetLogo 3D ".length(), "NetLogo 3D Preview 5".length());
    } else if (v.startsWith("NetLogo 3D")) {
      return v.substring("NetLogo 3D ".length(), "NetLogo 3D 4.0".length());
    } else {
      return v.substring("NetLogo ".length(), "NetLogo 4.0".length());
    }
  }

  public static String buildDate() {
    return buildDate;
  }

  public static String fullVersion() {
    return version + " (" + buildDate + ")";
  }

}
