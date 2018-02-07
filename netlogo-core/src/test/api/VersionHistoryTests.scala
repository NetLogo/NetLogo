// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.{ ArityIndependent, TaggedFunSuite }

class VersionHistoryTests extends TaggedFunSuite(ArityIndependent) {
  val knownVersions = Seq(
    "NetLogo 1.0",
    "NetLogo 1.1",
    "NetLogo 1.2",
    "NetLogo 2.0pre1",
    "NetLogo 2.0alpha1",
    "NetLogo 2.0beta1",
    "NetLogo 2.0beta2",
    "NetLogo 2.0beta3",
    "NetLogo 2.0beta4",
    "NetLogo 2.0beta5",
    "NetLogo 2.0",
    "NetLogo 2.1pre",
    "NetLogo 2.1beta1",
    "NetLogo 2.1beta2",
    "NetLogo 2.1beta3",
    "NetLogo 2.1",
    "NetLogo 2.2pre1",
    "NetLogo 2.2pre2",
    "NetLogo 2.2pre3",
    "NetLogo 3.0pre1",
    "NetLogo 3.0pre2",
    "NetLogo 3.0pre3",
    "NetLogo 3.0pre4",
    "NetLogo 3.0beta1",
    "NetLogo 3.0beta2",
    "NetLogo 3.0beta3",
    "NetLogo 3.0beta4",
    "NetLogo 3-D Preview 1",
    "NetLogo 3.0",
    "NetLogo 3.1pre1",
    "NetLogo 3.1pre2",
    "NetLogo 3.1beta1",
    "NetLogo 3.1beta2",
    "NetLogo 3.1beta3",
    "NetLogo 3.1beta4",
    "NetLogo 3.1beta5",
    "NetLogo 3-D Preview 2",
    "NetLogo 3.1",
    "NetLogo 3.2pre1",
    "NetLogo 3.2pre2",
    "NetLogo 3.2pre3",
    "NetLogo 3D Preview 3",
    "NetLogo 3D Preview 4",
    "NetLogo 3.2pre4",
    "NetLogo 3.2pre5",
    "NetLogo 3.2",
    "NetLogo 4.0pre1",
    "NetLogo 4.0pre2",
    "NetLogo 4.0pre3",
    "NetLogo 4.0pre4",
    "NetLogo 4.0alpha1",
    "NetLogo 4.0alpha2",
    "NetLogo 4.0alpha3",
    "NetLogo 4.0beta1",
    "NetLogo 4.0beta2",
    "NetLogo 4.0beta3",
    "NetLogo 4.0beta4",
    "NetLogo 4.0beta5",
    "NetLogo 3D Preview 5",
    "NetLogo 4.0") ++
    withAndWithout3D("NetLogo 4.1") ++
    withAndWithout3D("NetLogo 4.2pre1") ++
    withAndWithout3D("NetLogo 4.2pre2") ++
    withAndWithout3D("NetLogo 4.2pre3") ++
    withAndWithout3D("NetLogo 4.2pre4") ++
    withAndWithout3D("NetLogo 4.2pre5") ++
    withAndWithout3D("NetLogo 5.0") ++
    withAndWithout3D("NetLogo 5.0.1") ++
    withAndWithout3D("NetLogo 5.0.2") ++
    withAndWithout3D("NetLogo 5.0.3") ++
    withAndWithout3D("NetLogo 5.0.4") ++
    withAndWithout3D("NetLogo 5.0.5") ++
    withAndWithout3D("NetLogo 5.1.0") ++
    withAndWithout3D("NetLogo 5.2.0") ++
    withAndWithout3D("NetLogo 5.2.1") ++
    withAndWithout3D("NetLogo 5.3.0") ++
    withAndWithout3D("NetLogo 5.3.1")

    def withAndWithout3D(v: String): Seq[String] =
      Seq(v, v.replaceAll("NetLogo", "NetLogo 3D"))

  test("olderThan42pre2 returns true for older versions, false for newer") {
    val before = knownVersions.takeWhile(_  != "NetLogo 4.2pre2")
    val after = knownVersions.dropWhile(_  != "NetLogo 4.2pre2")
    before.foreach { v =>
      assert(VersionHistory.olderThan42pre2(v), s"for version $v")
    }
    after.foreach { v =>
      assert(! VersionHistory.olderThan42pre2(v), s"for version $v")
    }
  }
  test("olderThan40beta2 returns true for older versions, false for newer") {
    val before = knownVersions.takeWhile(_  != "NetLogo 4.0beta2")
    val after = knownVersions.dropWhile(_  != "NetLogo 4.0beta2")
    before.foreach { v =>
      assert(VersionHistory.olderThan40beta2(v), s"for version $v")
    }
    after.foreach { v =>
      assert(! VersionHistory.olderThan40beta2(v), s"for version $v")
    }
  }
}
