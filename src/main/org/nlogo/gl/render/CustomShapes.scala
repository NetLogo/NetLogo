// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.util.{ List => JList, Map => JMap }
import javax.media.opengl.{ GL, GL2, GL2GL3}
import org.nlogo.shape.InvalidShapeDescriptionException
import collection.JavaConverters._

private object CustomShapes {

  case class Description(name: String, lines: List[String]) {
    def javaLines = lines.asJava
  }

  def addNewShape(gl: GL2, shapes: JMap[String, GLShape],
                  shape: Description): Int = {
    val lastList = gl.glGenLists(1)
    shapes.put(shape.name, new GLShape(shape.name, lastList))
    gl.glNewList(lastList, GL2.GL_COMPILE)
    for (line <- shape.lines)
      lineHandler(line).get(gl)
    gl.glEndList()
    lastList
  }

  // serves the dual purpose of checking whether the line is valid (if it isn't None is returned)
  // and associating valid lines with actions
  private def lineHandler(line: String): Option[GL2 => Unit] =
    try line.split(" ") match {
      case Array("tris") =>
        Some(_.glBegin(GL.GL_TRIANGLES))
      case Array("quads") =>
        Some(_.glBegin(GL2GL3.GL_QUADS))
      case Array("stop") =>
        Some(_.glEnd())
      case Array("normal:", s0, s1, s2) =>
        val (f0, f1, f2) = (s0.toFloat, s1.toFloat, s2.toFloat)
        Some(_.glNormal3f(f0, f1, f2))
      case Array(s0, s1, s2) =>
        val (f0, f1, f2) = (s0.toFloat, s1.toFloat, s2.toFloat)
        Some(_.glVertex3f(f0, f1, f2))
      case _ =>
        None
    }
    catch { case _: NumberFormatException => None }

  def updateShapes(gl: GL2, lastList: Int,
                   shapes: JMap[String, GLShape],
                   customShapes: JMap[String, JList[String]]): Int = {
    for (((shapeName, lines), index) <- customShapes.asScala.zipWithIndex) {
      shapes.put(shapeName, new GLShape(shapeName, lastList + index))
      customShapes.put(shapeName, lines)
      gl.glNewList(lastList + index, GL2.GL_COMPILE)
      for (line <- lines.asScala)
        lineHandler(line).get(gl)
      gl.glEndList()
    }
    lastList + customShapes.size
  }

  def readShapes(filename: String): JList[Description] = {
    val lines: Iterator[String] = {
      import java.io.{ File, FileReader, BufferedReader }
      val shapeFile = new File(filename)
      val shapeReader = new BufferedReader(new FileReader(shapeFile))
      Iterator.continually(shapeReader.readLine().trim)
    }
    def readShape(): Description = {
      val name = lines.next()
      val shapeLines = lines.takeWhile(_ != "end-shape").toList
      if(!shapeLines.forall(lineHandler(_).isDefined))
        throw new InvalidShapeDescriptionException
      Description(name, shapeLines)
    }
    (0 until lines.next().toInt)
      .map(_ => readShape())
      .asJava
  }

}
