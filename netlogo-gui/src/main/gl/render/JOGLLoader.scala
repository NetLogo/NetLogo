// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

object JOGLLoader {

  def getVersion(classLoader: ClassLoader) = {
    val pkgName = "com.jogamp.opengl"
    val className = "GL"
    try {
      classLoader.loadClass(pkgName + "." + className)
      Option(Package.getPackage(pkgName))
        .map(_.getImplementationVersion)
        .getOrElse("not available")
    }
    catch {
      case e: ClassNotFoundException => "not available"
    }
  }
}
