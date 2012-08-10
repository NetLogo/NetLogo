// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Describes a single patch.
 */
public final strictfp class PatchData
    extends AgentData {

  /**
   * Description mask for PatchData object with no descriptive content. *
   */
  public static final short DEAD = 0x0000;

  /**
   * Description mask for patch pcolor. *
   */
  public static final short PCOLOR = 0x0001;

  /**
   * Description mask for patch label. *
   */
  public static final short PLABEL = 0x0002;

  /**
   * Description mask for label color. *
   */
  public static final short PLABEL_COLOR = 0x0004;

  public static final short PXCOR = 0x0008;
  public static final short PYCOR = 0x0010;

  /**
   * Description mask for PatchData object that completely describes a patch. *
   */
  public static final short COMPLETE = PCOLOR | PLABEL | PLABEL_COLOR | PXCOR | PYCOR;

  /**
   * The id of the patch. *
   */
  private final long id;

  /**
   * The color of the patch. *
   */
  private LogoList pcolor;

  /**
   * The label of the patch. *
   */
  private String plabel = "";

  /**
   * The label color of the patch. *
   */
  private LogoList plabelColor;

  private int pxcor;
  private int pycor;

  /**
   * Specifies which patch attributes are described by this
   * PatchData object.
   * The bitwise or of all the description masks that apply
   * to this PatchData.<p>
   * For example: a PatchData that describes the color and
   * the label color of a patch, but not the label itself
   * should have mask <code>PCOLOR | LABEL_COLOR</code>.
   */
  private short mask;

  int[] patchColors;


  /**
   * Creates a new patch data.
   */
  PatchData(long id, short mask, int pxcor, int pycor, Object pcolor, String plabel, Object plabelColor) {
    this.id = id;
    this.pxcor = pxcor;
    this.pycor = pycor;
    this.mask = mask;
    pcolor(pcolor);
    this.plabel = plabel;
    plabelColor(plabelColor);
  }

  /**
   * Reconstructs a serialized patch data object.
   *
   * @param is stream containing serialized patch data.
   */
  PatchData(DataInputStream is)
      throws IOException {
    id = is.readLong();
    mask = is.readShort();
    if ((mask & PXCOR) == PXCOR) {
      pxcor = is.readInt();
    }
    if ((mask & PYCOR) == PYCOR) {
      pycor = is.readInt();
    }
    if ((mask & PCOLOR) == PCOLOR) {
      pcolor(is.readInt());
    }
    if ((mask & PLABEL) == PLABEL) {
      plabel = is.readUTF();
    }
    if ((mask & PLABEL_COLOR) == PLABEL_COLOR) {
      plabelColor(is.readInt());
    }
  }

  /**
   * Creates a new patch data with no descriptive content.
   */
  private PatchData(long id) {
    this.id = id;
    mask = DEAD;
  }

  /**
   * Returns the id of the patch this object describes.
   */
  public long id() {
    return id;
  }

  public int pxcor() {
    return pxcor;
  }

  public int pycor() {
    return pycor;
  }

  /**
   * Returns the color of the patch this object describes.
   */
  public Object pcolor() {
    return pcolor;
  }

  public void pcolor(Object pcolor) {
    if (pcolor instanceof Double) {
      pcolor((Double) pcolor);
    } else {
      pcolor((LogoList) pcolor);
    }
  }

  private void pcolor(Double pcolor) {
    pcolor(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(pcolor));
  }

  private void pcolor(org.nlogo.api.LogoList pcolor) {
    this.pcolor = pcolor;
    if (patchColors != null) {
      patchColors[(int) id] = org.nlogo.api.Color.getARGBIntByRGBAList(pcolor);
    }
  }

  private void pcolor(int argb) {
    this.pcolor = org.nlogo.api.Color.getRGBAListByARGB(argb);
    if (patchColors != null) {
      patchColors[(int) id] = argb;
    }
  }

  /**
   * Returns the label of the patch this object describes.
   */
  public String plabel() {
    return plabel;
  }

  public void plabel(Object plabel) {
    this.plabel = Dump.logoObject(plabel);
  }

  /**
   * Returns the label color of the patch this object describes.
   */
  public Object plabelColor() {
    return plabelColor;
  }

  public void plabelColor(Object plabelColor) {
    if (plabelColor instanceof Double) {
      plabelColor((Double) plabelColor);
    } else {
      plabelColor((LogoList) plabelColor);
    }
  }

  public void plabelColor(Double plabelColor) {
    plabelColor(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(plabelColor));
  }

  public void plabelColor(LogoList plabelColor) {
    this.plabelColor = plabelColor;
  }

  public void plabelColor(int argb) {
    plabelColor(org.nlogo.api.Color.getRGBAListByARGB(argb));
  }

  public boolean hasLabel() {
    return plabel.length() > 0;
  }

  private static final String[] OVERRIDE_VARIABLES = new String[]{"PCOLOR", "PLABEL", "PLABEL-COLOR"};
  private static final String[] OVERRIDE_METHODS = new String[]{"pcolor", "plabel", "plabelColor"};

  @Override
  public String getMethodName(int index) {
    return OVERRIDE_METHODS[index];
  }

  public static int getOverrideIndex(String varName) {
    return getOverrideIndex(OVERRIDE_VARIABLES, varName);
  }

  @Override
  public double xcor() {
    return pxcor;
  }

  @Override
  public double ycor() {
    return pycor;
  }

  @Override
  public double spotlightSize() {
    return 1;
  }

  @Override
  public boolean wrapSpotlight() {
    return false;
  }

  /**
   * Returns a string representation of the patch this object describes.
   */
  String stringRep() {
    return "Patch " + pxcor + " " + pycor + " (" + org.nlogo.api.Color.getARGBIntByRGBAList(pcolor)
        + ", " + plabel + ", " + org.nlogo.api.Color.getARGBIntByRGBAList(plabelColor) + ")";
  }

  /**
   * Returns true if this PatchData completely describes a patch.
   */
  boolean isComplete() {
    return (mask & COMPLETE) == COMPLETE;
  }

  /**
   * Updates this PatchData with data from another PatchData.
   *
   * @param otherPatch another PatchData from which to update this PatchData
   * @return a new PatchData representing the changes made to
   *         this PatchData (i.e. the difference between the two objects).
   *         If nothing was changed, returns null.
   */
  PatchData updateFrom(PatchData otherPatch) {
    // start out with a "dead" patch, but we'll fill it in...
    PatchData diffs = new PatchData(id);

    // update the values...
    if ((otherPatch.mask & PXCOR) == PXCOR && pxcor != otherPatch.pxcor) {
      pxcor = otherPatch.pxcor;
      diffs.mask |= PXCOR;
      diffs.pxcor = pxcor;
    }
    if ((otherPatch.mask & PYCOR) == PYCOR && pycor != otherPatch.pycor) {
      pycor = otherPatch.pycor;
      diffs.mask |= PYCOR;
      diffs.pycor = pycor;
    }
    if ((otherPatch.mask & PCOLOR) == PCOLOR && !pcolor.equals(otherPatch.pcolor)) {
      pcolor = otherPatch.pcolor;
      diffs.mask |= PCOLOR;
      diffs.pcolor = pcolor;
    }
    if ((otherPatch.mask & PLABEL) == PLABEL && !plabel.equals(otherPatch.plabel)) {
      plabel = otherPatch.plabel;
      diffs.mask |= PLABEL;
      diffs.plabel = plabel;
    }
    if ((otherPatch.mask & PLABEL_COLOR) == PLABEL_COLOR && !plabelColor.equals(otherPatch.plabelColor)) {
      plabelColor(otherPatch.plabelColor);
      diffs.mask |= PLABEL_COLOR;
      diffs.plabelColor = plabelColor;
    }

    if (diffs.mask != DEAD) {
      return diffs;
    } else {
      return null;
    }
  }

  /**
   * Serializes this patch data object.
   */
  void serialize(DataOutputStream os)
      throws IOException {
    os.writeLong(id);
    os.writeShort(mask);
    if ((mask & PXCOR) == PXCOR) {
      os.writeInt(pxcor);
    }
    if ((mask & PYCOR) == PYCOR) {
      os.writeInt(pycor);
    }
    if ((mask & PCOLOR) == PCOLOR) {
      os.writeInt(org.nlogo.api.Color.getARGBIntByRGBAList(pcolor));
    }
    if ((mask & PLABEL) == PLABEL) {
      os.writeUTF(plabel);
    }
    if ((mask & PLABEL_COLOR) == PLABEL_COLOR) {
      os.writeInt(org.nlogo.api.Color.getARGBIntByRGBAList(plabelColor));
    }
  }
}
