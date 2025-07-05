// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Container, Dimension, LayoutManager2 }

// a row-based dialog layout that allows rows to have fixed height or expand to fill the available space,
// adjusting dynamically based on the state of the components in each row. this is an improvement over
// GridBagLayout, which requires the fixed vs stretch properties to be static and predetermined. (Isaac B 7/4/25)
class DynamicRowLayout(target: Container, spacing: Int) extends LayoutManager2 {
  private var rows = Seq[Row]()

  private var existingSize: Option[Dimension] = None
  private var cachedMinimumSize: Option[Dimension] = None

  override def addLayoutComponent(name: String, component: Component): Unit = {
    addLayoutComponent(component, name)
  }

  // this should not be called manually, use addRow instead for simplicity and readability (Isaac B 7/4/25)
  override def addLayoutComponent(component: Component, constraints: AnyRef): Unit = {
    constraints match {
      case row: Integer =>
        rows = rows.updated(row, rows(row).copy(components = rows(row).components :+ component))

        cachedMinimumSize = None

      case _ =>
        throw new IllegalStateException("Can't add component with specified constraints.")
    }
  }

  // wrapper around add and addLayoutComponent to make calling code more readable (Isaac B 7/4/25)
  def addRow(components: Seq[Component], expandX: Boolean = true, expandY: () => Boolean = () => false): Unit = {
    val row = rows.size

    rows :+= Row(Seq(), expandX, expandY)

    components.foreach(component => target.add(component, Int.box(row)))
  }

  override def layoutContainer(target: Container): Unit = {
    target.getTreeLock synchronized {
      val size = target.getSize

      existingSize = Option(size)

      val (fixedHeight, stretch) = rows.foldLeft((0, 0)) {
        case ((height, current), Row(components, _, expandY)) =>
          if (expandY()) {
            (height, current + 1)
          } else {
            (height + components.maxBy(_.getPreferredSize.height).getPreferredSize.height, current)
          }
      }

      val expandHeight = (size.height - (rows.size + 1) * spacing - fixedHeight) / stretch.max(1)
      var y = spacing

      rows.foreach {
        case Row(components, expandX, expandY) =>
          val equalWidth = (size.width - (components.size + 1) * spacing) / components.size
          var x = spacing
          var maxHeight = 0

          if (expandY()) {
            components.foreach { component =>
              if (component.isVisible) {
                val width = {
                  if (expandX) {
                    equalWidth
                  } else {
                    component.getPreferredSize.width
                  }
                }

                component.setBounds(x, y, width, expandHeight)

                x += width + spacing
                maxHeight = expandHeight
              }
            }
          } else {
            components.foreach { component =>
              if (component.isVisible) {
                val width = {
                  if (expandX) {
                    equalWidth
                  } else {
                    component.getPreferredSize.width
                  }
                }

                val height = component.getPreferredSize.height

                component.setBounds(x, y, width, height)

                x += width + spacing
                maxHeight = maxHeight.max(height)
              }
            }
          }

          if (maxHeight > 0)
            y += maxHeight + spacing
      }
    }
  }

  override def minimumLayoutSize(target: Container): Dimension = {
    cachedMinimumSize.getOrElse {
      target.getTreeLock synchronized {
        val (maxWidth, totalHeight) = rows.foldLeft((0, 0)) {
          case ((width, height), Row(components, _, _)) =>
            val (rowWidth, rowHeight) = components.foldLeft((0, 0)) {
              case ((currentWidth, currentHeight), component) =>
                if (component.isVisible) {
                  (currentWidth + component.getPreferredSize.width + spacing,
                  currentHeight.max(component.getPreferredSize.height))
                } else {
                  (currentWidth, currentHeight)
                }
            }

            if (rowHeight == 0) {
              (width, height)
            } else {
              (width.max(rowWidth), height + rowHeight + spacing)
            }
        }

        val size = new Dimension(maxWidth + spacing, totalHeight + spacing)

        cachedMinimumSize = Option(size)

        size
      }
    }
  }

  override def maximumLayoutSize(target: Container): Dimension =
    preferredLayoutSize(target)

  override def preferredLayoutSize(target: Container): Dimension =
    existingSize.getOrElse(minimumLayoutSize(target))

  override def removeLayoutComponent(component: Component): Unit = {}

  override def getLayoutAlignmentX(target: Container): Float =
    0

  override def getLayoutAlignmentY(target: Container): Float =
    0

  override def invalidateLayout(target: Container): Unit = {
    cachedMinimumSize = None
  }

  private case class Row(components: Seq[Component], expandX: Boolean, expandY: () => Boolean)
}
