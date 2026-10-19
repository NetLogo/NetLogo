import { findElemByID, findElems, findFirstElem, setInputByID } from "../common/DOM.js"
import { unsafe                                               } from "../common/Util.js"

import { optionValueToContainerID } from "./Util.js"

import type { El, InputEl, Str } from "../common/Types.js"

export class DOMManager {

  public findElemByID:  <T extends El>(id:       Str)                                 => T
  public findElems:     <T extends El>(selector: Str)                                 => Array<T>
  public findFirstElem: <T extends El>(selector: Str)                                 => T
  public setInputByID:                (id: string, value: { toString: () => string }) => void

  constructor(doc: Document) {

    const pane = findElemByID(doc)("advanced-pane")

    this.findElemByID  = findElemByID (doc)
    this.findElems     = findElems    (pane)
    this.findFirstElem = findFirstElem(pane)
    this.setInputByID  = setInputByID (doc)

  }

  findActiveControls(): El {
    const controls = this.findElems(".repr-controls-container .repr-controls")
    const active   = controls.find((c: El) => c.style.display === "flex")
    return unsafe(active)
  }

  findInputValues(): Array<string> {
    const dropdown     = this.findReprDropdown()
    const targetElemID = unsafe(optionValueToContainerID[dropdown.value])
    const container    = this.findElemByID(targetElemID)
    return findElems<InputEl>(container)(".repr-input").map((i) => i.value)
  }

  findOutputDropdown(): InputEl {
    return this.findElemByID<InputEl>("output-format-dropdown")
  }

  findReprDropdown(): InputEl {
    return this.findElemByID<InputEl>("repr-dropdown")
  }

}
