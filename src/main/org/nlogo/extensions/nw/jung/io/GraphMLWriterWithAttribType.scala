// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw.jung.io

import java.io.BufferedWriter
import java.io.IOException

import org.apache.commons.collections15.Transformer

import edu.uci.ics.jung.io.GraphMLMetadata
import edu.uci.ics.jung.io.GraphMLWriter

/**
 * Subclass of jung.io.GraphMLWriter that,
 * unlike Jung's, writes out attrib.type.
 * NP 2013-06-21
 */
class GraphMLWriterWithAttribType[V, E] extends GraphMLWriter[V, E] {

  // map indexed by concatenation of "node" or "edge" with attribute id
  val attribTypes = collection.mutable.Map[String, String]()

  def addVertexData(id: String, description: String, default_value: String,
    attrType: String, vertex_transformer: Transformer[V, String]): Unit = {
    super.addVertexData(id, description, default_value, vertex_transformer)
    attribTypes += ("node" + id) -> attrType
  }

  def addEdgeData(id: String, description: String, default_value: String,
    attrType: String, edge_transformer: Transformer[E, String]): Unit = {
    super.addEdgeData(id, description, default_value, edge_transformer)
    attribTypes += ("edge" + id) -> attrType
  }

  /**
   * This is a direct translation of super.writeKeySpecification
   *  except for the writing of attrib.type if appropriate
   */
  @throws(classOf[IOException])
  override def writeKeySpecification(
    key: String, `type`: String,
    ds: GraphMLMetadata[?], bw: BufferedWriter): Unit = {

    bw.write("<key id=\"" + key + "\" for=\"" + `type` + "\"")

    // This is the only addition to the super method:
    for (attrType <- attribTypes.get(`type` + key) if attrType != "")
      bw.write(" attr.name=\"" + key + "\" attr.type=\"" + attrType + "\"")

    var closed = false
    // write out description if any
    val desc = ds.description
    if (desc != null) {
      if (!closed) {
        bw.write(">\n")
        closed = true
      }
      bw.write("<desc>" + desc + "</desc>\n")
    }
    // write out default if any
    val `def`: Any = ds.default_value
    if (`def` != null) {
      if (!closed) {
        bw.write(">\n")
        closed = true
      }
      bw.write("<default>" + `def`.toString() + "</default>\n")
    }
    if (!closed)
      bw.write("/>\n")
    else
      bw.write("</key>\n")
  }
}
