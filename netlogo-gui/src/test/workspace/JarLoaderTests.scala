// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.{ File, FileOutputStream, IOException }
import java.net.{ URL, URLClassLoader }
import java.util.jar.{ JarOutputStream, Manifest => JarManifest }
import java.util.zip.ZipEntry

import org.nlogo.api.ClassManager
import ExtensionManager.ExtensionData

import org.scalatest.{ BeforeAndAfter, FunSuite }

class JarLoaderTests extends FunSuite with BeforeAndAfter {
  val dummyWorkspace = new DummyWorkspace

  val jarLoader = new JarLoader(dummyWorkspace)

  val arrayJarURL = new File("extensions/array/array.jar").toURI.toURL
  val madeUpURL   = new File("extensions/foobar/foobar.jar").toURI.toURL

  val dummyJarFile        = new File("tmp/dummy/dummy.jar")
  val dummyJarURL         = dummyJarFile.toURI.toURL
  val dummyJarFileWSpaces = new File("tmp/space path/dummy/space dummy.jar")
  val dummyJarURLWSpaces  = dummyJarFileWSpaces.toURI.toURL
  val otherJarFileWSpaces = new File("tmp/space path/dummy/other.jar")
  val otherJarURLWSpaces  = otherJarFileWSpaces.toURI.toURL
  val dummyOtherJarFile   = new File("tmp/dummy/other.jar")
  val emptyJarFile        = new File("tmp/empty/empty.jar")

  val dummyExtensionData =
    new ExtensionData("dummy", dummyJarURL, "dummy", "org.nlogo.workspace.DummyClassManager", Some("6.0"), 0)

  val dummyManifest = {
    val m = new JarManifest
    val attrs = m.getMainAttributes
    attrs.putValue("NetLogo-Extension-API-Version", "6.0")
    attrs.putValue("Extension-Name", dummyExtensionData.prefix)
    attrs.putValue("Class-Manager", dummyExtensionData.classManagerName)
    m
  }


  before {
    val streams = Seq((dummyJarFile, dummyManifest), (dummyJarFileWSpaces, dummyManifest), (otherJarFileWSpaces, null), (dummyOtherJarFile, null), (emptyJarFile, null)).map {
      case (f, manifest) =>
        if (! f.getParentFile.getParentFile.exists)
          f.getParentFile.getParentFile.mkdir()
        if (! f.getParentFile.exists)
          f.getParentFile.mkdir()
        if (f.exists)
          f.delete()
        if (manifest == null)
          new JarOutputStream(new FileOutputStream(f))
        else
          new JarOutputStream(new FileOutputStream(f), manifest)
    }

    val dummyStream = streams.head
    val dummyClassManagerStream = getClass.getClassLoader.getResourceAsStream("org/nlogo/workspace/DummyClassManager.class")

    dummyStream.putNextEntry(new ZipEntry("org/nlogo/workspace/DummyClassManager.class"))

    var finished = false

    while (! finished) {
      val nextByte = dummyClassManagerStream.read()
      if (nextByte == -1)
        finished = true
      else
        dummyStream.write(nextByte)
    }

    dummyStream.closeEntry

    streams.foreach { s =>
      s.finish()
      s.flush()
      s.close()
    }
  }

  test("locateExtension returns a URL to an extension that exists") {
    val optionURL = jarLoader.locateExtension("array")
    assert(optionURL == Some(arrayJarURL))
  }

  test("locateExtension returns None when an extension doesn't exist in the extensions directory") {
    assert(jarLoader.locateExtension("foobar").isEmpty)
  }

  test("extensionData raises an IOException when it cannot connect to a jar") {
    intercept[IOException] { jarLoader.extensionData("foobar", madeUpURL) }
  }

  test("extensionData returns ExtensionData when it succeeds in connecting to a jar") {
    val extensionData = jarLoader.extensionData("array", arrayJarURL)
    val modified = new File(arrayJarURL.toURI).lastModified()
    assert(extensionData.extensionName == "array")
    assert(extensionData.fileURL == arrayJarURL)
    assert(extensionData.prefix == "array")
    assert(extensionData.classManagerName == "org.nlogo.extensions.array.ArrayExtension")
    assert(extensionData.version == Some("6.0"))
    assert(extensionData.modified == modified)
  }

  test("extensionData raises an error when the jar manifest doesn't have the required fields") {
    intercept[ExtensionManagerException] { jarLoader.extensionData("empty", emptyJarFile.toURI.toURL) }
  }

