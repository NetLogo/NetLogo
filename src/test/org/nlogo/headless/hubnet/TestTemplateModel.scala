package org.nlogo.headless.hubnet

import org.nlogo.hubnet.protocol.WidgetControl

// this test doesnt work in sbt. see SimpleTestClient for details
class TestTemplateModel extends Base {

  hubnetTest("test client login") {
    login()
    reporter("count turtles") -> 1
  }

  hubnetTest("test multiple client login") {
    loginN(10)
    reporter("count turtles") -> 10
  }

  hubnetTest("have client send an activity message") {
    val client = login()

    client.sendActivityCommandAndWait("up", false)

    observer>> "go"

    client.assertNextMessage(WidgetControl("(0,1)","location"))
    client.assertNextMessage(ViewUpdate)
  }

  templateLoadTest(
    "have 10 clients send 5*2 activity messages at once",
    numClients = 10,
    numMessages = 5,
    loginTimeoutMillis = 1000, // give 1 second for each client to connect
    messagesTimeoutMillis = 10000 // only wait up to 10 seconds for the messages to arrive
  )
}

// this test doesnt work in sbt. see SimpleTestClient for details
// also, at least in intellij, i have to make the heap bigger here. i use 512m.
// im not sure how small i could get away with.
// it would be a good exercise to figure out how many messages we can actually hold on a server
// before we collapse, given a particular heap size.

// these tests are not running as of 3/31/11. i plan to revisit them soon. -JC.
//  templateLoadTest(
//    "have 200 clients send 10*2 activity messages at once",
//    numClients = 200,
//    numMessages = 10,
//    loginTimeoutMillis = 2000, // give 2 seconds for each client to connect
//    messagesTimeoutMillis = 10000 // wait up to 10 seconds for the messages to arrive

abstract class Base extends
  TestHeadless("models/HubNet Activities/Code Examples/Template.nlogo"){

  def login(clientName:String="client", timeoutMillis: Long = 1000) = {
    val client = waitForNMessages (1, timeoutMillis){ new InSameJVMTestClient(clientName) }

    // one view update is automatic when a client logs in.
    client.assertNextMessage(ViewUpdate, timeoutMillis)

    // this will process the EnterMessage for this client
    // and then send back a location message.
    observer>> "listen-clients"

    // get that location message and make sure its good.
    // it should always be the origin, because thats where this model places the turtles.
    client.assertNextMessage(WidgetControl("(0,0)","location"), timeoutMillis)

    // do this just for good measure
    // call display, and make sure a view update comes back to the client.
    observer>>"display"
    client.assertNextMessage(ViewUpdate)
    client
  }

  def loginN(numClients:Int, timeoutMillis: Long = 1000) =
    for (i <- 1 to numClients) yield login("client" + i, timeoutMillis)

  def templateLoadTest(name:String, numClients: Int, numMessages:Int,
                       loginTimeoutMillis: Long, messagesTimeoutMillis: Long){
    hubnetTest(name) {
      // log in N clients
      val clients = loginN(numClients, loginTimeoutMillis)

      // have them each send numMessages up and numMessages right messages
      // wait up to
      waitForNMessages(numClients * numMessages * 2, messagesTimeoutMillis){
        for (client <- clients; i <- 1 to numMessages) {
          client.sendActivityCommand("up", false)
          client.sendActivityCommand("right", false)
        }
      }

      // "listen-clients" consumes all each up and right message
      // and sends the location back to the client for each message.
      // so a client at 1,1 who goes up and then right will get two messages:
      // (1,2) and then (2,2)
      observer >> "listen-clients"

      // check all the messages for each client to make sure they got all 10 location updates
      for (client <- clients) {
        // clients get lots of view updates when other clients log in
        // because we say "go" in the login function
        // discard all the view updates here, and just focus on the location updates
        val messages = client.getWidgetControls
        for (i <- 0 to (numMessages - 1)) {
          val index = i * 2
          // first message is from pressing up, so the y increments
          assert(messages(index) === WidgetControl("(" + i + "," + (i+1) + ")", "location"))
          // next message is from pressing right, so the x increments.
          assert(messages(index + 1) === WidgetControl("(" + (i+1) + "," + (i+1) + ")", "location"))
        }
      }
      // just for good measure, make sure there are 200 clients
      reporter("count turtles") -> numClients
    }
  }
}
