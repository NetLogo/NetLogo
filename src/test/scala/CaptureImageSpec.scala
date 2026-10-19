package org.nlogo.extensions.vid

import java.awt.image.BufferedImage

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

class CaptureImageSpec extends AnyFeatureSpec with GivenWhenThen with VidHelpers {

  Feature("capture-image") {
    Scenario("capture-image errors when no movie") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("vid:capture-image 640 480",
          vid.`capture-image`(Double.box(640), Double.box(480)))
        thenShouldSeeError("vid: no selected source")
      }
    }

    Scenario("invalid dimensions") {
      new VidSpecHelpers with ExpectError {
        givenOpenMovie()
        whenRunForError("vid:capture-image -1 -1",
          vid.`capture-image`(Double.box(-1), Double.box(-1)))
        thenShouldSeeError("vid: invalid dimensions")
      }
    }

    Scenario("capture-image returns a scaled image from the active video source") {
      new VidSpecHelpers {
        givenOpenMovie()

        When("I call vid:capture-image 32 32")
        val capturedImage =
          vid.`capture-image`(Double.box(32), Double.box(32))

        Then("I should have a BufferedImage scaled to fit a 32x32 box")
        capturedImage match {
          case image: BufferedImage =>
            assert(image.getWidth  <= 32)
            assert(image.getHeight <= 32)
          case _ => fail("expected BufferedImage to be returned")
        }
      }
    }

    Scenario("capture-image returns native-resolution image from active video source") {
      new VidSpecHelpers {
        givenOpenMovie()

        When("I call vid:capture-image")
        shouldMatchBufferedImage(vid.`capture-image`())
      }
    }

    Scenario("capture-image can capture an image while movie is playing") {
      new VidSpecHelpers {
        givenOpenMovie(started = true)

        When("I call vid:capture-image")
        shouldMatchBufferedImage(vid.`capture-image`())
      }
    }

    Scenario("capture-image can capture an image after stopping a movie") {
      new VidSpecHelpers {
        givenOpenMovie(started = true)
        And("I have called vid:stop")
        vid.`stop`()
        When("I call vid:capture-image")
        shouldMatchBufferedImage(vid.`capture-image`())
      }
    }
  }
}
