// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public strictfp class TieManager {
  private final World world;
  final LinkManager linkManager;

  TieManager(World world, LinkManager linkManager) {
    this.world = world;
    this.linkManager = linkManager;
  }

  public void reset() {
    tieCount = 0;
  }

  /// tie support

  int tieCount = 0;

  public void setTieMode(Link link, String mode) {
    if (link.isTied()) {
      if (mode.equals(Link.MODE_NONE)) {
        tieCount--;
      }
    } else if (!mode.equals(Link.MODE_NONE)) {
      tieCount++;
    }
  }

  List<Turtle> tiedTurtles(Turtle root) {
    ArrayList<Turtle> myTies = new ArrayList<Turtle>();
    if (linkManager.srcMap.containsKey(root)) {
      for (Link link : linkManager.srcMap.get(root)) {
        if (link.isTied()) {
          Turtle t = link.end2();
          myTies.add(t);
        }
      }
    }
    if (linkManager.destMap.containsKey(root)) {
      for (Link link : linkManager.destMap.get(root)) {
        if (!link.getBreed().isDirected()
            && link.isTied()) {
          Turtle t = link.end1();
          myTies.add(t);
        }
      }
    }
    return myTies;
  }

  void turtleMoved(Turtle root, double newX, double newY,
                   double originalXcor, double originalYcor) {
    turtleMoved(root, newX, newY, originalXcor, originalYcor,
        new HashSet<Turtle>());
  }

  // this is recursive it calls turtle.xandycor which in turn calls this
  void turtleMoved(Turtle root, double newX, double newY,
                   double originalXcor, double originalYcor,
                   Set<Turtle> seenTurtles) {
    boolean weroot = false;
    try {
      if (seenTurtles.isEmpty()) {
        // we need to add ourselves to that we are not updated in
        // this run
        seenTurtles.add(root);
        weroot = true;
      }
      // update my leaf positions and tell
      List<Turtle> myTies = tiedTurtles(root);
      Iterator<Turtle> i = myTies.iterator();

      // add my links to seen turtles
      while (i.hasNext()) {
        Turtle t = i.next();
        if (t.id() == -1
            || seenTurtles.contains(t)) {
          i.remove(); // removes seen turtles from myTies
          continue;
        }
        seenTurtles.add(t);
      }

      // update positions
      i = myTies.iterator();
      while (i.hasNext()) {
        Turtle t = i.next();

        double changex = newX - originalXcor;
        double changey = newY - originalYcor;
        try {
          t.xandycor(t.xcor + changex,
              t.ycor + changey, seenTurtles);
        } catch (AgentException ex) {
          // We get here if the xandycor throws an error for
          // topological reasons.  In such cases we want to
          // keep the turtle where it is at and translate
          // all the other tied turtles. -- CLB
          org.nlogo.util.Exceptions.ignore(ex);
        }
      }
    } finally {
      // if we were the root turtle, we clear the seenTurtles list
      if (weroot) {
        seenTurtles.clear();
      }
    }
  }

  void turtleTurned(Turtle root, double newHeading, double originalHeading) {
    turtleTurned(root, newHeading, originalHeading,
        new HashSet<Turtle>());
  }

  // this is recursive it calls turtle.heading which calls this
  void turtleTurned(Turtle root, double newHeading, double originalHeading,
                    Set<Turtle> seenTurtles) {
    boolean weroot = false;
    try {
      if (seenTurtles.isEmpty()) {
        // we need to add ourselves to that we are not updated in
        // this run
        seenTurtles.add(root);
        weroot = true;
      }
      List<Turtle> myTies = tiedTurtles(root);
      Iterator<Turtle> i = myTies.iterator();

      // add my links to seen turtles
      while (i.hasNext()) {
        Turtle t = i.next();
        if (t.id() == -1
            || seenTurtles.contains(t)) {
          i.remove(); // removes seen turtles from myTies
          continue;
        }
        seenTurtles.add(t);
      }

      // update positions
      for (Turtle t : myTies) {
        try {
          Link link = linkManager.findLink(root, t, world.links(), true);
          boolean rigid = link.mode().equals(Link.MODE_FIXED);

          double dh = Turtle.subtractHeadings(newHeading, originalHeading);
          double dist = world.protractor().distance(root.xcor, root.ycor, t.xcor, t.ycor, true);

          if (dist == 0) {
            if (rigid) {
              t.heading(t.heading + dh, seenTurtles);
            }

          } else {

            // When we calculate the new location of
            // the leaf, we get unwrapped coordinates
            // which are "relative" to the root node.
            // However, In order for trail drawing to
            // work we need to call xandycor with
            // unwrapped coordinates relative to the
            // leaf node.  So, we calculate the leaf's
            // current location relative to the root,
            // and then it's new location.  We take
            // the dx/dy of that and add it to the
            // leaf turtle's coordinates.
            double towards = world.protractor().towards(root, t, true);
            double oldHeadingRadians = StrictMath.toRadians(towards);
            double newHeadingRadians = StrictMath.toRadians(towards + dh);
            double ocos = StrictMath.cos(oldHeadingRadians);
            double osin = StrictMath.sin(oldHeadingRadians);
            double ncos = StrictMath.cos(newHeadingRadians);
            double nsin = StrictMath.sin(newHeadingRadians);

            if (StrictMath.abs(ocos) < org.nlogo.api.Constants.Infinitesimal()) {
              ocos = 0;
            }
            if (StrictMath.abs(osin) < org.nlogo.api.Constants.Infinitesimal()) {
              osin = 0;
            }


            if (StrictMath.abs(ncos) < org.nlogo.api.Constants.Infinitesimal()) {
              ncos = 0;
            }
            if (StrictMath.abs(nsin) < org.nlogo.api.Constants.Infinitesimal()) {
              nsin = 0;
            }

            double oldtx = (root.xcor + (dist * osin));
            double oldty = (root.ycor + (dist * ocos));
            double newtx = (root.xcor + (dist * nsin));
            double newty = (root.ycor + (dist * ncos));

            double dx = newtx - oldtx;
            double dy = newty - oldty;

            Set<Turtle> snapshot = seenTurtles;
            try {
              // clone the seen Turtles list so we can
              // reset it after xandycor are updated;
              if (rigid) {
                snapshot = new HashSet<Turtle>(seenTurtles);
              }
              t.xandycor(t.xcor + dx,
                  t.ycor + dy, seenTurtles);
              // if the move fails, heading is not updated.
              // This is on purpose.-- CLB
            } finally {
              if (rigid) {
                seenTurtles = snapshot;
              }
            }
            if (rigid) {
              t.heading(t.heading + dh, seenTurtles);
            }
          }
        } catch (AgentException ex) {
          // We get here if the towards call fails (which
          // shouldn't happen) or xandycor throws an error for
          // topological reasons.  In such cases we want to
          // keep the turtle where it is at and translate
          // all the other tied turtles. -- CLB
          org.nlogo.util.Exceptions.ignore(ex);
        }
      }
    } finally {
      // if we were the root turtle, we clear the seenTurtles list
      if (weroot) {
        seenTurtles.clear();
      }
    }
  }
}
