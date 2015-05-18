// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Container, Window }
import java.text.SimpleDateFormat
import java.util.Date
import org.nlogo.awt.EventQueue
import scala.collection.mutable._

// I had an itch to try and generify this so e.g. instead of
// FooEventHandler we'd have EventHandler[Foo], but when I tried it
// pretty soon I got lost.  It seemed like maybe Event should be
// declared as Event[T <: Event[T]] ? I dunno... I wound up
// concluding there was no real benefit to generifying, since client
// code is already typesafe, since all of the dynamic type casts are
// encapsulated here in this one file in code that has stood the test
// of time. - ST 3/14/08

object Event {
  var logEvents = false
  // this is a map from raiser objects to maps, where the submaps
  // map from event classes to a List of handler objects.  So basically
  // it's table where you get a list of handler objects by doing lookup
  // with two keys, the raiser object and the event class - ST 9/8/04
  private val handlers = new HashMap[AnyRef, HashMap[Class[_], ArrayBuffer[AnyRef]]]

  // sometimes it's nice to have this for troubleshooting purposes - ST 9/8/04
  private val recentEvents = new ArrayBuffer[Array[AnyRef]]

  def recentEventTrace = {
    EventQueue.mustBeEventDispatchThread()
    val buf = new StringBuilder
    recentEvents.foreach { info =>
      val event = info(0).asInstanceOf[Event]
      val raiser = info(1)
      val thread = info(2).asInstanceOf[Thread]
      val time = info(3).asInstanceOf[Date]
      val timeString = new SimpleDateFormat("hh:mm:ss.SSS").format(time)
      buf.append(s"$timeString ${eventName(event)} (${readableName(raiser)}) ${thread.getName}\n")
    }
    buf.toString
  }

  // In general, this code assumes a static GUI in which new objects
  // aren't coming on board.  We cache things in the handlers map on
  // that assumption.  But sometimes, for example when the user adds
  // a new widget to the interface tab, or when an agent monitor
  // opens, the information in the handler map could get out of
  // date.  Hence we provide this method for external code to call
  // to prevent such situations. - ST 9/8/04
  def rehash() = handlers.clear()

  // this used to indent the debugging output when event
  // are "nested", that is, a new event is raised in the course
  // of handling an old event - ST 9/8/04
  private var nestingDepth = 0

  private def eventName(o: AnyRef) = {
    val longName = o.getClass.getName
    val shorterName = longName.substring(longName.lastIndexOf('.') + 1)
    shorterName.substring(shorterName.lastIndexOf('$') + 1)
  }

  // so we get e.g. "org.nlogo.app.App$7 (org.nlogo.window.GUIWorkspace)"
  // instead of just "org.nlogo.app.App$7", for inner classes - ST 7/19/10
  private def readableName(o: AnyRef) = {
    var clazz: Class[_] = o.getClass
    val longName = clazz.getName
    while(clazz.getName.indexOf('$') != -1)
      clazz = clazz.getSuperclass
    if(clazz == o.getClass)
      longName
    else
      s"$longName (${clazz.getName})"
  }

  private val eventsHandledMap = new HashMap[Class[_], Set[Class[_ <: Event]]]

  def getAllInterfaces(_theClass: Class[_]) = {
    var theClass = _theClass
    val result = new ArrayBuffer[Class[_]]
    val stack = new Stack[Class[_]]
    stack.push(theClass)
    while(!stack.isEmpty) {
      theClass = stack.pop
      if(theClass.isInterface)
        result += theClass
      else if (theClass.getSuperclass != null)
        stack.push(theClass.getSuperclass)
      theClass.getInterfaces.foreach { interface =>
        stack.push(interface)
      }
    }
    result.toArray
  }

  trait LinkChild {
    def getLinkParent: AnyRef
  }

  trait LinkParent {
    def getLinkChildren: Array[AnyRef]
  }
}

abstract class Event {

  // normally the event thread, but if raiseLater() is used,
  // this will record what thread raiseLater() was called
  // from; sometimes this is nice information to have when
  // troubleshooting - ST 9/8/04
  private var raisingThread: Thread = null

  ///

  def raiseLater(raiser: AnyRef) = {
    raisingThread = Thread.currentThread
    EventQueue.invokeLater(new Runnable {
        // call doRaise(), not raise(), so raisingThread
        // doesn't get overwritten - ST 9/8/04
        def run() = doRaise(raiser)
      })
  }

  def raise(raiser: AnyRef) = {
    raisingThread = Thread.currentThread
    doRaise(raiser)
  }

  ///

