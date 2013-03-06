// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

// the next few methods (dealing with the breedShapes Map) need to be
// synchronized so since they can run on both the JobManager thread and the
// event thread.  this could leave the breedShapes map in an undetermined
// state.  we are safe from deadlock here since these methods are self
// contained and don't call methods that require one of the two threads
// waiting on the other.
//
// we can't lock on the breedShapes object itself since we change the object
// it is pointing to in the setUpBreedShapes method.  we don't want to
// synchronize on the world since other classes do that and this really
// shouldn't affect their performance.  so we use a lock object.
// --mag 10/03/03

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public strictfp class BreedShapes {
  private final Object lock = new Object();
  private Map<String, String> shapes = null;
  private final String genericBreedName;

  BreedShapes(String genericBreedName) {
    this.genericBreedName = genericBreedName;
  }

  public void setUpBreedShapes(boolean clear, Map<String, Object> breeds) {
    synchronized (lock) {
      if (clear || shapes == null) {
        shapes = new HashMap<String, String>();
      }
      Map<String, String> newBreedShapes =
          new HashMap<String, String>();
      if (breeds != null) {
        for (Iterator<Object> iter =
                 breeds.values().iterator();
             iter.hasNext();) {
          String breedName =
              ((AgentSet) iter.next()).printName();
          String oldShape = shapes.get(breedName);
          newBreedShapes.put(breedName,
              oldShape == null
                  ? "__default"
                  : oldShape);
        }
        String oldShape = shapes.get(genericBreedName);
        newBreedShapes.put(genericBreedName,
            oldShape == null
                ? "default"
                : oldShape);
      }
      shapes = newBreedShapes;
    }
  }

  public void removeFromBreedShapes(String shapeName) {
    synchronized (lock) {
      if (shapes.containsValue(shapeName)) {
        for (Map.Entry<String, String> breedShapePair : shapes.entrySet()) {
          if (breedShapePair.getValue().equals(shapeName)) {
            breedShapePair.setValue("__default");
          }
        }
      }
    }
  }

  public String breedShape(AgentSet breed) {
    synchronized (lock) {
      String result = shapes.get(breed.printName());
      if (result.equals("__default")) {
        result = shapes.get(genericBreedName);
      }
      return result;
    }
  }

  public boolean breedHasShape(AgentSet breed) {
    synchronized (lock) {
      String result = shapes.get(breed.printName());
      if (result.equals("__default")) {
        return false;
      }
      return true;
    }
  }

  public void setBreedShape(AgentSet breed, String shapeName) {
    synchronized (lock) {
      shapes.put(breed.printName(), shapeName);
    }
  }
}
