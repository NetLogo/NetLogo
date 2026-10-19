package org.nlogo.extensions.vid

import org.nlogo.core.LogoList

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

// this is for all the features which don't justify their own spec yet
// as features get larger, they should get moved out.
class VidExtensionSpec extends AnyFeatureSpec with GivenWhenThen with VidHelpers {
  Feature("set-time") {
    Scenario("set-time errors when no video source has been selected") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("vid:set-time 100", vid.`set-time`(Double.box(100)))
        thenShouldSeeError("vid: no selected source")
      }
    }

    Scenario("set-time errors when a video source cannot be set to a time") {
      new VidSpecHelpers with ExpectError {
        givenOpenMovie()
        whenRunForError("vid:set-time -1", vid.`set-time`(Double.box(-1)))
        thenShouldSeeError("vid: invalid time")
      }
    }
  }

  Feature("camera-names") {
    Scenario("camera-names displays names of available cameras") {
      new VidSpecHelpers {
        When("I call vid:camera-names")
        val cameras = vid.`camera-names`()
        Then("I get a list of cameras available")
        assert(cameras.isInstanceOf[LogoList])
        assert(cameras.asInstanceOf[LogoList].scalaIterator.toSeq == Seq("camera"))
      }
    }
  }
}
