// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

object JOGLLoader {

  def getVersion(classLoader: ClassLoader) = {
    val pkgName = "com.jogamp.opengl"
    val className = "GL"
    try {
      classLoader.loadClass(pkgName + "." + className)
      // is use of getClass correct - AAB 06/2021
      Option(getClass.getClassLoader.getDefinedPackage(pkgName))
        .map(_.getImplementationVersion)
        .getOrElse("not available")
    }
    catch {
      case e: ClassNotFoundException => "not available"
    }
  }
}
