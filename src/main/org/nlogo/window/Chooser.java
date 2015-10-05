// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.agent.ChooserConstraint;
import org.nlogo.api.CompilerServices;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoList;

import java.awt.event.MouseWheelListener;

public abstract strictfp class Chooser
    extends SingleErrorWidget
    implements MouseWheelListener {

  protected final CompilerServices compiler;

  // The constraint track the list of choices, and ensures the
  // global is always one of them.  We use it to track our current
  // index too (the selected value in the chooser). -- CLB
  ChooserConstraint constraint = new ChooserConstraint(
      ChooserConstraint.$lessinit$greater$default$1(), ChooserConstraint.$lessinit$greater$default$2());

  // sub-elements of Switch
  private final ChooserClickControl control = new ChooserClickControl();


  // visual parameters
  private static final int MIN_WIDTH = 92;
  private static final int MIN_PREFERRED_WIDTH = 120;
  private static final int CHOOSER_HEIGHT = 45;
  private static final int MARGIN = 4;
  private static final int PADDING = 14;

  /// setup and layout

  public Chooser(CompilerServices compiler) {
    this.compiler = compiler;
    setOpaque(true);
    setBackground(InterfaceColors.SLIDER_BACKGROUND);
    setLayout(null);
    add(control);
    doLayout();
    org.nlogo.awt.Fonts.adjustDefaultFont(this);
    this.addMouseWheelListener(this);
  }

  /// attributes

  private String name = "";

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
    repaint();
  }

  protected int index() {
    return constraint.defaultIndex();
  }

  protected void index(int index) {
    constraint.defaultIndex_$eq(index);
    updateConstraints();
    repaint();
  }

  protected void choices(LogoList acceptedValues) {
    constraint.acceptedValues(acceptedValues);
  }

  public Object value() {
    return constraint.defaultValue();
  }

  @Override
  public void updateConstraints() {
    if (name().length() > 0) {
      new org.nlogo.window.Events.AddChooserConstraintEvent(name, constraint)
          .raise(this);
    }
  }


  @Override
  public void doLayout() {
    int controlHeight = getHeight() / 2;
    control.setBounds(MARGIN,
        getHeight() - MARGIN - controlHeight,
        getWidth() - 2 * MARGIN,
        controlHeight);
  }

  /// size calculations

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(MIN_WIDTH, CHOOSER_HEIGHT);
  }

  @Override
  public java.awt.Dimension getMaximumSize() {
    return new java.awt.Dimension(10000, CHOOSER_HEIGHT);
  }

  @Override
  public java.awt.Dimension getPreferredSize(java.awt.Font font) {
    int width = MIN_PREFERRED_WIDTH;
    java.awt.FontMetrics metrics = getFontMetrics(font);
    width = StrictMath.max(width, metrics.stringWidth(name) + 2 * MARGIN + PADDING);
    width = StrictMath.max
        (width,
            longestChoiceWidth(metrics) + triangleSize() + 5 * MARGIN + PADDING + 2); // extra 2 for triangle shadow
    return new java.awt.Dimension(width, CHOOSER_HEIGHT);
  }

  private int longestChoiceWidth(java.awt.FontMetrics metrics) {
    int result = 0;
    for (int i = 0; i < constraint.acceptedValues().size(); i++) {
      int width = metrics.stringWidth(Dump.logoObject(constraint.acceptedValues().get(i)));
      result = StrictMath.max(result, width);
    }
    return result;
  }

  private int triangleSize() {
    return control.getBounds().height / 2 - MARGIN;
  }


  /// respond to user actions

  public void popup() {
    org.nlogo.swing.WrappingPopupMenu menu = new org.nlogo.swing.WrappingPopupMenu();
    populate(menu);
    menu.show(this,
        control.getBounds().x + 3,   // the 3 aligns us with the inside edge
        control.getBounds().y + control.getBounds().height);
  }

  public void populate(javax.swing.JPopupMenu menu) {
    if (constraint.acceptedValues().isEmpty()) {
      javax.swing.JMenuItem nullItem =
          new javax.swing.JMenuItem("<No Choices>");
      nullItem.setEnabled(false);
      menu.add(nullItem);
    } else {
      for (int i = 0; i < constraint.acceptedValues().size(); i++) {
        javax.swing.JMenuItem item =
            new javax.swing.JMenuItem
                (Dump.logoObject(constraint.acceptedValues().get(i)));
        final int iFinal = i;
        item.addActionListener
            (new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                index(iFinal);
              }
            }
            );
        menu.add(item);
      }
    }
  }

  ///

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g); // paint background
    java.awt.Dimension size = getSize();
    java.awt.Rectangle cb = control.getBounds();

    g.setColor(getForeground());
    java.awt.FontMetrics metrics = g.getFontMetrics();
    int fontAscent = metrics.getMaxAscent();
    int fontHeight = fontAscent + metrics.getMaxDescent();

    String shortenedName =
        org.nlogo.awt.Fonts.shortenStringToFit
            (name, size.width - 2 * MARGIN, metrics);
    g.drawString
        (shortenedName,
            MARGIN,
            MARGIN + (cb.y - MARGIN - fontHeight) / 2 + fontAscent);

    String shortenedValue =
        org.nlogo.awt.Fonts.shortenStringToFit
            (Dump.logoObject(value()),
                cb.width - MARGIN * 3 - triangleSize() - 2, // extra 2 for triangle shadow
                metrics);
    g.drawString
        (shortenedValue,
            cb.x + MARGIN,
            cb.y + (cb.height - fontHeight) / 2 + fontAscent);

    ((java.awt.Graphics2D) g).setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    filledDownTriangle
        (g,
            cb.x + cb.width - MARGIN - triangleSize() - 2,  // extra 2 for triangle shadow
            cb.y + (cb.height - triangleSize()) / 2 + 1,
            triangleSize());

    setToolTipText(shortenedName != name ? name : null);
    control.setToolTipText(!shortenedValue.equals(Dump.logoObject(value())) ? Dump.logoObject(value()) : null);
  }

  private static void filledDownTriangle(java.awt.Graphics g, int x, int y, int size) {
    java.awt.Polygon shadowTriangle = new java.awt.Polygon();
    shadowTriangle.addPoint(x + size / 2, y + size + 2);
    shadowTriangle.addPoint(x - 1, y - 1);
    shadowTriangle.addPoint(x + size + 2, y - 1);
    g.setColor(java.awt.Color.DARK_GRAY);
    g.fillPolygon(shadowTriangle);

    java.awt.Polygon downTriangle = new java.awt.Polygon();
    downTriangle.addPoint(x + size / 2, y + size);
    downTriangle.addPoint(x, y);
    downTriangle.addPoint(x + size, y);
    g.setColor(InterfaceColors.SLIDER_HANDLE);
    g.fillPolygon(downTriangle);
  }

  private strictfp class ChooserClickControl
      extends javax.swing.JComponent {
    ChooserClickControl() {
      setBackground(InterfaceColors.SLIDER_BACKGROUND);
      setBorder(widgetBorder());
      setOpaque(false);
      addMouseListener
          (new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
              popup();
            }
          });
    }
  }

  public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {

    if (e.getWheelRotation() >= 1) {
      int max = constraint.acceptedValues().size() - 1;
      index(StrictMath.min(max, index() + 1));
    } else {
      index(StrictMath.max(0, index() - 1));
    }

  }
}
