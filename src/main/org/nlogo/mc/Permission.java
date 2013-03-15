// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import java.util.HashMap;
import java.util.Map;

public strictfp class Permission {

  private String id;
  private String name;

  private Permission(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return getName();
  }

  private static Map<String, Permission> permissions;

  static {
    permissions = new HashMap<String, Permission>(3);
    permissions.put("a", new Permission("a", "everyone"));
    permissions.put("g", new Permission("g", "group members only"));
    permissions.put("u", new Permission("u", "you only"));
  }

  public static Map<String, Permission> getPermissions() {
    return permissions;
  }

}

