// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite
import Version._

class VersionTests extends FunSuite {
  /// update this section every time the version changes -- ev 11/7/07
  if (!is3D) test("currentVersion2D") {
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
  else test("currentVersion3D") {
    assert(compatibleVersion("NetLogo 3D 6.0"))
    assert(!compatibleVersion("NetLogo 3D 5.0"))
    assert(!compatibleVersion("NetLogo 3D Preview 5"))
    assert(!compatibleVersion("NetLogo 3D Preview 4"))
  }
  test("futureMinor") {
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
  if (buildDate != "INTERIM DEVEL BUILD") test("testDate") {
    import java.text.SimpleDateFormat
    val format = new SimpleDateFormat("MMMM d, yyyy")
    val date = format.parse(buildDate)
    assertResult(format.format(date))(buildDate)
    assert(date.after(new SimpleDateFormat("y").parse("1998")))
    assert(date.before(new SimpleDateFormat("y").parse("2100")))
  }
  // no need to change this part it's just testing the string comparing part -- ev
  test("testStringComparisonLogic") {
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
    assert(compatibleVersion(noVersion))
  }
  test("numeric value") {
    assert(numericValue("NetLogo (no version)") == 0)
    assert(numericValue("NetLogo 5.2") == 502000)
    assert(numericValue("NetLogo 3D 5.2") == 502000)
    assert(numericValue("NetLogo 5.2.1") == 502010)
    assert(numericValue("NetLogo 4.0") == 400000)
    assert(numericValue("NetLogo 5.2.1") == 502010)
    assert(numericValue("NetLogo 6.0-M1") == 590000)
    assert(numericValue("NetLogo 6.0-RC1") == 595000)
    assert(numericValue("NetLogo 6.0-RC2") == 595001)
    assert(numericValue("NetLogo 6.1-RC1") == 600900)
    assert(numericValue("NetLogo 6.1-BETA1") == 600900)
    assert(numericValue("NetLogo 6.0.1-RC1") == 600006)
    assert(numericValue("NetLogo 3D Preview 5") == 390050)
    assert(numericValue("NetLogo 6.0-CONSTRUCTIONISM-2016-PREVIEW") == 590000)
    assert(numericValue("NetLogo 4.0beta1") == 390001)
    assert(numericValue("NetLogo 3-D Preview 1") == 390010)
    assert(numericValue("NetLogo 2.2pre3") == 201903)
  }
}
