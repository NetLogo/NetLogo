package org.nlogo.nvm;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.api.Dump;
import org.nlogo.api.SourceOwner;
import org.nlogo.api.Let;
import org.nlogo.api.Token;

public strictfp class Procedure {

  // maybe we should just use a boolean for this - ST 2/7/11
  public enum Type {
    COMMAND, REPORTER
  }

  public final Type tyype;
  public final Token nameToken;
  public final String fileName; // used by cities include-file stuff
  public final String name;
  public final String displayName;
  public int pos;
  public int endPos;
  public List<String> args = new ArrayList<String>();
  public String usableBy = "OTPL";
  public int localsCount = 0;
  public boolean topLevel = false;
  private SourceOwner owner;
  public final Procedure parent;
  public final scala.collection.mutable.ArrayBuffer<Procedure> children =
      new scala.collection.mutable.ArrayBuffer<Procedure>();

  public boolean isLambda() {
    return parent != null;
  }

  public int size; // cache args.size() for efficiency with making Activations

  // ExpressionParser doesn't know how many parameters the lambda is going to take;
  // that's determined by LambdaVisitor. so for now this is mutable - ST 2/4/11
  public final scala.collection.mutable.ArrayBuffer<Let> lambdaFormals =
      new scala.collection.mutable.ArrayBuffer<Let>();

  public Let getLambdaFormal(int n, Token token) {
    while (lambdaFormals.size() < n) {
      lambdaFormals.$plus$eq(
          new Let("?" + n, token.startPos(), token.endPos(),
              java.util.Collections.<Let>emptyList()));
    }
    return lambdaFormals.apply(n - 1);
  }

  public final List<Let> lets = new ArrayList<Let>();
  // each Int is the position of that variable in the procedure's args list
  public final scala.collection.mutable.HashMap<Let, Integer> alteredLets =
      new scala.collection.mutable.HashMap<Let, Integer>();

  public Command[] code = new Command[0];

  public Procedure(Type tyype, Token nameToken, String name, scala.Option<String> displayName, Procedure parent) {
    this.tyype = tyype;
    this.nameToken = nameToken;
    this.name = name;
    this.fileName = nameToken.fileName();
    this.parent = parent;
    this.displayName = buildDisplayName(displayName);
  }

  private String buildDisplayName(scala.Option<String> displayName) {
    return isLambda() ?
        "(command task from: " + parent.displayName + ")" :
        (displayName.isDefined() ? displayName.get() : ("procedure " + getNameAndFile()));
  }

  private String getNameAndFile() {
    if (fileName == null || fileName.length() == 0) {
      return name;
    }
    return name + " (" + fileName + ")";
  }

  public Syntax syntax() {
    int[] right = new int[(args.size() - localsCount)];
    for (int i = 0; i < right.length; i++) {
      right[i] = Syntax.TYPE_WILDCARD;
    }
    switch (tyype) {
      case COMMAND:
        return Syntax.commandSyntax(right);
      case REPORTER:
        return Syntax.reporterSyntax(right, Syntax.TYPE_WILDCARD);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public String toString() {
    return super.toString() +
        "[" + name + ":" + Dump.list(args) + ":" + usableBy + "]";
  }

  public String dump() {
    StringBuilder buf = new StringBuilder();
    boolean indent = isLambda();
    if (indent) {
      buf.append("   ");
    }
    buf.append(displayName);
    if (parent != null) {
      buf.append(":" + parent.displayName);
    }
    buf.append(":" + Dump.list(args) + "{" + usableBy + "}:\n");
    for (int i = 0; i < code.length; i++) {
      if (indent) {
        buf.append("   ");
      }
      Command command = code[i];
      buf.append("[" + i + "]");
      buf.append(command.dump(indent ? 6 : 3));
      buf.append("\n");
    }
    for (Procedure p : org.nlogo.util.JCL.toJavaIterable(children)) {
      buf.append("\n");
      buf.append(p.dump());
    }
    return buf.toString();
  }

  public void init(Workspace workspace) {
    size = args.size();
    for (int i = 0; i < code.length; i++) {
      code[i].init(workspace);
    }
    for (Procedure p : org.nlogo.util.JCL.toJavaIterable(children)) {
      p.init(workspace);
    }
  }

  public SourceOwner getOwner() {
    return owner;
  }

  public void setOwner(SourceOwner owner) {
    this.owner = owner;
    for (Procedure p : org.nlogo.util.JCL.toJavaIterable(children)) {
      p.setOwner(owner);
    }
  }
}
