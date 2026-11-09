import { ChangeDesc, type Extension, StateEffect, StateField, Transaction } from "@codemirror/state";
import { Decoration, type DecorationSet, EditorView } from "@codemirror/view";

interface Range {
  from: number;
  to: number;
}

function underline() {
  const path: string = '<path d="m0 2.5 l2 -2.5 l1 0 l2 2.5 l1 0" stroke="red" fill="none" stroke-width="1"/>';
  const svg: string = `<svg xmlns="http://www.w3.org/2000/svg" width="6" height="5">${encodeURIComponent(path)}</svg>`;

  return `url('data:image/svg+xml,${svg}')`;
}

const underlineMark = Decoration.mark({
  class: "nl-underline"
});

export const underlineEffect = StateEffect.define<Range>({
  map(range: Range, change: ChangeDesc): Range {
    return {
      from: change.mapPos(range.from),
      to: change.mapPos(range.to)
    }
  }
});

const underlineField = StateField.define<DecorationSet>({
  create() {
    return Decoration.none;
  },

  update(underlines, transaction: Transaction) {
    if (transaction.isUserEvent("clear-lint")) {
      return Decoration.none;
    }

    underlines = underlines.map(transaction.changes);

    for (const effect of transaction.effects) if (effect.is(underlineEffect)) {
      underlines = underlines.update({
        add: [
          underlineMark.range(effect.value.from, effect.value.to)
        ]
      });
    }

    return underlines;
  },

  provide(decorations): Extension {
    return EditorView.decorations.from(decorations);
  }
});

export function lint(): Extension {
  return [
    underlineField,
    EditorView.theme({
      ".nl-underline": {
        backgroundImage: underline(),
        backgroundPosition: "left bottom",
        backgroundRepeat: "repeat-x",
        paddingBottom: "1.5px"
      }
    })
  ];
}
