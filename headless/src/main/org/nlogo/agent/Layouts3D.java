// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// All methods in this class assume their AgentSet arguments
// have already been checked that they are turtle sets, not
// patch sets. - ST 3/10/06

public final strictfp class Layouts3D {

  // this class is not instantiable
  private Layouts3D() {
    throw new IllegalStateException();
  }

  /// spring 3D

  // THIS CODE IS ALMOST ALL THE SAME AS Layouts.spring2D();
  // GOOD CHANCE THAT ANY EDITS MADE HERE SHOULD BE MADE THERE
  // AS WELL.  ~Forrest (12/5/2006)
  public static void spring3D(AgentSet nodeset, AgentSet linkset,
                              double spr, double len, double rep,
                              org.nlogo.util.MersenneTwisterFast random) {
    World3D world = (World3D) nodeset.world();
    int nodeCount = nodeset.count();
    if (nodeCount == 0) {
      return;
    }
    double[] ax = new double[nodeCount];
    double[] ay = new double[nodeCount];
    double[] az = new double[nodeCount];
    int i = 0;
    HashMap<Turtle3D, Integer> tMap =
        new HashMap<Turtle3D, Integer>();
    int[] degCount = new int[nodeCount];

    Turtle3D[] agt = new Turtle3D[nodeCount];
    for (AgentIterator it = nodeset.shufflerator(random); it.hasNext(); i++) {
      Turtle3D t = (Turtle3D) it.next();
      agt[i] = t;
      tMap.put(t, Integer.valueOf(i));
      ax[i] = 0.0;
      ay[i] = 0.0;
      az[i] = 0.0;
    }

    for (AgentIterator it = linkset.iterator(); it.hasNext(); i++) {
      Link link = (Link) it.next();
      Turtle t1 = link.end1();
      Turtle t2 = link.end2();
      if (tMap.containsKey(t1)) {
        int t1Index = tMap.get(t1).intValue();
        degCount[t1Index]++;
      }
      if (tMap.containsKey(t2)) {
        int t2Index = tMap.get(t2).intValue();
        degCount[t2Index]++;
      }
    }

    for (AgentIterator it = linkset.iterator(); it.hasNext(); i++) {
      Link link = (Link) it.next();
      double dx = 0;
      double dy = 0;
      double dz = 0;
      Turtle3D t1 = (Turtle3D) link.end1();
      Turtle3D t2 = (Turtle3D) link.end2();
      int t1Index = -1;
      int degCount1 = 0;
      if (tMap.containsKey(t1)) {
        t1Index = tMap.get(t1).intValue();
        degCount1 = degCount[t1Index];
      }
      int t2Index = -1;
      int degCount2 = 0;
      if (tMap.containsKey(t2)) {
        t2Index = tMap.get(t2).intValue();
        degCount2 = degCount[t2Index];
      }
      double dist = world.protractor().distance(t1, t2, false);
      // links that are connecting high degree nodes should not
      // be as springy, to help prevent "jittering" behavior
      double div = (degCount1 + degCount2) / 2.0;
      div = StrictMath.max(div, 1.0);

      if (dist == 0) {
        dx += (spr * len) / div; // arbitrary x-dir push-off

      } else {
        double f = spr * (dist - len) / div;
        dx = dx + (f * (t2.xcor() - t1.xcor()) / dist);
        dy = dy + (f * (t2.ycor() - t1.ycor()) / dist);
        dz = dz + (f * (t2.zcor() - t1.zcor()) / dist);
      }

      if (t1Index != -1) {
        ax[t1Index] += dx;
        ay[t1Index] += dy;
        az[t1Index] += dz;
      }

      if (t2Index != -1) {
        ax[t2Index] -= dx;
        ay[t2Index] -= dy;
        az[t2Index] -= dz;
      }
    }

    for (i = 0; i < nodeCount; i++) {
      Turtle3D t1 = agt[i];
      for (int j = i + 1; j < nodeCount; j++) {
        Turtle3D t2 = agt[j];
        double dx = 0.0;
        double dy = 0.0;
        double dz = 0.0;
        double div = (degCount[i] + degCount[j]) / 2.0;
        div = StrictMath.max(div, 1.0);

        if (t2.xcor() == t1.xcor() && t2.ycor() == t1.ycor() && t2.zcor() == t1.zcor()) {
          // push off in random direction
          double ang = 360 * random.nextDouble();
          double zVal = rep * (2 * random.nextDouble() - 1.0);
          double repFlat = StrictMath.sqrt(rep * rep - zVal * zVal);
          dx = -(repFlat * StrictMath.sin(StrictMath.toRadians(ang)));
          dy = -(repFlat * StrictMath.cos(StrictMath.toRadians(ang)));
          dz = -zVal;
        } else {
          double dist = world.protractor().distance(t1, t2, false);
          // repulse according to an inverse square function
          double f = rep / (dist * dist) / div;
          dx = -(f * (t2.xcor() - t1.xcor()) / dist);
          dy = -(f * (t2.ycor() - t1.ycor()) / dist);
          dz = -(f * (t2.zcor() - t1.zcor()) / dist);
        }
        ax[i] += dx;
        ay[i] += dy;
        az[i] += dz;
        ax[j] -= dx;
        ay[j] -= dy;
        az[j] -= dz;
      }
    }

    // we need to bump some node a small amount, in case all nodes
    // are stuck on a single line
    if (nodeCount > 1) {
      double perturbAmt = (world.worldWidth() + world.worldHeight()) / (1.0E10);
      ax[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
      ay[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
      az[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
    }

    // try to choose something that's reasonable perceptually --
    // for temporal aliasing, don't want to jump too far on any given timestep.
    double limit = (world.worldWidth() + world.worldHeight() + world.worldDepth()) / 75.0;

    for (i = 0; i < nodeCount; i++) {
      Turtle3D t = agt[i];
      double fx = ax[i];
      double fy = ay[i];
      double fz = az[i];

      if (fx > limit) {
        fx = limit;
      } else if (fx < -limit) {
        fx = -limit;
      }
      if (fy > limit) {
        fy = limit;
      } else if (fy < -limit) {
        fy = -limit;
      }
      if (fz > limit) {
        fz = limit;
      } else if (fz < -limit) {
        fz = -limit;
      }

      double newx = t.xcor() + fx;
      double newy = t.ycor() + fy;
      double newz = t.zcor() + fz;

      if (newx > world.maxPxcor()) {
        newx = world.maxPxcor();
      } else if (newx < world.minPxcor()) {
        newx = world.minPxcor();
      }

      if (newy > world.maxPycor()) {
        newy = world.maxPycor();
      } else if (newy < world.minPycor()) {
        newy = world.minPycor();
      }
      if (newz > world.maxPzcor()) {
        newz = world.maxPzcor();
      } else if (newz < world.minPzcor()) {
        newz = world.minPzcor();
      }

      t.xyandzcor(newx, newy, newz);
    }
  }

}
