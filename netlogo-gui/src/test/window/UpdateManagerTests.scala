// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.scalatest.FunSuite
import org.nlogo.core.UpdateMode, UpdateMode._
import org.nlogo.agent.TickCounter

class UpdateManagerTests extends FunSuite {

  // at present, we're not testing the stateful parts of UpdateManager, we're just testing
  // the (functional) formulas in the two UpdatePolicy objects. - ST 2/28/11

  class MyUpdateManager(mode: UpdateMode) extends UpdateManager(new TickCounter()) {
    updateMode(mode)
  }

  def debugInfo(mode: UpdateMode, speed: Int) = {
    val manager = new MyUpdateManager(mode)
    manager.speed = speed
    manager.debugInfo
  }

  // first test some common speeds
  test("tick-based: default speed") {
    assert(debugInfo(TickBased, 0) ===
      "speed = 0, frameRateGap = 30.00 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks")
  }
  test("tick-based: max speed") {
    assert(debugInfo(TickBased, 50) ===
      "speed = 50, frameRateGap = 498256.10 fps, nanoGap = 0.33 fps, slowdown = 0.0 ms, every 1000026.000 ticks")
  }
  test("tick-based: min speed") {
    assert(debugInfo(TickBased, -50) ===
      "speed = -50, frameRateGap = 0.15 fps, nanoGap = Infinity fps, slowdown = 9000.0 ms, every 0.010 ticks")
  }

  test("tick-based: all speeds") {
    assert((-50 to 50).map(debugInfo(TickBased, _)).mkString("\n", "\n", "\n")
           === tickBased)
  }

  test("continuous: all speeds") {
    assert((-50 to 50).map(debugInfo(Continuous, _)).mkString("\n", "\n", "\n")
           === continuous)
  }

