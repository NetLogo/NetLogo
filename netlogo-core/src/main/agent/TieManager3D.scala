// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, Matrix3D, Vect }

import java.util.{ Arrays, HashSet, Iterator => JIterator, List => JList, Set => JSet }

import scala.collection.JavaConverters._

class TieManager3D(links: TreeAgentSet, linkManager: LinkManager, protractor: Protractor3D)
  extends TieManager(links, linkManager, protractor) {

  // the set of turtles we are updating
  private var seenTurtles: Set[Turtle3D] = Set.empty[Turtle3D]

  def turtleMoved(root: Turtle3D,
    newX: Double, newY: Double, newZ: Double,
    originalXcor: Double, originalYcor: Double, originalZcor: Double): Unit = {
    var weroot = false

    try {
      if (seenTurtles.isEmpty) {
        // we need to add ourselves to that we are not updated in
        // this run
        seenTurtles += root
        weroot = true
      }

      // update my leaf positions and tell
      val myTies =
        tiedTurtles3d(root, seenTurtles)

      if (!myTies.isEmpty) {
        seenTurtles ++= myTies.toSet
        val changex: Double = newX - originalXcor
        val changey: Double = newY - originalYcor
        val changez: Double = newZ - originalZcor
        myTies.foreach { (t: Turtle3D) =>
          // In order to get wrapping and line drawing to work properly
          // we have to compute our transform in coordinates relative to the
          // root turtle -- CLB 05/11/06
          t.xyandzcor(t.xcor() + changex, t.ycor() + changey, t.zcor() + changez)
        }
      }
    } finally {
      // if we were the root turtle, we clear the seenTurtles list
      if (weroot) {
        seenTurtles = Set.empty[Turtle3D]
      }
    }
  }

  protected def tiedTurtles3d(root: Turtle, seenTurtles: Set[Turtle3D]): Seq[Turtle3D] = {
    linkManager.outLinks(root, links).collect {
      case link if link.isTied => link.otherEnd(root)
    }.collect {
      case t: Turtle3D if t.id != -1 && ! seenTurtles.contains(t) => t
    }
  }

  override def turtleTurned(root: Turtle, newHeading: Double, originalHeading: Double): Unit = {
    val t: Turtle3D = root.asInstanceOf[Turtle3D]
    turtleOrientationChanged(t, newHeading, t.pitch, t.roll, originalHeading, t.pitch, t.roll)
  }

  protected def turtleOrientationChanged(root: Turtle3D,
       newHeading: Double, newPitch: Double, newRoll: Double,
       oldHeading: Double, oldPitch: Double, oldRoll: Double) {
    var weroot = false
    try {
      if (seenTurtles.isEmpty) {
        // we need to add ourselves to that we are not updated in
        // this run
        seenTurtles += root
        weroot = true
      }

      val myTies = tiedTurtles3d(root, seenTurtles)

      if (!myTies.isEmpty) {
        seenTurtles ++= myTies.toSet
        // create a matrix transform for translating the location
        // of leaf turtles
        val ptrans: Matrix3D = new Matrix3D();
        val rtrans: Matrix3D = new Matrix3D();
        val dh: Double = Turtle.subtractHeadings(newHeading, oldHeading);
        val dp: Double = Turtle.subtractHeadings(newPitch, oldPitch);
        val dr: Double = Turtle.subtractHeadings(newRoll, oldRoll);

        val htrans: Matrix3D = Rotations3D.zrot(-dh); // this transform method takes degrees, not radians
        var vects: Array[Vect] = Vect.toVectors(newHeading, oldPitch, 0)
        ptrans.vrot(0, 0, 0,
            vects(1).x, vects(1).y, vects(1).z, StrictMath.toRadians(dp));
        vects = Vect.toVectors(newHeading, newPitch, oldRoll);
        rtrans.vrot(0, 0, 0,
            vects(0).x, vects(0).y, vects(0).z, StrictMath.toRadians(dr));


        val out = new Array[Double](3)
        myTies.foreach { (t: Turtle3D) =>
          try {
            val rigid =
              Arrays.stream(linkManager.linksWith(root, t, links))
                .anyMatch(l => l.mode == Link.MODE_FIXED)

            // In order to get wrapping and line drawing to work properly
            // we have to compute our transform in coordinates relative to the
            // root turtle -- CLB 05/11/06
            val leaf = protractor.towardsVector(
              root.xcor(), root.ycor(), root.zcor(),
              t.xcor(),    t.ycor(),    t.zcor(), true)
            htrans.transform(leaf, out, 1)
            ptrans.transform(out, out, 1)
            rtrans.transform(out, out, 1)
            val nx: Double = t.xcor() + (out(0) - leaf(0))
            val ny: Double = t.ycor() + (out(1) - leaf(1))
            val nz: Double = t.zcor() + (out(2) - leaf(2))

            var snapshot = seenTurtles
            try {
              if (rigid) {
                snapshot = seenTurtles
              }
              t.xyandzcor(nx, ny, nz)
            } finally {
              if (rigid) {
                seenTurtles = snapshot
              }
            }

            // if the move fails, heading is not updated.
            // This is on purpose.-- CLB
            if (rigid) {
              val hvs = Vect.toVectors(t.heading, t.pitch, t.roll)

              hvs(0) = hvs(0).transform(htrans)
              hvs(1) = hvs(1).transform(htrans)
              hvs(0) = hvs(0).transform(ptrans)
              hvs(1) = hvs(1).transform(ptrans)
              hvs(0) = hvs(0).transform(rtrans)
              hvs(1) = hvs(1).transform(rtrans)

              val leaf2 = Vect.toAngles(hvs(0), hvs(1))
              t.headingPitchAndRoll(leaf2(0), leaf2(1), leaf2(2))
            }
          } catch {
            case ex: AgentException =>
              // We get here if the towards call fails (which
              // shouldn't happen) or xandycor() throws an error for
              // topological reasons.  In such cases we want to
              // keep the turtle where it is at and translate
              // all the other tied turtles. -- CLB
              org.nlogo.api.Exceptions.ignore(ex)
          }
        }
      }
    } finally {
      // if we were the root turtle, we clear the seenTurtles list
      if (weroot) {
        seenTurtles = Set.empty[Turtle3D]
      }
    }
  }
}
