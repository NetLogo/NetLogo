package org.nlogo.modelingcommons;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Permission {
  private String id;
  private String name;
  public Permission(String id, String name) {
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