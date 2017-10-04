// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.UpdateMode
import org.nlogo.agent.TickCounter
import System.nanoTime
import StrictMath.pow

// This class determines how the speed slider behaves, that is, how often the display gets updated.
// To be more precise: code elsewhere determines when *opportunities* to update arise; the logic in
// this class determines which of those opportunities actually get taken.  Note the private fields
// below; this class basically exists in order to encapsulate those fields. - ST 2/19/07, 3/16/08

// The difference between tick-based and continuous updates are abstracted out using the
// UpdatePolicy trait, which has two implementations.

// Reminder:
//   1 second (s) =
//   1,000 milliseconds (ms) =
//   1,000,000 microseconds (us) =
//   1,000,000,000 nanoseconds (ns)

// On Mac OS X, nanoTime goes up in increments of 1,000 nanoseconds, that is, increments of
// 1 microsecond.  We do all our calculations in nanoseconds, but since we can't actually get useful
// timing info at the nanosecond level, the smallest unit of time that we actually target is the
// microsecond. - ST 3/16/08

// A strange word appearing below is "pseudo-tick".  Sorry, I can't think of a better name for this.
// It basically means something happening that isn't actually a tick, but that we treat like one for
// the purpose of scheduling updates.
//
// The DISPLAY command always causes a pseudo-tick, regardless of update mode.
//
// With continuous updates only, GUIWorkspace generates a pseudo-tick every time the engine comes up
// for air, which basically means every time one agent runs one command.

class UpdateManager(tickCounter: TickCounter) extends UpdateManagerInterface {

  // abstract methods
  def ticks: Double = tickCounter.ticks
  private var frameRateSetPoint: Double = 30.0
  private var _updateMode: UpdateMode = UpdateMode.Continuous

  def frameRate: Double = frameRateSetPoint
  def frameRate(f: Double) = {
    frameRateSetPoint = f
    recompute()
  }

  def updateMode: UpdateMode = _updateMode
  def updateMode(u: UpdateMode) = {
    _updateMode = u
    recompute()
  }

  // ticks returns -1 if the tick counter is clear, so for the logic below to work, we need to use
  // an even more negative value.  (yeah, kludgy.) - ST 4/28/10
  private val ForeverAgo = -2.0

  private var lastUpdateNanos = 0L
  private var lastUpdateTicks = ForeverAgo
  private var pseudoTicks = 0

  // these are values we calculate and store when the speed slider moves
  private var nanoGap = 0L  // nanoseconds
  private var tickGap = 1.0
  private var frameRateGap = 0L // nanoseconds

  def reset() {
    lastUpdateTicks = ForeverAgo
    pseudoTicks = 0
    lastUpdateNanos = 0
  }

  def pseudoTick() { pseudoTicks += 1 }

  def shouldUpdateNow = updatePolicy.shouldUpdateNow(nanoTime)

  def shouldComeUpForAirAgain = updatePolicy.shouldComeUpForAirAgain

  def beginPainting() {
    if(updatePolicy.frameDoneWhenPaintingBegins)
      frameDone()
  }

  def donePainting() {
    if(!updatePolicy.frameDoneWhenPaintingBegins)
      frameDone()
  }

  private var timeSmoothingWillBeDone = 0L

  def isDoneSmoothing() = {
    val now = nanoTime
    if(now >= timeSmoothingWillBeDone) {
      lastUpdateNanos = now
      true
    }
    else synchronized {
      // we're on the event thread, so don't sleep for very long or we'll turn the UI to molasses.
      // return false; the caller should process UI events and then call us again - ST 9/21/11
      try sleep(10000000L min (timeSmoothingWillBeDone - now))
      catch { case ex: InterruptedException =>
        // if we were interrupted, it's because the model is being halted, so we need to make sure
        // we re-interrupt so the halt will happen - ST 8/16/07
        Thread.currentThread.interrupt()
      }
      false
    }
  }

  private def frameDone() {
    val now = nanoTime
    timeSmoothingWillBeDone = now + updatePolicy.smoothingPause(now)
    lastUpdateNanos = nanoTime
    lastUpdateTicks = ticks
    pseudoTicks = 0
  }

  // ranges from -50 to 50
  private var _speed = 0.0
  def speed = _speed
  def speed_=(speed: Double) {
    _speed = speed
    recompute()
    // println(debugInfo)
  }

  def recompute() {
    nanoGap = updatePolicy.nanoGap
    tickGap = updatePolicy.tickGap
    frameRateGap = updatePolicy.frameRateGap
  }

