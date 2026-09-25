import {
  type Completion, CompletionContext, type CompletionResult, acceptCompletion, autocompletion, completionKeymap
} from "@codemirror/autocomplete";
import {
  cursorGroupBackward, cursorGroupForward, deleteGroupBackward, deleteGroupForward, history, indentLess, indentMore,
  moveLineDown, moveLineUp, redo, selectGroupBackward, selectGroupForward, undo
} from "@codemirror/commands";
import {
  HighlightStyle, bracketMatching, foldGutter, LRLanguage, LanguageSupport, syntaxHighlighting, defaultHighlightStyle,
  ensureSyntaxTree, foldAll, foldEffect, foldService, unfoldAll, unfoldEffect
} from "@codemirror/language";
import { highlightSelectionMatches } from "@codemirror/search";
import { Compartment, EditorState, Line, SelectionRange, Text, Transaction } from "@codemirror/state";
import {
  EditorView, keymap, drawSelection, highlightActiveLine, rectangularSelection, crosshairCursor, lineNumbers,
  highlightActiveLineGutter, ViewUpdate
} from "@codemirror/view";
import { styleTags, Tag, tags } from "@lezer/highlight";

import { toggleComments } from "./comment.js";
import { executeIndentations } from "./indent.js";
import { parser } from "./netlogo.js";
import { End, To } from "./netlogo.terms.js";

interface ColorTheme {
  background: string;
  gutterBorder: string;
  scrollBarBackground: string;
  scrollBarForeground: string;
  scrollBarForegroundHover: string;
  caret: string;
  lineHighlight: string;
  selection: string;
  selectionError: string;
  default: string;
  comment: string;
  constant: string;
  keyword: string;
  command: string;
  reporter: string;
}

interface FoldRange {
  from: number;
  to: number;
}

interface CompletionPlus extends Completion {
  core: boolean;
}

interface BasicLine {
  number: number;
  text: string;
}

class Trie {
  readonly core: boolean;

  value?: string;
  type?: string;
  children: Map<string, Trie> = new Map<string, Trie>();

  constructor(core: boolean) {
    this.core = core;
  }

  append(value: string, type: string, offset: number = 0): void {
    if (value[offset]) {
      const child: Trie = this.children.get(value[offset]) ?? new Trie(this.core);

      child.append(value, type, offset + 1);

      this.children.set(value[offset], child);
    } else {
      this.value = value;
      this.type = type;
    }
  }

  appendAll(values: string[], type: string): void {
    values.forEach(value => {
      this.append(value, type);
    });
  }

  match(value: string, offset: number = 0): CompletionPlus | undefined {
    if (value[offset]) {
      return this.children.get(value[offset])?.match(value, offset + 1);
    }

    if (this.value == value) {
      return {
        label: this.value,
        type: this.type,
        core: this.core
      };
    }

    return undefined;
  }

  matches(value: string, offset: number = 0): CompletionPlus[] {
    if (value[offset]) {
      return this.children.get(value[offset])?.matches(value, offset + 1) ?? [];
    }

    const entries: CompletionPlus[] = [];

    this.children.forEach((child: Trie) => {
      entries.push(...child.matches("", offset + 1));
    });

    if (this.value) {
      entries.push({
        label: this.value,
        type: this.type,
        core: this.core
      });
    }

    return entries;
  }
}

class Program {
  decls: string[] = [];

  private core: Trie = new Trie(true);
  private compiled: Trie = new Trie(false);

  setCore(keywords: string[], constants: string[], commands: string[], reporters: string[]) {
    this.decls = keywords.filter(value => !value.match("to|to-report|import|export"));

    this.core = new Trie(true);

    this.core.appendAll(keywords, "keyword");
    this.core.appendAll(constants, "constant");
    this.core.appendAll(commands, "command");
    this.core.appendAll(reporters, "reporter");
  }

  setCompiled(keywords: string[], globals: string[], variables: string[], commands: string[], reporters: string[]) {
    this.compiled = new Trie(false);

    this.compiled.appendAll(keywords, "keyword");
    this.compiled.appendAll(globals, "global");
    this.compiled.appendAll(variables, "variable");
    this.compiled.appendAll(commands, "command");
    this.compiled.appendAll(reporters, "reporter");
  }

