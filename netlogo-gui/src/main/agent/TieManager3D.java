// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.Matrix3D;
import org.nlogo.api.Vect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import scala.collection.JavaConversions;

public strictfp class TieManager3D
    extends TieManager {
  World3D world3D;

  TieManager3D(World3D world3D, LinkManager linkManager) {
    super(world3D, linkManager);
    this.world3D = world3D;
  }

  ///

  // the set of turtles we are updating
  private Set<Turtle3D> seenTurtles =
      new HashSet<Turtle3D>();

  void turtleMoved(Turtle3D root,
                   double newX, double newY, double newZ,
                   double originalXcor, double originalYcor, double originalZcor) {
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
        Turtle3D t = (Turtle3D) i.next();
        if (t.id == -1
            || seenTurtles.contains(t)) {
          i.remove(); // removes seen turtles from myTies
          continue;
        }
        seenTurtles.add(t);
      }

      if (!myTies.isEmpty()) {
        double changex = newX - originalXcor;
        double changey = newY - originalYcor;
        double changez = newZ - originalZcor;
        for (Turtle t : myTies) {
          Turtle3D t2 = (Turtle3D) t;
          // In order to get wrapping and line drawing to work properly
          // we have to compute our transform in coordinates relative to the
          // root turtle -- CLB 05/11/06
          t2.xyandzcor(t2.xcor + changex,
              t2.ycor + changey,
              t2.zcor + changez);
        }
      }
    } finally {
      // if we were the root turtle, we clear the seenTurtles list
      if (weroot) {
        seenTurtles.clear();
      }
    }
  }

  @Override
  void turtleTurned(Turtle root, double newHeading, double originalHeading) {
    Turtle3D t = (Turtle3D) root;
    turtleOrientationChanged(t, newHeading, t.pitch(), t.roll(),
        originalHeading, t.pitch(), t.roll());
  }

  void turtleOrientationChanged
      (Turtle3D root,
       double newHeading, double newPitch, double newRoll,
       double oldHeading, double oldPitch, double oldRoll) {
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
        Turtle3D t = (Turtle3D) i.next();
        if (t.id == -1
            || seenTurtles.contains(t)) {
          i.remove(); // removes seen turtles from myTies
          continue;
        }
        seenTurtles.add(t);
      }

      if (!myTies.isEmpty()) {
        // create a matrix transform for translating the location
        // of leaf turtles
        Matrix3D ptrans = new Matrix3D();
        Matrix3D rtrans = new Matrix3D();
        double dh = Turtle.subtractHeadings(newHeading, oldHeading);
        double dp = Turtle.subtractHeadings(newPitch, oldPitch);
        double dr = Turtle.subtractHeadings(newRoll, oldRoll);

        Matrix3D htrans = Rotations3D.zrot(-dh); // this transform method takes degrees, not radians
        Vect[] vects = Vect.toVectors(newHeading, oldPitch, 0);
        ptrans.vrot(0, 0, 0,
            vects[1].x(), vects[1].y(), vects[1].z(), StrictMath.toRadians(dp));
        vects = Vect.toVectors(newHeading, newPitch, oldRoll);
        rtrans.vrot(0, 0, 0,
            vects[0].x(), vects[0].y(), vects[0].z(), StrictMath.toRadians(dr));


        double[] out = new double[3];
        for (Turtle t1 : myTies) {
          try {
            Turtle3D t = (Turtle3D) t1;
            boolean rigid = Arrays.stream(linkManager.linksWith(root, t, world3D.links()))
                    .anyMatch(l -> l.mode().equals(Link.MODE_FIXED));

            // In order to get wrapping and line drawing to work properly
            // we have to compute our transform in coordinates relative to the
            // root turtle -- CLB 05/11/06
            double[] leaf = ((Protractor3D) (world3D.protractor())).towardsVector
                (root.xcor(), root.ycor(), root.zcor(),
                    t.xcor(), t.ycor(), t.zcor(), true);
            htrans.transform(leaf, out, 1);
            ptrans.transform(out, out, 1);
            rtrans.transform(out, out, 1);
            double nx = t.xcor + (out[0] - leaf[0]);
            double ny = t.ycor + (out[1] - leaf[1]);
            double nz = t.zcor + (out[2] - leaf[2]);

            Set<Turtle3D> snapshot = seenTurtles;
            try {
              if (rigid) {
                snapshot = new HashSet<Turtle3D>(seenTurtles);
              }
              t.xyandzcor(nx, ny, nz);
            } finally {
              if (rigid) {
                seenTurtles = snapshot;
              }
            }

            // if the move fails, heading is not updated.
            // This is on purpose.-- CLB
            Vect[] hvs = new Vect[2];
            if (rigid) {
              hvs = Vect.toVectors(t.heading(), t.pitch(), t.roll());

              hvs[0] = hvs[0].transform(htrans);
              hvs[1] = hvs[1].transform(htrans);
              hvs[0] = hvs[0].transform(ptrans);
              hvs[1] = hvs[1].transform(ptrans);
              hvs[0] = hvs[0].transform(rtrans);
              hvs[1] = hvs[1].transform(rtrans);

              leaf = Vect.toAngles(hvs[0], hvs[1]);
              t.headingPitchAndRoll(leaf[0], leaf[1], leaf[2]);
            }
          } catch (AgentException ex) {
            // We get here if the towards call fails (which
            // shouldn't happen) or xandycor throws an error for
            // topological reasons.  In such cases we want to
            // keep the turtle where it is at and translate
            // all the other tied turtles. -- CLB
            org.nlogo.api.Exceptions.ignore(ex);
          }
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
