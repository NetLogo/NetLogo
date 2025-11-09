// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.File
import java.nio.file.Files

import org.nlogo.api.{ AbstractModelLoader, ModelType, Version }
import org.nlogo.swing.AutomationUtils
import org.nlogo.util.GuiTest

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{ Failure, Success }
import scala.util.hashing.MurmurHash3

// make sure all library models can be loaded and resaved in the GUI without changes (Isaac B 10/27/25)
class RoundTripTests extends AnyFunSuite with BeforeAndAfterAll {
  private val extension: String = {
    if (Version.is3D) {
      "nlogox3d"
    } else {
      "nlogox"
    }
  }

  private lazy val fileManager: FileManager = App.app.fileManager
  private lazy val modelLoader: AbstractModelLoader = App.app.modelLoader

  recurseModels(new File("models"))

  override def beforeAll(): Unit = {
    App.reset()
    App.main(Array("--testing", "--automated"))
  }

  private def recurseModels(path: File): Unit = {
    if (path.isDirectory) {
      path.listFiles.foreach(recurseModels)
    } else if (path.getName.endsWith(s".$extension")) {
      test(path.toString, GuiTest.Tag) {
        val oldChecksum = MurmurHash3.stringHash(Files.readString(path.toPath).replace("\r\n", "\n"))
        var newChecksum: Option[Int] = None

        var failures = 0

        // sometimes the EventQueue gets stuck, but it doesn't usually happen multiple times in a row,
        // so if that happens just try to load the model again to avoid needless test failures. (Isaac B 10/27/25)
        while (newChecksum.isEmpty && failures < 5) {
          newChecksum = AutomationUtils.waitForGUI(() => {
            fileManager.openFromPath(path.getAbsolutePath, ModelType.Library, true)

            modelLoader.sourceString(fileManager.currentModel, extension) match {
              case Success(str) =>
                MurmurHash3.stringHash(str)

              case Failure(t) =>
                fail(t)
            }
          }, 15)

          if (newChecksum.isEmpty) {
            failures += 1

            alert("EventQueue invocation took too long, trying again...")
          }
        }

        if (newChecksum.isDefined) {
          assert(newChecksum.contains(oldChecksum))
        } else {
          fail("Too many load timeouts, there is likely an infinite loop in the model loading process.")
        }
      }
    }
  }
}