  test("extensionClassLoader returns a URLClassLoader with the provided loader as a parent") {
    val loader = jarLoader.extensionClassLoader(dummyJarURL, getClass.getClassLoader)
    loader match {
      case ucl: URLClassLoader =>
        assert(ucl.getParent == getClass.getClassLoader)
        assert(ucl.getURLs.contains(dummyJarURL))
        assert(ucl.getURLs.contains(dummyOtherJarFile.toURI.toURL))
      case _ => fail("should create a URLClassLoader")
    }
  }

  test("extensionClassLoader returns a URLClassLoader when the file has a space name") {
    jarLoader.extensionClassLoader(dummyJarURLWSpaces, getClass.getClassLoader) match {
      case ucl: URLClassLoader =>
        assert(ucl.getURLs.contains(dummyJarURLWSpaces))
        assert(ucl.getURLs.contains(otherJarURLWSpaces))
      case _ => fail("should create a URLClassLoader")
    }
  }

  test("extensionClassManager returns a loaded class if the class is valid and can be instantiated with newInstance") {
    val classManager = getClassManager("DummyClassManager")
    assert(classManager.getClass.getName == "org.nlogo.workspace.DummyClassManager")
    assert(classManager.isInstanceOf[ClassManager])
  }

  test("extensionClassManager raises an ExtensionManagerException if the class isn't found") {
    intercept[ExtensionManagerException] { getClassManager("NotAClass") }
  }

  test("extensionClassManager raises an IllegalStateException if the class cannot be instantiated") {
    intercept[IllegalStateException] { getClassManager("UninstantiableClassManager") }
  }

  test("extensionClassManager raises an ExtensionManagerException if the class isn't a ClassManager") {
    intercept[ExtensionManagerException] { getClassManager("NotAClassManager") }
  }

  test("resolvePathAsURL resolves URLs as URLs ") {
    assert(jarLoader.resolvePathAsURL("file:///tmp").get == new URL("file:/tmp"))
  }

  test("resolvePathAsURL resolves paths with slashes relative to the model location") {
    val expectedURL = dummyWorkspace.dummyFileManager.fooExt.toURI.toURL
    assert(jarLoader.resolvePathAsURL("extensions/foo").get == expectedURL)
  }

  test("resolvePathAsURL resolves paths relative to the model location") {
    val expectedURL = dummyWorkspace.dummyFileManager.foobarFile.toURI.toURL
    assert(jarLoader.resolvePathAsURL("foobar").get == expectedURL)
  }

  test("resolvePathAsURL resolves extensions relative to the working directory") {
    val expectedURL = new java.io.File("extensions" + java.io.File.separator + "array").toURI.toURL
    assert(jarLoader.resolvePathAsURL("array").get == expectedURL)
  }

  test("resolvePathAsURL returns None if the file cannot be found") {
    assert(jarLoader.resolvePathAsURL("notfound").isEmpty)
  }

  def getClassManager(className: String): ClassManager = {
    jarLoader.extensionClassManager(getClass.getClassLoader,
      dummyExtensionData.copy(classManagerName = s"org.nlogo.workspace.$className"))
  }
}

class DummyClassManager extends ClassManager {
  val barPrim = new org.nlogo.core.Primitive {
    def getAgentClassString: String = ???
    def getSyntax: org.nlogo.core.Syntax = ???
  }
  var methodsRun: Seq[String] = Seq()
  def additionalJars: java.util.List[String] = null
  def clearAll(): Unit = methodsRun = methodsRun :+ "clearAll"
  def exportWorld: java.lang.StringBuilder = null
  def importWorld(lines: java.util.List[Array[String]],reader: org.nlogo.api.ExtensionManager,handler: org.nlogo.api.ImportErrorHandler): Unit = methodsRun = methodsRun :+ "importWorld"
  def load(primManager: org.nlogo.api.PrimitiveManager): Unit = {
    primManager.addPrimitive("bar", barPrim)
    methodsRun = methodsRun :+ "load"
  }
  def readExtensionObject(reader: org.nlogo.api.ExtensionManager,typeName: String,value: String): org.nlogo.core.ExtensionObject = null
  def runOnce(em: org.nlogo.api.ExtensionManager): Unit = methodsRun = methodsRun :+ "runOnce"
  def unload(em: org.nlogo.api.ExtensionManager): Unit = methodsRun = methodsRun :+ "unload"
}

class UninstantiableClassManager(foo: String, bar: Int) extends DummyClassManager

class NotAClassManager