  // Suppose we're pausing after a view update, then suddenly the user moves the speed slider to a
  // faster setting.  The pause should end early.  That's the purpose of the following code.
  // These are the only two methods that use the UpdateManager object as a lock. - ST 8/16/07
  def nudgeSleeper() { synchronized { notifyAll() } }
  private def sleep(nanos: Long) {
    synchronized {
      try wait(nanos / 1000000, (nanos % 1000000).toInt)
      catch { case ex: InterruptedException =>
        // if we were interrupted, it's because the model is being halted, so we need to make sure
        // we re-interrupt so the halt will happen - ST 8/16/07
        Thread.currentThread.interrupt()
      }
    }
  }
  // this one runs on the job thread
  def pause() {
    val now = nanoTime
    pauseUntil(() => now + updatePolicy.slowdown)
  }
  // here we might be on either thread, depending on which kind of pause this is - ST 8/10/11
  private def pauseUntil(deadline: () => Long) {
    var remaining = deadline() - nanoTime
    synchronized {
      // There are two reasons that sleep() might return -- because it timed out, or because
      // nudgeSleeper() nudged us.  So we check to see how long we've been waiting and go back to
      // sleep if there's more time left.  But note that we call deadline() again because
      // if the user moved the slider, the value might be different now. - ST 8/16/07, 3/3/11
      while(remaining > 0 && !Thread.currentThread.isInterrupted) {
        sleep(remaining)
        remaining = deadline() - nanoTime
      }
    }
  }

  // used for unit testing and debugging
  def debugInfo =
    "speed = %.0f, frameRateGap = %.2f fps, nanoGap = %.2f fps, slowdown = %.1f ms, every %.3f ticks".format(
      speed, 1000000000.0 / frameRateGap, 1000000000.0 / nanoGap,
      updatePolicy.slowdown / 1000000.0, tickGap)

  // a nasty bit of stateful logic here.  might not be too hard to get rid of, actually - ST 2/28/11
  private def checkTicks() = {
    val result = ticks
    if(result < lastUpdateTicks)
      lastUpdateTicks = ForeverAgo
    result
  }

  /// NO SIDE EFFECTS BELOW HERE PLEASE!  not even calling System.nanoTime()!
  // (well, except for the nasty bit of stateful logic in checkTicks() which we call below)

  private def updatePolicy: UpdatePolicy = _updateMode match {
    case UpdateMode.Continuous => ContinuousPolicy
    case UpdateMode.TickBased  => TickBasedPolicy
  }

  private trait UpdatePolicy {
    def frameDoneWhenPaintingBegins: Boolean
    def slowdown: Long
    def frameRateGap: Long
    def nanoGap: Long
    def tickGap: Double
    def shouldUpdateNow(now: Long): Boolean
    def smoothingPause(now: Long): Long
    def shouldComeUpForAirAgain: Boolean
    protected def ticksSinceUpdate =
      (checkTicks() - lastUpdateTicks) + pseudoTicks
    protected def enoughTimePassed(now: Long) =
      now - lastUpdateNanos >= nanoGap
  }

  // Here's how tick-based updates work.
  //
  // When the slider is in the center, we simply update after every tick.
  //
  // The left and right halves of the slider work completely differently from each other.
  //
  // In the left half, we update after every tick, but then we also pause before continuing.  The
  // length of the pause increases as you go farther left.  (An extra wrinkle is that some models
  // use fractional ticks, so in addition to ramping up the pause, we also ramp down what fraction
  // of a tick will cause an update.)
  //
  // In the right half, we skip some updates.  To decide whether to skip an update, we consider two
  // pieces of information: how many ticks have passed, and also how much clock time has passed.  If
  // enough of either quantity has elapsed, we update.
  //
  // It's necessary to take both pieces of information into account in order for the slider to
  // behave well regardless of whether ticks are very long or very short.  nanoGap is how many
  // nanoseconds we allow to pass before updating, and tickGap is how many ticks we allow to pass
  // before updating.  In a model where each tick takes a long time, nanoGap takes over.  When ticks
  // are very short, tickGap takes over.
  //
  // We use exponential functions for scaling so that moving the slider a short distance from the center
  // makes small changes but big moves make really big changes.  Sometimes a plain exponential function
  // would increase too slowly at the beginning, so we insert a "speed +" to give the flat initial part
  // of the curve some extra slope.

  private object TickBasedPolicy extends UpdatePolicy {
    def frameDoneWhenPaintingBegins = true
    def slowdown =
      if(speed >= 0)
        0L
      else  // from 1 millisecond (at -1) to 9 seconds (at -50)
        1000000L * pow(pow(9000, 1 / 50.0), - speed).toLong
    def frameRateGap =
      if(speed >= 0)
        (1000000000L / (frameRateSetPoint + speed - 1 + pow(1.3, speed))).toLong
      else
        (1000000000L / (frameRateSetPoint * pow (0.9, - speed))).toLong
    def nanoGap =
      if(speed <= 25)
        0L
      else  // from 1 millisecond (at 26) to 3 seconds (speed 50)
        1000000L * (speed + pow(pow(3000, 1 / 25.0), speed - 25)).toLong
    def tickGap =
      if(speed <= 0)  // from 1 tick (at 10) to 0.01 ticks (at -50)
        pow(pow(0.01, 1 / 50.0), - speed)
      else if (speed <= 25)
        1
      else if (speed <= 40)
        speed - 24
      else
        (speed - 24 + pow(pow(1000000, 1 / 10.0), speed - 40)).toInt
    def shouldUpdateNow(now: Long) =
      ticksSinceUpdate >= tickGap ||
      (speed > 25 && ticksSinceUpdate >= 1 && enoughTimePassed(now))
    def smoothingPause(now: Long) =
      frameRateGap - (now - lastUpdateNanos)
    def shouldComeUpForAirAgain =
      false
  }