  val tickBased =
    """
speed = -50, frameRateGap = 0.15 fps, nanoGap = Infinity fps, slowdown = 9000.0 ms, every 0.010 ticks
speed = -49, frameRateGap = 0.17 fps, nanoGap = Infinity fps, slowdown = 7501.0 ms, every 0.011 ticks
speed = -48, frameRateGap = 0.19 fps, nanoGap = Infinity fps, slowdown = 6252.0 ms, every 0.012 ticks
speed = -47, frameRateGap = 0.21 fps, nanoGap = Infinity fps, slowdown = 5211.0 ms, every 0.013 ticks
speed = -46, frameRateGap = 0.24 fps, nanoGap = Infinity fps, slowdown = 4344.0 ms, every 0.014 ticks
speed = -45, frameRateGap = 0.26 fps, nanoGap = Infinity fps, slowdown = 3620.0 ms, every 0.016 ticks
speed = -44, frameRateGap = 0.29 fps, nanoGap = Infinity fps, slowdown = 3018.0 ms, every 0.017 ticks
speed = -43, frameRateGap = 0.32 fps, nanoGap = Infinity fps, slowdown = 2515.0 ms, every 0.019 ticks
speed = -42, frameRateGap = 0.36 fps, nanoGap = Infinity fps, slowdown = 2096.0 ms, every 0.021 ticks
speed = -41, frameRateGap = 0.40 fps, nanoGap = Infinity fps, slowdown = 1747.0 ms, every 0.023 ticks
speed = -40, frameRateGap = 0.44 fps, nanoGap = Infinity fps, slowdown = 1456.0 ms, every 0.025 ticks
speed = -39, frameRateGap = 0.49 fps, nanoGap = Infinity fps, slowdown = 1214.0 ms, every 0.028 ticks
speed = -38, frameRateGap = 0.55 fps, nanoGap = Infinity fps, slowdown = 1012.0 ms, every 0.030 ticks
speed = -37, frameRateGap = 0.61 fps, nanoGap = Infinity fps, slowdown = 843.0 ms, every 0.033 ticks
speed = -36, frameRateGap = 0.68 fps, nanoGap = Infinity fps, slowdown = 703.0 ms, every 0.036 ticks
speed = -35, frameRateGap = 0.75 fps, nanoGap = Infinity fps, slowdown = 586.0 ms, every 0.040 ticks
speed = -34, frameRateGap = 0.83 fps, nanoGap = Infinity fps, slowdown = 488.0 ms, every 0.044 ticks
speed = -33, frameRateGap = 0.93 fps, nanoGap = Infinity fps, slowdown = 407.0 ms, every 0.048 ticks
speed = -32, frameRateGap = 1.03 fps, nanoGap = Infinity fps, slowdown = 339.0 ms, every 0.052 ticks
speed = -31, frameRateGap = 1.14 fps, nanoGap = Infinity fps, slowdown = 282.0 ms, every 0.058 ticks
speed = -30, frameRateGap = 1.27 fps, nanoGap = Infinity fps, slowdown = 235.0 ms, every 0.063 ticks
speed = -29, frameRateGap = 1.41 fps, nanoGap = Infinity fps, slowdown = 196.0 ms, every 0.069 ticks
speed = -28, frameRateGap = 1.57 fps, nanoGap = Infinity fps, slowdown = 163.0 ms, every 0.076 ticks
speed = -27, frameRateGap = 1.74 fps, nanoGap = Infinity fps, slowdown = 136.0 ms, every 0.083 ticks
speed = -26, frameRateGap = 1.94 fps, nanoGap = Infinity fps, slowdown = 113.0 ms, every 0.091 ticks
speed = -25, frameRateGap = 2.15 fps, nanoGap = Infinity fps, slowdown = 94.0 ms, every 0.100 ticks
speed = -24, frameRateGap = 2.39 fps, nanoGap = Infinity fps, slowdown = 79.0 ms, every 0.110 ticks
speed = -23, frameRateGap = 2.66 fps, nanoGap = Infinity fps, slowdown = 65.0 ms, every 0.120 ticks
speed = -22, frameRateGap = 2.95 fps, nanoGap = Infinity fps, slowdown = 54.0 ms, every 0.132 ticks
speed = -21, frameRateGap = 3.28 fps, nanoGap = Infinity fps, slowdown = 45.0 ms, every 0.145 ticks
speed = -20, frameRateGap = 3.65 fps, nanoGap = Infinity fps, slowdown = 38.0 ms, every 0.158 ticks
speed = -19, frameRateGap = 4.05 fps, nanoGap = Infinity fps, slowdown = 31.0 ms, every 0.174 ticks
speed = -18, frameRateGap = 4.50 fps, nanoGap = Infinity fps, slowdown = 26.0 ms, every 0.191 ticks
speed = -17, frameRateGap = 5.00 fps, nanoGap = Infinity fps, slowdown = 22.0 ms, every 0.209 ticks
speed = -16, frameRateGap = 5.56 fps, nanoGap = Infinity fps, slowdown = 18.0 ms, every 0.229 ticks
speed = -15, frameRateGap = 6.18 fps, nanoGap = Infinity fps, slowdown = 15.0 ms, every 0.251 ticks
speed = -14, frameRateGap = 6.86 fps, nanoGap = Infinity fps, slowdown = 12.0 ms, every 0.275 ticks
speed = -13, frameRateGap = 7.63 fps, nanoGap = Infinity fps, slowdown = 10.0 ms, every 0.302 ticks
speed = -12, frameRateGap = 8.47 fps, nanoGap = Infinity fps, slowdown = 8.0 ms, every 0.331 ticks
speed = -11, frameRateGap = 9.41 fps, nanoGap = Infinity fps, slowdown = 7.0 ms, every 0.363 ticks
speed = -10, frameRateGap = 10.46 fps, nanoGap = Infinity fps, slowdown = 6.0 ms, every 0.398 ticks
speed = -9, frameRateGap = 11.62 fps, nanoGap = Infinity fps, slowdown = 5.0 ms, every 0.437 ticks
speed = -8, frameRateGap = 12.91 fps, nanoGap = Infinity fps, slowdown = 4.0 ms, every 0.479 ticks
speed = -7, frameRateGap = 14.35 fps, nanoGap = Infinity fps, slowdown = 3.0 ms, every 0.525 ticks
speed = -6, frameRateGap = 15.94 fps, nanoGap = Infinity fps, slowdown = 2.0 ms, every 0.575 ticks
speed = -5, frameRateGap = 17.71 fps, nanoGap = Infinity fps, slowdown = 2.0 ms, every 0.631 ticks
speed = -4, frameRateGap = 19.68 fps, nanoGap = Infinity fps, slowdown = 2.0 ms, every 0.692 ticks
speed = -3, frameRateGap = 21.87 fps, nanoGap = Infinity fps, slowdown = 1.0 ms, every 0.759 ticks
speed = -2, frameRateGap = 24.30 fps, nanoGap = Infinity fps, slowdown = 1.0 ms, every 0.832 ticks
speed = -1, frameRateGap = 27.00 fps, nanoGap = Infinity fps, slowdown = 1.0 ms, every 0.912 ticks
speed = 0, frameRateGap = 30.00 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 1, frameRateGap = 31.30 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 2, frameRateGap = 32.69 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 3, frameRateGap = 34.20 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 4, frameRateGap = 35.86 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 5, frameRateGap = 37.71 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 6, frameRateGap = 39.83 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 7, frameRateGap = 42.27 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 8, frameRateGap = 45.16 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 9, frameRateGap = 48.60 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 10, frameRateGap = 52.79 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 11, frameRateGap = 57.92 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 12, frameRateGap = 64.30 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 13, frameRateGap = 72.29 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 14, frameRateGap = 82.37 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 15, frameRateGap = 95.19 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 16, frameRateGap = 111.54 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 17, frameRateGap = 132.50 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 18, frameRateGap = 159.46 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 19, frameRateGap = 194.19 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 20, frameRateGap = 239.05 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 21, frameRateGap = 297.06 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 22, frameRateGap = 372.18 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 23, frameRateGap = 469.54 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 24, frameRateGap = 595.80 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 25, frameRateGap = 759.64 fps, nanoGap = Infinity fps, slowdown = 0.0 ms, every 1.000 ticks
speed = 26, frameRateGap = 972.33 fps, nanoGap = 37.04 fps, slowdown = 0.0 ms, every 2.000 ticks
speed = 27, frameRateGap = 1248.53 fps, nanoGap = 35.71 fps, slowdown = 0.0 ms, every 3.000 ticks
speed = 28, frameRateGap = 1607.30 fps, nanoGap = 33.33 fps, slowdown = 0.0 ms, every 4.000 ticks
speed = 29, frameRateGap = 2073.39 fps, nanoGap = 31.25 fps, slowdown = 0.0 ms, every 5.000 ticks
speed = 30, frameRateGap = 2679.00 fps, nanoGap = 29.41 fps, slowdown = 0.0 ms, every 6.000 ticks
speed = 31, frameRateGap = 3466.00 fps, nanoGap = 27.03 fps, slowdown = 0.0 ms, every 7.000 ticks
speed = 32, frameRateGap = 4488.79 fps, nanoGap = 24.39 fps, slowdown = 0.0 ms, every 8.000 ticks
speed = 33, frameRateGap = 5818.15 fps, nanoGap = 22.22 fps, slowdown = 0.0 ms, every 9.000 ticks
speed = 34, frameRateGap = 7545.97 fps, nanoGap = 19.61 fps, slowdown = 0.0 ms, every 10.000 ticks
speed = 35, frameRateGap = 9791.92 fps, nanoGap = 16.95 fps, slowdown = 0.0 ms, every 11.000 ticks
speed = 36, frameRateGap = 12711.33 fps, nanoGap = 14.49 fps, slowdown = 0.0 ms, every 12.000 ticks
speed = 37, frameRateGap = 16506.28 fps, nanoGap = 12.05 fps, slowdown = 0.0 ms, every 13.000 ticks
speed = 38, frameRateGap = 21439.44 fps, nanoGap = 9.80 fps, slowdown = 0.0 ms, every 14.000 ticks
speed = 39, frameRateGap = 27852.05 fps, nanoGap = 7.87 fps, slowdown = 0.0 ms, every 15.000 ticks
speed = 40, frameRateGap = 36188.62 fps, nanoGap = 6.21 fps, slowdown = 0.0 ms, every 16.000 ticks
speed = 41, frameRateGap = 47025.63 fps, nanoGap = 4.78 fps, slowdown = 0.0 ms, every 20.000 ticks
speed = 42, frameRateGap = 61113.49 fps, nanoGap = 3.66 fps, slowdown = 0.0 ms, every 33.000 ticks
speed = 43, frameRateGap = 79428.12 fps, nanoGap = 2.77 fps, slowdown = 0.0 ms, every 82.000 ticks
speed = 44, frameRateGap = 103241.79 fps, nanoGap = 2.07 fps, slowdown = 0.0 ms, every 271.000 ticks
speed = 45, frameRateGap = 134192.16 fps, nanoGap = 1.54 fps, slowdown = 0.0 ms, every 1021.000 ticks
speed = 46, frameRateGap = 174428.75 fps, nanoGap = 1.14 fps, slowdown = 0.0 ms, every 4003.000 ticks
speed = 47, frameRateGap = 226757.37 fps, nanoGap = 0.84 fps, slowdown = 0.0 ms, every 15871.000 ticks
speed = 48, frameRateGap = 294724.43 fps, nanoGap = 0.61 fps, slowdown = 0.0 ms, every 63119.000 ticks
speed = 49, frameRateGap = 383141.76 fps, nanoGap = 0.45 fps, slowdown = 0.0 ms, every 251213.000 ticks
speed = 50, frameRateGap = 498256.10 fps, nanoGap = 0.33 fps, slowdown = 0.0 ms, every 1000026.000 ticks
"""

