// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import org.nlogo.agent.Agent;
import org.nlogo.core.CompilerException;
import org.nlogo.api.I18N;
import org.nlogo.core.TokenType;
import org.nlogo.window.EditorColorizer;
import org.nlogo.window.Widget;

import java.util.ArrayList;
import java.util.List;

strictfp class CommandLine
    extends org.nlogo.window.JobWidget
    implements
    java.awt.event.ActionListener,
    java.awt.event.KeyListener,
    org.nlogo.window.Events.CompiledEvent.Handler {
  static final String PROMPT = ">";
  static final String OBSERVER_PROMPT = I18N.guiJ().get("common.observer") + PROMPT;
  static final String TURTLE_PROMPT = I18N.guiJ().get("common.turtles") + PROMPT;
  static final String PATCH_PROMPT = I18N.guiJ().get("common.patches") + PROMPT;
  static final String LINK_PROMPT = I18N.guiJ().get("common.links") + PROMPT;

  private final org.nlogo.window.CommandCenterInterface commandCenter;
  private final boolean echoCommandsToOutput;
  public final org.nlogo.editor.EditorField<TokenType> textField;
  private final org.nlogo.nvm.Workspace workspace;

  // this is needed for if we're embedded in an agent monitor instead
  // of the command center - ST 7/30/03
  private org.nlogo.agent.Agent agent;

  public void agent(org.nlogo.agent.Agent agent) {
    this.agent = agent;
  }

  public org.nlogo.agent.Agent agent() {
    return agent;
  }

  ///

  public CommandLine(org.nlogo.window.CommandCenterInterface commandCenter,
                     boolean echoCommandsToOutput, int fontSize,
                     org.nlogo.nvm.Workspace workspace) {
    super(workspace.world().mainRNG);
    this.commandCenter = commandCenter;
    this.echoCommandsToOutput = echoCommandsToOutput;
    this.workspace = workspace;
    agentClass(org.nlogo.agent.Observer.class);
    textField =
        new org.nlogo.editor.EditorField<TokenType>
            (30,
                new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont(),
                    java.awt.Font.PLAIN, 12),
                true, new EditorColorizer(workspace),
                I18N.guiJ().fn());
    textField.setFont(textField.getFont().deriveFont((float) fontSize));
    textField.addKeyListener(this);
    setLayout(new java.awt.BorderLayout());
    displayName(classDisplayName());
    add(new javax.swing.JScrollPane
        (textField,
            javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
        java.awt.BorderLayout.CENTER);
  }

  ///

  // I have no idea why, but at least on Macs, without this our minimum
  // height is larger than our preferred height, which doesn't make sense
  // - ST 8/26/03

  @Override
  public java.awt.Dimension getMinimumSize() {
    return new java.awt.Dimension(super.getMinimumSize().width,
        getPreferredSize().height);
  }

  ///

  @Override
  public String classDisplayName() {
    return "Command Center";
  } // for Errors tab

  @Override
  public boolean isCommandCenter() {
    return true;  // so CompilerManager treats us specially - ST 6/9/03
  }

  ///

  private String getText() {
    return textField.getText();
  }

  private void setText(String s) {
    textField.setText(s);
  }

  ///

  @Override
  public boolean isFocusable() {
    return false;
  }

  @Override
  public void requestFocus() {
    textField.requestFocus();
  }

  /// keyboard handling for the text field

  public void keyReleased(java.awt.event.KeyEvent e) {
  }

  public void keyTyped(java.awt.event.KeyEvent e) {
  }

  public void keyPressed(java.awt.event.KeyEvent e) {
    switch (e.getKeyCode()) {
      case java.awt.event.KeyEvent.VK_ENTER:
        executeCurrentBuffer();
        e.consume();
        break;
      case java.awt.event.KeyEvent.VK_TAB:
        commandCenter.cycleAgentType(!e.isShiftDown());
        e.consume();
        break;
      case java.awt.event.KeyEvent.VK_DOWN:
        cycleListForward();
        break;
      case java.awt.event.KeyEvent.VK_UP:
        cycleListBack();
        break;
      default:
        // do nothing
        break;
    }
  }

  ///

  public void actionPerformed(java.awt.event.ActionEvent e) {
    executeCurrentBuffer();
  }

  private void executeCurrentBuffer() {
    String inner = getText();
    if (inner.trim().equals("")) {
      setText("");
      return;
    }
    if(workspace.isReporter(inner)) {
      inner = "show " + inner;
      setText(inner);
    }
    String header = "to __commandline [] ";
    String footer = "__done end";
    if (agentClass() == org.nlogo.agent.Observer.class) {
      header += "__observercode ";
    } else if (agentClass() == org.nlogo.agent.Turtle.class) {
      header += "__turtlecode ";
    } else if (agentClass() == org.nlogo.agent.Patch.class) {
      header += "__patchcode ";
    } else if (agentClass() == org.nlogo.agent.Link.class) {
      header += "__linkcode ";
    }
    source(header, inner, "\n" + footer); // the \n is to protect against comments in inner
  }

  @Override
  public void handle(org.nlogo.window.Events.CompiledEvent e) {
    super.handle(e);
    if (e.sourceOwner == this) {
      error(e.error);
      if (error() == null) {
        setText("");
        String outStr = innerSource();
        if (!outStr.trim().equals("")) {
          addToHistory(outStr);
          if (echoCommandsToOutput) {
            if (agentClass() == org.nlogo.agent.Turtle.class) {
              outStr = TURTLE_PROMPT + " " + outStr;
            } else if (agentClass() == org.nlogo.agent.Patch.class) {
              outStr = PATCH_PROMPT + " " + outStr;
            } else if (agentClass() == org.nlogo.agent.Link.class) {
              outStr = LINK_PROMPT + " " + outStr;
            } else {
              outStr = OBSERVER_PROMPT + " " + outStr;
            }
            new org.nlogo.window.Events.OutputEvent
                (false, new org.nlogo.agent.OutputObject("", outStr, true, false),
                    false, true).raise(this);
          }
          if (agent != null) {
            org.nlogo.agent.AgentSet agentSet =
                new org.nlogo.agent.ArrayAgentSet(agentClass(), 1, false, agent.world());
            agentSet.add(agent);
            agents(agentSet);
          }
          new org.nlogo.window.Events.AddJobEvent(this, agents(), procedure())
              .raise(this);
        }
      } else if (error() instanceof CompilerException) {
        int offset = headerSource.length();
        // highlight error location

        textField.select(((CompilerException) error()).start() - offset,
            ((CompilerException) error()).end() - offset);

        // print error message
        new org.nlogo.window.Events.OutputEvent
            (false,
                new org.nlogo.agent.OutputObject
                    ("", "ERROR: " + error().getMessage(), true, true),
                true, true).raise(this);
      }
    }
  }

  /// history handling

  private static final int MAX_HISTORY_SIZE = 40;
  private int historyPosition = -1;
  private String historyBase = "";
  private Class<? extends Agent> historyBaseClass = org.nlogo.agent.Observer.class;
  private final List<ExecutionString> history =
      new ArrayList<ExecutionString>(MAX_HISTORY_SIZE);

  private void addToHistory(String str) {
    ExecutionString executionString =
        new ExecutionString(agentClass(), str);
    if (history.isEmpty() ||
        !executionString.equals(history.get(0))) {
      history.add(0, executionString);
      while (history.size() > MAX_HISTORY_SIZE) {
        history.remove(history.size() - 1);
      }
    }
    historyPosition = -1;
  }

  void cycleListBack() {
    if (!history.isEmpty()) {
      if (historyPosition == -1) {
        historyBase = getText();
        historyBaseClass = agentClass();
      }
      if (historyPosition + 1 < history.size()) {
        historyPosition++;
        ExecutionString es = history.get(historyPosition);
        setText(es.string);
        agentClass(es.agentClass);
      }
    }
    commandCenter.repaintPrompt();
  }

  void cycleListForward() {
    if (historyPosition == 0) {
      setText(historyBase);
      agentClass(historyBaseClass);
      historyPosition = -1;
    } else if (historyPosition > 0 && !history.isEmpty()) {
      historyPosition--;
      ExecutionString es = history.get(historyPosition);
      setText(es.string);
      agentClass(es.agentClass);
    }
    commandCenter.repaintPrompt();
  }

  List<ExecutionString> getExecutionList() {
    return history;
  }

  void reset() {
    clearList();
    setText("");
    agentClass(org.nlogo.agent.Observer.class);
  }

  void clearList() {
    history.clear();
    historyPosition = 0;
  }

  void setExecutionString(ExecutionString es) {
    setText(es.string);
    agentClass(es.agentClass);
    textField.setCaretPosition(getText().length());
    commandCenter.repaintPrompt();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    textField.setEnabled(enabled);
  }

  @Override
  public String save() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object load(String[] strings, Widget.LoadHelper helper) {
    throw new UnsupportedOperationException();
  }

  static strictfp class ExecutionString {
    final Class<? extends Agent> agentClass;
    final String string;

    ExecutionString(Class<? extends Agent> agentClass, String string) {
      this.agentClass = agentClass;
      this.string = string;
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj ||
          (obj instanceof ExecutionString &&
              ((ExecutionString) obj).agentClass == agentClass &&
              ((ExecutionString) obj).string.equals(string));
    }

    // not actually using this at present, but pmd yells at us if
    // we override equals() without overriding hashCode() too.. - ST 9/1/03
    @Override
    public int hashCode() {
      return agentClass.hashCode() + string.hashCode();
    }
  }

}
