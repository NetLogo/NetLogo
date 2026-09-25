import { ContextTracker, type Stack } from "@lezer/lr";

import {
  As, Globals, Export, Extensions, From, Import, Includes, Breed, To, End, Own, Command, Reporter, Constant,
  CloseBracket
} from "./netlogo.terms.js";

enum Context {
  Top,
  Declaration,
  Procedure
}

export function keywords(name: string, stack: Stack): number {
  const nameLower: string = name.toLowerCase();

  switch (nameLower) {
    case "import": return Import;
    case "export": return Export;
    case "from": return From;
    case "as": return As;
    case "globals": return Globals;
    case "extensions": return Extensions;
    case "__includes": return Includes;
    case "breed": return stack.context == Context.Top ? Breed : Reporter;
    case "directed-link-breed":
    case "undirected-link-breed": return Breed;
    case "to":
    case "to-report": return To;
    case "end": return End;
    default:
      const match = window.program.match(nameLower);

      switch (match?.type) {
        case "keyword": return nameLower.endsWith("-own") ? Own : -1;
        case "constant": return Constant;
        case "variable":
        case "reporter": return (stack.context == Context.Declaration && !match.core) ? -1 : Reporter;
        case "command": return Command;
        default: return -1;
      }
  }
};

export const tracker = new ContextTracker<Context>({
  start: Context.Top,
  shift(context: Context, term: number, _, __): number {
    switch (term) {
      case Import:
      case Export:
      case Globals:
      case Extensions:
      case Includes:
      case Breed:
      case Own: return Context.Declaration;
      case CloseBracket: return context == Context.Declaration ? Context.Top : context;
      case To: return Context.Procedure;
      case End: return Context.Top;
      default: return context;
    }
  }
});
