// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import scala.language.implicitConversions
import java.io.{ ByteArrayOutputStream, DataOutputStream, DataOutput,
                 ByteArrayInputStream, DataInputStream }
import collection.immutable.{ ListMap, Vector }
import org.nlogo.{ api, shape }

object Serializer {

  val UnknownType = 0
  val StringType = 1
  val IntType = 2
  val LongType = 3
  val DoubleType = 4
  val TrueType = 5
  val FalseType = 6
  val PairType = 7
  val NoneType = 8
  val SomeType = 9
  val SeqType = 10
  val ListMapType = 11
  val ByteArrayType = 12
  val ShapeListType = 13
  val LogoListType = 14

  private var missingTypes = Set[String]()

  def toBytes(update: Update): Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    val data = new DataOutputStream(bytes)
    serialize(update, data)
    bytes.toByteArray
  }

  def serialize(update: Update, data: DataOutput) {
    def writeValue(x: AnyRef) {
      x match {
        case s: String =>
          data.writeByte(StringType)
          data.writeUTF(s)
        case i: java.lang.Integer =>
          data.writeByte(IntType)
          data.writeInt(Int.unbox(i))
        case l: java.lang.Long =>
          data.writeByte(LongType)
          data.writeLong(Long.unbox(l))
        case d: java.lang.Double =>
          data.writeByte(DoubleType)
          data.writeDouble(Double.unbox(d))
        case b: java.lang.Boolean =>
          data.writeByte(if (Boolean.unbox(b)) TrueType else FalseType)
        case (x1: AnyRef, x2: AnyRef) =>
          data.writeByte(PairType)
          writeValue(x1)
          writeValue(x2)
        case xs: Seq[_] =>
          data.writeByte(SeqType)
          writeSeq(xs.asInstanceOf[Seq[AnyRef]])
        case xs: api.LogoList =>
          data.writeByte(LogoListType)
          writeSeq(xs.toVector)
        case xs: ListMap[_, _] =>
          data.writeByte(ListMapType)
          writeSeq(xs.toSeq)
        case None =>
          data.writeByte(NoneType)
        case Some(x: AnyRef) =>
          data.writeByte(SomeType)
          writeValue(x)
        case bytes: Array[Byte] =>
          data.writeByte(ByteArrayType)
          data.writeInt(bytes.size)
          data.write(bytes, 0, bytes.size)
        case shapes: api.ShapeList =>
          import collection.JavaConverters._
          data.writeByte(ShapeListType)
          data.writeUTF(shapes.kind.toString)
          writeSeq(shapes.getShapes.asScala.map(_.toString))
        case _ =>
          data.writeByte(UnknownType)
          val name = x.getClass.toString
          if(!missingTypes(name)) {
            System.err.println("can't serialize " + name)
            missingTypes += name
          }
      }
    }
    def writeSeq(xs: Seq[AnyRef]) {
      data.writeInt(xs.size)
      xs.foreach(writeValue)
    }
    data.writeInt(update.deaths.size)
    for(Death(AgentKey(kind, id)) <- update.deaths) {
      data.writeByte(kind)
      data.writeLong(id)
    }
    data.writeInt(update.births.size)
    for(Birth(AgentKey(kind, id), values) <- update.births) {
      data.writeByte(kind)
      data.writeLong(id)
      writeSeq(values)
    }
    data.writeInt(update.changes.size)
    for((AgentKey(kind, id), changes) <- update.changes) {
      data.writeByte(kind)
      data.writeLong(id)
      data.writeInt(changes.size)
      for(Change(variable, value) <- changes) {
        data.writeInt(variable)
        writeValue(value)
      }
    }
  }

  // TODO cache the AgentKey objects - ST 7/23/12

  def fromBytes(bytes: Array[Byte]): Update = {
    val data = new DataInputStream(
      new ByteArrayInputStream(bytes))
    def readValue(): AnyRef =
      data.readByte().toInt match {
        case UnknownType =>
          null
        case StringType =>
          data.readUTF()
        case IntType =>
          Int.box(data.readInt())
        case LongType =>
          Long.box(data.readLong())
        case DoubleType =>
          Double.box(data.readDouble())
        case TrueType =>
          java.lang.Boolean.TRUE
        case FalseType =>
          java.lang.Boolean.FALSE
        case PairType =>
          (readValue(), readValue())
        case NoneType =>
          None
        case SomeType =>
          Some(readValue())
        case SeqType =>
          readValues()
        case LogoListType =>
          api.LogoList.fromVector(readValues())
        case ListMapType =>
          ListMap(readValues().asInstanceOf[Seq[(_, _)]]: _*)
        case ByteArrayType =>
          val size = data.readInt()
          val bytes = new Array[Byte](size)
          data.read(bytes, 0, size)
          bytes
        case ShapeListType =>
          val kind = data.readUTF()
          val parser: Array[String] => java.util.List[api.Shape] =
            kind match {
              case "Turtle" =>
                shape.VectorShape.parseShapes(_, api.Version.version)
              case "Link" =>
                shape.LinkShape.parseShapes(_, api.Version.version)
            }
          val result = new api.ShapeList(
            kind match {
              case "Turtle" =>
                api.AgentKind.Turtle
              case "Link" =>
                api.AgentKind.Link
            })
          result.replaceShapes(
            parser(
              readValues().mkString("\n\n").split("\n")))
          result
      }
    def readValues(): Vector[AnyRef] =
      (for(_ <- 0 until data.readInt())
       yield readValue())(collection.breakOut)
    def readAgentKey(): AgentKey =
      AgentKey(kind = agentKindFromInt(data.readByte().toInt),
               id = data.readLong())
    var deaths = Vector[Death]()
    for(_ <- 0 until data.readInt())
      deaths :+= Death(readAgentKey())
    var births = Vector[Birth]()
    for(_ <- 0 until data.readInt())
      births :+= Birth(readAgentKey(),
                       readValues())
    var changes = Vector[(AgentKey, Seq[Change])]()
    for(_ <- 0 until data.readInt())
      changes :+= ((readAgentKey(),
                   (for(_ <- 0 until data.readInt())
                    yield Change(data.readInt(), readValue()))))
    Update(deaths = deaths,
           births = births,
           changes = changes)
  }

  val agentKindFromInt: Int => Kind =
    Seq(Mirrorables.Observer,
        Mirrorables.Turtle,
        Mirrorables.Patch,
        Mirrorables.Link,
        Mirrorables.World,
        Mirrorables.WidgetValue)

  implicit def agentKindToInt(kind: Kind): Int =
    kind match {
      case Mirrorables.Observer => 0
      case Mirrorables.Turtle => 1
      case Mirrorables.Patch => 2
      case Mirrorables.Link => 3
      case Mirrorables.World => 4
      case Mirrorables.WidgetValue => 5
    }

}
