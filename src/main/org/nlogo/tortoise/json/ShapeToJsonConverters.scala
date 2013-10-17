// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise.json

import
  org.nlogo.{ api, shape },
    api.Shape,
    shape.{ LinkShape, VectorShape }

import org.json4s._
import scala.language.implicitConversions

sealed trait ShapeConverter[T <: Shape] extends JsonConverter[T]

object ShapeToJsonConverters {

  implicit def shape2Json(shape: Shape): JsonWritable =
    shape match {
      case vs: VectorShape => new VectorShapeConverter(vs)
      case ls: LinkShape   => new LinkShapeConverter(ls)
    }

  class VectorShapeConverter(override protected val target: VectorShape) extends ShapeConverter[VectorShape] {
    import scala.collection.JavaConverters._
    import org.nlogo.tortoise.json.ElemToJsonConverters.elem2Json
    override protected val extraProps = JObject(List(
      "rotate"   -> JBool(target.isRotatable),
      "elements" -> JArray((target.getElements.asScala map (_.toJsonObj)).toList)
    ))
  }

  class LinkShapeConverter(override protected val target: LinkShape) extends ShapeConverter[LinkShape] {
    override protected val extraProps = JObject(List())
  }

}
