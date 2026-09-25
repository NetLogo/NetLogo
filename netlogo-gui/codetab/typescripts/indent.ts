import { type ChangeSpec, Line, SelectionRange, Text } from "@codemirror/state";
import { EditorView } from "@codemirror/view";
import { type SyntaxNodeRef, Tree } from "@lezer/common";

import { parser } from "./netlogo.js";
import {
  Breed, CloseBracket, Command, End, Extensions, Globals, Includes, OpenBracket, Own, To
} from "./netlogo.terms.js";

class TokenizedLine {
  readonly line: Line;
  readonly tokens: SyntaxNodeRef[];
  readonly leading: number;
  readonly bracketDelta: number;
  readonly bracketsClosed: number;

  constructor(line: Line, tokens: SyntaxNodeRef[]) {
    this.line = line;
    this.tokens = tokens.filter((node: SyntaxNodeRef) => !node.type.isTop && !node.type.isError);
    this.bracketDelta = 0;
    this.bracketsClosed = 0;

    const start = this.line.text.search(/\S/);

    if (start == -1) {
      this.leading = this.line.length;
    } else {
      this.leading = start;
    }

    for (const token of this.tokens) {
      switch (token.type.id) {
        case OpenBracket:
          this.bracketDelta++;

          break;

        case CloseBracket:
          this.bracketDelta--;
          this.bracketsClosed = Math.max(this.bracketsClosed, -this.bracketDelta);

          break;
      }
    }
  }
}

class IndentUpdate {
  readonly changes: ChangeSpec[];
  readonly caretShift: number;

  constructor(changes: ChangeSpec[], caretShift: number) {
    this.changes = changes;
    this.caretShift = caretShift;
  }
}

