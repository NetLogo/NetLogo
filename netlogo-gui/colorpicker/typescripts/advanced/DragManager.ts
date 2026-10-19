import type { El, Num } from "../common/Types.js"

export class DragManager {

  setupDrag1DX(elem: El, doc: Document, setter: (x: Num) => void): void {

    let mouseDown = false

    const setPosition = (x: number): void => {

      const { left, right, width } = elem.getBoundingClientRect()

      const absX       = x
      const clampedX   = (absX < left) ? left : (absX > right) ? right : absX
      const relX       = clampedX - left
      const percentage = Math.round(relX / width * 100)

      setter(percentage)

    }

    const onMouseDown = (e: MouseEvent): void => {
      if (e.button == 0) {
        mouseDown = true
        setPosition(e.clientX)
        doc.body.classList.add("dragging")
      }
    }

    const onMouseMove = (e: MouseEvent): void => {
      if (mouseDown) {
        setPosition(e.clientX)
      }
    }

    const onMouseUp = (e: MouseEvent): void => {
      if (e.button == 0) {
        mouseDown = false
        doc.body.classList.remove("dragging")
      }
    }

    elem.addEventListener("mousedown" , onMouseDown, false)
    doc .addEventListener("mousemove" , onMouseMove, false)
    doc .addEventListener("mouseup"   , onMouseUp  , false)
    doc .addEventListener("mouseleave", onMouseUp  , false)

  }

  setupDrag1DY(elem: El, doc: Document, setter: (y: Num) => void): void {

    let mouseDown = false

    const setPosition = (y: number): void => {

      const { bottom, top, height } = elem.getBoundingClientRect()

      const absY       = y
      const clampedY   = (absY < top) ? top : (absY > bottom) ? bottom : absY
      const relY       = bottom - clampedY
      const percentage = Math.round(relY / height * 100)

      setter(percentage)

    }

    const onMouseDown = (e: MouseEvent): void => {
      if (e.button == 0) {
        mouseDown = true
        setPosition(e.clientY)
        doc.body.classList.add("dragging")
      }
    }

    const onMouseMove = (e: MouseEvent): void => {
      if (mouseDown) {
        setPosition(e.clientY)
      }
    }

    const onMouseUp = (e: MouseEvent): void => {
      if (e.button == 0) {
        mouseDown = false
        doc.body.classList.remove("dragging")
      }
    }

    elem.addEventListener("mousedown" , onMouseDown, false)
    doc .addEventListener("mousemove" , onMouseMove, false)
    doc .addEventListener("mouseup"   , onMouseUp  , false)
    doc .addEventListener("mouseleave", onMouseUp  , false)

  }

  setupDrag2D(elem: El, doc: Document, setter: (x: Num, y: Num) => void): void {

    let mouseDown = false

    const setPosition = (x: number, y: number): void => {

      const { left, right, width } = elem.getBoundingClientRect()

      const absX        = x
      const clampedX    = (absX < left) ? left : (absX > right) ? right : absX
      const relX        = clampedX - left
      const xPercentage = Math.round(relX / width * 100)

      const { top, bottom, height } = elem.getBoundingClientRect()

      const absY        = y
      const clampedY    = (absY < top) ? top : (absY > bottom) ? bottom : absY
      const relY        = clampedY - top
      const yPercentage = Math.round(relY / height * 100)

      setter(xPercentage, yPercentage)

    }

    const onMouseDown = (e: MouseEvent): void => {
      if (e.button == 0) {
        mouseDown = true
        setPosition(e.clientX, e.clientY)
        doc.body.classList.add("dragging")
      }
    }

    const onMouseMove = (e: MouseEvent): void => {
      if (mouseDown) {
        setPosition(e.clientX, e.clientY)
      }
    }

    const onMouseUp = (e: MouseEvent): void => {
      if (e.button == 0) {
        mouseDown = false
        doc.body.classList.remove("dragging")
      }
    }

    elem.addEventListener("mousedown" , onMouseDown, false)
    doc .addEventListener("mousemove" , onMouseMove, false)
    doc .addEventListener("mouseup"   , onMouseUp  , false)
    doc .addEventListener("mouseleave", onMouseUp  , false)

  }

}
