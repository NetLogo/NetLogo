package org.nlogo.gl.render

import java.util.{ List => JList, Map => JMap }
import javax.media.opengl.GL
import org.nlogo.shape.InvalidShapeDescriptionException
import collection.JavaConverters._

private object CustomShapes {

  def addNewShape(gl: GL, shapes: JMap[String, GLShape], customShapes: JMap[String, JList[String]],
                  shape: CustomShapeDescription): Int = {
    val lastList = gl.glGenLists(1)
    shapes.put(shape.name, new GLShape(shape.name, lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    for (next <- shape.lines.asScala)
      if (next == "tris")
        gl.glBegin(GL.GL_TRIANGLES)
      else if (next == "quads")
        gl.glBegin(GL.GL_QUADS)
      else if (next == "stop")
        gl.glEnd()
      else if (next.startsWith("normal: "))
        next.substring(8).split(" ") match {
          case Array(f0, f1, f2) =>
            gl.glNormal3f(f0.toFloat, f1.toFloat, f2.toFloat)
        }
      else next.split(" ") match {
        case Array(f0, f1, f2) =>
          gl.glVertex3f(f0.toFloat, f1.toFloat, f2.toFloat)
      }
    gl.glEndList()
    customShapes.put(shape.name, shape.lines)
    lastList
  }

  private def isVertex(line: String): Boolean =
    try line.split(" ") match {
      case fs @ Array(_, _, _) =>
        fs.foreach(_.toFloat)
        true
      case _ => false
    }
    catch {
      case e: NumberFormatException =>
        false
    }

  def updateShapes(gl: GL, lastList: Int,
                   shapes: JMap[String, GLShape],
                   customShapes: JMap[String, JList[String]]): Int = {
    import collection.JavaConverters._
    var more = 0
    for (shapeName <- customShapes.keySet.asScala) {
      more += 1
      shapes.put(shapeName, new GLShape(shapeName, lastList + more))
      val lines = customShapes.get(shapeName)
      customShapes.put(shapeName, lines)
      gl.glNewList(lastList + more, GL.GL_COMPILE)
      for (next <- lines.asScala)
        if (next == "tris")
          gl.glBegin(GL.GL_TRIANGLES)
        else if (next == "quads")
          gl.glBegin(GL.GL_QUADS)
        else if (next == "stop")
          gl.glEnd()
        else if (next.startsWith("normal: "))
          next.substring(8).split(" ") match {
            case Array(f0, f1, f2) =>
              gl.glNormal3f(f0.toFloat, f1.toFloat, f2.toFloat)
          }
        else next.split(" ") match {
          case Array(f0, f1, f2) =>
            gl.glVertex3f(f0.toFloat, f1.toFloat, f2.toFloat)
        }
      gl.glEndList()
    }
    lastList + more
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[InvalidShapeDescriptionException])
  def readShapes(filename: String): JList[CustomShapeDescription] = {
    import java.io._
    val shapeFile = new File(filename)
    val shapeReader = new BufferedReader(new FileReader(shapeFile))
    val line = shapeReader.readLine()
    var shapeCount = line.toInt
    val result = new java.util.ArrayList[CustomShapeDescription]
    for(i <- 0 until shapeCount) {
      val shapeName = shapeReader.readLine()
      var next = shapeReader.readLine()
      val shape = new CustomShapeDescription(shapeName)
      while (next != "end-shape") {
        if (next == "tris" ||
            next == "quads" ||
            next == "stop" ||
            next.startsWith("normal:") ||
            isVertex(next))
          shape.lines.add(next)
        else
          throw new InvalidShapeDescriptionException
        next = shapeReader.readLine()
      }
      result.add(shape)
    }
    result
  }

}
