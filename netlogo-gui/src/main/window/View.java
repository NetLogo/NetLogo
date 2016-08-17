// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.AgentKindJ;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentFollowingPerspective;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.RendererInterface;
import org.nlogo.awt.ImageSelection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public strictfp class View
    extends javax.swing.JComponent
    implements
    org.nlogo.window.Events.LoadBeginEvent.Handler,
    org.nlogo.window.Events.LoadEndEvent.Handler,
    org.nlogo.window.Events.CompiledEvent.Handler,
    org.nlogo.window.Events.IconifiedEvent.Handler,
    org.nlogo.api.ViewSettings,
    java.awt.event.ActionListener,
    LocalViewInterface {

  protected final GUIWorkspace workspace;
  final ViewMouseHandler mouser;
  public RendererInterface renderer;

  private boolean paintingImmediately = false;
  private boolean framesSkipped = false;

  private int fontSize = 13;

  public View(final GUIWorkspace workspace) {
    this.workspace = workspace;
    setOpaque(true);
    renderer = workspace.newRenderer();
    mouser = new ViewMouseHandler(this, workspace.world(), this);
    addMouseListener(mouser);
    addMouseMotionListener(mouser);
    workspace.viewManager.add(this);
  }

  public boolean isHeadless() {
    return workspace.isHeadless();
  }

  public boolean displayOn() {
    return (!workspace.glView.isFullscreen() &&
        workspace.world().displayOn() &&
        workspace.displaySwitchOn());
  }

  public void displaySwitch(boolean on) {
    workspace.viewWidget.displaySwitch().setOn(on);
  }

  public boolean displaySwitch() {
    return workspace.viewWidget.displaySwitch().isSelected();
  }

  private final Runnable paintRunnable =
      new Runnable() {
        public void run() {
          paintingImmediately = true;
          paintImmediately();
          paintingImmediately = false;
        }
      };

  public void incrementalUpdateFromEventThread() {
    paintRunnable.run();
  }

  public void dirty() {
    dirty = true;
  }

  public void framesSkipped() {
    framesSkipped = true;
  }

  public int fontSize() {
    return fontSize;
  }

  public void resetMouseCors() {
    mouser.resetMouseCors();
  }

  public double mouseXCor() {
    return mouser.mouseXCor();
  }

  public double mouseYCor() {
    return mouser.mouseYCor();
  }

  public boolean mouseDown() {
    return mouser.mouseDown();
  }

  public boolean mouseInside() {
    return mouser.mouseInside();
  }

  public void mouseDown(boolean mouseDown) {
    mouser.mouseDown(mouseDown);
  }

  public boolean isDead() {
    return false;
  }

  /// sizing

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension
        (workspace.world().worldWidth(), workspace.world().worldHeight());
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    int width = (int) (viewWidth * patchSize());
    int height = (int) (viewHeight * patchSize());
    return new java.awt.Dimension(width, height);
  }

  /// iconified checking

  boolean iconified = false;

  public void handle(org.nlogo.window.Events.IconifiedEvent e) {
    if (e.frame == org.nlogo.awt.Hierarchy.getFrame(this)) {
      iconified = e.iconified;
    }
  }

  public boolean iconified() {
    return iconified;
  }

  /// offscreen stuff

  private java.awt.Image offscreenImage = null;
  private java.awt.Graphics2D gOff = null;
  private boolean dirty = true; // is the offscreen image out of date?

  private boolean beClean() // return = success true/false
  {
    // it used to be ok for the height and width to be 0
    // as this method would never get called but
    // now setFont calls it and forces the image to be created
    if (dirty && getWidth() > 0 && getHeight() > 0)
    // this check fixes a bug where during halting,
    // the event thread would wait forever for the world
    // lock. probably not a 100% correct fix, but an
    // improvement, at least - ST 1/10/07
    {
      if (workspace.jobManager.isInterrupted()) {
        return false;
      }
      if (offscreenImage == null) {
        offscreenImage = createImage(getWidth(), getHeight());
        if (offscreenImage != null) {
          gOff = (java.awt.Graphics2D) offscreenImage.getGraphics();
          gOff.setFont(getFont());
        }
      }
      // this might happen since the view widget is not displayable in 3D ev 7/5/07
      if (gOff != null) {
        synchronized (workspace.world()) {
          renderer.paint(gOff, this);
        }
      }
      dirty = false;
    }
    return true;
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    java.awt.Rectangle bounds = getBounds();
    // only set the bounds if they've changed
    if (width != bounds.width || height != bounds.height || x != bounds.x || y != bounds.y) {
      super.setBounds(x, y, width, height);
      discardOffscreenImage();
    }
  }

  public void discardOffscreenImage() {
    offscreenImage = null;
    gOff = null;
    dirty = true;
  }

  @Override
  public void setBounds(java.awt.Rectangle bounds) {
    setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  /// painting

  public int frameCount = 0;

  @Override
  public void paint(java.awt.Graphics g) {
    if (!isDead()) {
      workspace.updateManager().beginPainting();
      super.paint(g);
      workspace.updateManager().donePainting();

      // update the mouse coordinates if following
      if (workspace.world().observer().perspective() instanceof AgentFollowingPerspective) {
        mouser.updateMouseCors();
      }
    }
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    frameCount++;
    if (frozen || !workspace.world().displayOn()) {
      if (dirty) {
        g.setColor(InterfaceColors.GRAPHICS_BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
      } else {
        g.drawImage(offscreenImage, 0, 0, null);
      }
      framesSkipped = false;
    } else if (paintingImmediately) {
      synchronized (workspace.world()) {
        renderer.paint((java.awt.Graphics2D) g, this);
      }
      framesSkipped = false;
    } else {
      if (beClean()) {
        g.drawImage(offscreenImage, 0, 0, null);
        framesSkipped = false;
      } else {
        framesSkipped = true;
      }
    }
  }

  public void paintingImmediately(boolean paintingImmediately) {
    this.paintingImmediately = paintingImmediately;
  }

  public boolean paintingImmediately() {
    return paintingImmediately;
  }

  public void paintImmediately(boolean force) {
    if (viewIsVisible() && (framesSkipped || force)) {
      paintingImmediately(true);
      paintImmediately();
      paintingImmediately(false);
    }
  }

  public boolean viewIsVisible() {
    return !iconified && isShowing();
  }

  public void paintImmediately() {
    paintImmediately(0, 0, getWidth(), getHeight());
  }

  public java.awt.image.BufferedImage exportView() {
    // unfortunately we can't just call awt.Utils.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call renderer's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04
    java.awt.image.BufferedImage image =
        new java.awt.image.BufferedImage
            (getWidth(), getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D graphics =
        (java.awt.Graphics2D) image.getGraphics();
    graphics.setFont(getFont());
    synchronized (workspace.world()) {
      renderer.paint(graphics, this);
    }
    return image;
  }

  /// freeze/thaw

  private boolean frozen = false;

  void freeze() {
    if (!frozen) {
      frozen = true;
      if (workspace.world().displayOn()) {
        beClean();
      }
    }
  }

  void thaw() {
    if (frozen) {
      frozen = false;
      repaint();
    }
  }

  /// shapes

  // for notification from ShapesManager
  public void shapeChanged(org.nlogo.core.Shape shape) {
    dirty = true;
    new org.nlogo.window.Events.DirtyEvent().raise(this);
    renderer.resetCache(patchSize());
    repaint();
  }

  /// event handlers

  public void handle(org.nlogo.window.Events.LoadBeginEvent e) {
    setVisible(false);
    patchSize = 13;
    zoom = 0;
    renderer = workspace.newRenderer();
  }

  public void handle(org.nlogo.window.Events.LoadEndEvent e) {
    renderer.changeTopology(workspace.world().wrappingAllowedInX(),
        workspace.world().wrappingAllowedInY());
    setVisible(true);
  }

  public void handle(org.nlogo.window.Events.CompiledEvent e) {
    if (e.sourceOwner instanceof ProceduresInterface) {
      renderer.resetCache(patchSize());
    }
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
      dirty = true;
      beClean();
    }
  }

  void setTrueFontSize(int size) {
    fontSize = size;
  }

  // Our Rendered gets Font size from the graphics context it is
  // given so we need to make sure our offScreenImage's graphics
  // context has our new font size. -- CLB
  @Override
  public void setFont(java.awt.Font font) {
    super.setFont(font);
    offscreenImage = null;
    discardOffscreenImage();
  }

  public void applyNewFontSize(int newFontSize, int zoom) {
    java.awt.Font font = getFont();
    java.awt.Font newFont =
        new java.awt.Font(font.getName(), font.getStyle(), (newFontSize + zoom));
    setTrueFontSize(newFontSize);
    setFont(newFont);
    dirty();
    repaint();
  }

  public java.awt.Component getExportWindowFrame() {
    return workspace.viewWidget;
  }

  protected double patchSize = 13.0;
  private double zoom = 0.0;

  public double patchSize() {
    return patchSize + zoom;
  }

  public void visualPatchSize(double patchSize) {
    double oldZoom = zoom;
    zoom = patchSize - this.patchSize;
    if (zoom != oldZoom) {
      renderer.resetCache(patchSize());
    }
  }

  protected double viewWidth;

  public double viewWidth() {
    return viewWidth;
  }

  protected double viewHeight;

  public double viewHeight() {
    return viewHeight;
  }

  public void setSize(int worldWidth, int worldHeight, double patchSize) {
    this.patchSize = patchSize;
    this.viewWidth = worldWidth;
    this.viewHeight = worldHeight;

    renderer.resetCache(patchSize());
  }

  public void setSize(int worldWidth, int worldHeight, double viewHeight, double viewWidth, double patchSize) {
    this.patchSize = patchSize;
    this.viewWidth = viewWidth;
    this.viewHeight = viewHeight;

    renderer.resetCache(patchSize());
  }

  public Perspective perspective() {
    return workspace.world().observer().perspective();
  }

  public boolean drawSpotlight() {
    return true;
  }

  public double viewOffsetX() {
    return workspace.world().observer().followOffsetX();
  }

  public double viewOffsetY() {
    return workspace.world().observer().followOffsetY();
  }

  public boolean renderPerspective = true;

  public boolean renderPerspective() {
    return renderPerspective;
  }

  public java.awt.Point populateContextMenu(javax.swing.JPopupMenu menu, java.awt.Point p, java.awt.Component source) {
    // certain menu items dont work in Applets.
    // the only ones that do are watch, follow and reset-perspective
    // this check (and others below) prevent items from being added
    // when we are running in Applet. JC - 6/8/10
    JMenuItem copyItem = new JMenuItem("Copy View");
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
          new ImageSelection(exportView()), null);
      }
    });
    menu.add(copyItem);
    JMenuItem exportItem = new JMenuItem("Export View...");
    exportItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        workspace.doExportView(View.this);
      }
    });
    menu.add(exportItem);

    JMenuItem open3DView = new JMenuItem(workspace.switchTo3DViewAction);
    menu.add(open3DView);

    menu.add(new JPopupMenu.Separator());

    JMenuItem inspectGlobalsItem = new JMenuItem("inspect globals");
    inspectGlobalsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        workspace.inspectAgent(AgentKindJ.Observer());
      }
    });
    menu.add(inspectGlobalsItem);

    if (!workspace.world().observer().atHome2D()) {
      menu.add(new JPopupMenu.Separator());
      JMenuItem resetItem =
        new JMenuItem(
            "<html>"
            + org.nlogo.awt.Colors.colorize("reset-perspective", SyntaxColors.COMMAND_COLOR));
      resetItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          workspace.world().observer().resetPerspective();
          workspace.viewManager.incrementalUpdateFromEventThread();
        }
      });
      menu.add(resetItem);
    }
    p = new java.awt.Point(p);
    mouser.translatePointToXCorYCor(p);
    synchronized (workspace.world()) {
      double xcor = mouser.translatePointToUnboundedX(p.x);
      double ycor = mouser.translatePointToUnboundedY(p.y);

      org.nlogo.agent.Patch patch = null;

      try {
        patch = workspace.world().getPatchAt(xcor, ycor);
        menu.add(new JPopupMenu.Separator());
        menu.add(new AgentMenuItem(patch, AgentMenuType.INSPECT, "inspect", false));
      } catch (AgentException e) {
        org.nlogo.api.Exceptions.ignore(e);
      }

      boolean linksAdded = false;
      for (AgentSet.Iterator links = workspace.world().links().iterator();
           links.hasNext();) {
        org.nlogo.agent.Link link = (org.nlogo.agent.Link) links.next();

        if (!link.hidden() &&
            workspace.world().protractor().distance(link, xcor, ycor, true) < link.lineThickness() + 0.5) {
          if (!linksAdded) {
            menu.add(new javax.swing.JPopupMenu.Separator());
            linksAdded = true;
          }
          menu.add(new AgentMenuItem(link, AgentMenuType.INSPECT, "inspect", false));
        }
      }

      // detect any turtles in the pick-ray
      boolean turtlesAdded = false;
      for (AgentSet.Iterator turtles = workspace.world().turtles().iterator();
           turtles.hasNext();) {
        org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) turtles.next();
        if (!turtle.hidden()) {
          double offset = turtle.size() * 0.5;
          if (offset * workspace.world().patchSize() < 3) {
            offset += (3 / workspace.world().patchSize());
          }

          org.nlogo.shape.VectorShape shape = (org.nlogo.shape.VectorShape)
              workspace.world().turtleShapeList().shape(turtle.shape());

          if (shape.isRotatable() && !turtle.hidden()) {
            double dist = workspace.world().protractor().distance(turtle, xcor, ycor, true);

            if (dist <= offset) {
              if (!turtlesAdded) {
                menu.add(new javax.swing.JPopupMenu.Separator());
                turtlesAdded = true;
              }

              addTurtleToContextMenu(menu, turtle);
            }
          } else {
            // otherwise the turtle takes a square shape
            double xCor = turtle.xcor();
            double yCor = turtle.ycor();
            double xMouse = xcor;
            double yMouse = ycor;

            if (workspace.world().wrappingAllowedInX()) {
              double x = xCor > xMouse ? xMouse + workspace.world().worldWidth() :
                  xMouse - workspace.world().worldWidth();
              xMouse = StrictMath.abs(xMouse - xCor)
                  < StrictMath.abs(x - xCor) ? xMouse : x;
            }
            if (workspace.world().wrappingAllowedInY()) {
              double y = yCor > yMouse ? yMouse + workspace.world().worldHeight() :
                  yMouse - workspace.world().worldHeight();
              yMouse = StrictMath.abs(yMouse - yCor)
                  < StrictMath.abs(y - yCor) ? yMouse : y;
            }

            if ((xMouse >= xCor - offset) && (xMouse <= xCor + offset) &&
                (yMouse >= yCor - offset) && (yMouse <= yCor + offset)) {
              if (!turtlesAdded) {
                menu.add(new JPopupMenu.Separator());
                turtlesAdded = true;
              }

              addTurtleToContextMenu(menu, turtle);
            }
          }
        }
      }

      int x = 0;
      int y = 0;

      if (patch != null) {
        x = (int) StrictMath.round(renderer.graphicsX(patch.pxcor + 1, patchSize(), viewOffsetX()));
        y = (int) StrictMath.round(renderer.graphicsY(patch.pycor - 1, patchSize(), viewOffsetY()));

        p.x += StrictMath.min((x - p.x), 15);
        p.y += StrictMath.min((y - p.y), 15);
      }
    }

    return p;
  }

  private void addTurtleToContextMenu(javax.swing.JPopupMenu menu,
                                      org.nlogo.agent.Turtle turtle) {
    javax.swing.JMenu submenu = new AgentMenu(turtle);
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.INSPECT, "inspect", true));
    submenu.add(new javax.swing.JPopupMenu.Separator());
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.WATCH, "watch", true));
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.FOLLOW, "follow", true));
    menu.add(submenu);
  }

  /// context menu

  enum AgentMenuType {INSPECT, FOLLOW, WATCH}

  private class AgentMenuItem
      extends javax.swing.JMenuItem {
    org.nlogo.agent.Agent agent;
    AgentMenuType type;
    boolean submenu = false;

    AgentMenuItem(org.nlogo.agent.Agent agent, AgentMenuType type, String caption, boolean submenu) {
      super("<html>"
          + org.nlogo.awt.Colors.colorize(
          caption,
          SyntaxColors.COMMAND_COLOR)
          + " "
          + org.nlogo.awt.Colors.colorize(
          agent.classDisplayName(),
          SyntaxColors.REPORTER_COLOR)
          + org.nlogo.awt.Colors.colorize(
          agent.toString().substring(agent.classDisplayName().length()),
          SyntaxColors.CONSTANT_COLOR)
      );
      this.agent = agent;
      this.type = type;
      addActionListener(View.this);
      this.submenu = submenu;
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
      super.menuSelectionChanged(isIncluded);
      if (!submenu) {
        renderer.outlineAgent((isIncluded) ? agent : null);
        workspace.viewManager.incrementalUpdateFromEventThread();
      }
    }
  }

  private class AgentMenu
      extends javax.swing.JMenu {
    org.nlogo.agent.Agent agent;
    int type;

    AgentMenu(org.nlogo.agent.Agent agent) {
      super(agent.toString());
      this.agent = agent;
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
      super.menuSelectionChanged(isIncluded);
      renderer.outlineAgent((isIncluded) ? agent : null);
      workspace.viewManager.incrementalUpdateFromEventThread();
    }
  }

  public void actionPerformed(java.awt.event.ActionEvent e) {
    AgentMenuItem item = (AgentMenuItem) e.getSource();
    Perspective newPerspective;
    switch (item.type) {
      case INSPECT:
        // we usually use a default radius of 3, but that doesnt work when the world
        // has a radius of less than 3. so simply take the miniumum. - JC 7/1/10
        double minWidthOrHeight =
          StrictMath.min(workspace.world().worldWidth() / 2, workspace.world().worldHeight() / 2);
        double radius = StrictMath.min(3, minWidthOrHeight / 2);
        workspace.inspectAgent(item.agent.kind(), item.agent, radius);
        return;
      case FOLLOW:
        int distance = (int) ((org.nlogo.agent.Turtle) item.agent).size() * 5;
        newPerspective = PerspectiveJ.create(PerspectiveJ.FOLLOW, item.agent,
            StrictMath.max(1, StrictMath.min(distance, 100)));
        workspace.world().observer().setPerspective(newPerspective);
        break;
      case WATCH:
        workspace.world().observer().home();
        newPerspective = PerspectiveJ.create(PerspectiveJ.WATCH, item.agent);
        workspace.world().observer().setPerspective(newPerspective);
        break;
      default:
        throw new IllegalStateException();
    }

    workspace.viewManager.incrementalUpdateFromEventThread();
  }
}