  private def doRaise(raiser: AnyRef) = {
    EventQueue.mustBeEventDispatchThread()
    val oldNestingDepth = Event.nestingDepth
    try {
      // first do preparatory stuff, including logging
      Event.nestingDepth += 1
      Array(this, raiser, raisingThread, new Date) +=: Event.recentEvents
      if(Event.recentEvents.size > 10)
        Event.recentEvents.remove(Event.recentEvents.size - 1)
      val eventClass = getClass
      val name = Event.eventName(this)
      // if we logged these event types, the log would be choked
      // with them, so let's ignore them
      if(Event.logEvents && name != "PeriodicUpdateEvent" && name != "InterfaceGlobalEvent") {
        print(" "*oldNestingDepth)
        println(s"raising $name: ${Event.readableName(raiser)}")
      }
      if(raiser == null)
        throw new IllegalStateException("event raised with null raiser")

      // OK, all preparation is out of the way... time to actually locate
      // our handlers.  Old information about who handles what is cached
      // in the handlers map.  We look there first; if we don't find
      // information there, we add it.

      // step 1: using raiser as key, get map back which maps
      // from event class to handlers.  if no such map in cache,
      // create one.
      var events = Event.handlers.getOrElseUpdate(raiser, new HashMap[Class[_], ArrayBuffer[AnyRef]])

      // step 2: using event class as key, get list of handlers.
      // if no such list in cache, create one.
      // findHandlers does the grunt work of actually
      // walking the component hierarchy looking for
      // handlers
      var handlersV = events.getOrElseUpdate(eventClass, findHandlers(findTop(raiser), eventClass))

      // step 3: call the handle() method on every handler we find
      handlersV.foreach { handler =>
        // if we logged these event types, the log would
        // be choked with them, so let's ignore them
        if(Event.logEvents && name != "PeriodicUpdateEvent" && name != "InterfaceGlobalEvent") {
          print(" "*Event.nestingDepth)
          println(s"handling ${Event.eventName(this)}: ${Event.readableName(handler)}")
        }
        beHandledBy(handler)
      }
      Event.nestingDepth -= 1
    } finally {
      Event.nestingDepth = oldNestingDepth
    }
  }

  def beHandledBy(handler: AnyRef)

  /// now for private helper methods

  // findHandlers() and findTop() do the grunt work of actually
  // walking the component hierarchy looking for handlers.  first
  // findTop() chases getParent() and getLinkParent() links upward,
  // until it hits the object at the root of the entire tree
  // (probably a Window or an Applet or something like that).  then
  // findHandlers() traverses the entire tree by following
  // getComponents() and getLinkChildren() links, collecting any
  // objects which implement the handler interface we're looking for

  // the purpose of the Event.LinkChild and Event.LinkParent
  // stuff is twofold:
  //   1) to allow objects that aren't actually components to
  //      participate in event raising and handling
  //   2) to allow trees of components in different windows to get
  //      events back and forth between each other.  For example, in
  //      NetLogo agent monitors are rooted in Window

  private def findTop(_top: AnyRef): Component = {
    var top = _top
    while(top != null) {
      var parent: Component = null
      top match {
        case linkChild: Event.LinkChild =>
          var linkParent = linkChild.getLinkParent
          while(linkParent != null && !linkParent.isInstanceOf[Component])
            linkParent = linkParent.asInstanceOf[Event.LinkChild].getLinkParent
          parent = linkParent.asInstanceOf[Component]
        case comp: Component if !top.isInstanceOf[Window] =>
          parent = comp.getParent
        case _ =>
      }
      if(null == parent)
        return top.asInstanceOf[Component]
      top = parent
    }
    top.asInstanceOf[Component]
  }

  private def findHandlers(top: AnyRef, eventClass: Class[_ <: Event]): ArrayBuffer[AnyRef] = {
    val result = new ArrayBuffer[AnyRef]
    top match {
      case container: Container => container.getComponents.foreach { comp =>
          result ++= findHandlers(comp, eventClass)
        }
      case _ =>
    }
    top match {
      case linkParent: Event.LinkParent => linkParent.getLinkChildren.foreach { obj =>
          result ++= findHandlers(obj, eventClass)
        }
      case _ =>
    }
    if(isHandler(top, eventClass))
      result += top
    result
  }

  /// more private helper methods.

  // we use reflection to map from each event class to its handler trait

  private def isHandler(handler: AnyRef, eventClass: Class[_ <: Event]) = {
    var handlerClass: Class[_] = handler.getClass
    val freshEventsHandled = !Event.eventsHandledMap.contains(handlerClass)
    var eventsHandled = Event.eventsHandledMap.getOrElseUpdate(handlerClass, new HashSet[Class[_ <: Event]])
    if(freshEventsHandled)
      while(handlerClass != null) {
        Event.getAllInterfaces(handlerClass).foreach { interface =>
          val interfaceName = interface.getName
          if(interfaceName.endsWith("EventHandler"))
            eventsHandled += eventClassForHandlerClassName(
              interfaceName.substring(0, interfaceName.length - 7))
        }
        handlerClass = handlerClass.getSuperclass
      }
    eventsHandled.contains(eventClass)
  }

  @SuppressWarnings(Array("unchecked"))  // we're using reflection, so an unsafe cast is required
  private def eventClassForHandlerClassName(name: String): Class[_ <: Event] =
    try {
      return Class.forName(name).asSubclass(classOf[Event])
    } catch {
      case ex: ClassNotFoundException => throw new IllegalStateException(ex)
    }
}
