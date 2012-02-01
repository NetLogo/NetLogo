package org.nlogo.hubnet.mirroring;

import java.util.Comparator;

class ClientWorldS {

  // this class is not instantiable
  private ClientWorldS() {
    throw new IllegalStateException();
  }

  static class TurtleKey {
    long who;
    int breedIndex;

    public TurtleKey(long who, int breedIndex) {
      this.who = who;
      this.breedIndex = breedIndex;
    }

    @Override
    public boolean equals(Object o) {
      return (who == ((TurtleKey) o).who) &&
          (breedIndex == ((TurtleKey) o).breedIndex);
    }

    @Override
    public int hashCode() {
      return (int) (breedIndex * 1000 + who);
    }

    @Override
    public String toString() {
      return "(" + who + " " + breedIndex + ")";
    }
  }

  static class TurtleKeyComparator
      implements Comparator<TurtleKey> {
    public int compare(TurtleKey tk1, TurtleKey tk2) {
      if (tk1.breedIndex == tk2.breedIndex) {
        return (int) (tk1.who - tk2.who);
      } else {
        return tk1.breedIndex - tk2.breedIndex;
      }
    }
  }

  static class LinkKey {
    long id;
    long end1;
    long end2;
    int breedIndex;

    public LinkKey(long id, long end1, long end2, int breedIndex) {
      this.id = id;
      this.end1 = end1;
      this.end2 = end2;
      this.breedIndex = breedIndex;
    }

    @Override
    public boolean equals(Object o) {
      return id == ((LinkKey) o).id;
    }

    @Override
    public int hashCode() {
      return (int) id;
    }

    @Override
    public String toString() {
      return "(" + id + " " + breedIndex + ")";
    }
  }

  static class LinkKeyComparator
      implements Comparator<LinkKey> {
    public int compare(LinkKey key1, LinkKey key2) {
      if (key1.end1 == key2.end1) {
        if (key1.end2 == key2.end2) {
          if (key1.breedIndex == key2.breedIndex) {
            return (int) (key1.id - key2.id);
          } else {
            return key1.breedIndex - key2.breedIndex;
          }
        } else {
          return (int) (key1.end2 - key2.end2);
        }
      } else {
        return (int) (key1.end1 - key2.end1);
      }
    }
  }

}
