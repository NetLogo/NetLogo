// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public strictfp class HubNetLinkStamp
    extends HubNetDrawingMessage {
  static final long serialVersionUID = 0L;

  public double x1;
  public double y1;
  public double x2;
  public double y2;
  public String shape;
  public Object color;
  public boolean hidden;
  public double lineThickness;
  public boolean erase;
  public boolean directedLink;
  public double destSize;
  public double heading;
  public double size;

  public HubNetLinkStamp() {
    super(HubNetDrawingMessage.Type.STAMP);
  }

  public HubNetLinkStamp(org.nlogo.api.Link link, boolean erase) {
    super(HubNetDrawingMessage.Type.STAMP);
    x1 = link.x1();
    y1 = link.y1();
    x2 = link.x2();
    y2 = link.y2();
    shape = link.shape();
    color = link.color();
    hidden = link.hidden();
    lineThickness = link.lineThickness();
    directedLink = link.isDirectedLink();
    destSize = link.linkDestinationSize();
    heading = link.heading();
    size = link.size();
    this.erase = erase;
  }

  @Override
  public String toString() {
    return x1 + " " + y1 + " " + x2 + " " + y2 + " " + color + " "
        + shape + " " + hidden + " " + lineThickness + " " + erase;
  }
}
