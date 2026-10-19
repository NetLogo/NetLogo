package org.nlogo.extensions.vid

import java.awt.image.BufferedImage

import javafx.scene.shape.Rectangle

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import org.nlogo.api.ExtensionException

import scala.language.dynamics

trait VidHelpers { suite: AnyFeatureSpec & GivenWhenThen =>
  trait VidSpecHelpers extends WithLoadedVidExtension {

    class GivenWhenThenAndRunner(backing: CommandPrimitiveLoader, gwta: (String) => Unit) extends Dynamic {
      def applyDynamic(name: String)(args: AnyRef*): AnyRef = {
        val argStrings = args.map {
          case s: String => s""""$s""""
          case other => other.toString
        }
        gwta(s"I run vid:$name ${argStrings.mkString(" ")}")
        backing.applyDynamic(name)(args*)
      }
    }

    lazy val givenIHave = new GivenWhenThenAndRunner(vid, Given)
    lazy val andIRun    = new GivenWhenThenAndRunner(vid, And)
    lazy val whenIRun   = new GivenWhenThenAndRunner(vid, When)

    val dummyImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB)

    class DummySource(
      val startPlaying: Boolean,
      nodeConstructor: Option[(Double, Double)] => BoundedNode) extends VideoSource {
      var isPlaying = startPlaying
      var isClosed  = false
      override def play(): Unit = { isPlaying = true }
      override def stop(): Unit = { isPlaying = false }
      def close(): Unit = { isClosed = true }
      def captureImage() = dummyImage
      def setTime(time: Double): Unit = {
        if (time < 0)
          throw new IllegalArgumentException("bad time!")
      }

      override def videoNode(bounds: Option[(Double, Double)]) =
        nodeConstructor(bounds)
    }

    val dummyMovie = new DummySource(false, new MovieNode(_))

    val dummyCamera = new DummySource(true, new CameraNode(_))

    val movieFactory = new MovieFactory {
      override def open(filePath: String): Option[VideoSource] = {
        filePath match {
          case "/currentdir/foobar.mp4"      => Some(dummyMovie)
          case "/currentdir/unsupported.ogg" => throw new InvalidFormatException
          case "/tmp/foobar.mp4"             => Some(dummyMovie)
          case _ => None
        }
      }

      override def openRemote(uri: String): Option[VideoSource] = {
        uri match {
          case "http://example.org/somevideo.mp4"  => Some(dummyMovie)
          case "https://example.org/somevideo.mp4" => throw new InvalidProtocolException()
          case "http://example.org/somevideo.ogv"  => throw new InvalidFormatException()
          case _ => None
        }
      }
    }

    val cameraFactory = new CameraFactory {
      var cameraNames = Seq("camera")
      var defaultCameraName = cameraNames.headOption
      override def open(cameraName: String): Option[VideoSource] = {
        cameraName match {
          case "camera" => Some(dummyCamera)
          case _        => None
        }
      }
    }

    override val player: DummyPlayer = new DummyPlayer()

    val recorder: DummyRecorder = new DummyRecorder()

    val selector: DummySelector = new DummySelector()

    def givenOpenMovie(started: Boolean = false): Unit = {
      suite.Given("I have opened a movie")
      vid.`movie-open`("foobar.mp4")
      if (started) {
        suite.And("I have stared the movie")
        vid.start()
      }
    }

    def thenStatusShouldBe(status: String): Unit = {
      suite.Then(s"""vid:status should show "$status"""")
      assert(vid.`status`() == status)
    }

    def andStatusShouldBe(status: String): Unit = {
      suite.And(s"""vid:status should show "$status"""")
      assert(vid.`status`() == status)
    }

    def shouldMatchBufferedImage(capturedImage: AnyRef): Unit = {
      suite.Then("I should get a BufferedImage matching the image from the video source")
      assert(capturedImage == dummyImage)
    }
  }

  trait ExpectError {
    this: VidSpecHelpers =>

    var _error = Option.empty[ExtensionException]

    def whenRunForError(errorCondition: String, f: => Unit): Unit = {
      suite.When(s"I run $errorCondition")
      try {
        f
        suite.fail(s"expected $errorCondition to error")
      } catch {
        case e: ExtensionException => _error = Some(e)
      }
    }

    def thenShouldSeeError(errorMessage: String): Unit = {
      suite.Then(s"I should see an error - $errorMessage")
      assert(_error.nonEmpty)
      assert(_error.get.getMessage.contains(errorMessage))
    }
  }
}

class DummyRecorder extends Recorder {
  import java.nio.file.{ Files, Path }
  var isRecording = false
  var lastFrame: BufferedImage = null
  var recordingResolution: (Int, Int) = (-1, -1)
  def start(): Unit = {
    if (isRecording)
      throw Recorder.AlreadyStarted
    isRecording = true
  }
  def reset(): Unit = {
    isRecording = false
  }
  def recordFrame(image: BufferedImage): Unit = {
    lastFrame = image
  }
  def setResolution(width: Int, height: Int): Unit = {
    recordingResolution = (width, height)
  }
  def save(dest: Path): Unit = {
    if (!isRecording)
      throw Recorder.NotRecording
    val d =
      if (dest.startsWith("/currentdir")) dest.subpath(1, dest.getNameCount)
      else dest
    if (! Files.exists(d.toAbsolutePath.getParent))
      throw new java.io.FileNotFoundException("no such directory: " + d.toString)
    if (lastFrame == null)
      throw Recorder.NoFrames
    Files.write(d, "test".getBytes)
  }
}

class DummyPlayer extends Player {

  var activeNode: BoundedNode = null
  def boundedSize = Option(activeNode).flatMap(_.enforcedBounds)
  var isShowing = false
  def hide() = { isShowing = false }
  def show(): Unit = { isShowing = true }
  def emptyNode(bounds: Option[(Double, Double)]): BoundedNode =
    new EmptyNode(bounds)
  def present(theNode: BoundedNode) =
    activeNode = theNode
}

class DummySelector extends Selector {
  var selected: Option[String] = None

  override def selectOneOf(choices: Seq[String]): Option[String] = selected

  override def selectFile: Option[String] = selected

  def select(name: String) = { selected = Some(name) }

  def cancel(): Unit =
    selected = None
}

class DummyNode(bds: Option[(Double, Double)])
  extends BoundedNode(
    new Rectangle(bds.map(_._1).getOrElse(640), bds.map(_._2).getOrElse(480)), null, bds)

class EmptyNode(enforcedBounds: Option[(Double, Double)])
  extends DummyNode(enforcedBounds)

class MovieNode(enforcedBounds: Option[(Double, Double)])
  extends DummyNode(enforcedBounds)

class CameraNode(enforcedBounds: Option[(Double, Double)])
  extends DummyNode(enforcedBounds)
