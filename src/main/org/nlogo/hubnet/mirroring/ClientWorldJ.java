// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.nlogo.hubnet.mirroring.ClientWorldS.TurtleKey;
import static org.nlogo.hubnet.mirroring.ClientWorldS.TurtleKeyComparator;
import static org.nlogo.hubnet.mirroring.ClientWorldS.LinkKey;
import static org.nlogo.hubnet.mirroring.ClientWorldS.LinkKeyComparator;

public abstract strictfp class ClientWorldJ
    implements org.nlogo.api.World {

  // since we want to keep the turtles sorted, but we also
  // want to be able to look them up just by who number, we
  // keep two parallel maps, one sorted, one not -- AZS 11/16/04

  final java.util.SortedMap<TurtleKey, TurtleData> sortedTurtles =
    new TreeMap<TurtleKey, TurtleData>(new TurtleKeyComparator());
  final Map<Long, TurtleKey> turtleKeys =
    new HashMap<Long, TurtleKey>();    

  final Map<Long, TurtleData> uninitializedTurtles = new HashMap<Long, TurtleData>();

  final java.util.SortedMap<LinkKey, LinkData> sortedLinks =
    new TreeMap<LinkKey, LinkData>(new LinkKeyComparator());
  final Map<Long, LinkKey> linkKeys =
    new HashMap<Long, LinkKey>();    

  final Map<Long, LinkData> uninitializedLinks = new HashMap<Long, LinkData>();

}
