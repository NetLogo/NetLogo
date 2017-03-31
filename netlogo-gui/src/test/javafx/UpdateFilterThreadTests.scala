// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import java.util.concurrent.LinkedBlockingQueue

import org.nlogo.internalapi.{ JobDone, ModelUpdate, MonitorsUpdate, WorldUpdate }

import org.scalatest.{ FunSuite, Inside }

class UpdateFilterThreadTests extends FunSuite with Inside {

  trait Helper {
    val updates = new LinkedBlockingQueue[ModelUpdate]()
    var _currentTime = 0
    def elapse(i: Int) = {
      _currentTime += i
    }
    var timesProcessed = 0
    val subject = new UpdateFilter {
      val updateInterval = 50
      val worldUpdates = updates
      def currentTime: Long = _currentTime
      def processUpdates(): Unit = {
        timesProcessed += 1
      }
    }
  }

  test("forwards ModelUpdates that are not WorldUpdate or MonitorsUpdate") { new Helper {
    updates.add(JobDone("abc"))
    subject.step()
    inside(subject.filteredUpdates.peek) { case JobDone(_) => }
  } }

  test("sends the a single world update per periodic interval") { new Helper {
    updates.add(WorldUpdate(null, 1234))
    updates.add(WorldUpdate(null, 1235))
    subject.step()
    subject.step()
    assert(subject.filteredUpdates.size == 1)
  } }

  test("sends another world update if the interval has elapsed") { new Helper {
    updates.add(WorldUpdate(null, 1234))
    subject.step()
    elapse(100)
    updates.add(WorldUpdate(null, 1234))
    subject.step()
    assert(subject.filteredUpdates.size == 2)
  } }

  test("forwards the latest world update if the interval has elapsed and another message is sent") { new Helper {
    updates.add(WorldUpdate(null, 1234))
    subject.step()
    elapse(25)
    updates.add(WorldUpdate(null, 1235))
    subject.step()
    elapse(50)
    updates.add(JobDone("abc"))
    subject.step()
    assert(subject.filteredUpdates.size == 3)
  } }

  test("runs the process callback when it receives a message") { new Helper {
    updates.add(WorldUpdate(null, 1234))
    subject.step()
    assert(timesProcessed == 1)
  } }

  test("runs the process callback only once in each updateInterval") { new Helper {
    updates.add(JobDone("abc"))
    subject.step()
    assert(timesProcessed == 1)
    updates.add(JobDone("abc"))
    subject.step()
    assert(timesProcessed == 1)
    elapse(60)
    updates.add(JobDone("abc"))
    subject.step()
    assert(timesProcessed == 2)
  } }

  test("sends a single monitor update per periodic interval") { new Helper {
    updates.add(MonitorsUpdate(Map(), 123))
    updates.add(MonitorsUpdate(Map(), 123))
    subject.step()
    subject.step()
    assert(subject.filteredUpdates.size == 1)
  } }

}
