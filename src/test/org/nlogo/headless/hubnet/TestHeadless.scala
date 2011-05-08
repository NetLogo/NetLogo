package org.nlogo.headless.hubnet

import org.nlogo.headless.AbstractTestModels
import org.nlogo.util.SlowTest
import org.scalatest.{Reporter, Stopper, Filter, Distributor, Tracker}
import java.util.concurrent.Executors
import org.nlogo.hubnet.protocol.{TestClient, Message, ViewUpdate => ViewUp}

// TODO:
// Commented out the test method here. tests will not run.
// they are too fragile right now and i need to spend more time on understanding why
// right now, its the tests themselves causing the problems, not the code.

// this test doesnt work in sbt. see SimpleTestClient for details
abstract class TestHeadless(modelName:String) extends AbstractTestModels with SlowTest {

  def hubnetTest(name:String)(f: => Unit){
    testModelFile(name, modelName) {
      workspace.hubnetManager.reset()
      workspace.command("setup")
      // this next line seems necessary to allow the server socket to start up.
      // otherwise we seem to regularly get 'Connection refused' errors
      Thread.sleep(50)
      f
    }
  }

  // we cant reasonably create the byte array that goes in the ViewUpdate
  // instead, when asserting, we just check if things are ViewUpdates.
  // this is a little helper so we dont have to put ViewUpdate(null) all over.
  def ViewUpdate = new ViewUp(null)

  // use an executor here so we dont have to start a new thread for every client.
  implicit val executor = Executors.newCachedThreadPool()

  class InSameJVMTestClient(userId: String) extends TestClient(userId) {
    // sends a message to the server, and waits to make sure that the server has received it.
    def sendActivityCommandAndWait(message:String, content: Any, timeoutMillis:Long=500){
      waitForNMessages(1, timeoutMillis){ sendActivityCommand(message, content) }
    }

    // get the next message from the client, removing it from its queue.
    // wait timeoutMillis milliseconds before giving up.
    // if the message is there, check to make sure its the same as the expected message.
    def assertNextMessage(expectedMessage:Message, timeoutMillis:Long=200){
      (expectedMessage, nextMessage(timeoutMillis)) match {
        case (_, None) => fail(userId + " no message received!")
        case (ViewUp(_), Some(ViewUp(_))) => // ok. we dont check view update contents.
        case (exp, Some(act)) => assert(exp === act)
      }
    }
  }

  def numberOfMessagesOnServer = workspace.hubnetManager.numberOfMessagesWaiting

  // runs the given function f, and waits to make sure that the server has received n messages.
  // presumably, f is a function that sends n messages
  // wait timeoutMillis milliseconds before giving up.
  // doing it this way makes it so we can always safely call 'listen-clients' on the server
  // and only have to call it once, because we know it has the messages we need it to process.
  def waitForNMessages[T](n: Int, timeoutMillis: Long)(f: => T): T = {
    val start = System.currentTimeMillis
    def timedOut = System.currentTimeMillis - start > timeoutMillis
    val numberBeforeSend = numberOfMessagesOnServer
    val t = f
    while (true) {
      val numOnServerNow = numberOfMessagesOnServer
      // use of > is a little funny here
      // what if more than n messages got to the server?
      if (numOnServerNow - numberBeforeSend >= n) {
        // all the messages got to the server
        return t
      }
      if (timedOut) {
        val msg = "tried to send " + n + " messages, but the server only received " +
                   numOnServerNow + " before timing out (" + timeoutMillis + "ms)"
        throw new IllegalStateException(msg)
      }
      Thread.sleep(25)
    }
    t
  }

  // override this so we can shut the executor down after the suite runs
  // otherwise, the vm never shuts down.
  override def run(testName: Option[String],
                   reporter: Reporter,
                   stopper: Stopper,
                   filter: Filter,
                   configMap: Map[String, Any],
                   distributor: Option[Distributor],
                   tracker: Tracker): Unit = {
    super.run(testName, reporter, stopper, filter, configMap, distributor, tracker)
    executor.shutdown()
  }
}
