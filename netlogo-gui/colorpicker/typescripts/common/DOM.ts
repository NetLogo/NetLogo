import { unsafe } from "./Util.js"

import type { El, InputEl } from "./Types.js"

const findElemByID = <T extends El>(doc: Document) => (id: string): T => {
  return unsafe(doc.getElementById(id)) as T
}

const findElems = <T extends El>(container: Document | Element) => (selector: string): Array<T> => {
  return Array.from(container.querySelectorAll(selector)) as Array<T>
}

const findFirstElem = <T extends El>(container: Document | Element) => (selector: string): T => {
  return findElems(container)(selector)[0] as T
}

const setInput = (elem: InputEl) => (value: { toString: () => string }): void => {
  elem.value = value.toString()
}

const setInputByID = (doc: Document) => (id: string, value: { toString: () => string }): void => {
  setInput(findElemByID<InputEl>(doc)(id))(value)
}

export { findElemByID, findElems, findFirstElem, setInput, setInputByID }