  match(value: string, offset: number = 0): CompletionPlus | undefined {
    return this.core.match(value, offset) ?? this.compiled.match(value, offset);
  }

  matches(value: string, offset: number = 0): CompletionPlus[] {
    return this.core.matches(value, offset).concat(this.compiled.matches(value, offset));
  }
}

const identRegex: RegExp = /[\w\-:.?=*!<>#+/%$\^'&]+/;

const commandTag: Tag = Tag.define("command", tags.name);
const reporterTag: Tag = Tag.define("reporter", tags.name);

declare global {
  interface Window {
    view: EditorView;

    themeConfig: Compartment;
    selectionConfig: Compartment;
    highlightConfig: Compartment;
    syntaxConfig: Compartment;
    historyConfig: Compartment;
    fontConfig: Compartment;
    editableConfig: Compartment;
    readOnlyConfig: Compartment;
    lineNumbersConfig: Compartment;

    program: Program;

    currentTheme: ColorTheme;

    overwriting: boolean;
    highlightActive: boolean;
    smartIndent: boolean;
    lineNumbers: boolean;
    completeOnType: boolean;

    getText: () => string;
    getSelectionStart: () => number;
    getSelectionEnd: () => number;
    getSelectedText: () => string;
    getCaretPosition: () => number;
    getCaretX: () => number;
    getCaretY: () => number;
    getTokenAtCaret: () => string;
    getLinesForOffsets: (offsets: number[]) => BasicLine[];
    setText: (text: string) => void;
    refreshText: () => void;
    undo: () => void;
    redo: () => void;
    resetHistory: () => void;
    copy: () => void;
    cut: () => void;
    paste: () => void;
    select: (start: number, end: number) => void;
    selectAll: () => void;
    replaceSelection: (text: string) => void;
    shiftLeft: () => void;
    shiftRight: () => void;
    indent: (view: EditorView) => boolean;
    unindent: (view: EditorView) => boolean;
    handleEnter: (view: EditorView) => boolean;
    toggleComments: () => void;
    isEditable: () => boolean;
    setEditable: (editable: boolean) => void;
    setIndenter: (smart: boolean) => void;
    getLineNumbers: () => boolean;
    setLineNumbers: (visible: boolean) => void;
    setCompleteOnType: (enabled: boolean) => void;
    setFont: (family: string, size: number) => void;
    setNormalSelection: () => void;
    setErrorSelection: () => void;
    setHighlight: (active: boolean) => void;
    getFold: (state: EditorState, start: number, end: number) => FoldRange | null;
    getFolds: (state: EditorState) => FoldRange[];
    foldSelected: () => void;
    unfoldSelected: () => void;
    foldAll: () => void;
    unfoldAll: () => void;
    setCoreProgram: (keywords: string[], constants: string[], commands: string[], reporters: string[]) => void;
    setCompiledProgram: (keywords: string[], globals: string[], variables: string[], commands: string[],
                         reporters: string[]) => void;
    autocomplete: (context: CompletionContext) => CompletionResult | null;
    doScroll: (mouseX: number, mouseY: number, scrollX: number, scrollY: number) => void;
    syncTheme: (theme: ColorTheme) => void;
    nullHandler: (view: EditorView) => boolean;
    toBase64: (text: string) => string;
    fromBase64: (text: string) => string;

    bridge: {
      log: (message: String) => void;
      textUpdated: (overwriting: boolean, canUndo: boolean, canRedo: boolean) => void;
      writeClipboard: (text: String) => void;
      readClipboard: () => string;
      jumpToDeclaration: () => void;
    };
  }
}

window.onload = () => {
  window.themeConfig = new Compartment();
  window.selectionConfig = new Compartment();
  window.highlightConfig = new Compartment();
  window.syntaxConfig = new Compartment();
  window.historyConfig = new Compartment();
  window.fontConfig = new Compartment();
  window.editableConfig = new Compartment();
  window.readOnlyConfig = new Compartment();
  window.lineNumbersConfig = new Compartment();

  window.program = new Program();

  window.currentTheme = {
    background: "",
    gutterBorder: "",
    scrollBarBackground: "",
    scrollBarForeground: "",
    scrollBarForegroundHover: "",
    caret: "",
    lineHighlight: "",
    selection: "",
    selectionError: "",
    default: "",
    comment: "",
    constant: "",
    keyword: "",
    command: "",
    reporter: "",
  };

  window.view = new EditorView({
    parent: document.body,
    extensions: [
      foldGutter(),
      drawSelection(),
      EditorState.allowMultipleSelections.of(true),
      bracketMatching(),
      rectangularSelection({
        eventFilter: (event: MouseEvent) => event.shiftKey && event.altKey
      }),
      crosshairCursor(),
      highlightActiveLine(),
      highlightActiveLineGutter(),
      highlightSelectionMatches({
        wholeWords: true
      }),
      autocompletion({
        icons: false,
        optionClass: (completion: Completion): string => `cm-completionLabel-${completion.type}`
      }),
      new LanguageSupport(LRLanguage.define({
        parser: parser.configure({
          props: [
            styleTags({
              Comment: tags.comment,
              Import: tags.keyword,
              Export: tags.keyword,
              From: tags.keyword,
              As: tags.keyword,
              Globals: tags.keyword,
              Breed: tags.keyword,
              Own: tags.keyword,
              Extensions: tags.keyword,
              Includes: tags.keyword,
              To: tags.keyword,
              End: tags.keyword,
              Identifier: tags.name,
              Number: tags.literal,
              String: tags.literal,
              Command: commandTag,
              Reporter: reporterTag,
              Var: reporterTag,
              Constant: tags.literal
            })
          ]
        }),
        languageData: {
          "wordChars": "_.?=*!<>#+/%$^'&-",
          "autocomplete": window.autocomplete
        }
      })),
      foldService.of(window.getFold),
      keymap.of([
        { key: "Tab", run: acceptCompletion },
        ...completionKeymap,
        { key: "Mod-z", run: window.nullHandler },
        { key: "Mod-y", run: window.nullHandler },
        { key: "Mod-x", run: window.nullHandler },
        { key: "Mod-v", run: window.nullHandler },
        { key: "Mod-m", run: window.nullHandler },
        { key: "Mod-u", run: window.nullHandler },
        { key: "Mod-Backspace", mac: "Alt-Backspace", run: deleteGroupBackward },
        { key: "Mod-Delete", mac: "Alt-Delete", run: deleteGroupForward },
        { key: "Mod-ArrowLeft", mac: "Alt-ArrowLeft", run: cursorGroupBackward, shift: selectGroupBackward },
        { key: "Mod-ArrowRight", mac: "Alt-ArrowRight", run: cursorGroupForward, shift: selectGroupForward },
        { key: "Alt-ArrowUp", run: moveLineUp },
        { key: "Alt-ArrowDown", run: moveLineDown },
        { key: "Tab", run: window.indent, shift: window.unindent },
        { key: "Enter", run: window.handleEnter },
        { key: "Alt-f", run: window.nullHandler },
        { key: "Alt-e", run: window.nullHandler },
        { key: "Alt-t", run: window.nullHandler },
        { key: "Alt-z", run: window.nullHandler },
        { key: "Alt-a", run: window.nullHandler },
        { key: "Alt-h", run: window.nullHandler },
      ]),
      EditorView.clickAddsSelectionRange.of((event: MouseEvent) => event.altKey),
      EditorView.domEventHandlers({
        click: (event: MouseEvent) => {
          if (event.ctrlKey && event.button == 0) {
            window.bridge.jumpToDeclaration();
          }
        }
      }),
      EditorView.updateListener.of((update: ViewUpdate) => {
        if (update.docChanged) {
          const state: EditorState = update.view.state;

          const canUndo = undo({ state, dispatch: () => {} });
          const canRedo = redo({ state, dispatch: () => {} });

          window.bridge.textUpdated(window.overwriting, canUndo, canRedo);

          window.setHighlight(true);

          if (!state.readOnly && window.smartIndent) {
            update.changes.iterChanges((_, __, ___, ____, text: Text) => {
              const str: string = text.toString();

              if (str.match(/[\[\]\n]/) ||
                  (str.match(/[end]/) && state.doc.lineAt(state.selection.main.head).text.match(/^ *end/i))) {
                executeIndentations(update.view);
              }
            });
          }
        } else if (update.selectionSet) {
          window.setHighlight(update.state.selection.main.empty);
        }
      }),
      window.themeConfig.of(EditorView.theme({})),
      window.selectionConfig.of(EditorView.theme({})),
      window.highlightConfig.of(EditorView.theme({})),
      window.syntaxConfig.of(syntaxHighlighting(defaultHighlightStyle)),
      window.historyConfig.of(history()),
      window.fontConfig.of(EditorView.theme({})),
      window.editableConfig.of(EditorView.editable.of(true)),
      window.readOnlyConfig.of(EditorState.readOnly.of(false)),
      window.lineNumbersConfig.of([])
    ]
  });
};

window.getText = () => {
  return window.toBase64(window.view.state.doc.toString());
};

window.getSelectionStart = () => {
  return window.view.state.selection.ranges[0]?.from ?? 0;
};

window.getSelectionEnd = () => {
  const ranges: readonly SelectionRange[] = window.view.state.selection.ranges;

  return ranges[ranges.length - 1]?.to ?? 0;
};

window.getSelectedText = () => {
  const state: EditorState = window.view.state;
  const selection: string = state.selection.ranges.map(range => state.sliceDoc(range.from, range.to)).join("\n");

  return window.toBase64(selection);
};

window.getCaretPosition = () => {
  return window.view.state.selection.main.head;
};

window.getCaretX = () => {
  return Math.round(window.view.coordsAtPos(window.view.state.selection.main.head)?.left ?? 0);
};

window.getCaretY = () => {
  return Math.round(window.view.coordsAtPos(window.view.state.selection.main.head)?.bottom ?? 0);
};

window.getTokenAtCaret = () => {
  const caret: number = window.view.state.selection.main.head;
  const line: Line = window.view.state.doc.lineAt(caret);
  const text: string = line.text;
  const offset: number = caret - line.from;

  let start = offset;

  while (start > 0 && /\S/.test(text[start - 1] ?? "")) {
    start--;
  }

  let end = offset;

  while (end < text.length && /\S/.test(text[end] ?? "")) {
    end++;
  }

  return text.slice(start, end);
};

window.getLinesForOffsets = (offsets: number[]) => {
  const doc: Text = window.view.state.doc;

  return offsets.map(offset => doc.lineAt(offset));
};

window.setText = (text: string) => {
  const state: EditorState = window.view.state;
  const transaction: Transaction = state.update({
    changes: { from: 0, to: state.doc.length, insert: window.fromBase64(text) },
    selection: { anchor: 0, head: 0 },
    scrollIntoView: true
  });

  window.overwriting = true;
  window.view.dispatch([ transaction ]);
  window.overwriting = false;

  window.resetHistory();
};

window.refreshText = () => {
  window.overwriting = true;

  window.view.dispatch({
    changes: {
      from: 0,
      to: window.view.state.doc.length,
      insert: window.view.state.doc.toString()
    },
    selection: {
      anchor: 0,
      head: 0
    },
    scrollIntoView: true
  });

  window.overwriting = false;
}

window.undo = () => {
  undo(window.view);

  window.setHighlight(window.view.state.selection.main.empty);
};

window.redo = () => {
  redo(window.view);

  window.setHighlight(window.view.state.selection.main.empty);
};

window.resetHistory = () => {
  window.view.dispatch({
    effects: [
      window.historyConfig.reconfigure([])
    ]
  });

  window.view.dispatch({
    effects: [
      window.historyConfig.reconfigure(history())
    ]
  });
};

window.copy = () => {
  const state: EditorState = window.view.state;
  const selection: string = state.selection.ranges.map(range => state.sliceDoc(range.from, range.to)).join("\n");

  window.bridge.writeClipboard(selection);

  return true;
};

window.cut = () => {
  if (window.view.state.readOnly) {
    return false;
  }

  window.copy();

  window.view.dispatch(window.view.state.replaceSelection(""));

  return true;
};

window.paste = () => {
  if (window.view.state.readOnly) {
    return false;
  }

  window.view.dispatch(window.view.state.replaceSelection(window.bridge.readClipboard()));

  return true;
};

window.select = (start: number, end: number) => {
  const length: number = window.view.state.doc.length;
  const anchor: number = Math.min(start, length);

  window.view.dispatch({
    selection: { anchor: anchor, head: Math.min(end, length) },
    effects: EditorView.scrollIntoView(anchor, {
      x: "center",
      y: "center"
    })
  });
};

window.selectAll = () => {
  window.view.dispatch({
    selection: { anchor: 0, head: window.view.state.doc.length }
  });
};

window.replaceSelection = (text: string) => {
  if (!window.view.state.readOnly) {
    window.view.dispatch(window.view.state.replaceSelection(text));
  }
};

window.shiftLeft = () => {
  indentLess(window.view);
};

window.shiftRight = () => {
  indentMore(window.view);
};

window.indent = (view: EditorView) => {
  if (view.state.readOnly) {
    return false;
  }

  if (window.smartIndent) {
    executeIndentations(view);
  } else {
    indentMore(view);
  }

  return true;
};

window.unindent = (view: EditorView) => {
  if (view.state.readOnly) {
    return false;
  }

  if (window.smartIndent) {
    executeIndentations(view);
  } else {
    indentLess(view);
  }

  return true;
};

window.handleEnter = (view: EditorView) => {
  if (view.state.readOnly) {
    return false;
  }

  view.dispatch(view.state.replaceSelection("\n"), { scrollIntoView: true });

  return true;
}

window.toggleComments = () => {
  if (window.view.state.readOnly) {
    return false;
  }

  toggleComments(window.view);

  return true;
};

window.isEditable = () => {
  return !window.view.state.readOnly;
};

window.setEditable = (editable: boolean) => {
  window.view.dispatch({
    effects: [
      window.editableConfig.reconfigure(EditorView.editable.of(editable)),
      window.readOnlyConfig.reconfigure(EditorState.readOnly.of(!editable))
    ]
  });
};

window.setIndenter = (smart: boolean) => {
  window.smartIndent = smart;
};

window.getLineNumbers = () => {
  return window.lineNumbers;
};

window.setLineNumbers = (visible: boolean) => {
  window.lineNumbers = visible;

  if (visible) {
    window.view.dispatch({
      effects: [
        window.lineNumbersConfig.reconfigure(lineNumbers())
      ]
    });
  } else {
    window.view.dispatch({
      effects: [
        window.lineNumbersConfig.reconfigure([])
      ]
    });
  }
};

window.setCompleteOnType = (enabled: boolean) => {
  window.completeOnType = enabled;
}

window.setFont = (family: string, size: number) => {
  window.view.dispatch({
    effects: [
      window.fontConfig.reconfigure(EditorView.theme({
        "&, .cm-completionLabel, .cm-content, .cm-gutters": {
          fontSize: `${size}pt`,
          fontFamily: `${family}, monospace`
        }
      }))
    ]
  })
};

window.setNormalSelection = () => {
  window.view.dispatch({
    effects: [
      window.selectionConfig.reconfigure(EditorView.theme({
        "&.cm-focused .cm-selectionBackground, & .cm-selectionBackground": {
          backgroundColor: window.currentTheme.selection + " !important"
        }
      }))
    ]
  });
};

window.setErrorSelection = () => {
  window.view.dispatch({
    effects: [
      window.selectionConfig.reconfigure(EditorView.theme({
        "&.cm-focused .cm-selectionBackground, & .cm-selectionBackground": {
          backgroundColor: window.currentTheme.selectionError + " !important"
        }
      }))
    ]
  });
};

window.setHighlight = (active: boolean) => {
  if (window.highlightActive != active) {
    window.highlightActive = active;

    window.view.dispatch({
      effects: [
        window.highlightConfig.reconfigure(EditorView.theme({
          ".cm-activeLine": {
            backgroundColor: active ? window.currentTheme.lineHighlight : "transparent"
          }
        }))
      ]
    });
  }
}

window.getFold = (state: EditorState, start: number, end: number) => {
  const doc: Text = state.doc;
  const startLine: Line = doc.lineAt(start);

  if (startLine.text.trimStart().toLowerCase().startsWith("to")) {
    let endLine: number = startLine.number + 1;

    while (endLine <= doc.lines) {
      const line: string = doc.line(endLine).text.trimStart().toLowerCase();

      if (line.startsWith("end")) {
        break;
      }

      if (line.startsWith("to")) {
        return null;
      }

      endLine++;
    }

    if (endLine > doc.lines) {
      return null;
    }

    const to: number = doc.line(endLine).to;

    if (end == to) {
      return null;
    }

    return { from: end, to: to };
  }

  return null;
};

window.getFolds = (state: EditorState) => {
  const doc: Text = state.doc;

  return state.selection.ranges.flatMap((range: SelectionRange) => {
    const end: number = doc.lineAt(range.to).number;
    const folds: FoldRange[] = [];

    let current: number = doc.lineAt(range.from).number;

    while (current <= end) {
      const line: Line = doc.line(current);
      const fold: FoldRange | null = window.getFold(state, line.from, line.to);

      if (fold) {
        folds.push(fold);

        current = doc.lineAt(fold.to).number + 1;
      } else {
        current++;
      }
    }

    return folds;
  });
}

window.foldSelected = () => {
  window.getFolds(window.view.state).forEach((fold) => {
    window.view.dispatch({
      effects: foldEffect.of(fold)
    });
  });
};

window.unfoldSelected = () => {
  window.getFolds(window.view.state).forEach((fold) => {
    window.view.dispatch({
      effects: unfoldEffect.of(fold)
    });
  });
};

window.foldAll = () => {
  foldAll(window.view);
};

window.unfoldAll = () => {
  unfoldAll(window.view);
};

window.setCoreProgram = (keywords: string[], constants: string[], commands: string[], reporters: string[]) => {
  window.program.setCore(keywords, constants, commands, reporters);
};

window.setCompiledProgram = (keywords: string[], globals: string[], variables: string[], commands: string[],
                             reporters: string[]) => {
  window.program.setCompiled(keywords, globals, variables, commands, reporters);
};

window.autocomplete = (context: CompletionContext) => {
  let scope = 0;

  ensureSyntaxTree(window.view.state, context.pos)?.iterate({
    from: 0,
    to: context.pos,
    enter(node) {
      if (node.type.id == To) {
        scope++;
      } else if (node.type.id == End && scope > 0) {
        scope--;
      }
    }
  });

  const match = context.matchBefore(identRegex);

  if (scope == 1 && match?.text == "to") {
    scope = 0;
  }

  const inProc = scope > 0;

  if (context.explicit) {
    return {
      from: match?.from ?? context.pos,
      options: window.program.matches(match?.text.toLowerCase() ?? "").filter((completion) => {
        return (completion.type == "keyword") != inProc;
      })
    };
  }

  const doc: Text = context.state.doc;

  if (match && window.completeOnType && doc.sliceString(match.from - 1, match.from) != '"') {
    const line: string = doc.sliceString(doc.lineAt(match.from).from, match.from).toLowerCase();

    const procMatch = line.match(`^\\s*(to|to-report)\\s+(${identRegex}\\s*\\[)?`);
    const modMatch = line.match(/^\s*(import|export)\s+\[?/);
    const declMatch = line.match(`^\\s*(${window.program.decls.join("|")})\\s*\\[`);
    const letMatch = line.match(/^\s*let\s+$/);
    const semiMatch = line.match(/^.*;/);
    const quoteMatch = line.match(/^.*"/);

    if (procMatch || modMatch || declMatch || letMatch ||
        (semiMatch && (semiMatch[0].match(/(?<!\\)"/g)?.length ?? 0) % 2 == 0) ||
        (quoteMatch && (quoteMatch[0].match(/(?<!\\)"/g)?.length ?? 0) % 2 != 0)) {
      return null;
    }

    return {
      from: match.from,
      options: window.program.matches(match.text.toLowerCase()).filter((completion) => {
        return (completion.type == "keyword") != inProc;
      })
    };
  }

  return null;
};

window.doScroll = (mouseX: number, mouseY: number, scrollX: number, scrollY: number) => {
  const popup: Element | null = document.querySelector(".cm-tooltip-autocomplete ul");

  if (popup?.contains(document.elementFromPoint(mouseX, mouseY))) {
    popup.scrollBy(scrollX, scrollY);
  } else {
    window.view.scrollDOM.scrollBy(scrollX, scrollY);
  }
}

window.syncTheme = (theme: ColorTheme) => {
  document.body.style.background = theme.background;

  const root = document.querySelector(":root") as HTMLElement;

  root.style.setProperty("--scrollbar-background", theme.scrollBarBackground);
  root.style.setProperty("--scrollbar-foreground", theme.scrollBarForeground);
  root.style.setProperty("--scrollbar-foreground-hover", theme.scrollBarForegroundHover);

  window.currentTheme = theme;

  window.view.dispatch({
    effects: [
      window.themeConfig.reconfigure(EditorView.theme({
        "&": {
          height: "100%"
        },
        "&.cm-focused": {
          outline: "none"
        },
        "&.cm-focused .cm-nonmatchingBracket": {
          backgroundColor: "transparent !important"
        },
        "&, .cm-gutters, .cm-gutter, .cm-gutterElement": {
          backgroundColor: theme.background,
          color: theme.default
        },
        ".cm-gutters": {
          borderRightColor: theme.gutterBorder
        },
        "& .cm-cursor, & .cm-dropCursor": {
          borderLeftColor: theme.caret
        },
        ".cm-selectionMatch": {
          backgroundColor: theme.selection + " !important"
        },
        ".cm-tooltip-autocomplete": {
          backgroundColor: theme.background
        },
        ".cm-tooltip-autocomplete ul li[aria-selected]": {
          backgroundColor: theme.selection
        },
        ".cm-completionLabel-keyword, .cm-tooltip-autocomplete ul .cm-completionLabel-keyword[aria-selected]": {
          color: theme.keyword
        },
        ".cm-completionLabel-constant, .cm-tooltip-autocomplete ul .cm-completionLabel-constant[aria-selected]": {
          color: theme.constant
        },
        ".cm-completionLabel-global, .cm-tooltip-autocomplete ul .cm-completionLabel-global[aria-selected]": {
          color: theme.default
        },
        ".cm-completionLabel-variable, .cm-tooltip-autocomplete ul .cm-completionLabel-variable[aria-selected]": {
          color: theme.reporter
        },
        ".cm-completionLabel-reporter, .cm-tooltip-autocomplete ul .cm-completionLabel-reporter[aria-selected]": {
          color: theme.reporter
        },
        ".cm-completionLabel-command, .cm-tooltip-autocomplete ul .cm-completionLabel-command[aria-selected]": {
          color: theme.command
        }
      })),
      window.highlightConfig.reconfigure(EditorView.theme({
        ".cm-activeLine": {
          backgroundColor: theme.lineHighlight
        }
      })),
      window.syntaxConfig.reconfigure(syntaxHighlighting(HighlightStyle.define([
        { tag: tags.name, color: theme.default },
        { tag: tags.comment, color: theme.comment },
        { tag: tags.keyword, color: theme.keyword, fontWeight: "bold" },
        { tag: tags.literal, color: theme.constant },
        { tag: commandTag, color: theme.command },
        { tag: reporterTag, color: theme.reporter }
      ])))
    ]
  });

  window.setNormalSelection();
};

window.nullHandler = (_: EditorView) => {
  return true;
};

window.toBase64 = (text: string) => {
  let encoder = new TextEncoder();

  let str = "";

  for (let i = 0; i < text.length; i += 32766) {
    str += btoa(String.fromCodePoint(...encoder.encode(text.substring(i, Math.min(i + 32766, text.length)))));
  }

  return str;
};

window.fromBase64 = (text: string) => {
  return new TextDecoder().decode(Uint8Array.from(atob(text), c => c.codePointAt(0) ?? 0));
}
