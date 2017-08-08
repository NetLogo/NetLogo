// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.nlogo.core.{ model, Model },
    model.{ Element, ElementFactory }

import
  org.nlogo.api.{ ComponentSerialization, PreviewCommands }

import
  scala.util.{ Failure, Success, Try }

class NLogoXPreviewCommandsFormat(factory: ElementFactory)
  extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] {

  def componentName: String = "org.nlogo.modelsection.previewcommands"

  def validationErrors(m: Model) = None

  override def deserialize(s: NLogoXFormat.Section): Model => Try[Model] = { (m: Model) =>
    s.children.collect { case e: Element => e }.headOption.map { previewElem =>
      PreviewCommandsXml.read(previewElem) match {
        case Valid(pc) =>
          Success(m.withOptionalSection[PreviewCommands](componentName, Some(pc), PreviewCommands.Default))
        case Invalid(err) =>
          Failure(new NLogoXFormatException(err.message))
      }
    }.getOrElse(Success(m.withOptionalSection[PreviewCommands](componentName, None, PreviewCommands.Default)))
  }

  def serialize(m: Model): NLogoXFormat.Section =
    factory.newElement("previewCommands")
      .withElement(
        PreviewCommandsXml.write(
          m.optionalSectionValue(componentName).getOrElse(PreviewCommands.Default), factory))
      .build
}