function getUpdate(view: EditorView): IndentUpdate {
  const doc: Text = view.state.doc;

  let caretShift = 0;

  const changes: ChangeSpec[] = view.state.selection.ranges.flatMap((range: SelectionRange) => {
    const startLine: Line = doc.lineAt(range.from);
    const endLine: Line = doc.lineAt(range.to);
    const caretLine: Line = doc.lineAt(range.head);

    const tree: Tree = parser.parse(doc.sliceString(0, endLine.to));

    const parsedLines: TokenizedLine[] = [];

    let currentLine: Line = doc.lineAt(range.from);
    let currentTokens: SyntaxNodeRef[] = [];

    tree.iterate({
      enter: (node: SyntaxNodeRef) => {
        let line: Line = doc.lineAt(node.from);

        if (line.number == currentLine.number) {
          currentTokens.push(node.node);
        } else {
          if (currentTokens.length > 0) {
            parsedLines.push(new TokenizedLine(currentLine, currentTokens));
          }

          currentLine = line;
          currentTokens = [ node.node ];
        }

        return true;
      }
    });

    if (currentTokens.length > 0) {
      parsedLines.push(new TokenizedLine(currentLine, currentTokens));
    }

    if ((parsedLines[parsedLines.length - 1]?.line.number ?? 0) < endLine.number) {
      parsedLines.push(new TokenizedLine(endLine, []));
    }

    let indentLevels: number[] = [];
    const indents: number[] = [];

    for (const line of parsedLines) {
      const lastIndent: number = indentLevels[indentLevels.length - 1] ?? 0;

      switch (line.tokens[0]?.type.id ?? -1) {
        case Globals:
        case Breed:
        case Own:
        case Extensions:
        case Includes:
        case To:
        case End:
          indents.push(0);

          break;

        case CloseBracket:
          if (line.bracketDelta == 0) {
            indents.push(Math.max(lastIndent - 2, 0));
          } else if (line.bracketDelta < 0) {
            if (indentLevels.length > line.bracketsClosed) {
              indents.push(Math.max(indentLevels[indentLevels.length - line.bracketsClosed - 1] ?? 0, 0));
            } else {
              indents.push((indentLevels[0] ?? 2) - 2);
            }
          } else {
            indents.push(lastIndent);
          }

          break;

        case OpenBracket:
          if (line.leading > lastIndent) {
            const command: boolean = parsedLines[parsedLines.length - 1]?.tokens[0]?.type.id == Command;
            const delta: boolean = parsedLines[parsedLines.length - 1]?.bracketDelta == 0;

            if (command && delta) {
              const newLeading = line.leading + (indents[indents.length - 1] ?? 0);

              if (newLeading % 2 == 1) {
                indents.push(newLeading + 1);
              } else {
                indents.push(newLeading);
              }
            } else {
              indents.push(lastIndent);
            }
          } else {
            indents.push(lastIndent);
          }

          break;

        default:
          indents.push(lastIndent);
      }

      switch (line.tokens[0]?.type.id) {
        case To:
          indentLevels = [ 2 ];

          break;

        case Globals:
        case Breed:
        case Own:
        case Extensions:
        case Includes:
        case End:
          if (line.bracketDelta == 0) {
            indentLevels = [];
          } else if (line.bracketDelta > 0) {
            for (let i = 0; i < line.bracketDelta; i++) {
              indentLevels.push(lastIndent + 2);
            }
          } else if (line.bracketDelta < 0) {
            for (let i = 0; i > line.bracketDelta; i--) {
              indentLevels.pop();
            }
          }

          break;

        case OpenBracket:
          if (line.bracketDelta > 0) {
            let indent = lastIndent;

            if (line.leading > lastIndent) {
              const command: boolean = parsedLines[parsedLines.length - 1]?.tokens[0]?.type.id == Command;
              const delta: boolean = parsedLines[parsedLines.length - 1]?.bracketDelta == 0;

              if (command && delta) {
                const newLeading = line.leading + (indents[indents.length - 1] ?? 0);

                if (newLeading % 2 == 1) {
                  indent = newLeading + 1;
                } else {
                  indent = newLeading;
                }
              }
            }

            for (let i = 0; i < line.bracketDelta; i++) {
              indentLevels.push(indent + 2);
            }
          } else if (line.bracketDelta > 0) {
            for (let i = 0; i < line.bracketDelta; i++) {
              indentLevels.push(lastIndent + 2);
            }
          } else if (line.bracketDelta < 0) {
            for (let i = 0; i > line.bracketDelta; i--) {
              indentLevels.pop();
            }
          }

          break;

        default:
          if (line.bracketDelta > 0) {
            for (let i = 0; i < line.bracketDelta; i++) {
              indentLevels.push(lastIndent + 2);
            }
          } else if (line.bracketDelta < 0) {
            for (let i = 0; i > line.bracketDelta; i--) {
              indentLevels.pop();
            }
          }
      }
    }

    const shifts: ChangeSpec[] = [];

    for (let i = 0; i < parsedLines.length; i++) {
      if ((parsedLines[i]?.line.number ?? 0) >= startLine.number) {
        const leading: number = parsedLines[i]?.leading ?? 0;
        const indent: number = indents[i] ?? 0;

        if (leading != indent) {
          const start: number = parsedLines[i]?.line.from ?? 0;

          shifts.push({
            from: start,
            to: start + leading,
            insert: " ".repeat(indent)
          });

          if (parsedLines[i]?.line.number == caretLine.number) {
            caretShift = indent - leading;
          }
        }
      }
    }

    return shifts;
  });

  return new IndentUpdate(changes, caretShift);
}

export function executeIndentations(view: EditorView) {
  const update: IndentUpdate = getUpdate(view);

  if (update.caretShift == 0) {
    view.dispatch({
      changes: update.changes
    });
  } else {
    const newHead: number = view.state.selection.main.head + update.caretShift;

    view.dispatch({
      changes: update.changes,
      selection: {
        anchor: newHead,
        head: newHead
      }
    });
  }

  window.setHighlight(view.state.selection.main.empty);
};
