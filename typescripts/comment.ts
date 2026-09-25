import { EditorView } from "@codemirror/view";
import { Line, SelectionRange, Text } from "@codemirror/state";

export function toggleComments(view: EditorView) {
  const doc: Text = view.state.doc;

  const lines: number[] = view.state.selection.ranges.flatMap((range: SelectionRange) => {
    const startLine: number = doc.lineAt(range.from).number;
    const endLine: number = doc.lineAt(range.to).number;

    const lines: number[] = [];

    for (let i = startLine; i <= endLine; i++) {
      lines.push(i);
    }

    return lines;
  });

  const nonEmptyLines: string[] = [];

  for (const i of lines) {
    const line: string = doc.line(i).text;

    if (line.trim().length > 0) {
      nonEmptyLines.push(line);
    }
  }

  if (nonEmptyLines.length == 0) {
    view.dispatch({
      changes: lines.map((i: number) => {
        const offset: number = doc.line(i).from;

        return {
          from: offset,
          to: offset,
          insert: "; "
        };
      })
    });
  } else if (nonEmptyLines.every((line: string) => line.trimStart().startsWith(";"))) {
    view.dispatch({
      changes: lines.map((i: number) => {
        const line: Line = doc.line(i);

        const text: string = line.text;
        const index: number = text.indexOf(";");

        if (index == -1) {
          return [];
        }

        const offset: number = line.from + index;

        if (text.length > index + 1 && /\s/.test(text[index + 1] ?? "")) {
          return {
            from: offset,
            to: offset + 2,
            insert: ""
          };
        }

        return {
          from: offset,
          to: offset + 1,
          insert: ""
        };
      })
    });
  } else {
    const offset: number = Math.min(...nonEmptyLines.map((line: string) => line.search(/\S/)));

    view.dispatch({
      changes: lines.map((i: number) => {
        const line: Line = doc.line(i);

        if (line.text.trim().length == 0) {
          return [];
        }

        const start: number = line.from + offset;

        return {
          from: start,
          to: start,
          insert: "; "
        };
      })
    });
  }
}
