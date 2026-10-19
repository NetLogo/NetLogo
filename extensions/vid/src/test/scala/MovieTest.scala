package org.nlogo.extensions.vid

import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.LinkedTransferQueue

import javafx.embed.swing.JFXPanel
import javafx.scene.media.{ Media, MediaPlayer }
import javafx.scene.media.MediaPlayer.{ Status => MPStatus }
import javafx.util.Duration

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.concurrent.Waiters
import org.scalatest.time.{ Millis, Span }

import util.FunctionToCallback.function2ChangeListener

class MovieTest extends AnyFunSuite with Waiters {

  val _ = new JFXPanel() // init JavaFX

  val ValidMoviePath    = "src/test/resources/small.mp4"
  val NotFoundMoviePath = "/tmp/notreal"
  val InvalidMoviePath  = "src/test/resources/small.ogv"

  //val ValidMovieURL = "https://www.sample-videos.com/video321/mp4/480/big_buck_bunny_480p_1mb.mp4"
  // see comment below
  // val InvalidMovieURL = "http://v2v.cc/~j/samples/failed_vorbis_size.ogv"
  val RssMovieURL = "rss://raw.githubusercontent.com/NetLogo/vid/master/src/test/resources/small.mp4"
  val NotFoundMovieURL = "http://raw.githubusercontent.com/NetLogo/vid/master/src/test/resources/notreal.mp4"

  trait MovieFixture {
    val isReady = new LinkedTransferQueue[Boolean]
    val media = new Media(new File(ValidMoviePath).toURI.toString)
    val mediaPlayer = new MediaPlayer(media)
    mediaPlayer.setOnReady(() => isReady.add(true))
    val movie = new Movie(media, mediaPlayer)

    def expectTransition(status: MPStatus, w: Waiter) =
      mediaPlayer.statusProperty.addListener(
        function2ChangeListener {
          (_: MPStatus, newStatus: MPStatus) =>
            if (newStatus == status) w.dismiss()
        }
      )

    if (! (mediaPlayer.getStatus == MediaPlayer.Status.READY))
      isReady.take
  }

  test("given movie doesn't exist, open returns None") {
    assert(Movie.open(NotFoundMoviePath).isEmpty)
  }

  test("given movie isn't in a supported format, open throws exception") {
    intercept[InvalidFormatException] {
      Movie.open(InvalidMoviePath)
    }
  }

  test("when a movie exists and is in a supported format, returns a Movie") {
    val m = Movie.open(ValidMoviePath)
    assert(m.nonEmpty)
  }

  test("when a movie opened remotely doesn't exist, returns None") {
    assert(Movie.openRemote(NotFoundMovieURL).isEmpty)
  }

  test("when a movie is opened remotely with a bad protocol, raised InvalidProtocolException") {
    intercept[InvalidProtocolException] {
      Movie.openRemote(RssMovieURL)
    }
  }

  // the provided video is no longer accessible remotely, need to replace
  // with an equivalent but accessible video (Isaac B 7/16/25)

  // test("when a movie exists remotely, but has invalid format, raises InvalidFormatException") {
  //   intercept[InvalidFormatException] {
  //     Movie.openRemote(InvalidMovieURL)
  //   }
  // }

  //test("opens a movie at a remote location") {
  //  val m = Movie.openRemote(ValidMovieURL)
  //  assert(m.nonEmpty)
  //}

  test("when an attempt is made to setTime outside the time of the media, it raises IllegalArgumentException") {
    new MovieFixture {
      intercept[IllegalArgumentException]{ movie.setTime(-1) }
      intercept[IllegalArgumentException]{ movie.setTime(media.getDuration.toSeconds + 1.0) }
    }
  }

  test("when an attempt is made to setTime to a time within the movie, that time is set") {
    new MovieFixture {
      // unfortunately, the currentTimeProperty only changes when the movie is playing
      // even though getCurrentTime is updated whether or not the movie is playing
      movie.setTime(0.5)
      Thread.sleep(50)
      assert(mediaPlayer.getCurrentTime.equals(Duration.millis(500)))
    }
  }

  test("play starts playback") {
    new MovieFixture {
      val w = new Waiter()
      assert(! movie.isPlaying)
      expectTransition(MPStatus.PLAYING, w)
      movie.play()
      w.await(timeout(Span(100, Millis)), dismissals(1))
      assert(movie.isPlaying)
    }
  }

  test("stop pauses playback") {
    new MovieFixture {
      val w = new Waiter()
      assert(! movie.isPlaying)
      mediaPlayer.statusProperty.addListener(
        function2ChangeListener {
          (oldStatus: MediaPlayer.Status, newStatus: MediaPlayer.Status) =>
            if (newStatus == MediaPlayer.Status.PLAYING) movie.stop()
        }
      )
      movie.play()
      expectTransition(MPStatus.PAUSED, w)
      w.await(timeout(Span(100, Millis)), dismissals(1))
      assert(! movie.isPlaying)
    }
  }

  test("close disposes media player") {
    new MovieFixture {
      val w = new Waiter()
      expectTransition(MPStatus.DISPOSED, w)
      movie.close()
      w.await(timeout(Span(100, Millis)), dismissals(1))
    }
  }

  // this test code doesn't work due to a known Java bug, need to find a workaround (Isaac B 7/16/25)

  // test("captureImage records the image available") {
  //   new MovieFixture {
  //     import javax.imageio.ImageIO
  //     val image = movie.captureImage()

  //     assert(image.isInstanceOf[BufferedImage])
  //     assert(image.getWidth()  == 560)
  //     assert(image.getHeight() == 320)

  //     val expectedOutput = new java.io.FileInputStream(new File("src/test/resources/captured-image.png"))

  //     class VerifyingOutputStream extends java.io.OutputStream {

  //       var position: Int = 0

  //       val IMAGE_SIZE = 179775 // if the image ever changes, this should also change

  //       def write(i: Int): Unit = {
  //         val expectedByte = expectedOutput.read()
  //         // sometime i is negative (go figure), so we need to make it positive before comparing it
  //         assert((i + 256) % 256 == expectedByte, s"Difference at position $position")
  //         position += 1
  //       }

  //       def verify(): Unit = {
  //         assert(position == IMAGE_SIZE)
  //       }
  //     }

  //     try {
  //       val verifyingStream = new VerifyingOutputStream()
  //       val written = ImageIO.write(image, "png", new javax.imageio.stream.MemoryCacheImageOutputStream(verifyingStream))
  //       assert(written)
  //       verifyingStream.verify()
  //     } catch {
  //       case e: Exception =>
  //         val outputStream = new java.io.FileOutputStream("target/failed-img.png")
  //         ImageIO.write(image, "png", outputStream)
  //         println("non-matching image written to target/failed-img.png")
  //         throw e
  //     }
  //   }
  // }
}
