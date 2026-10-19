package org.nlogo.extensions.bitmap

import java.io.{ ByteArrayInputStream, IOException }
import java.util.Base64

import javax.imageio.ImageIO

import org.nlogo.api.{ Argument, Context, ExtensionException, Reporter }
import org.nlogo.core.Syntax

class FromBase64 extends Reporter {

  def getSyntax:           Syntax = Syntax.reporterSyntax(ret = Syntax.WildcardType, right = List(Syntax.StringType))
  def getAgentClassString: String = "OTPL"

  def report(args: Array[Argument], context: Context): AnyRef = {

    val arr = args(0).getString.split(",")

    val base64 =
      if (arr.length == 1) {
        arr(0)
      } else if (arr.length == 2) {
        arr(1)
      } else {
        throw new ExtensionException("This primitive only accepts input that is base64-encoded.")
      }

    val bytes = Base64.getDecoder().decode(base64)

    try {
      val image = ImageIO.read(new ByteArrayInputStream(bytes))
      new LogoBitmap(image)
    } catch {
      case ex: IOException => throw new ExtensionException(ex)
    }

  }

}