  // And here's how continuous updates work.
  //
  // First, there isn't anything special about the middle position on the slider (speed = 0).
  // It's just a place where the target frame rate has a nice reasonable value that gives
  // results similar to what previous versions of NetLogo did, before the slider existed.
  //
  // The left 1/4 of the slider is different from the right 3/4.
  //
  // In the right 3/4, we have a target frame rate ranging from every millisecond to every 3
  // seconds.
  //
  // In the left 1/4, we're drawing absolutely every frame, every time the engine comes up for air.
  // Then as we move farther left, we start inserting longer and longer pauses after each frame.
  //
  // So far, so good -- it's simple.  When that was all there was to it, though, we had a problem.
  // The transition point (at speed -25) was very noticeable.  Instead of getting a smooth
  // slowdown as you moved left over that point, the speed would suddenly drop by a huge factor.
  // So I had to find a way to smooth that over somehow.
  //
  // The problem is that even 1 millisecond is a very long time on a modern computer.  Even when our
  // target frame rate is once per millisecond, that still leaves enough for a lot of computation to
  // happen between two frames.
  //
  // So here's what we do.  In addition to the timing stuff described above, we also take ticks and
  // pseudo-ticks into account.  Remember that in continuous update mode, there's a pseudo-tick
  // every time the engine comes up for air.  In the left 1/4, we're going to be updating every
  // pseudo-tick.  So if the slider is nearing that point, in addition to increasing the target
  // frame rate, we also need to make sure that we're allowing fewer and fewer pseudoticks to pass
  // between frames.  At some point, these two curves -- the frame rate curve (nanoGap) and the
  // pseudo-ticks curve (tickGap) will cross.  Where they cross will depend on the particular model.
  // But by the time we get to the left 1/4, we know we'll have already reduced tickGap enough that
  // crossing the threshold will feel smooth.

  private object ContinuousPolicy extends UpdatePolicy {
    def frameDoneWhenPaintingBegins = false
    def slowdown =
      if(speed >= -25)
        0L
      else  // from 1 millisecond (at speed -26) to 1.5 seconds (at speed -50)
        1000000L * pow(pow(1500, 1 / 25.0), -25 - speed).toLong
    def frameRateGap = 0
    // right half: gap from 1 to ~3000 ms, exponentially, with a little extra slope at the start
    def nanoGap =
      if(speed < -12)
        1000000L
      else if (speed <= 0)  // from 1 millisecond (at speed -12) to frame rate (at speed 0)
        (1000000 * pow(pow(1000 / frameRateSetPoint, 1 / 12.0), speed + 12)).toLong
      else  // from frame rate (speed 0) to every 3 seconds (at speed 50)
        (1000000000.0 / (frameRateSetPoint + speed * ((0.333333 - frameRateSetPoint) / 50))).toLong
    def tickGap =
      if(speed <= -25)
        1
      else  // from 2 ticks (at speed -24) to 50,000 (at speed 0) to some giant number (at speed 50)
        (speed + 25 + pow(pow(50000, 1 / 25.0), speed + 25)).toInt
    def shouldUpdateNow(now: Long) =
      enoughTimePassed(now) || (ticksSinceUpdate >= tickGap)
    def smoothingPause(now: Long) =
      0
    // this determines whether we come up for air every time, or whether we wait for Lifeguard to
    // make us come up.  coming up for air takes time, so we don't want to do it too much at normal
    // or fast speed, but at slow speed, coming up for air constantly helps us slice time fine, for
    // smoothness - ST 3/2/11
    def shouldComeUpForAirAgain =
      speed < 45
  }

  // Whew!  So now you understand both update modes.  Um, right?
  //
  // To fully understand, it may help to look at the test cases in UpdateManagerTests.
  //
  // The other thing that's really useful for grasping this is to add a println(debugInfo) in
  // recompute().  Then run some different models with both update modes, moving the speed slider
  // back and forth, watching the view, and looking at the debug output.  You'll want to do this
  // with models with very fast ticks (e.g. Ising), models with very slow ticks (e.g.  B-Z Reaction,
  // or almost any model with a large world size), and models in the middle.

}
