// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public strictfp class ModelsLibrary {

  // this class is not instantiable
  private ModelsLibrary() {
    throw new IllegalStateException();
  }

  public static Node rootNode = null;

  public static String[] getModelPaths() {
    scanForModels();
    List<String> result = new ArrayList<String>();
    String fileSep = File.separator;
    for (Enumeration<?> models = rootNode.depthFirstEnumeration(); models.hasMoreElements();) {
    Node node = (Node) models.nextElement();
    if (!node.isFolder() && !node.path.contains(String.format("%smodels%stest%s", fileSep, fileSep, fileSep))) {
        result.add(node.path);
      }
    }
    return result.toArray(new String[]{});
  }

  public static String[] getModelPathsAtRoot(String path) {
    Node rnode = scanForModelsAtRoot(path);
    List<String> result = new ArrayList<String>();
    for (Enumeration<?> models = rnode.depthFirstEnumeration(); models.hasMoreElements();) {
      Node node = (Node) models.nextElement();
      if (!node.isFolder()) {
        result.add(node.path);
      }
    }
    return result.toArray(new String[]{});
  }


  public static List<String> findModelsBySubstring(String targetName) {
    scanForModels();
    List<String> result = new ArrayList<String>();
    // first look for exact match
    for (Enumeration<?> models = rootNode.depthFirstEnumeration(); models.hasMoreElements();) {
      Node node = (Node) models.nextElement();
      if (node.isFolder()) {
        continue;
      }
      String path = node.path.replace(java.io.File.separator.charAt(0), '/');
      int loc = path.toUpperCase().lastIndexOf("/" + targetName.toUpperCase() + ".NLOGO");
      if (loc != -1 && loc == path.lastIndexOf('/')) {
        result.add(node.name);
        return result;
      }
    }
    // then look for initial match
    for (Enumeration<?> models = rootNode.depthFirstEnumeration(); models.hasMoreElements();) {
      Node model = (Node) models.nextElement();
      if (model.isFolder()) {
        continue;
      }
      String path = model.path.replace(java.io.File.separator.charAt(0), '/');
      if (path.toUpperCase().lastIndexOf("/" + targetName.toUpperCase()) == path.lastIndexOf('/') &&
          !result.contains(model.name)) {
        result.add(model.name);
      }
    }
    // then look for anywhere match
    for (Enumeration<?> models = rootNode.depthFirstEnumeration(); models.hasMoreElements();) {
      Node model = (Node) models.nextElement();
      if (model.isFolder()) {
        continue;
      }
      String path = model.path.replace(java.io.File.separator.charAt(0), '/');
      if (path.toUpperCase().lastIndexOf(targetName.toUpperCase()) > path.lastIndexOf('/') &&
          !result.contains(model.name)) {
        result.add(model.name);
      }
    }
    return result;
  }

  /**
   * scans for and returns the full path name to the given model in the
   * models library.
   *
   * @param targetName the name of the model to scan for, not including the
   *                   ".nlogo" extension.
   * @return the path to the model, or null if no such model is in the
   *         library.
   */
  public static String getModelPath(String targetName) {
    scanForModels();
    for (Enumeration<?> models = rootNode.depthFirstEnumeration(); models.hasMoreElements();) {
      Node model = (Node) models.nextElement();
      String path = model.path.replace(
          java.io.File.separator.charAt(0), '/');
      if (path.toUpperCase().indexOf("/" + targetName.toUpperCase()
          + ".NLOGO") == path.lastIndexOf('/')) {
        return model.path;
      }
    }
    return null;
  }

  public static boolean needsModelScan() {
    return rootNode == null;
  }

  public static void scanForModels() {
    if (!needsModelScan()) {
      return;
    }
    java.io.File directoryRoot = new java.io.File("models", "");
    rootNode = new Node("models", "", true);
    scanDirectory(directoryRoot, null, rootNode);
  }

  public static Node scanForModelsAtRoot(String path) {
    java.io.File directoryRoot = new java.io.File(path, "");
    Node node = new Node(path, "", true);
    scanDirectory(directoryRoot, null, node);
    return node;
  }

  public static String getInfoWindow(String filePath)
      throws java.io.IOException {
    String file = org.nlogo.api.FileIO.file2String(filePath);
    // parse out info text
    String delimiter = org.nlogo.api.ModelReader$.MODULE$.SEPARATOR();
    int dlength = delimiter.length();
    int startIndex = 0;
    int endIndex;
    for (int i = 0; i < 2; i++) {
      startIndex = file.indexOf(delimiter, startIndex);
      if (i < 1) {
        startIndex += dlength;
      }
    }
    startIndex += delimiter.length();
    endIndex = file.indexOf(delimiter, startIndex);
    if (startIndex >= 0 && startIndex <= file.length() &&
        endIndex >= 0 && endIndex <= file.length()) {
      return file.substring(startIndex, endIndex).trim();
    } else {
      return "";
    }
  }

  public static String getImagePath(String filePath) {
    int index = filePath.indexOf(".nlogo");
    if (index != -1) {
      filePath = filePath.substring(0, index);
    }
    return filePath + ".png";
  }

  private static void scanDirectory(java.io.File directory, Node grandparent, Node parent) {
    if (!directory.isDirectory()) {
      return;
    }

    String[] rawEntries = directory.list();
    List<String> orderedEntries = new ArrayList<String>();
    for (int i = 0; i < rawEntries.length; i++) {
      orderedEntries.add(rawEntries[i]);
    }
    orderedEntries = orderItems(orderedEntries, parent.isRoot());
    // this test is so empty directories don't even show up at all
    // - ST 8/5/04
    if (!orderedEntries.isEmpty() && grandparent != null) {
      grandparent.add(parent);
    }
    for (int i = 0; i < orderedEntries.size(); i++) {
      String fileName = orderedEntries.get(i);
      if (isBadName(fileName)) {
        continue;
      }
      java.io.File file = new java.io.File(directory, fileName);
      Node child = new Node(translateNameForDisplay(fileName),
          file.getAbsolutePath()
              + (file.isDirectory() ? "/" : ""),
          file.isDirectory());
      if (child.isFolder()) {
        scanDirectory(file, parent, child);
      } else {
        if (fileName.toUpperCase().endsWith(".NLOGO")) {
          parent.add(child);
        }
      }
    }
  }

  private static String translateNameForDisplay(String name) {
    if (name.equalsIgnoreCase("UNVERIFIED")) {
      return "(unverified)";
    } else {
      return removeSuffix(name);
    }
  }

  /// helpers

  // we use this so that "Foo.nlogo" sorts before "Foo
  // Alternate.nlogo", not after - ST 8/31/04
  private static class CustomComparator
      implements Comparator<String> {
    public int compare(String s1, String s2) {
      return String.CASE_INSENSITIVE_ORDER.compare
          (munge(s1), munge(s2));
    }

    private String munge(String s) {
      s = s.toUpperCase();
      return s.endsWith(".NLOGO")
          ? s.substring(0, s.length() - 6)
          : s;
    }
  }

  private static List<String> orderItems(List<String> names, boolean topLevel) {
    String[] orderednames;
    if (topLevel) {
      orderednames = new String[]{
        "SAMPLE MODELS", "CURRICULAR MODELS", "CODE EXAMPLES"};
    } else {
      orderednames = new String[0];
    }
    names = new ArrayList<String>(names);
    Collections.sort(names, new CustomComparator());
    List<String> tempFolders = new ArrayList<String>();
    int number = 0;
    while (number < orderednames.length) {
      //add the ordered elements in the proper order
      for (int i = 0; i < names.size(); i++) {
        String tempName = names.get(i);
        if (tempName.equalsIgnoreCase(orderednames[number])) {
          tempFolders.add(names.get(i));
          names.remove(i);
          break;
        }
      }
      number++;
    }
    //add the rest of the elements
    String unverified = null;
    for (int i = 0; i < names.size(); i++) {
      String temp = names.get(i).toUpperCase();
      if (!isBadName(temp)) {
        if (temp.equals("UNVERIFIED")) {
          unverified = names.get(i);
        } else {
          tempFolders.add(names.get(i));
        }
      }
    }
    if (unverified != null) {
      tempFolders.add(unverified);
    }
    return tempFolders;
  }

  private static boolean isBadName(String name) {
    return
        // ignore invisible stuff
        name.startsWith(".") ||
            // ignore the directory containing the sample beats
            // for the Beatbox model
            name.equals("BEATS") ||
            // when we're not 3D ignore the 3D models.
            name.equals("3D") ;
  }

  private static String removeSuffix(String reference) {
    if (reference.endsWith(".nlogo")) {
      return reference.substring(0, reference.lastIndexOf(".nlogo"));
    }
    else {
      return reference;
    }
  }

  ///

  // Normally nothing in this package should depend on Swing stuff,
  // but actually DefaultMutableTreeNode isn't a GUI class, and in
  // fact if you look at Sun's source for it there's a comment:
  //   // ISSUE: this class depends on nothing in AWT -- move to java.util?
  // so I think it's kosher for us to use it here. - ST 9/18/03

  public static strictfp class Node
      extends javax.swing.tree.DefaultMutableTreeNode {
    private final String name;
    private final String path;

    Node(String name, String path, boolean isFolder) {
      this.name = name;
      this.path = path;
      allowsChildren = isFolder;
    }

    public boolean isFolder() {
      return allowsChildren;
    }

    public String getName() {
      return name;
    }

    public String getFilePath() {
      return path;
    }

    @Override
    public String toString() {
      return name;
    }

    // necessary or empty folders will appear as leaves - ST 6/9/04
    @Override
    public boolean isLeaf() {
      return !isFolder();
    }

  }

}
