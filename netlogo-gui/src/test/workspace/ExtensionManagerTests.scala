// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URL

import org.nlogo.api.ExtensionException
import ExtensionManager.ExtensionLoader

import org.scalatest.{ BeforeAndAfter, FunSuite }

import scala.collection.JavaConverters._

class ExtensionManagerTests extends FunSuite with BeforeAndAfter {
  before {
    AbstractWorkspace.isApplet(false)
  }

  after {
    AbstractWorkspace.isApplet(true)
  }

  val dummyWorkspace = new DummyWorkspace
  val emptyManager = new ExtensionManager(dummyWorkspace, new JarLoader(dummyWorkspace))

  class ErrorSourceException extends Exception("problem")

  trait LoadingExtensionTest {
    var errorMessage: String = null
    val errorSource = new org.nlogo.core.ErrorSource(null) {
      override def signalError(message: String): Nothing = {
        errorMessage = message
        throw new ErrorSourceException()
      }
    }
    def extraLoaders: Seq[ExtensionLoader] = Seq()

    lazy val loadingManager = {
      val m = new ExtensionManager(dummyWorkspace, new JarLoader(dummyWorkspace))
      extraLoaders.foreach(m.addLoader)
      m
    }
  }

  trait WithLoadedArrayExtension extends LoadingExtensionTest {
    lazy val loadedManager = {
      loadingManager.importExtension("array", errorSource)
      loadingManager
    }
  }

  trait InMemoryExtensionTest {
    lazy val dummyClassManager = new DummyClassManager()
    lazy val memoryLoader = new InMemoryExtensionLoader("foo", dummyClassManager)
    lazy val inmemoryManager = new ExtensionManager(dummyWorkspace, memoryLoader)
  }

  test("loadedExtensions returns empty list when no extensions loaded") {
    assert(emptyManager.loadedExtensions.asScala.isEmpty)
  }

  test("loadedExtensions returns a list of extensions when extensions loaded") {
    new WithLoadedArrayExtension {
      assert(loadedManager.loadedExtensions.asScala.nonEmpty)
      assert(loadedManager.loadedExtensions.asScala.head.getClass.getCanonicalName == "org.nlogo.extensions.array.ArrayExtension")
    }
  }

  test("anyExtensionsLoaded returns false with no extensions loaded") {
    assert(! emptyManager.anyExtensionsLoaded)
  }

  test("anyExtensionsLoaded returns true when one extension is loaded") {
    new WithLoadedArrayExtension {
      assert(loadedManager.anyExtensionsLoaded)
    }
  }

  test("retrieveObject returns null when no object has been stored") {
    assert(emptyManager.retrieveObject == null)
  }

  test("retrieveObject reads objects set by storeObject") {
    emptyManager.storeObject("foo")
    assert(emptyManager.retrieveObject == "foo")
  }

  test("importExtension signals an error when extension doesn't exist") {
    new LoadingExtensionTest {
      intercept[ErrorSourceException] {
        loadingManager.importExtension("notfound", errorSource)
      }
      assert(errorMessage == ExtensionManager.EXTENSION_NOT_FOUND + "notfound")
    }
  }

  test("importExtension succeeds if the extension is located and valid") {
    new LoadingExtensionTest {
      loadingManager.importExtension("array", errorSource)
      assert(errorMessage == null)
      assert(loadingManager.anyExtensionsLoaded)
    }
  }

  test("importExtension loads from an alternate loader, if provided") {
    new LoadingExtensionTest with InMemoryExtensionTest {
      override def extraLoaders = Seq(memoryLoader)
      loadingManager.importExtension("foo", errorSource)
      assert(errorMessage == null)
      assert(loadingManager.anyExtensionsLoaded)
    }
  }

  test("readFromString proxies through to workspace") {
    assert(emptyManager.readFromString("foobar") == "foobar")
  }

