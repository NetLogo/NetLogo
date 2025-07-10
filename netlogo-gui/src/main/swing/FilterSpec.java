// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import com.sun.jna.Structure;
import com.sun.jna.WString;

// this class has to be in Java because Scala freaks about the FieldOrder annotation (Isaac B 7/10/25)
@Structure.FieldOrder({ "pszName", "pszSpec" })
public class FilterSpec extends Structure {
  public static class ByValue extends FilterSpec implements Structure.ByValue {}

  public WString pszName = null;
  public WString pszSpec = null;

  public void setSpec(String name, String spec) {
    pszName = new WString(name);
    pszSpec = new WString(spec);
  }
}
