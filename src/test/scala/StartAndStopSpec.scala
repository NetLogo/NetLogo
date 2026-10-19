package org.nlogo.extensions.vid

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

class StartAndStopSpec extends AnyFeatureSpec with GivenWhenThen with VidHelpers {

  Feature("Starting and stopping") {
    Scenario("no source selected") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("vid:start", vid.start())
        thenShouldSeeError("vid: no selected source")
      }
    }

    Scenario("starts stopped source") {
      new VidSpecHelpers {
        givenOpenMovie()

        When("I start the movie")
        vid.start()

        thenStatusShouldBe("playing")
      }
    }

    Scenario("start and stop movie") {
      new VidSpecHelpers {
        givenOpenMovie(started = true)

        When("I run vid:stop")
        vid.stop()

        thenStatusShouldBe("stopped")
      }
    }
  }
}
