// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// I had an itch to try and generify this so e.g. instead of
// FooEvent.Handler we'd have Event.Handler<Foo>, but when I tried it
// pretty soon I got lost.  It seemed like maybe Event should be
// declared as Event<T extends Event<T>> ? I dunno... I wound up
// concluding there was no real benefit to generifying, since client
// code is already typesafe, since all of the dynamic type casts are
// encapsulated here in this one file in code that has stood the test
// of time. - ST 3/14/08

public abstract strictfp class Event {

  // individual Event subclasses will also subclass this
  // interface - ST 9/8/04
  public interface Handler {
  }

  public abstract void beHandledBy(Handler handler);

  ///

  // normally the event thread, but if raiseLater() is used,
  // this will record what thread raiseLater() was called
  // from; sometimes this is nice information to have when
  // troubleshooting - ST 9/8/04
  private Thread raisingThread = null;

  public static boolean logEvents = false;

  // this is a map from raiser objects to maps, where the submaps
  // map from event classes to a List of handler objects.  So basically
  // it's table where you get a list of handler objects by doing lookup
  // with two keys, the raiser object and the event class - ST 9/8/04
  private static Map<Object, Map<Class<?>, List<Handler>>> handlers =
      new HashMap<Object, Map<Class<?>, List<Handler>>>();

  // sometimes it's nice to have this for troubleshooting purposes - ST 9/8/04
  private static List<Object[]> recentEvents = new ArrayList<Object[]>();

  public static String recentEventTrace() {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
    StringBuilder buf = new StringBuilder();
    for (Object[] info : recentEvents) {
      Event event = (Event) info[0];
      Object raiser = info[1];
      Thread thread = (Thread) info[2];
      java.util.Date time = (java.util.Date) info[3];
      String timeString =
          new java.text.SimpleDateFormat("hh:mm:ss.SSS").format(time);
      buf.append(timeString + " " + eventName(event) +
          " (" + readableName(raiser) + ") " +
          thread.getName() + "\n");
    }
    return buf.toString();
  }

  ///

  // In general, this code assumes a static GUI in which new objects
  // aren't coming on board.  We cache things in the handlers map on
  // that assumption.  But sometimes, for example when the user adds
  // a new widget to the interface tab, or when an agent monitor
  // opens, the information in the handler map could get out of
  // date.  Hence we provide this method for external code to call
  // to prevent such situations. - ST 9/8/04
  public static void rehash() {
    handlers.clear();
  }

  // this used to indent the debugging output when event
  // are "nested", that is, a new event is raised in the course
  // of handling an old event - ST 9/8/04
  private static int nestingDepth = 0;

  private static String eventName(Object o) {
    String longName = o.getClass().getName();
    String shorterName = longName.substring(longName.lastIndexOf('.') + 1);
    return shorterName.substring(shorterName.lastIndexOf('$') + 1);
  }

  // so we get e.g. "org.nlogo.app.App$7 (org.nlogo.window.GUIWorkspace)"
  // instead of just "org.nlogo.app.App$7", for inner classes - ST 7/19/10
  private static String readableName(Object o) {
    Class<?> clazz = o.getClass();
    String longName = clazz.getName();
    while (clazz.getName().indexOf('$') != -1) {
      clazz = clazz.getSuperclass();
    }
    if (clazz == o.getClass()) {
      return longName;
    } else {
      return longName + " (" + clazz.getName() + ")";
    }
  }

  ///

  public void raiseLater(final Object raiser) {
    raisingThread = Thread.currentThread();
    org.nlogo.awt.EventQueue.invokeLater
        (new Runnable() {
          public void run() {
            // call doRaise(), not raise(), so raisingThread
            // doesn't get overwritten - ST 9/8/04
            doRaise(raiser);
          }
        });
  }

  public void raise(Object raiser) {
    raisingThread = Thread.currentThread();
    doRaise(raiser);
  }

  ///

  private void doRaise(Object raiser) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
    int oldNestingDepth = nestingDepth;
    try {

      // first do preparatory stuff, including logging
      nestingDepth++;
      recentEvents.add
          (0,
              new Object[]
                  {this, raiser, raisingThread, new java.util.Date()});
      if (recentEvents.size() > 10) {
        recentEvents.remove(recentEvents.size() - 1);
      }
      Class<? extends Event> eventClass = getClass();
      String name = eventName(this);
      // if we logged these event types, the log would be choked
      // with them, so let's ignore them
      if (logEvents
          && !name.equals("PeriodicUpdateEvent")
          && !name.equals("InterfaceGlobalEvent")
          ) {
        for (int i = 0; i < oldNestingDepth; i++) {
          System.out.print(' ');
        }
        System.out.println("raising " + name + ": " + readableName(raiser));
      }
      if (raiser == null) {
        throw new IllegalStateException
            ("event raised with null raiser");
      }

      // OK, all preparation is out of the way... time to actually locate
      // our handlers.  Old information about who handles what is cached
      // in the handlers map.  We look there first; if we don't find
      // information there, we add it.

      // step 1: using raiser as key, get map back which maps
      // from event class to handlers.  if no such map in cache,
      // create one.
      Map<Class<?>, List<Handler>> events = handlers.get(raiser);
      if (null == events) {
        events = new HashMap<Class<?>, List<Handler>>();
        handlers.put(raiser, events);
      }

      // step 2: using event class as key, get list of handlers.
      // if no such list in cache, create one.
      // for events in the || clauses below, some relevant handlers are
      // not yet created when the lists are first built, and hence need
      // to be recomputed. It would be better to only recompute once.
      // It may be possible to remove these tests by adding Event.rehash() to
      // code involved with separate code windows. AAB 10/2020
      List<Handler> handlersV = events.get(eventClass);
      if (null == handlersV ||
        eventClass.toString().equals("class org.nlogo.window.Events$CompiledEvent") ||
        eventClass.toString().equals("class org.nlogo.window.Events$LoadModelEvent") ||
        eventClass.toString().equals("class org.nlogo.app.common.Events$SwitchedTabsEvent")) {
        // findHandlers does the grunt work of actually
        // walking the component hierarchy looking for
        // handlers
        handlersV = findHandlers(findTop(raiser), eventClass);
        events.put(eventClass, handlersV);
      }

      // step 3: call the beHandledBy() method on every handler we find
      for (Handler handler : handlersV) {
        if (logEvents
            // if we logged these event types, the log would
            // be choked with them, so let's ignore them
            && !name.equals("PeriodicUpdateEvent")
            && !name.equals("InterfaceGlobalEvent")
            ) {
          for (int i = 0; i < nestingDepth; i++) {
            System.out.print(' ');
          }
          System.out.println("handling " + eventName(this)
              + ": " + readableName(handler));
        }
        beHandledBy(handler);
      }
      nestingDepth--;
    } finally {
      nestingDepth = oldNestingDepth;
    }
  }

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

  private java.awt.Component findTop(Object top) {
    while (top != null) {
      java.awt.Component parent = null;
      if (top instanceof Event.LinkChild) {
        Object linkParent = ((Event.LinkChild) top).getLinkParent();
        while (linkParent != null && !(linkParent instanceof java.awt.Component)) {
          linkParent = ((Event.LinkChild) linkParent).getLinkParent();
        }
        parent = (java.awt.Component) linkParent;
      } else if (top instanceof java.awt.Component && !(top instanceof java.awt.Window)) {
        parent = ((java.awt.Component) top).getParent();
      }
      if (null == parent) {
        break;
      }
      top = parent;
    }
    return (java.awt.Component) top;
  }

  private List<Handler> findHandlers(Object top, Class<? extends Event> eventClass) {
    List<Handler> result = new ArrayList<Handler>();
    if (top instanceof java.awt.Container) {
      java.awt.Component[] comps = ((java.awt.Container) top).getComponents();
      for (int i = 0; i < comps.length; i++) {
        result.addAll(findHandlers(comps[i], eventClass));
      }
    }
    if (top instanceof Event.LinkParent) {
      Object[] objs = ((Event.LinkParent) top).getLinkChildren();
      for (int i = 0; i < objs.length; i++) {
        result.addAll(findHandlers(objs[i], eventClass));
      }
    }
    if (isHandler(top, eventClass)) {
      result.add((Handler) top);
    }
    return result;
  }

  /// more private helper methods.

  // we use reflection to map from each event class to its Handler
  // interface.

  private static Map<Class<?>, Set<Class<? extends Event>>> eventsHandledMap =
      new HashMap<Class<?>, Set<Class<? extends Event>>>();

  private boolean isHandler(Object comp, Class<? extends Event> eventClass) {
    if (!(comp instanceof Handler)) {
      return false;
    }
    Handler handler = (Handler) comp;
    Set<Class<? extends Event>> eventsHandled = eventsHandledMap.get(comp.getClass());
    if (eventsHandled == null) {
      eventsHandled = new HashSet<Class<? extends Event>>();
      eventsHandledMap.put(comp.getClass(), eventsHandled);
      Class<?> handlerClass = handler.getClass();
      while (handlerClass != null) {
        Class<?>[] interfaces = getAllInterfaces(handlerClass);
        for (int i = 0; i < interfaces.length; i++) {
          String interfaceName = interfaces[i].getName();
          if (interfaceName.endsWith("$Handler")) {
            eventsHandled.add
                (eventClassForHandlerClassName
                    (interfaceName.substring
                        (0, interfaceName.length() - 8)));
          }
        }
        handlerClass = handlerClass.getSuperclass();
      }
    }
    return eventsHandled.contains(eventClass);
  }

  @SuppressWarnings("unchecked")  // we're using reflection, so an unsafe cast is required
  private Class<? extends Event> eventClassForHandlerClassName(String name) {
    try {
      return (Class<? extends Event>) Class.forName(name);
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public interface LinkChild {
    Object getLinkParent();
  }

  public interface LinkParent {
    Object[] getLinkChildren();
  }

  ///

  public static Class<?>[] getAllInterfaces(Class<?> theClass) {
    List<Class<?>> result = new ArrayList<Class<?>>();
    java.util.Stack<Class<?>> stack = new java.util.Stack<Class<?>>();
    stack.addElement(theClass);
    while (!stack.empty()) {
      theClass = stack.pop();
      if (theClass.isInterface()) {
        result.add(theClass);
      } else if (theClass.getSuperclass() != null) {
        stack.push(theClass.getSuperclass());
      }
      Class<?>[] interfaces = theClass.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        stack.push(interfaces[i]);
      }
    }
    return result.toArray(new Class<?>[result.size()]);
  }

}
