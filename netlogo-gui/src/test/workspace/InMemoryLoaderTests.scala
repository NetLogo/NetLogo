// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URL

import org.nlogo.util.AnyFunSuiteEx

import ExtensionManager.ExtensionData

class InMemoryExtensionLoaderTests extends AnyFunSuiteEx {
  val dummyClassManager = new DummyClassManager()
  val loader = new InMemoryExtensionLoader("foo", dummyClassManager)
  val extURL = new URL("file:/tmp/extension/foo")
  val extensionData =
    new ExtensionData("foo", extURL, "foo", classOf[DummyClassManager].getCanonicalName, Some("7.0"), 0)

  test("InMemoryExtensionLoader returns None when asked to load an extension not matching its specified prefix") {
    assert(loader.locateExtension("bar").isEmpty)
  }

  test("InMemoryExtensionLoader produces a URL uniquely representing its extension") {
    assert(loader.locateExtension("foo") == Some(extURL))
  }

  test("InMemoryExtensionLoader errors when asked for extension data at the wrong URL") {
    intercept[ExtensionManagerException] { loader.extensionData("foo", new URL("file:/tmp/extension/bar")) }
  }

  test("InMemoryExtensionLoader produces data for its extension when given the URL back") {
    assert(loader.extensionData("foo", extURL) == extensionData)
  }

  test("InMemoryExtensionLoader returns the parent classLoader as the ClassLoader to use to load the Extension") {
    val classLoader = getClass.getClassLoader
    assert(loader.extensionClassLoader(extURL, classLoader) == classLoader)
  }

  test("InMemoryExtensionLoader returns the instance of classManager it was initialized with") {
    assert(loader.extensionClassManager(getClass.getClassLoader, extensionData) == dummyClassManager)
  }
}
