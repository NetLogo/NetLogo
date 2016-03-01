// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A set that keeps track of times and can expire inactive elements.
 * This implementation is synchronized.
 */
strictfp class TimedSet
    extends java.util.AbstractSet<Object> {

  /**
   * (Ordered) list of the elements in this set *
   */
  private final List<Object> contents;

  /**
   * Map of elements to timestamps *
   */
  private final Map<Object, TimeStamp> timestamps;

  /**
   * How long an unchaged element remains in the set before it's expired *
   */
  private final long idleLifetime;

  /**
   * Constructs an empty timed set.
   *
   * @param idleLifetime how long an inactive element stays in the set
   */
  public TimedSet(long idleLifetime) {
    this.idleLifetime = idleLifetime;

    contents = new ArrayList<Object>();
    timestamps = new HashMap<Object, TimeStamp>();
  }


  /**
   * Constructs an empty timed set with the specified initial capacity.
   *
   * @param idleLifetime how long an inactive element stays in the set
   */
  public TimedSet(long idleLifetime, int initialCapacity) {
    this.idleLifetime = idleLifetime;

    contents = new ArrayList<Object>(initialCapacity);
    timestamps = new HashMap<Object, TimeStamp>(initialCapacity);
  }


  /**
   * Adds an element to the set. If the set already contains the
   * element, the element is touched but the created time is not changed.
   *
   * @return <code>true</code> if this set did not already contain the specified element.
   */
  @Override
  public synchronized boolean add(Object o) {
    if (!touch(o)) {
      contents.add(o);
      timestamps.put(o, new TimeStamp());
      return true;
    }
    return false;
  }


  /**
   * Removes the specified element from this set if it is present.
   *
   * @return <code>true</code> if the set contained the specified element.
   */
  @Override
  public synchronized boolean remove(Object o) {
    return ((timestamps.remove(o) != null) && contents.remove(o));
  }

  /**
   * Returns the element at the specified position in this ordered set.
   *
   * @throws IndexOutOfBoundsException if the index is out of range (index  < 0 || index >= size()).
   */
  public Object get(int index) {
    return contents.get(index);
  }

  /**
   * Removes all of the elements from this set.
   */
  @Override
  public synchronized void clear() {
    contents.clear();
    timestamps.clear();
  }


  /**
   * Returns <code>true</code> if this set contains the specified element.
   */
  @Override
  public synchronized boolean contains(Object o) {
    return timestamps.containsKey(o);
  }


  /**
   * Returns the number of elements in this set.
   */
  @Override
  public synchronized int size() {
    return contents.size();
  }


  /**
   * Returns an iterator over the elements in this collection.
   * Elements are ordered by their age (how long they've been in the set).
   */
  @Override
  public synchronized Iterator<Object> iterator() {
    return contents.iterator();
  }


  /**
   * Returns an array containing all of the elements in this set.
   * Elements are ordered by their age (how long they've been in the set).
   */
  @Override
  public synchronized Object[] toArray() {
    return contents.toArray();
  }


  /**
   * Sets the modified time of an element to the current time.
   *
   * @return true if the object was touched, false if the object is not in the set
   */
  public synchronized boolean touch(Object o) {
    TimeStamp ts = timestamps.get(o);
    if (ts != null) {
      ts.modified = System.currentTimeMillis();
      return true;
    } else {
      return false;
    }
  }


  /**
   * Expire inactive elements. Removes all elements that have not been
   * modified in over <code>idleLifetime</code>.
   *
   * @return number of expired elements
   */
  public synchronized int expire() {
    long now = System.currentTimeMillis();

    Iterator<Object> it = iterator();

    int expired = 0;

    while (it.hasNext()) {
      Object o = it.next();
      TimeStamp ts = timestamps.get(o);
      if (now - ts.modified > idleLifetime) {
        timestamps.remove(o);
        it.remove();
        expired++;
      }
    }

    return expired;
  }


  /**
   * Returns the last time an element of this set was modified or touched.
   */
  public synchronized long getModifiedTime(Object o) {
    TimeStamp ts = timestamps.get(o);

    if (ts == null) {
      throw new java.util.NoSuchElementException
          ("TimedSet does not contain " + o);
    }
    return ts.modified;
  }

  /**
   * Returns the time an element was added to this set.
   */
  public synchronized long getAddedTime(Object o) {
    TimeStamp ts = timestamps.get(o);

    if (ts == null) {
      throw new java.util.NoSuchElementException
          ("TimedSet does not contain " + o);
    }

    return ts.created;
  }


  /**
   * Holds time information about an element of this set.
   */
  private static class TimeStamp {
    public long created;
    public long modified;

    public TimeStamp() {
      created = System.currentTimeMillis();
      modified = created;
    }
  }
}