  val continuous = """
speed = -50, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 1499.0 ms, every 1.000 ticks
speed = -49, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 1119.0 ms, every 1.000 ticks
speed = -48, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 835.0 ms, every 1.000 ticks
speed = -47, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 623.0 ms, every 1.000 ticks
speed = -46, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 465.0 ms, every 1.000 ticks
speed = -45, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 347.0 ms, every 1.000 ticks
speed = -44, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 259.0 ms, every 1.000 ticks
speed = -43, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 193.0 ms, every 1.000 ticks
speed = -42, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 144.0 ms, every 1.000 ticks
speed = -41, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 107.0 ms, every 1.000 ticks
speed = -40, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 80.0 ms, every 1.000 ticks
speed = -39, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 60.0 ms, every 1.000 ticks
speed = -38, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 44.0 ms, every 1.000 ticks
speed = -37, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 33.0 ms, every 1.000 ticks
speed = -36, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 24.0 ms, every 1.000 ticks
speed = -35, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 18.0 ms, every 1.000 ticks
speed = -34, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 13.0 ms, every 1.000 ticks
speed = -33, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 10.0 ms, every 1.000 ticks
speed = -32, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 7.0 ms, every 1.000 ticks
speed = -31, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 5.0 ms, every 1.000 ticks
speed = -30, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 4.0 ms, every 1.000 ticks
speed = -29, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 3.0 ms, every 1.000 ticks
speed = -28, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 2.0 ms, every 1.000 ticks
speed = -27, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 1.0 ms, every 1.000 ticks
speed = -26, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 1.0 ms, every 1.000 ticks
speed = -25, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 1.000 ticks
speed = -24, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 2.000 ticks
speed = -23, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 4.000 ticks
speed = -22, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 6.000 ticks
speed = -21, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 9.000 ticks
speed = -20, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 13.000 ticks
speed = -19, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 19.000 ticks
speed = -18, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 27.000 ticks
speed = -17, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 39.000 ticks
speed = -16, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 58.000 ticks
speed = -15, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 85.000 ticks
speed = -14, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 127.000 ticks
speed = -13, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 192.000 ticks
speed = -12, frameRateGap = Infinity fps, nanoGap = 1000.00 fps, slowdown = 0.0 ms, every 290.000 ticks
speed = -11, frameRateGap = Infinity fps, nanoGap = 746.61 fps, slowdown = 0.0 ms, every 441.000 ticks
speed = -10, frameRateGap = Infinity fps, nanoGap = 557.43 fps, slowdown = 0.0 ms, every 674.000 ticks
speed = -9, frameRateGap = Infinity fps, nanoGap = 416.18 fps, slowdown = 0.0 ms, every 1033.000 ticks
speed = -8, frameRateGap = Infinity fps, nanoGap = 310.72 fps, slowdown = 0.0 ms, every 1584.000 ticks
speed = -7, frameRateGap = Infinity fps, nanoGap = 231.99 fps, slowdown = 0.0 ms, every 2434.000 ticks
speed = -6, frameRateGap = Infinity fps, nanoGap = 173.21 fps, slowdown = 0.0 ms, every 3744.000 ticks
speed = -5, frameRateGap = Infinity fps, nanoGap = 129.32 fps, slowdown = 0.0 ms, every 5763.000 ticks
speed = -4, frameRateGap = Infinity fps, nanoGap = 96.55 fps, slowdown = 0.0 ms, every 8874.000 ticks
speed = -3, frameRateGap = Infinity fps, nanoGap = 72.08 fps, slowdown = 0.0 ms, every 13670.000 ticks
speed = -2, frameRateGap = Infinity fps, nanoGap = 53.82 fps, slowdown = 0.0 ms, every 21063.000 ticks
speed = -1, frameRateGap = Infinity fps, nanoGap = 40.18 fps, slowdown = 0.0 ms, every 32458.000 ticks
speed = 0, frameRateGap = Infinity fps, nanoGap = 30.00 fps, slowdown = 0.0 ms, every 50024.000 ticks
speed = 1, frameRateGap = Infinity fps, nanoGap = 29.41 fps, slowdown = 0.0 ms, every 77103.000 ticks
speed = 2, frameRateGap = Infinity fps, nanoGap = 28.81 fps, slowdown = 0.0 ms, every 118846.000 ticks
speed = 3, frameRateGap = Infinity fps, nanoGap = 28.22 fps, slowdown = 0.0 ms, every 183194.000 ticks
speed = 4, frameRateGap = Infinity fps, nanoGap = 27.63 fps, slowdown = 0.0 ms, every 282390.000 ticks
speed = 5, frameRateGap = Infinity fps, nanoGap = 27.03 fps, slowdown = 0.0 ms, every 435305.000 ticks
speed = 6, frameRateGap = Infinity fps, nanoGap = 26.44 fps, slowdown = 0.0 ms, every 671031.000 ticks
speed = 7, frameRateGap = Infinity fps, nanoGap = 25.85 fps, slowdown = 0.0 ms, every 1034415.000 ticks
speed = 8, frameRateGap = Infinity fps, nanoGap = 25.25 fps, slowdown = 0.0 ms, every 1594591.000 ticks
speed = 9, frameRateGap = Infinity fps, nanoGap = 24.66 fps, slowdown = 0.0 ms, every 2458132.000 ticks
speed = 10, frameRateGap = Infinity fps, nanoGap = 24.07 fps, slowdown = 0.0 ms, every 3789326.000 ticks
speed = 11, frameRateGap = Infinity fps, nanoGap = 23.47 fps, slowdown = 0.0 ms, every 5841434.000 ticks
speed = 12, frameRateGap = Infinity fps, nanoGap = 22.88 fps, slowdown = 0.0 ms, every 9004868.000 ticks
speed = 13, frameRateGap = Infinity fps, nanoGap = 22.29 fps, slowdown = 0.0 ms, every 13881474.000 ticks
speed = 14, frameRateGap = Infinity fps, nanoGap = 21.69 fps, slowdown = 0.0 ms, every 21399025.000 ticks
speed = 15, frameRateGap = Infinity fps, nanoGap = 21.10 fps, slowdown = 0.0 ms, every 32987737.000 ticks
speed = 16, frameRateGap = Infinity fps, nanoGap = 20.51 fps, slowdown = 0.0 ms, every 50852365.000 ticks
speed = 17, frameRateGap = Infinity fps, nanoGap = 19.91 fps, slowdown = 0.0 ms, every 78391656.000 ticks
speed = 18, frameRateGap = Infinity fps, nanoGap = 19.32 fps, slowdown = 0.0 ms, every 120844965.000 ticks
speed = 19, frameRateGap = Infinity fps, nanoGap = 18.73 fps, slowdown = 0.0 ms, every 186289042.000 ticks
speed = 20, frameRateGap = Infinity fps, nanoGap = 18.13 fps, slowdown = 0.0 ms, every 287174633.000 ticks
speed = 21, frameRateGap = Infinity fps, nanoGap = 17.54 fps, slowdown = 0.0 ms, every 442695240.000 ticks
speed = 22, frameRateGap = Infinity fps, nanoGap = 16.95 fps, slowdown = 0.0 ms, every 682438685.000 ticks
speed = 23, frameRateGap = Infinity fps, nanoGap = 16.35 fps, slowdown = 0.0 ms, every 1052016201.000 ticks
speed = 24, frameRateGap = Infinity fps, nanoGap = 15.76 fps, slowdown = 0.0 ms, every 1621739973.000 ticks
speed = 25, frameRateGap = Infinity fps, nanoGap = 15.17 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 26, frameRateGap = Infinity fps, nanoGap = 14.57 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 27, frameRateGap = Infinity fps, nanoGap = 13.98 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 28, frameRateGap = Infinity fps, nanoGap = 13.39 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 29, frameRateGap = Infinity fps, nanoGap = 12.79 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 30, frameRateGap = Infinity fps, nanoGap = 12.20 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 31, frameRateGap = Infinity fps, nanoGap = 11.61 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 32, frameRateGap = Infinity fps, nanoGap = 11.01 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 33, frameRateGap = Infinity fps, nanoGap = 10.42 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 34, frameRateGap = Infinity fps, nanoGap = 9.83 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 35, frameRateGap = Infinity fps, nanoGap = 9.23 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 36, frameRateGap = Infinity fps, nanoGap = 8.64 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 37, frameRateGap = Infinity fps, nanoGap = 8.05 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 38, frameRateGap = Infinity fps, nanoGap = 7.45 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 39, frameRateGap = Infinity fps, nanoGap = 6.86 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 40, frameRateGap = Infinity fps, nanoGap = 6.27 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 41, frameRateGap = Infinity fps, nanoGap = 5.67 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 42, frameRateGap = Infinity fps, nanoGap = 5.08 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 43, frameRateGap = Infinity fps, nanoGap = 4.49 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 44, frameRateGap = Infinity fps, nanoGap = 3.89 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 45, frameRateGap = Infinity fps, nanoGap = 3.30 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 46, frameRateGap = Infinity fps, nanoGap = 2.71 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 47, frameRateGap = Infinity fps, nanoGap = 2.11 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 48, frameRateGap = Infinity fps, nanoGap = 1.52 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 49, frameRateGap = Infinity fps, nanoGap = 0.93 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
speed = 50, frameRateGap = Infinity fps, nanoGap = 0.33 fps, slowdown = 0.0 ms, every 2147483647.000 ticks
"""

}
