// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

/***
 * LinkManager -- Keeps track of links.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.nlogo.api.AgentKindJ;

public strictfp class LinkManager {

  private final World world;

  public LinkManager(World world) {
    this.world = world;
  }

  ///

  // Use LinkedHashMap not HashMap for these so model results are
  // reproducible. - ST 12/21/05, 3/15/06, 7/20/07
  final Map<Turtle, List<Link>> srcMap =
      new LinkedHashMap<Turtle, List<Link>>();
  final Map<Turtle, List<Link>> destMap =
      new LinkedHashMap<Turtle, List<Link>>();

  private void bless(Link link) {
    Turtle end1 = link.end1();
    Turtle end2 = link.end2();
    // add to source map
    if (srcMap.containsKey(end1)) {
      srcMap.get(end1).add(link);
    } else {
      ArrayList<Link> recList =
          new ArrayList<Link>();
      recList.add(link);
      srcMap.put(end1, recList);
    }
    // add to destination map
    if (destMap.containsKey(end2)) {
      destMap.get(end2).add(link);
    } else {
      List<Link> recList =
          new ArrayList<Link>();
      recList.add(link);
      destMap.put(end2, recList);
    }
    if (link.getBreed() == world.links()) {
      countUnbreededLinks++;
    }
  }

  ///

  public void reset() {
    srcMap.clear();
    destMap.clear();
    world.tieManager.reset();
    countUnbreededLinks = 0;
    resetLinkDirectedness();
  }

  ///

  private double countUnbreededLinks = 0;

  private void resetLinkDirectedness() {
    if (countUnbreededLinks == 0) {
      world.links().clearDirected();
    }
  }


  /// link creation
  public Link createLink(Turtle src, Turtle dest, AgentSet breed) {
    Link link = newLink(world, src, dest, breed);
    link.colorDoubleUnchecked(Link.DEFAULT_COLOR);
    bless(link);
    return link;
  }

  // exists as separate method so we can override in LinkManager3D
  Link newLink(World world, Turtle src, Turtle dest, AgentSet breed) {
    return new Link(world, src, dest, breed);
  }

  /// lookups

  public Link findLink(Turtle src, Turtle dest, AgentSet breed, boolean includeAllLinks) {
    if (breed.isDirected()) {
      return findLinkFrom(src, dest, breed, includeAllLinks);
    } else {
      return findLinkEitherWay(src, dest, breed, includeAllLinks);
    }
  }

  public Link findLinkFrom(Turtle src, Turtle dest, AgentSet breed, boolean includeAllLinks) {
    if (src == null || dest == null) {
      return null;
    }
    Link link = (Link) world.links().getAgent(new DummyLink
        (world, src, dest, breed));
    if (link == null && includeAllLinks && breed == world.links()) {
      scala.collection.Iterator<String> iter =
        world.program().linkBreeds().keys().iterator();
      while(iter.hasNext()) {
        AgentSet agents = world.linkBreedAgents.get(iter.next());
        link = (Link) world.links().getAgent(new DummyLink(world, src, dest, agents));
        if (link != null) {
          return link;
        }
      }
    }
    return link;
  }

  public Link findLinkEitherWay(Turtle src, Turtle dest, AgentSet breed, boolean includeAllLinks) {
    Link link = findLinkFrom(src, dest, breed, includeAllLinks);
    if (link == null) {
      link = findLinkFrom(dest, src, breed, includeAllLinks);
    }
    return link;
  }

  public AgentSet findLinkedFrom(Turtle src, AgentSet sourceSet) {
    List<Link> fromList = srcMap.get(src);
    if (fromList != null) {
      AgentSet nodeset =
        new ArrayAgentSet(AgentKindJ.Turtle(), fromList.size(), false, world);
      addLinkNeighborsFrom(nodeset, fromList, sourceSet, true);
      return nodeset;
    } else {
      return world.noTurtles();
    }
  }

  public AgentSet findLinkedTo(Turtle target, AgentSet sourceSet) {
    List<Link> fromList = destMap.get(target);
    if (fromList != null) {
      AgentSet nodeset =
        new ArrayAgentSet(AgentKindJ.Turtle(), fromList.size(), false, world);
      addLinkNeighborsTo(nodeset, fromList, sourceSet, true);
      return nodeset;
    } else {
      return world.noTurtles();
    }
  }

  public AgentSet findLinkedWith(Turtle target, AgentSet sourceSet) {
    List<Link> toList = destMap.get(target);
    List<Link> fromList = srcMap.get(target);
    int size = (fromList == null ? 0 : fromList.size()) + (toList == null ? 0 : toList.size());
    if (size == 0) {
      return world.noTurtles();
    }
    AgentSet nodeset =
      new ArrayAgentSet(AgentKindJ.Turtle(), size, false, world);
    if (toList != null) {
      addLinkNeighborsTo(nodeset, toList, sourceSet, false);
    }
    if (fromList != null) {
      addLinkNeighborsFrom(nodeset, fromList, sourceSet, false);
    }
    return nodeset;
  }

  // the next two methods are essentially the same but are separate for
  // performance reasons ev 4/26/07
  // these are used in two cases, either for link-neighbors in which case
  // sourceSet will always be a breed. but layout-radial also uses it
  // and it might be any agentset.  ev 4/6/07
  private void addLinkNeighborsFrom(AgentSet nodeset,
                                    List<Link> links,
                                    AgentSet sourceSet,
                                    boolean directed) {
    boolean isBreed = sourceSet.printName() != null;
    boolean isAllLinks = sourceSet == world.links();
    boolean unbreededLinks = checkBreededCompatibility(true);
    for (Link link : links) {
      if ((!isBreed && sourceSet.contains(link)) ||
          (isAllLinks && (unbreededLinks ||
              (directed == link.getBreed().isDirected()
                  && !nodeset.contains(link.end1())))) ||
          (link.getBreed() == sourceSet)) {
        nodeset.add(link.end2());
      }
    }
  }

  private void addLinkNeighborsTo(AgentSet nodeset,
                                  List<Link> links,
                                  AgentSet sourceSet,
                                  boolean directed) {
    boolean isBreed = sourceSet.printName() != null;
    boolean isAllLinks = sourceSet == world.links();
    // if we have unbreeded links we know that there is only one possible link
    // between two turtles, thus we don't have to check if the end point is already
    // in the nodeset, which is slow. so only models that use breeds && link-neighbors
    // will take a performance hit ev 6/15/07
    boolean unbreededLinks = checkBreededCompatibility(true);
    for (Link link : links) {
      if ((!isBreed && sourceSet.contains(link)) ||
          (isAllLinks && (unbreededLinks ||
              (directed == link.getBreed().isDirected()
                  && !nodeset.contains(link.end1())))) ||
          (link.getBreed() == sourceSet))

      {
        nodeset.add(link.end1());
      }
    }
  }

  public boolean checkBreededCompatibility(boolean unbreeded) {
    AgentSet.Iterator it = world.links().iterator();
    if (!it.hasNext()) {
      return true;
    }
    return (((Link) it.next()).getBreed() == world.links()) == unbreeded;
  }

  public AgentSet findLinksFrom(Turtle src, AgentSet breed) {
    List<Link> fromList = srcMap.get(src);
    AgentSet linkset =
      new ArrayAgentSet(AgentKindJ.Link(), 1, false, world);
    boolean isAllLinks = breed == world.links();
    if (fromList != null) {
      for (Link link : fromList) {
        if (isAllLinks || link.getBreed() == breed) {
          linkset.add(link);
        }
      }
    }
    return linkset;
  }

  public AgentSet findLinksTo(Turtle target, AgentSet breed) {
    List<Link> fromList = destMap.get(target);
    AgentSet linkset =
      new ArrayAgentSet(AgentKindJ.Link(), 1, false, world);
    boolean isAllLinks = breed == world.links();
    if (fromList != null) {
      for (Link link : fromList) {
        if (isAllLinks || link.getBreed() == breed) {
          linkset.add(link);
        }
      }
    }
    return linkset;
  }

  public AgentSet findLinksWith(Turtle target, AgentSet breed) {
    List<Link> fromList = destMap.get(target);
    List<Link> toList = srcMap.get(target);
    List<Link> totalList = new ArrayList<Link>();
    if (fromList != null) {
      totalList.addAll(fromList);
    }
    if (toList != null) {
      totalList.addAll(toList);
    }
    boolean isAllLinks = breed == world.links();
    AgentSet linkset =
      new ArrayAgentSet(AgentKindJ.Link(), 1, false, world);
    for (Link link : totalList) {
      if (isAllLinks || link.getBreed() == breed) {
        linkset.add(link);
      }
    }
    return linkset;
  }

  /// cleaning up after dead turtles

  void cleanup(Link link) {
    // keep tie bookkeeping up to date
    link.untie();
    // remove from source map
    Turtle end1 = link.end1();
    List<Link> list = srcMap.get(end1);
    if (list != null) {
      list.remove(link);
      if (list.isEmpty()) {
        srcMap.remove(end1);
      }
    }
    // remove from dest map
    Turtle end2 = link.end2();
    list = destMap.get(end2);
    if (list != null) {
      list.remove(link);
      if (list.isEmpty()) {
        destMap.remove(end2);
      }
    }
    if (link.getBreed() == world.links()) {
      countUnbreededLinks--;
    }
    // were we the last link?
    resetLinkDirectedness();
  }

  // Turtle.die() calls this - ST 3/15/06, 7/21/07
  void cleanup(Turtle turtle) {
    // this part is a bit tricky -- we need to remove the turtle
    // from the src & dest maps first, so we don't end up in an
    // infinite loop where a dying node kills a link which tries
    // to kill the original node.  But we need the map entries
    // in order to find the links.  Hence the exact ordering
    // inside each if statement below. - ST 3/15/06
    if (srcMap.containsKey(turtle)) {
      List<Link> links = srcMap.get(turtle);
      srcMap.remove(turtle);
      for (Link link : links) {
        link.die();
      }
    }
    if (destMap.containsKey(turtle)) {
      List<Link> links = destMap.get(turtle);
      destMap.remove(turtle);
      for (Link link : links) {
        link.die();
      }
    }
  }
}
