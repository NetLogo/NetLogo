import { ContextTracker, type Stack } from "@lezer/lr";

import {
  As, Globals, Export, Extensions, From, Import, Includes, Breed, To, ToReport, End, Own, Command, Reporter, Constant,
  Var
} from "./netlogo.terms.js";

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
    case "breed": return stack.context ? Reporter : Breed;
    case "directed-link-breed":
    case "undirected-link-breed": return Breed;
    case "to": return To;
    case "to-report": return ToReport;
    case "end": return End;
    default:
      switch (window.program.match(nameLower)?.type) {
        case "keyword": return nameLower.endsWith("-own") ? Own : -1;
        case "constant": return Constant;
        case "global":
        case "variable": return Var;
        case "reporter": return Reporter;
        case "command": return Command;
        default: return -1;
      }
  }
};

export const tracker = new ContextTracker<boolean>({
  start: false,
  shift(context: boolean, term: number, _, __): boolean {
    switch (term) {
      case To: return true;
      case End: return false;
      default: return context;
    }
  }
});
