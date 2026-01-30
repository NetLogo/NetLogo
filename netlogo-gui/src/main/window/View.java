// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;

import org.nlogo.agent.AgentIterator;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentFollowingPerspective;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.RendererInterface;
import org.nlogo.api.WorkspaceContext;
import org.nlogo.awt.Colors;
import org.nlogo.awt.ImageSelection;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.I18N;
import org.nlogo.swing.Menu;
import org.nlogo.swing.MenuItem;
import org.nlogo.swing.PopupMenu;
import org.nlogo.theme.InterfaceColors;

import scala.Option;

public class View
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
    workspace.viewManager().add(this);
  }

  public boolean displayOn() {
    return (!workspace.getGlView().isFullscreen() &&
        workspace.world().displayOn() &&
        workspace.displaySwitchOn());
  }

  public void displaySwitch(boolean on) {
    workspace.viewWidget().displaySwitch().setOn(on);
  }

  public boolean displaySwitch() {
    return workspace.viewWidget().displaySwitch().isSelected();
  }

  public void incrementalUpdateFromEventThread() {
    paintImmediately();
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
      if (workspace.jobManager().isInterrupted()) {
        return false;
      }
      if (offscreenImage == null) {
        offscreenImage = createImage(getWidth(), getHeight());
        if (offscreenImage != null) {
          gOff = (java.awt.Graphics2D) offscreenImage.getGraphics();
          gOff.setFont(getFont());
          synchronized (workspace.world()) {
            renderer.paint(gOff, this);
          }
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
      if (paintingImmediately) {
        workspace.updateManager().beginPainting();
      }

      super.paint(g);

      if (paintingImmediately) {
        workspace.updateManager().donePainting();
      }

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
        g.setColor(InterfaceColors.viewBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
      } else {
        g.drawImage(offscreenImage, 0, 0, null);
      }
      framesSkipped = false;
    } else if (paintingImmediately) {
      if (offscreenImage == null) {
        offscreenImage = createImage(getWidth(), getHeight());
        gOff = (java.awt.Graphics2D)offscreenImage.getGraphics();
        gOff.setFont(getFont());
      }
      synchronized (workspace.world()) {
        renderer.paint(gOff, this);
      }
      g.drawImage(offscreenImage, 0, 0, null);
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

  public void paintImmediately(boolean force) {
    if (viewIsVisible() && (framesSkipped || force)) {
      paintImmediately();
    }
  }

  public boolean viewIsVisible() {
    return !iconified && isShowing();
  }

  public void paintImmediately() {
    paintingImmediately = true;
    paintImmediately(0, 0, getWidth(), getHeight());
    paintingImmediately = false;
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
    new org.nlogo.window.Events.DirtyEvent(Option.empty()).raise(this);
    renderer.resetCache(patchSize());
    repaint();
  }

  /// event handlers

  public void handle(org.nlogo.window.Events.LoadBeginEvent e) {
    setVisible(false);
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
    return workspace.viewWidget();
  }

  protected double patchSize = 13.0;
  private double zoom = 1.0;

  public double patchSize() {
    return patchSize * zoom;
  }

  public void visualPatchSize(double patchSize) {
    double oldZoom = zoom;
    zoom = patchSize / this.patchSize;
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

  public WorkspaceContext workspaceContext() {
    return workspace.workspaceContext();
  }

  public void populateContextMenu(PopupMenu menu, java.awt.Point p) {
    // certain menu items dont work in Applets.
    // the only ones that do are watch, follow and reset-perspective
    // this check (and others below) prevent items from being added
    // when we are running in Applet. JC - 6/8/10
    menu.add(new MenuItem(new AbstractAction(I18N.guiJ().get("tabs.run.widget.view.copy")) {
      public void actionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
          new ImageSelection(exportView()), null);
      }
    }, true));
    menu.add(new MenuItem(new AbstractAction(I18N.guiJ().get("tabs.run.widget.view.export")) {
      public void actionPerformed(ActionEvent e) {
        workspace.doExportView(View.this);
      }
    }, true));

    menu.add(new MenuItem(workspace.getSwitchTo3DViewAction(), true));

    menu.addSeparator();

    menu.add(new MenuItem(new AbstractAction(I18N.guiJ().get("tabs.run.widget.view.inspectGlobals")) {
      public void actionPerformed(ActionEvent actionEvent) {
        workspace.inspectAgent(AgentKindJ.Observer());
      }
    }, true));

    if (!workspace.world().observer().atHome2D()) {
      menu.addSeparator();
      menu.add(new MenuItem(new AbstractAction(
        "<html>" + Colors.colorize("reset-perspective", InterfaceColors.commandColor())) {
        public void actionPerformed(ActionEvent e) {
          workspace.world().observer().resetPerspective();
          workspace.viewManager().incrementalUpdateFromEventThread();
        }
      }, true));
    }
    p = new java.awt.Point(p);
    mouser.translatePointToXCorYCor(p);
    synchronized (workspace.world()) {
      double xcor = mouser.translatePointToUnboundedX(p.x);
      double ycor = mouser.translatePointToUnboundedY(p.y);

      org.nlogo.agent.Patch patch = null;

      try {
        patch = workspace.world().getPatchAt(xcor, ycor);
        menu.addSeparator();
        menu.add(new AgentMenuItem(patch, AgentMenuType.INSPECT, "inspect", false));
      } catch (AgentException e) {
        org.nlogo.api.Exceptions.ignore(e);
      }

      boolean linksAdded = false;
      for (AgentIterator links = workspace.world().links().iterator();
           links.hasNext();) {
        org.nlogo.agent.Link link = (org.nlogo.agent.Link) links.next();

        if (!link.hidden() &&
            workspace.world().protractor().distance(link, xcor, ycor, true) < link.lineThickness() + 0.5) {
          if (!linksAdded) {
            menu.addSeparator();
            linksAdded = true;
          }
          menu.add(new AgentMenuItem(link, AgentMenuType.INSPECT, "inspect", false));
        }
      }

      // detect any turtles in the pick-ray
      boolean turtlesAdded = false;
      for (AgentIterator turtles = workspace.world().turtles().iterator();
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
                menu.addSeparator();
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
                menu.addSeparator();
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
  }

  private void addTurtleToContextMenu(PopupMenu menu,
                                      org.nlogo.agent.Turtle turtle) {
    javax.swing.JMenu submenu = new AgentMenu(turtle);
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.INSPECT, "inspect", true));
    submenu.addSeparator();
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.WATCH, "watch", true));
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.FOLLOW, "follow", true));
    menu.add(submenu);
  }

  /// context menu

  enum AgentMenuType {INSPECT, FOLLOW, WATCH}

  private class AgentMenuItem
      extends MenuItem {
    org.nlogo.agent.Agent agent;
    AgentMenuType type;
    boolean submenu = false;

    AgentMenuItem(org.nlogo.agent.Agent agent, AgentMenuType type, String caption, boolean submenu) {
      super("<html>" + org.nlogo.awt.Colors.colorize(caption, InterfaceColors.commandColor()) + " " +
            org.nlogo.awt.Colors.colorize(agent.classDisplayName(), InterfaceColors.reporterColor()) +
            org.nlogo.awt.Colors.colorize(agent.toString().substring(agent.classDisplayName().length()),
            InterfaceColors.constantColor()));
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
        workspace.viewManager().incrementalUpdateFromEventThread();
      }
    }
  }

  private class AgentMenu
      extends Menu {
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
      workspace.viewManager().incrementalUpdateFromEventThread();
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

    workspace.viewManager().incrementalUpdateFromEventThread();
  }
}