  test("clearAll runs clearAll on all jars") {
    new InMemoryExtensionTest {
      inmemoryManager.importExtension("foo", null)
      inmemoryManager.clearAll()
      assert(dummyClassManager.methodsRun.contains("clearAll"))
    }
  }

  test("dumpExtensions prints an empty table when no extensions have been loaded") {
    assert(emptyManager.dumpExtensions ==
      """|EXTENSION	LOADED	MODIFIED	JARPATH
         |---------	------	--------	-------
         |""".stripMargin)
  }

  test("dumpExtensions prints a table with all loaded extensions") {
    new WithLoadedArrayExtension {
      val arrayJar = new java.io.File("extensions/array/array.jar")
      val modified = arrayJar.lastModified()
      val path = arrayJar.toURI.toURL.toString
      assert(loadedManager.dumpExtensions ==
        s"""|EXTENSION	LOADED	MODIFIED	JARPATH
            |---------	------	--------	-------
            |array	true	$modified	$path
            |""".stripMargin)
    }
  }

  test("dumpExtensionPrimitives prints an empty table when no extensions are loaded") {
    assert(emptyManager.dumpExtensionPrimitives ==
      """|EXTENSION	PRIMITIVE	TYPE
         |---------	---------	----
         |""".stripMargin)
  }

  test("dumpExtensionPrimitives prints a table with all loaded primitives") {
    new WithLoadedArrayExtension {
      assert(loadedManager.dumpExtensionPrimitives ==
        """|EXTENSION	PRIMITIVE	TYPE
           |---------	---------	----
           |array	TO-LIST	Reporter
           |array	SET	Command
           |array	ITEM	Reporter
           |array	LENGTH	Reporter
           |array	FROM-LIST	Reporter
           |""".stripMargin)
    }
  }

  test("reset unloads and clears all jars") {
    new WithLoadedArrayExtension {
      loadedManager.reset()
      assert(! loadedManager.anyExtensionsLoaded)
      assert(loadedManager.loadedExtensions.asScala.isEmpty)
    }
  }

  test("startFullCompilation can be called without error") {
    emptyManager.startFullCompilation()
  }

  test("finishFullCompilation doesn't error when specified extension went unused in compilation") {
    new WithLoadedArrayExtension {
      loadedManager.startFullCompilation()
      loadedManager.finishFullCompilation()
      assert(loadedManager.loadedExtensions.asScala.isEmpty)
    }
  }

  test("finishFullCompilation does not remove live jars if they are used during compilation") {
    new WithLoadedArrayExtension {
      loadedManager.startFullCompilation()
      loadedManager.importExtension("array", errorSource)
      loadedManager.finishFullCompilation()
      assert(loadedManager.loadedExtensions.asScala.toSeq.length == 1)
    }
  }

  test("importExtensionData takes an extension name, a bunch of data, and an importHandler, and imports the world for an extension") {
    new WithLoadedArrayExtension {
      loadedManager.importExtensionData("array", List(Array("{{array: 0: 0 0 0 0 0}}")).asJava, null)
    }
  }

  test("importExtensionData errors with ExtensionException if the named extension can't be loaded") {
    intercept[ExtensionException] {
      emptyManager.importExtensionData("notfound", List[Array[String]]().asJava, null)
    }
  }

  test("isExtensionName returns false when no extension of that name is loaded") {
    assert(! emptyManager.isExtensionName("array"))
  }

  test("isExtensionName returns true when the extension is loaded") {
    new WithLoadedArrayExtension {
      assert(loadedManager.isExtensionName("array"))
    }
  }

  test("finishFullCompilation catches exceptions thrown by the jar when unloading") {
    new InMemoryExtensionTest {
      override lazy val dummyClassManager = new DummyClassManager() {
        override def unload(em: org.nlogo.api.ExtensionManager) = { super.unload(em); throw new Exception("stuff") }
      }
      inmemoryManager.importExtension("foo", null)
      inmemoryManager.startFullCompilation()
      inmemoryManager.finishFullCompilation()
      assert(dummyClassManager.methodsRun.contains("unload"))
    }
  }
}
