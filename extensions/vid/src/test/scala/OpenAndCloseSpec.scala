package org.nlogo.extensions.vid

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

class OpenAndCloseSpec extends AnyFeatureSpec with GivenWhenThen with VidHelpers {

  Feature("opening and closing") {
    Scenario("no movie open") {
      new VidSpecHelpers {
        thenStatusShouldBe("inactive")
      }
    }

    Scenario("opens a movie") {
      new VidSpecHelpers {
        whenIRun.`movie-open`("foobar.mp4")
        thenStatusShouldBe("stopped")
      }
    }

    Scenario("opens a movie at an absolute path") {
      new VidSpecHelpers {
        whenIRun.`movie-open`("/tmp/foobar.mp4")
        thenStatusShouldBe("stopped")
      }
    }

    Scenario("opening a new source closes the first source") {
      new VidSpecHelpers {
        givenOpenMovie()
        whenIRun.`camera-open`("camera")

        Then("the movie should be closed")
        assert(dummyMovie.isClosed)
      }
    }

    Scenario("opens movie from a remote source") {
      new VidSpecHelpers {
        whenIRun.`movie-open-remote`("http://example.org/somevideo.mp4")
        thenStatusShouldBe("stopped")
      }
    }

    Scenario("attempt opening not found movie from remote source") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("""vid:movie-open-remote "http://example.org/notfound.mp4"""",
          vid.`movie-open-remote`("http://example.org/notfound.mp4"))
        thenShouldSeeError("""vid: no movie found""")
      }
    }

    Scenario("attempt opening invalid movie type from remote source") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("""vid:movie-open-remote "http://example.org/somevideo.ogv"""",
          vid.`movie-open-remote`("http://example.org/somevideo.ogv"))
        thenShouldSeeError("vid: format not supported")
      }
    }

    Scenario("attempt opening invalid movie protocol from remote source") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("""vid:movie-open-remote "https://example.org/somevideo.mp4"""",
          vid.`movie-open-remote`("https://example.org/somevideo.mp4"))
        thenShouldSeeError("vid: protocol not supported")
      }
    }

    Scenario("open a camera") {
      new VidSpecHelpers {
        whenIRun.`camera-open`("camera")
        thenStatusShouldBe("playing")
      }
    }

    Scenario("open a camera that doesn't exist") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("""vid:camera-open "nocamera"""",
          vid.`camera-open`("nocamera"))
        thenShouldSeeError("""vid: camera "nocamera" not found""")
        andStatusShouldBe("inactive")
      }
    }

    Scenario("opens a default camera") {
      new VidSpecHelpers {
        whenIRun.`camera-open`()
        thenStatusShouldBe("playing")
      }
    }

    Scenario("tries to open a default camera when none available") {
      new VidSpecHelpers with ExpectError {
        Given("there are no cameras available")
        cameraFactory.defaultCameraName = None
        whenRunForError("vid:camera-open", vid.`camera-open`())
        thenShouldSeeError("vid: no cameras found")
        andStatusShouldBe("inactive")
      }
    }

    Scenario("closes an opened movie") {
      new VidSpecHelpers {
        givenOpenMovie()
        whenIRun.close()
        thenStatusShouldBe("inactive")
        assert(dummyMovie.isClosed)
      }
    }

    Scenario("cannot find movie") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("""vid:movie-open "not-real.mp4"""",
          vid.`movie-open`("not-real.mp4"))
        thenShouldSeeError("vid: no movie found")
        andStatusShouldBe("inactive")
      }
    }

    Scenario("movie has invalid format") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("""vid:movie-open "unsupported.ogg"""",
          vid.`movie-open`("unsupported.ogg"))
        thenShouldSeeError("vid: format not supported")
        andStatusShouldBe("inactive")
      }
    }

    Scenario("camera-select when no cameras available") {
      new VidSpecHelpers with ExpectError {
        Given("There are no available cameras")
        cameraFactory.cameraNames = Seq()
        whenRunForError("vid:camera-select", vid.`camera-select`())
        thenShouldSeeError("vid: no cameras found")
      }
    }

    Scenario("camera-select selects available camera") {
      new VidSpecHelpers {
        When("I run vid:camera-select")
        And("I select a camera")
        selector.select("camera")
        vid.`camera-select`()

        Then("I should see that the camera is open")
        thenStatusShouldBe("playing")
      }
    }

    Scenario("camera doesn't open if I don't select a camera") {
      new VidSpecHelpers {
        When("I run vid:camera-select")
        And("I do not select a camera")
        selector.cancel()
        vid.`camera-select`()
        Then("I should see that no camera is open")
        thenStatusShouldBe("inactive")
      }
    }

    Scenario("movie select doesn't open a movie if user doesn't select one") {
      new VidSpecHelpers {
        When("I run vid:movie-select")
        And("I do not select a movie")
        selector.cancel()
        vid.`movie-select`()
        Then("I should see that no movie is open")
        thenStatusShouldBe("inactive")
      }
    }

    Scenario("movie select opens a movie when selected") {
      new VidSpecHelpers {
        When("I run vid:movie-select")
        And("I select a movie")
        selector.select("/currentdir/foobar.mp4")
        vid.`movie-select`()
        Then("I should see an open movie")
        thenStatusShouldBe("stopped")
      }
    }

    Scenario("movie select errors if a user selects a bad format") {
      new VidSpecHelpers with ExpectError {
        selector.select("/currentdir/unsupported.ogg")
        whenRunForError("vid:movie-select", vid.`movie-select`())
        And("I select a movie with an unsupported format")
        thenShouldSeeError("vid: format not supported")
      }
    }
  }
}
