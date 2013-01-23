package org.nlogo.mirror

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import org.nlogo.{ api, mirror }, api.AgentVariables, mirror._
import Mirrorables._

object JSONSerializer {
  def serialize(update: Update): String = {

    def toJValue(v: AnyRef): JValue = v match {
      case d: java.lang.Double  => JDouble(d)
      case b: java.lang.Boolean => JBool(b)
      case _                    => JString(v.toString)
    }

    def births(k: mirror.Kind): Seq[JField] =
      for {
        Birth(AgentKey(kind, id), values) <- update.births
        if kind == k
        varNames = getImplicitVariables(kind)
        varFields = varNames zip values.map(toJValue)
      } yield JField(id.toString, JObject(varFields: _*))

    def changes(k: mirror.Kind): Seq[JField] =
      for {
        (AgentKey(kind, id), changes) <- update.changes
        if kind == k
        varNames = getImplicitVariables(kind)
        implicitVars = for {
          Change(varIndex, value) <- changes
          varName = if (varNames.length > varIndex) varNames(varIndex) else varIndex.toString
        } yield varName -> toJValue(value)
      } yield JField(id.toString, JObject(implicitVars: _*))

    def deaths(k: mirror.Kind): Seq[JField] =
      for {
        Death(AgentKey(kind, id)) <- update.deaths
        if kind == k
      } yield JField(id.toString, JNull)

    val turtleUpdates = births(Turtle) ++ changes(Turtle) ++ deaths(Turtle)
    val patchUpdates = births(Patch) ++ changes(Patch)
    compact(render(
      ("turtles" -> JObject(turtleUpdates: _*)) ~
        ("patches" -> JObject(patchUpdates: _*))))
  }

  def getImplicitVariables(kind: Kind): Seq[String] =
    kind match {
      case Mirrorables.Turtle =>
        AgentVariables.getImplicitTurtleVariables(false)
      case Mirrorables.Patch =>
        AgentVariables.getImplicitPatchVariables(false)
      case Mirrorables.Link =>
        AgentVariables.getImplicitLinkVariables
      case _ =>
        println("Don't know how to get implicit vars for " + kind.toString)
        Seq()
    }

}
