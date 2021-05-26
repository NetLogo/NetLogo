// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.scalatest.FunSuite

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream }

class ClientWorldTests extends FunSuite {

  val BLACK = Double.box(0)
  val WHITE = Double.box(9.9)
  val GRAY = Double.box(5)

  // we pass "false" to the ClientWorld constructor so we don't
  // print out error messages since we are intending to trigger them
  // and it's confusing to see errors when there aren't any.
  val buf = new ClientWorld(20, false)

  test("creation") {
    buf.updatePatch(new PatchData(10, PatchData.COMPLETE, 0, 0, BLACK, "label", WHITE))
    // this one won't exist: we only allow a fixed number of patches.
    buf.updatePatch(new PatchData(30, PatchData.COMPLETE, 0, 0, GRAY, "label", WHITE))
    buf.updateTurtle(new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      WHITE, 90, 1, true,
      "label", BLACK, 0, 0))
    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (0.0, 0.0, default, -1, 90.0, 1.0, true, label, -16777216, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())
    for (i <- 0 until buf.patches.length)
      if (i != 10) {
        assert(buf.patches(i) != null)
        assertResult("Patch 0 0 (-16777216, , -16777216)")(buf.patches(i).stringRep())
      } else {
        assert(buf.patches(i) != null)
        assertResult("Patch 0 0 (-16777216, label, -1)")(buf.patches(i).stringRep())
      }
  }

  test("updatesToExistingObjects") {
    buf.updatePatch(new PatchData(10, PatchData.COMPLETE, 0, 0, BLACK, "label", WHITE))
    buf.updateTurtle(new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      BLACK, 90, 1, true, "label", WHITE,
      0, 0))
    buf.updateTurtle(new TurtleData(10, (TurtleData.XCOR | TurtleData.YCOR).toShort, 1, 2, "default",
      BLACK, 90, 1, true, "label", WHITE,
      0, 0))
    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (1.0, 2.0, default, -16777216, 90.0, 1.0, true, label, -1, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())
    for (i <- 0 until buf.patches.length)
      if (i != 10) {
        assert(buf.patches(i) != null)
        assertResult("Patch 0 0 (-16777216, , -16777216)")(buf.patches(i).stringRep())
      } else {
        assert(buf.patches(i) != null)
        assertResult("Patch 0 0 (-16777216, label, -1)")(buf.patches(i).stringRep())
      }
  }

  test("death message") {
    buf.updateTurtle(new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      WHITE, 90, 1, true,
      "label", BLACK, 0, 0))
    assertResult(1)(buf.sortedTurtles.size)
    buf.updateTurtle(new TurtleData(10))
    assertResult(0)(buf.sortedTurtles.size())
  }

  test("updatesToNonexistentTurtles") {
    buf.updateTurtle(new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      WHITE, 90, 1, true,
      "label", BLACK, 0, 0))
    buf.updateTurtle(new TurtleData(12, (TurtleData.XCOR | TurtleData.YCOR).toShort, 1, 2, "default",
      WHITE, 90, 1, true,
      "label", BLACK, 0, 0))
    buf.updateTurtle(new TurtleData(20))
    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (0.0, 0.0, default, -1, 90.0, 1.0, true, label, -16777216, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())
  }

  test("updatesWithRoundTrips") {
    buf.updatePatch(roundTripP(new PatchData(10, PatchData.COMPLETE, 0, 0, BLACK, "label", WHITE)))
    // no such patch... should be ignored.
    buf.updatePatch(roundTripP(new PatchData(30, PatchData.COMPLETE, 0, 0, GRAY, "label", WHITE)))
    // create a turtle.
    buf.updateTurtle(roundTripT(new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      WHITE, 90, 1, true,
      "label", BLACK, 0, 0)))
    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (0.0, 0.0, default, -1, 90.0, 1.0, true, label, -16777216, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())
    for (i <- 0 until buf.patches.length)
      if (i != 10) {
        assert(buf.patches(i) != null)
        assertResult("Patch 0 0 (-16777216, , -16777216)")(buf.patches(i).stringRep())
      } else {
        assert(buf.patches(i) != null)
        assertResult("Patch 0 0 (-16777216, label, -1)")(buf.patches(i).stringRep())
      }

    buf.updateTurtle(roundTripT(new TurtleData(10, (TurtleData.XCOR | TurtleData.YCOR).toShort, 1, 2, "default",
      WHITE, 0, 0, true, null, BLACK, 0, 0)))
    // no such turtle... should be ignored.
    buf.updateTurtle(roundTripT(new TurtleData(12, (TurtleData.XCOR | TurtleData.YCOR).toShort, 1, 2, "default",
      WHITE, 90, 1, true, null, BLACK, 0, 0)))

    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (1.0, 2.0, default, -1, 90.0, 1.0, true, label, -16777216, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())

    // no such turtle... should be ignored.
    buf.updateTurtle(roundTripT(new TurtleData(20)))
    buf.updateTurtle(roundTripT(new TurtleData(10)))

    assertResult(0)(buf.sortedTurtles.size)
  }

  private def roundTripT(turtle: TurtleData) = {
    val bos = new ByteArrayOutputStream()
    val os = new DataOutputStream(bos)
    turtle.serialize(os)
    new TurtleData(new DataInputStream(
      new ByteArrayInputStream(bos.toByteArray())))
  }

  private def roundTripP(patch: PatchData) = {
    val bos = new ByteArrayOutputStream()
    val os = new DataOutputStream(bos)
    patch.serialize(os)
    new PatchData(new DataInputStream(
      new ByteArrayInputStream(bos.toByteArray())))
  }

  test("updateWorldBuf") {
    val bos = new ByteArrayOutputStream()
    val os = new DataOutputStream(bos)
    // initialize stream...
    os.writeShort(DiffBuffer.PATCHES | DiffBuffer.TURTLES)
    // two patches to stream...
    os.writeInt(2)
    new PatchData(10, PatchData.COMPLETE, 0, 0, BLACK, "label", WHITE).serialize(os)
    // non-existent... will be ignored.
    new PatchData(30, PatchData.COMPLETE, 0, 0, GRAY, "label", WHITE).serialize(os)
    // one turtle to stream...
    os.writeInt(1)
    new TurtleData(10, TurtleData.COMPLETE, 0, 0, "default",
      WHITE, 90, 1, true,
      "label", BLACK, 0, 0).serialize(os)
    buf.updateFrom(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())))
    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (0.0, 0.0, default, -1, 90.0, 1.0, true, label, -16777216, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())
    for (i <- 0 until buf.patches.length) {
      assert(buf.patches(i) != null)
      if (i != 10)
        assertResult("Patch 0 0 (-16777216, , -16777216)")(buf.patches(i).stringRep())
      else
        assertResult("Patch 0 0 (-16777216, label, -1)")(buf.patches(i).stringRep())
    }

    bos.reset()
    // initialize stream...
    os.writeShort(DiffBuffer.PATCHES | DiffBuffer.TURTLES)
    // no patches...
    os.writeInt(0)
    // two turtles...
    os.writeInt(2)
    new TurtleData(10, (TurtleData.XCOR | TurtleData.YCOR).toShort, 1, 2, "default",
      WHITE, 0, 0, true,
      null, BLACK, 0, 0).serialize(os)
    // non-existent turtle... will be ignored.
    new TurtleData(12, (TurtleData.XCOR | TurtleData.YCOR).toShort, 1, 2, "default",
      WHITE, 0, 0, true,
      null, BLACK, 0, 0).serialize(os)

    buf.updateFrom(
      new DataInputStream(new ByteArrayInputStream(bos.toByteArray())))

    assertResult(1)(buf.sortedTurtles.size)
    assertResult("Turtle 10 (1.0, 2.0, default, -1, 90.0, 1.0, true, label, -16777216, 0, 0.0)")(
      buf.getTurtleDataByWho(10).stringRep())

    bos.reset()
    // initialize stream...
    os.writeShort(DiffBuffer.PATCHES | DiffBuffer.TURTLES)
    // no patches...
    os.writeInt(0)
    /// two turtles...
    os.writeInt(2)
    new TurtleData(10).serialize(os)
    // non-existent turtle... will be ignored.
    new TurtleData(20).serialize(os)

    buf.updateFrom(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())))

    assertResult(0)(buf.sortedTurtles.size())
    for (i <- 0 until buf.patches.length) {
      assert(buf.patches(i) != null)
      if (i != 10)
        assertResult("Patch 0 0 (-16777216, , -16777216)")(buf.patches(i).stringRep())
      else
        assertResult("Patch 0 0 (-16777216, label, -1)")(buf.patches(i).stringRep())
    }
  }
}
