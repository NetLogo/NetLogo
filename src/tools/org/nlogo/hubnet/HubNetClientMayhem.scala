package org.nlogo.hubnet

import org.nlogo.hubnet.protocol.TestClient

object HubNetClientMayhem {
  def main(args: Array[String]) {
    val random = new util.Random()
    val messages = List("left", "right", "up", "down")
    val clients = for (i <- 1 to 300) yield new TestClient(i.toString)
    Thread.sleep(10000)
    while(true){
      for( c <- clients ) c.sendActivityCommand(messages(random.nextInt(4)), false)
      Thread.sleep(200)
    }
  }
}
