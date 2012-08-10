// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util;

// we leave this in Java so we can use @SuppressWarnings

public final strictfp class JUtils {

  // this class is not instantiable
  private JUtils() { throw new IllegalStateException(); }

  // for 5.0 we have too much fragile, difficult-to-understand, under-tested code involving URLs --
  // we can't get rid of our uses of toURL() until 5.1, the risk of breakage is too high.  so for
  // now, at least we make this a separate method so we can find all the calls later - ST 12/7/09,
  // 8/6/11
  @SuppressWarnings("deprecation")
  public static java.net.URL toURL(java.io.File file)
    throws java.net.MalformedURLException
  {
    return file.toURL();
  }

}
