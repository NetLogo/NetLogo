// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public strictfp class OriginConfiguration {
  private final String displayName;
  private final boolean[] editorsEnabled;
  private final boolean[] setValue;

  public OriginConfiguration(String name, boolean[] enabled,
                             boolean[] setValue) {
    displayName = name;
    editorsEnabled = enabled;
    this.setValue = setValue;
  }

  public boolean getEditorEnabled(int i) {
    return editorsEnabled[i];
  }

  public boolean setValue(int i) {
    return setValue[i];
  }

  @Override
  public String toString() {
    return displayName;
  }
}
