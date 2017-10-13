// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.{ ArityIndependent, TaggedFunSuite }

import Version.noVersion

class VersionTests extends TaggedFunSuite(ArityIndependent) {

  /// update this section every time the version changes -- ev 11/7/07
  test("currentVersion2D") {
    import TwoDVersion.compatibleVersion
    // these differ from the current version in suffix only
    assert(compatibleVersion("NetLogo 6.0"))
    assert(compatibleVersion("NetLogo 6.0RC1"))
    assert(compatibleVersion("NetLogo 6.0beta1"))
    assert(compatibleVersion("NetLogo 6.0pre1"))
    assert(compatibleVersion("NetLogo 6.0alpha1"))
    assert(compatibleVersion("NetLogo 6.0.1"))
    assert(compatibleVersion("NetLogo 6.0.1weirdversion"))
    assert(compatibleVersion("NetLogo 6.0weirdversion"))
  }
  test("currentVersion3D") {
    val v = ThreeDVersion
    assert(v.compatibleVersion("NetLogo 3D 6.0"))
    assert(!v.compatibleVersion("NetLogo 3D 5.0"))
    assert(!v.compatibleVersion("NetLogo 3D Preview 5"))
    assert(!v.compatibleVersion("NetLogo 3D Preview 4"))
  }
  test("futureMinor") {
    import TwoDVersion.compatibleVersion
    // these differ from the current version by minor version only
    assert(compatibleVersion("NetLogo 6.0"))
    assert(compatibleVersion("NetLogo 6.0RC1"))
    assert(compatibleVersion("NetLogo 6.0beta1"))
    assert(compatibleVersion("NetLogo 6.0pre1"))
    assert(compatibleVersion("NetLogo 6.0alpha1"))
    assert(compatibleVersion("NetLogo 6.0.1"))
    assert(compatibleVersion("NetLogo 6.0.1weirdversion"))
    assert(compatibleVersion("NetLogo 6.0weirdversion"))
  }
  // these don't need to be changed very often; they should always
  // pass properly since we're long past these versions -- ev
  test("testOldVersions") {
    import TwoDVersion.compatibleVersion
    assert(!compatibleVersion("NetLogo 5.1"))
    assert(!compatibleVersion("NetLogo 5.1RC1"))
    assert(!compatibleVersion("NetLogo 5.1.1"))
    assert(!compatibleVersion("NetLogo 4.0"))
    assert(!compatibleVersion("NetLogo 4.0beta1"))
    assert(!compatibleVersion("NetLogo 4.0pre1"))
    assert(!compatibleVersion("NetLogo 4.0alpha1"))
    assert(!compatibleVersion("NetLogo 4.0.1"))
    assert(!compatibleVersion("NetLogo 4.0.1weirdversion"))
    assert(!compatibleVersion("NetLogo 4.0weirdversion"))
    assert(!compatibleVersion("NetLogo 3.1"))
    assert(!compatibleVersion("NetLogo 3.1beta1"))
    assert(!compatibleVersion("NetLogo 3.1pre1"))
    assert(!compatibleVersion("NetLogo 3.1alpha1"))
    assert(!compatibleVersion("NetLogo 3.1.1"))
    assert(!compatibleVersion("NetLogo 3.1.1weirdversion"))
    assert(!compatibleVersion("NetLogo 3.1weirdversion"))
    assert(!compatibleVersion("NetLogo 3.0"))
    assert(!compatibleVersion("NetLogo 3.0beta1"))
    assert(!compatibleVersion("NetLogo 3.0pre1"))
    assert(!compatibleVersion("NetLogo 3.0alpha1"))
    assert(!compatibleVersion("NetLogo 3.0.1"))
    assert(!compatibleVersion("NetLogo 3.0.1weirdversion"))
    assert(!compatibleVersion("NetLogo 3.0weirdversion"))
  }
  // no need to update this. it only checks that version.txt has a valid date - ST 12/18/08
  if (TwoDVersion.buildDate != "INTERIM DEVEL BUILD") test("testDate") {
    import TwoDVersion.buildDate
    import java.text.SimpleDateFormat
    val format = new SimpleDateFormat("MMMM d, yyyy")
    val date = format.parse(buildDate)
    assertResult(format.format(date))(buildDate)
    assert(date.after(new SimpleDateFormat("y").parse("1998")))
    assert(date.before(new SimpleDateFormat("y").parse("2100")))
  }
  // no need to change this part it's just testing the string comparing part -- ev
  test("testStringComparisonLogic") {
    import Version.compareVersions
    assert(compareVersions("NetLogo 5.0.5", "NetLogo 5.1")) // Exception for 5.0 to 5.1!
    assert(compareVersions("NetLogo 5.0", "NetLogo 5.0.5"))
    assert(compareVersions("NetLogo 5.0.0", "NetLogo 5.0.5"))
    assert(compareVersions("NetLogo 4.0", "NetLogo 4.0.1"))
    assert(compareVersions("NetLogo 4.0.1", "NetLogo 4.0.1"))
    assert(compareVersions("NetLogo 4.0", "NetLogo 4.0"))
    assert(compareVersions("NetLogo 4.0.2", "NetLogo 4.0.1"))
    assert(compareVersions("NetLogo 4.0.1", "NetLogo 4.0.1"))
    assert(compareVersions("NetLogo 4.0.1", "NetLogo 4.0.2"))
    assert(compareVersions("NetLogo 4.0beta1", "NetLogo 4.0.1"))
    assert(compareVersions("NetLogo 4.0.1", "NetLogo 4.0beta1"))
    assert(compareVersions("NetLogo 4.0", "NetLogo 4.0beta1"))
    assert(compareVersions("NetLogo 4.0beta1", "NetLogo 4.0"))
    assert(compareVersions("NetLogo 4.0beta1", noVersion))
    assert(compareVersions("NetLogo 4.0pre1", "NetLogo 4.0"))
    assert(compareVersions("NetLogo 4.0pre1", noVersion))
    assert(compareVersions("NetLogo 4.0alpha1", "NetLogo 4.0"))
    assert(compareVersions("NetLogo 4.0othergarbage", "NetLogo 4.0"))
    assert(compareVersions("NetLogo 4.0alpha1", noVersion))
    assert(!compareVersions("NetLogo 4.1", "NetLogo 4.0beta1"))
    assert(!compareVersions("NetLogo 4.1", "NetLogo 4.0.1"))
    assert(!compareVersions("NetLogo 4.1", "NetLogo 4.0.2"))
    assert(!compareVersions("NetLogo 4.1", "NetLogo 4.0"))
    assert(!compareVersions("NetLogo 4.1", "NetLogo 4.0pre1"))
    assert(!compareVersions("NetLogo 4.1", "NetLogo 4.0alpha1"))
    assert(!compareVersions("NetLogo 3.1", "NetLogo 4.0beta1"))
    assert(!compareVersions("NetLogo 3.1", "NetLogo 4.0.1"))
    assert(!compareVersions("NetLogo 3.1", "NetLogo 4.0.2"))
    assert(!compareVersions("NetLogo 3.1", "NetLogo 4.0"))
    assert(!compareVersions("NetLogo 3.1", "NetLogo 4.0pre1"))
    assert(!compareVersions("NetLogo 3.1", "NetLogo 4.0alpha1"))
    assert(!compareVersions("NetLogo 3.0", "NetLogo 4.0beta1"))
    assert(!compareVersions("NetLogo 3.0", "NetLogo 4.0.1"))
    assert(!compareVersions("NetLogo 3.0", "NetLogo 4.0.2"))
    assert(!compareVersions("NetLogo 3.0", "NetLogo 4.0"))
    assert(!compareVersions("NetLogo 3.0", "NetLogo 4.0pre1"))
    assert(!compareVersions("NetLogo 3.0", "NetLogo 4.0alpha1"))
    assert(TwoDVersion.compatibleVersion(noVersion))
  }
  test("numeric value") {
    import Version.numericValue
    assertResult(0)(numericValue("NetLogo (no version)"))
    assertResult(502000)(numericValue("NetLogo 5.2"))
    assertResult(502000)(numericValue("NetLogo 3D 5.2"))
    assertResult(502010)(numericValue("NetLogo 5.2.1"))
    assertResult(400000)(numericValue("NetLogo 4.0"))
    assertResult(502010)(numericValue("NetLogo 5.2.1"))
    assertResult(590000)(numericValue("NetLogo 6.0-M1"))
    assertResult(595000)(numericValue("NetLogo 6.0-RC1"))
    assertResult(595001)(numericValue("NetLogo 6.0-RC2"))
    assertResult(600900)(numericValue("NetLogo 6.1-RC1"))
    assertResult(600900)(numericValue("NetLogo 6.1-BETA1"))
    assertResult(600006)(numericValue("NetLogo 6.0.1-RC1"))
    assertResult(400000)(numericValue("NetLogo 3D Preview 5"))
    assertResult(590000)(numericValue("NetLogo 6.0-CONSTRUCTIONISM-2016-PREVIEW"))
    assertResult(390061)(numericValue("NetLogo 4.0beta1"))
    assertResult(300000)(numericValue("NetLogo 3-D Preview 1"))
    assertResult(201903)(numericValue("NetLogo 2.2pre3"))
  }
}
