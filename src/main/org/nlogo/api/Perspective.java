package org.nlogo.api;

// it's very tempting to get rid of ride entirely but for the interface
// "riding turtle 0" I supposed we still need it. ev 4/29/05
// In the old days this was an integer instead of an Enum, so in exported
// worlds it's still represented as an integer, hence the code here to
// convert back and forth to an integer at import or export time. - ST 3/18/08
public enum Perspective {
  OBSERVE, RIDE, FOLLOW, WATCH;
  public int export() {
    return ordinal();
  }
  public static Perspective load(int perspectiveAsInteger) {
    return Perspective.class.getEnumConstants()[perspectiveAsInteger];
  }
}
