package org.nlogo.api;

import java.util.ArrayList;
import java.util.List;

public strictfp class Options<T> {

  private final List<Option> options = new ArrayList<Option>();
  private Option current = null;

  public String chosenName() {
    return current.name;
  }

  public T chosenValue() {
    return current.value;
  }

  ///

  public void addOption(String name, T value) {
    options.add(new Option(name, value));
  }

  ///

  public List<String> getNames() {
    List<String> result = new ArrayList<String>();
    for (Option option : options) {
      result.add(option.name);
    }
    return result;
  }

  public List<T> getValues() {
    List<T> result = new ArrayList<T>();
    for (Option option : options) {
      result.add(option.value);
    }
    return result;
  }

  ///

  public void selectByName(String s) {
    for (Option option : options) {
      if (option.name != null && option.name.equals(s)) {
        current = option;
        return;
      }
    }
    throw new IllegalArgumentException("not found: " + s);
  }

  public void selectValue(T obj) {
    for (Option option : options) {
      if (option.value != null && option.value.equals(obj)) {
        current = option;
        return;
      }
    }
    throw new IllegalArgumentException("not found: " + obj);
  }

  private class Option {
    final String name;
    final T value;

    Option(String name, T value) {
      this.name = name;
      this.value = value;
    }
  }

}
