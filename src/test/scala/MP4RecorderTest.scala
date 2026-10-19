package org.nlogo.extensions.vid

import java.awt.image.BufferedImage
import java.nio.file.{ Files, Paths }

import org.scalatest.funsuite.AnyFunSuite

class MP4RecorderTest extends AnyFunSuite {
  trait Helper {
    val recorder = new MP4Recorder()
    val dummyFrame = new BufferedImage(480, 480, BufferedImage.TYPE_INT_ARGB)
    def tempFile = Files.createTempFile("vidrecordertest", "mp4")
  }

  test("recording a frame without starting a recording throws an exception") {
    new Helper {
      intercept[RuntimeException] { recorder.recordFrame(dummyFrame) }
    }
  }

  test("recording a frame after reset throws an exception") {
    new Helper {
      recorder.start()
      recorder.reset()
      intercept[RuntimeException] { recorder.recordFrame(dummyFrame) }
    }
  }

  test("recording a frame succeeds after recorder has been started") {
    new Helper {
      recorder.start()
      recorder.recordFrame(dummyFrame)
    }
  }

  test("starting the recorder twice raises AlreadyStarted") {
    new Helper {
      recorder.start()
      intercept[Recorder.AlreadyStarted.type] { recorder.start() }
    }
  }

  test("save saves a file to the specified location") {
    new Helper {
      val testLocation = tempFile
      recorder.start()
      recorder.recordFrame(dummyFrame)
      recorder.save(testLocation)
      assert(Files.size(testLocation) > 0)
    }
  }

  // this is a sanity-check that we're actually recording something
  test("longer files take more space") {
    new Helper {
      val short = tempFile
      val long = tempFile
      recorder.start()
      recorder.recordFrame(dummyFrame)
      recorder.save(short)
      recorder.start()
      for (i <- 1 to 100) recorder.recordFrame(dummyFrame)
      recorder.save(long)
      assert(Files.size(short) < Files.size(long))
    }
  }

  test("different resolution images can be recorded") {
    new Helper {
      recorder.start()
      recorder.recordFrame(dummyFrame)
      val differentResFrame = new BufferedImage(640, 640, BufferedImage.TYPE_INT_ARGB)
      recorder.recordFrame(differentResFrame)
      recorder.save(tempFile)
    }
  }

  test("saving to a file whose parent directory doesn't exist raises FileNotFoundException") {
    new Helper {
      recorder.start()
      recorder.recordFrame(dummyFrame)
      intercept[java.io.FileNotFoundException] { recorder.save(Paths.get("/nodir/nofile.mp4")) }
    }
  }

  test("saving before a recording has started raises Recorder.NotRecording") {
    new Helper {
      intercept[Recorder.NotRecording.type] { recorder.save(tempFile) }
    }
  }

  test("saving with no frames raises Recorder.NoFrames") {
    new Helper {
      recorder.start()
      intercept[Recorder.NoFrames.type] { recorder.save(tempFile) }
    }
  }
}
