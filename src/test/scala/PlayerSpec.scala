package org.nlogo.extensions.vid

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

class PlayerSpec extends AnyFeatureSpec with GivenWhenThen with VidHelpers {

  Feature("show-player") {
    Scenario("player can be hidden even if it has not been shown") {
      new VidSpecHelpers {
        Given("The player is not showing")
        assert(! player.isShowing)
        When("I call vid:hide-player")
        vid.`hide-player`()
        Then("I should see that there is no player")
        assert(! player.isShowing)
      }
    }

    Scenario("player should be showing empty if a video is closed while open") {
      new VidSpecHelpers {
        givenOpenMovie()
        And("I have an open player")
        vid.`show-player`()

        When("I close the video source")
        vid.close()

        Then("I should see that the player is playing empty")
        assert(player.activeNode.isInstanceOf[EmptyNode])
      }
    }

    Scenario("changing the video source changes the players video source") {
      new VidSpecHelpers {
        givenOpenMovie()
        And("I have an open player")
        vid.`show-player`()

        When("I open the camera")
        vid.`camera-open`()

        Then("I should see that the player is playing from the camera's source")
        assert(player.activeNode.isInstanceOf[CameraNode])
      }
    }

    Scenario("player must have valid dimensions") {
      new VidSpecHelpers with ExpectError {
        whenRunForError("vid:show-player -1 -1",
          vid.`show-player`(Double.box(-1), Double.box(-1)))
        thenShouldSeeError("vid: invalid dimensions")
      }
    }

    Scenario("player can be started with no source") {
      new VidSpecHelpers {
        When("I run vid:show-player")
        vid.`show-player`()

        Then("I should see a player showing with no video")
        assert(player.isShowing)
        assert(player.activeNode.isInstanceOf[EmptyNode])
      }
    }

    Scenario("player can be started with no dimensions") {
      new VidSpecHelpers {
        givenOpenMovie()

        When("I run vid:show-player")
        vid.`show-player`()

        Then("I should see a player running with native dimensions matching the video")
        assert(player.isShowing)
        assert(player.activeNode.isInstanceOf[MovieNode])
        assert(player.activeNode.enforcedBounds.isEmpty)
      }
    }

    Scenario("player can be started with specific dimensions") {
      new VidSpecHelpers {
        givenOpenMovie()

        When("I run vid:show-player 640 480")
        vid.`show-player`(Double.box(640), Double.box(480))

        Then("I should see a player with the specified dimensions")
        assert(player.activeNode != null)
        assert(player.activeNode.node.getBoundsInLocal.getWidth == 640)
        assert(player.activeNode.node.getBoundsInLocal.getHeight == 480)
      }
    }
  }

  Scenario("player maintains specified dimensions when a new source is opened") {
    new VidSpecHelpers {
      When("I run vid:show-player 100 100")
      vid.`show-player`(Double.box(100), Double.box(100))

      Then("I should see a scene of the specified size")
      assert(player.activeNode != null)
      assert(player.activeNode.node.getBoundsInLocal.getWidth <= 100)
      assert(player.activeNode.node.getBoundsInLocal.getHeight <= 100)

      When("I open a movie")
      vid.`movie-open`("foobar.mp4")

      Then("I should see that the player has fit the movie to the specified dimensions")
      assert(player.activeNode != null)
      assert(player.activeNode.node.getBoundsInLocal.getWidth <= 100)
      assert(player.activeNode.node.getBoundsInLocal.getWidth <= 100)
    }
  }

  Scenario("player maintains dimensions after source is closed") {
    new VidSpecHelpers {
      givenOpenMovie()
      And("I run vid:show-player 100 100")
      vid.`show-player`(Double.box(100), Double.box(100))

      When("I run vid:close")
      vid.`close`()

      Then("I should see the player still fits the specified dimensions")
      assert(player.activeNode != null)
      assert(player.activeNode.node.getBoundsInLocal.getWidth <= 100)
      assert(player.activeNode.node.getBoundsInLocal.getWidth <= 100)
    }
  }
}
