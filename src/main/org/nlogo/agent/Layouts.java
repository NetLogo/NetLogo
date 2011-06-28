package org.nlogo.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nlogo.api.AgentException;

import org.nlogo.api.LogoList;

// All methods in this class assume their AgentSet arguments
// have already been checked that they are turtle sets, not
// patch sets. - ST 3/10/06

public final strictfp class Layouts {

  // this class is not instantiable
  private Layouts() {
    throw new IllegalStateException();
  }

  /// circle

  public static void circle(World world, LogoList nodes, double radius)
      throws AgentException {
    int i = 0;
    int n = nodes.size();
    int midx = world.minPxcor() + (int) StrictMath.floor(world.worldWidth() / 2);
    int midy = world.minPycor() + (int) StrictMath.floor(world.worldHeight() / 2);
    for (Iterator<Object> it = nodes.iterator(); it.hasNext(); i++) {
      Object obj = it.next();
      if (obj instanceof Turtle) {
        Turtle t = (Turtle) obj;
        double heading = (i * 360) / n;
        // precheck so turtles don't end up in weird places.
        world.protractor().getPatchAtHeadingAndDistance(midx, midy, heading, radius);
        t.xandycor(midx, midy);
        t.heading(heading);
        t.jump(radius);
      }
    }
  }

  public static void circle(AgentSet nodes, double radius,
                            org.nlogo.util.MersenneTwisterFast random)
      throws AgentException {
    int i = 0;
    int n = nodes.count();
    World world = nodes.world();
    int midx = world.minPxcor() + (int) StrictMath.floor(world.worldWidth() / 2);
    int midy = world.minPycor() + (int) StrictMath.floor(world.worldHeight() / 2);
    for (AgentSet.Iterator it = nodes.shufflerator(random); it.hasNext(); i++) {
      Turtle t = (Turtle) it.next();
      double heading = (i * 360) / n;
      // precheck so turtles don't end up in weird places.
      world.protractor().getPatchAtHeadingAndDistance(midx, midy, heading, radius);
      t.xandycor(midx, midy);
      t.heading(heading);
      t.jump(radius);
    }
  }

  /// spring
  public static void spring(AgentSet nodeset, AgentSet linkset,
                            double spr, double len, double rep,
                            org.nlogo.util.MersenneTwisterFast random) {
    World world = nodeset.world();
    if (world.program().is3D()) {
      spring3D(nodeset, linkset, spr, len, rep, random);
    } else {
      spring2D(nodeset, linkset, spr, len, rep, random);
    }
  }

  // THIS CODE IS ALMOST ALL THE SAME AS "spring3D()" below
  // GOOD CHANCE THAT ANY EDITS MADE HERE SHOULD BE MADE THERE
  // AS WELL.  ~Forrest (12/5/2006)
  public static void spring2D(AgentSet nodeset, AgentSet linkset,
                              double spr, double len, double rep,
                              org.nlogo.util.MersenneTwisterFast random) {
    World world = nodeset.world();
    int nodeCount = nodeset.count();
    if (nodeCount == 0) {
      return;
    }
    double[] ax = new double[nodeCount];
    double[] ay = new double[nodeCount];
    int i = 0;
    HashMap<Turtle, Integer> tMap =
        new HashMap<Turtle, Integer>();
    int[] degCount = new int[nodeCount];

    Turtle[] agt = new Turtle[nodeCount];
    for (AgentSet.Iterator it = nodeset.shufflerator(random); it.hasNext(); i++) {
      Turtle t = (Turtle) it.next();
      agt[i] = t;
      tMap.put(t, Integer.valueOf(i));
      ax[i] = 0.0;
      ay[i] = 0.0;
    }

    for (AgentSet.Iterator it = linkset.iterator(); it.hasNext(); i++) {
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

    for (AgentSet.Iterator it = linkset.iterator(); it.hasNext(); i++) {
      Link link = (Link) it.next();
      double dx = 0;
      double dy = 0;
      Turtle t1 = link.end1();
      Turtle t2 = link.end2();
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
      }
      if (t1Index != -1) {
        ax[t1Index] += dx;
        ay[t1Index] += dy;
      }
      if (t2Index != -1) {
        ax[t2Index] -= dx;
        ay[t2Index] -= dy;
      }
    }

    for (i = 0; i < nodeCount; i++) {
      Turtle t1 = agt[i];
      for (int j = i + 1; j < nodeCount; j++) {
        Turtle t2 = agt[j];
        double dx = 0.0;
        double dy = 0.0;
        double div = (degCount[i] + degCount[j]) / 2.0;
        div = StrictMath.max(div, 1.0);

        if (t2.xcor() == t1.xcor() && t2.ycor() == t1.ycor()) {
          double ang = 360 * random.nextDouble();
          dx = -(rep / div * StrictMath.sin(StrictMath.toRadians(ang)));
          dy = -(rep / div * StrictMath.cos(StrictMath.toRadians(ang)));
        } else {
          double dist = world.protractor().distance(t1, t2, false);
          double f = rep / (dist * dist) / div;
          dx = -(f * (t2.xcor() - t1.xcor()) / dist);
          dy = -(f * (t2.ycor() - t1.ycor()) / dist);
        }
        ax[i] += dx;
        ay[i] += dy;
        ax[j] -= dx;
        ay[j] -= dy;
      }
    }

    // we need to bump some node a small amount, in case all nodes
    // are stuck on a single line
    if (nodeCount > 1) {
      double perturbAmt = (world.worldWidth() + world.worldHeight()) / (1.0E10);
      ax[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
      ay[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
    }

    // try to choose something that's reasonable perceptually --
    // for temporal aliasing, don't want to jump too far on any given timestep.
    double limit = (world.worldWidth() + world.worldHeight()) / 50.0;

    for (i = 0; i < nodeCount; i++) {
      Turtle t = agt[i];
      double fx = ax[i];
      double fy = ay[i];

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

      double newx = t.xcor() + fx;
      double newy = t.ycor() + fy;

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
      try {
        t.xandycor(newx, newy);
      } catch (AgentException ex) {
        // should never happen, because we already checked bounds
        throw new IllegalStateException(ex);
      }
    }
  }

  /// spring 3D

  // THIS CODE IS ALMOST ALL THE SAME AS "spring2D()" above
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
    for (AgentSet.Iterator it = nodeset.shufflerator(random); it.hasNext(); i++) {
      Turtle3D t = (Turtle3D) it.next();
      agt[i] = t;
      tMap.put(t, Integer.valueOf(i));
      ax[i] = 0.0;
      ay[i] = 0.0;
      az[i] = 0.0;
    }

    for (AgentSet.Iterator it = linkset.iterator(); it.hasNext(); i++) {
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

    for (AgentSet.Iterator it = linkset.iterator(); it.hasNext(); i++) {
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

  // layout-sphere
  // experimental

  // An experimental "__layout-sphere" for 3D.  Not polished yet.
  // Also not sure if it's a worthwhile primitive or not,
  // especially given how slow it runs for large numbers of turtles.
  //  ~Forrest (12/5/2006)
  public static void sphere(AgentSet nodeset, double radius, double initialTemp,
                            org.nlogo.util.MersenneTwisterFast random) {
    World3D world = (World3D) nodeset.world();
    int nodeCount = nodeset.count();
    if (nodeCount == 0) {
      return;
    }
    double[] nx = new double[nodeCount];
    double[] ny = new double[nodeCount];
    double[] nz = new double[nodeCount];

    Turtle3D[] agt = new Turtle3D[nodeCount];
    int i = 0;
    for (AgentSet.Iterator it = nodeset.iterator(); it.hasNext(); i++) {
      Turtle3D t = (Turtle3D) it.next();
      agt[i] = t;
      nx[i] = t.xcor();
      ny[i] = t.ycor();
      nz[i] = t.zcor();
      if (nx[i] == 0.0 && ny[i] == 0.0 && nz[i] == 0.0) {
        nz[i] = random.nextDouble() * 2.0 - 1.0;
        double remainder = StrictMath.sqrt(1.0 * 1.0 - nz[i] * nz[i]);
        double angle = random.nextDouble() * StrictMath.PI * 2;
        nx[i] = remainder * StrictMath.sin(angle);
        ny[i] = remainder * StrictMath.cos(angle);
      }
    }

    double temperature = initialTemp / nodeCount;
    for (int k = 0; k < 30; k++) {
      for (i = 0; i < nodeCount; i++) {
        for (int j = i + 1; j < nodeCount; j++) {
          double dx = nx[j] - nx[i];
          double dy = ny[j] - ny[i];
          double dz = nz[j] - nz[i];

          double distSq = dx * dx + dy * dy + dz * dz;
          if (distSq < 1.0E-20) {
            dx = temperature * (random.nextDouble() - 0.5);
            dy = temperature * (random.nextDouble() - 0.5);
            dz = temperature * (random.nextDouble() - 0.5);
          } else {
            // repulse according to an inverse cubic function
            double f = temperature / (distSq * distSq);
            dx = -(f * dx);
            dy = -(f * dy);
            dz = -(f * dz);
          }

          nx[i] += dx;
          ny[i] += dy;
          nz[i] += dz;
          double magnitude = StrictMath.sqrt(nx[i] * nx[i] + ny[i] * ny[i] + nz[i] * nz[i]);
          nx[i] = nx[i] / magnitude;
          ny[i] = ny[i] / magnitude;
          nz[i] = nz[i] / magnitude;

          nx[j] -= dx;
          ny[j] -= dy;
          nz[j] -= dz;
          magnitude = StrictMath.sqrt(nx[j] * nx[j] + ny[j] * ny[j] + nz[j] * nz[j]);
          nx[j] = nx[j] / magnitude;
          ny[j] = ny[j] / magnitude;
          nz[j] = nz[j] / magnitude;
        }
      }
      temperature *= 0.75;
    }

    for (i = 0; i < nodeCount; i++) {
      double newx = nx[i] * radius;
      double newy = ny[i] * radius;
      double newz = nz[i] * radius;

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

      agt[i].xyandzcor(newx, newy, newz);
    }
  }


  /// magspring

  private static final double MAGSPRING_SMALL_THRESHOLD = 0.0000001;

  private static final int FIELD_NONE = 0;
  private static final int FIELD_NORTH = 1;
  private static final int FIELD_NORTHEAST = 2;
  private static final int FIELD_EAST = 3;
  private static final int FIELD_SOUTHEAST = 4;
  private static final int FIELD_SOUTH = 5;
  private static final int FIELD_SOUTHWEST = 6;
  private static final int FIELD_WEST = 7;
  private static final int FIELD_NORTHWEST = 8;
  private static final int FIELD_POLAR = 9;
  private static final int FIELD_CONCENTRIC = 10;

  public static void magspring(AgentSet nodeset, AgentSet linkset,
                               double spr, double len, double rep,
                               double magStr, int fieldType,
                               boolean bidirectional,
                               org.nlogo.util.MersenneTwisterFast random) {
    World world = nodeset.world();

    int nodeCount = nodeset.count();

    Turtle[] agt = new Turtle[nodeCount];
    double[] ax = new double[nodeCount];
    double[] ay = new double[nodeCount];
    int ctr = 0;

    for (AgentSet.Iterator iter = nodeset.shufflerator(random); iter.hasNext(); ctr++) {
      agt[ctr] = (Turtle) iter.next();
    }

    for (int i = 0; i < nodeCount; i++) {
      Turtle t = agt[i];

      double fx = 0, fy = 0;
      for (AgentSet.Iterator it = world.links().shufflerator(random); it.hasNext();) {
        Link link = (Link) it.next();
        if ((link.end1() == t || link.end2() == t) &&
            (linkset.contains(link))) {
          Turtle other = link.end1();
          if (t == link.end1()) {
            other = link.end2();
          }
          double dist = world.protractor().distance(t, other, false);
          double dx = other.xcor() - t.xcor();
          double dy = other.ycor() - t.ycor();
          if (StrictMath.abs(dist) < MAGSPRING_SMALL_THRESHOLD) {
            if (t == link.end1()) {
              fx += (spr * len);
            } else {
              fx -= (spr * len);
            }
          } else {
            // calculate attractive force
            double f = spr * (dist - len);
            fx = fx + (f * dx / dist);
            fy = fy + (f * dy / dist);

            // calculate magnetic force
            java.awt.geom.Point2D.Double mf = magForce(t.xcor(), t.ycor(), fieldType);

            // we want to know the angle between the link and the magnetic field
            // calculate dotProduct first
            double dot = mf.x * dx + mf.y * dy;
            // then calculate the cosine of
            double cosAngle = StrictMath.abs(dot) / dist;

            // keep track of whether the dotProduct was negative
            // so we can push or pull, accordingly
            double negFlag = (dot < 0) ? 1 : -1;

            if (!bidirectional) {
              negFlag = 1;
            }

            // cosAngle can be > 1 because of weird float rounding.
            // if cosAngle >= 1, then angle = 0, and no magnetism occurs.
            if (cosAngle < 1) {
              double angle = StrictMath.abs(StrictMath.acos(cosAngle));
              // 1.5 is an arbitrary choice, chosen because it seemed to work well
              // some other power > 1 could be used as well.
              fx = fx + negFlag * magStr * mf.x * StrictMath.pow(angle, 1.5);
              fy = fy + negFlag * magStr * mf.y * StrictMath.pow(angle, 1.5);
            }
          }
        }
      }
      for (AgentSet.Iterator it = nodeset.shufflerator(random); it.hasNext();) {
        Turtle other = (Turtle) it.next();
        if (other != t) {
          double dx = other.xcor() - t.xcor();
          double dy = other.ycor() - t.ycor();

          if (dx == 0 && dy == 0) {
            double ang = 360 * random.nextDouble();
            fx = fx - (rep * StrictMath.sin(StrictMath.toRadians(ang)));
            fy = fy - (rep * StrictMath.cos(StrictMath.toRadians(ang)));
          } else {
            double dist = StrictMath.sqrt((dx * dx) + (dy * dy));
            //if ( dist <= 2 * len )
            //{
            double f = rep / (dist * dist);
            fx = fx - (f * dx / dist);
            fy = fy - (f * dy / dist);
            //}

          }
        }
      }
      double limit = 1;
      if (fx > limit) {
        fx = limit;
      } else {
        if (fx < -limit) {
          fx = -limit;
        }
      }
      if (fy > limit) {
        fy = limit;
      } else {
        if (fy < -limit) {
          fy = -limit;
        }
      }
      fx += t.xcor();
      fy += t.ycor();
      if (fx > world.maxPxcor()) {
        fx = world.maxPxcor();
      } else {
        if (fx < world.minPxcor()) {
          fx = world.minPxcor();
        }
      }
      if (fy > world.maxPycor()) {
        fy = world.maxPycor();
      } else {
        if (fy < world.minPycor()) {
          fy = world.minPycor();
        }
      }
      ax[i] = fx;
      ay[i] = fy;
    }

    // we need to bump some node a small amount, in case all nodes
    // are stuck on a single line
    if (nodeCount > 1) {
      double perturbAmt = (world.worldWidth() + world.worldHeight()) / (1.0E10);
      ax[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
      ay[0] += random.nextDouble() * perturbAmt - perturbAmt / 2.0;
    }

    reposition(agt, ax, ay);
  }

  private static final double COS45 = StrictMath.sqrt(2.0) / 2.0;

  private static java.awt.geom.Point2D.Double magForce(double x, double y, int fieldType) {
    double dist;
    switch (fieldType) {
      case FIELD_NORTH:
        return new java.awt.geom.Point2D.Double(0, 1);
      case FIELD_NORTHEAST:
        return new java.awt.geom.Point2D.Double(COS45, COS45);
      case FIELD_EAST:
        return new java.awt.geom.Point2D.Double(1, 0);
      case FIELD_SOUTHEAST:
        return new java.awt.geom.Point2D.Double(COS45, -COS45);
      case FIELD_SOUTH:
        return new java.awt.geom.Point2D.Double(0, -1);
      case FIELD_SOUTHWEST:
        return new java.awt.geom.Point2D.Double(-COS45, -COS45);
      case FIELD_WEST:
        return new java.awt.geom.Point2D.Double(-1, 0);
      case FIELD_NORTHWEST:
        return new java.awt.geom.Point2D.Double(-COS45, COS45);
      case FIELD_POLAR:
        dist = StrictMath.sqrt((x * x) + (y * y));
        if (StrictMath.abs(dist) < MAGSPRING_SMALL_THRESHOLD) {
          return new java.awt.geom.Point2D.Double(0, 0);
        }
        return new java.awt.geom.Point2D.Double(x / dist, y / dist);
      case FIELD_CONCENTRIC:
        dist = StrictMath.sqrt((x * x) + (y * y));
        if (StrictMath.abs(dist) < MAGSPRING_SMALL_THRESHOLD) {
          return new java.awt.geom.Point2D.Double(0, 0);
        }
        return new java.awt.geom.Point2D.Double(y / dist, -x / dist);
      case FIELD_NONE:
        return new java.awt.geom.Point2D.Double(0, 0);
      default:
        throw new IllegalStateException();
    }
  }

  /// Tutte

  public static void tutte(AgentSet nodeset, AgentSet linkset,
                           double radius, org.nlogo.util.MersenneTwisterFast random)
      throws AgentException {
    World world = nodeset.world();
    java.util.ArrayList<Turtle> anchors = new java.util.ArrayList<Turtle>();
    for (AgentSet.Iterator iter = linkset.iterator();
         iter.hasNext();) {
      Link link = (Link) iter.next();
      if (!nodeset.contains(link.end1()) && !anchors.contains(link.end1())) {
        anchors.add(link.end1());
      }
      if (!nodeset.contains(link.end2()) && !anchors.contains(link.end2())) {
        anchors.add(link.end2());
      }
    }
    circle(world, LogoList.fromJava(anchors), radius);
    int n = nodeset.count();

    Turtle[] agt = new Turtle[n];
    double[] ax = new double[n];
    double[] ay = new double[n];
    int ctr2 = 0;

    for (AgentSet.Iterator iter = nodeset.shufflerator(random); iter.hasNext(); ctr2++) {
      agt[ctr2] = (Turtle) iter.next();
    }

    for (int i = 0; i < n; i++) {
      Turtle t = agt[i];
      double fx = 0, fy = 0;
      int degree = 0;
      for (AgentSet.Iterator it = world.links().shufflerator(random); it.hasNext();) {
        Link link = (Link) it.next();
        if ((link.end1() == t || link.end2() == t)
            && linkset.contains(link)) {
          Turtle other = link.end1();
          if (t == link.end1()) {
            other = link.end2();
          }
          fx = fx + other.xcor();
          fy = fy + other.ycor();
          degree++;
        }
      }
      fx = fx / degree;
      fy = fy / degree;
      fx = fx - t.xcor();
      fy = fy - t.ycor();

      double limit = 100;
      if (fx > limit) {
        fx = limit;
      } else {
        if (fx < -limit) {
          fx = -limit;
        }
      }
      if (fy > limit) {
        fy = limit;
      } else {
        if (fy < -limit) {
          fy = -limit;
        }
      }
      fx += t.xcor();
      fy += t.ycor();
      if (fx > world.maxPxcor()) {
        fx = world.maxPxcor();
      } else {
        if (fx < world.minPxcor()) {
          fx = world.minPxcor();
        }
      }
      if (fy > world.maxPycor()) {
        fy = world.maxPycor();
      } else {
        if (fy < world.minPycor()) {
          fy = world.minPycor();
        }
      }
      ax[i] = fx;
      ay[i] = fy;
    }
    reposition(agt, ax, ay);
  }

  /// helpers

  private static void reposition(Turtle[] agents, double[] x, double[] y) {
    try {
      for (int i = 0; i < agents.length; i++) {
        agents[i].xandycor(x[i], y[i]);
      }
    } catch (AgentException ex) {
      // should not be possible! the methods that call us are supposed
      // to have done their own bounds checking already
      throw new IllegalStateException(ex);
    }
  }

  public static void radial(World world, AgentSet nodeset, AgentSet linkset, Turtle root)
      throws AgentException {
    double rootX = (world.minPxcor() + world.maxPxcor()) / 2.0;
    double rootY = (world.minPycor() + world.maxPycor()) / 2.0;

    org.nlogo.agent.LinkManager linkManager = world.linkManager;

    Map<Turtle, TreeNode> nodeTable =
        new HashMap<Turtle, TreeNode>(nodeset.count());
    ArrayList<TreeNode> queue =
        new ArrayList<TreeNode>(nodeset.count());
    TreeNode rootNode = new TreeNode(root, null);
    queue.add(rootNode);
    nodeTable.put(rootNode.val, rootNode);

    // used to find the maximum depth
    TreeNode lastNode = rootNode;

    while (!queue.isEmpty()) {
      TreeNode node = queue.remove(0);
      lastNode = node;
      AgentSet neighbors = linkManager.findLinkedWith(node.val, linkset);

      for (AgentSet.Iterator iter = neighbors.iterator(); iter.hasNext();) {
        Turtle t = (Turtle) iter.next();

        if (nodeset.contains(t) && !nodeTable.containsKey(t)) {
          TreeNode child = new TreeNode(t, node);
          node.children.add(child);
          nodeTable.put(t, child);
          queue.add(child);
        }
      }
    }

    rootNode.layoutRadial(0.0, 360.0);

    double maxDepth = lastNode.getDepth() + 0.2;
    if (maxDepth < 1.0) {
      maxDepth = 1.0;
    }
    double xDistToEdge = StrictMath.min(world.maxPxcor() - rootX, rootX - world.minPxcor());
    double yDistToEdge = StrictMath.min(world.maxPycor() - rootY, rootY - world.minPycor());
    double distToEdge = StrictMath.min(xDistToEdge, yDistToEdge);
    double layerGap = distToEdge / maxDepth;

    Iterator<TreeNode> it = nodeTable.values().iterator();
    while (it.hasNext()) {
      TreeNode node = it.next();
      Turtle t = node.val;
      t.heading(node.angle);
      t.xandycor(rootX, rootY);
      t.jump(node.getDepth() * layerGap);
    }
  }

  private static strictfp class TreeNode {
    public Turtle val;
    public TreeNode parent;
    public List<TreeNode> children = new ArrayList<TreeNode>(10);
    public double angle = 0.0;

    public TreeNode(Turtle val, TreeNode parent) {
      this.val = val;
      this.parent = parent;
    }

    public int getDepth() {
      int i = 0;
      TreeNode myParent = parent;
      while (myParent != null) {
        myParent = myParent.parent;
        i++;
      }
      return i;
    }

    // A heuristic used to decide how much space each
    // node should be alotted, based on its children.
    public double getWeight() {
      double myWeight = children.size() + 1;
      double maxChildWeight = 0.0;
      for (TreeNode child : children) {
        double cweight = child.getWeight();
        if (cweight > maxChildWeight) {
          maxChildWeight = cweight;
        }
      }
      maxChildWeight = maxChildWeight * 0.8;

      return StrictMath.max(myWeight, maxChildWeight);
    }

    public void layoutRadial(double arcStart, double arcEnd) {
      angle = (arcStart + arcEnd) / 2.0;

      double weightSum = 0.0;

      for (TreeNode child : children) {
        weightSum += child.getWeight();
      }

      double childStart = arcStart;
      for (TreeNode child : children) {
        double childEnd = childStart + (arcEnd - arcStart) * child.getWeight() / weightSum;
        child.layoutRadial(childStart, childEnd);
        childStart = childEnd;
      }
    }
  }
}
