// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import
  org.objectweb.asm, asm.Opcodes.ASM5

class EmptyClassVisitor extends asm.ClassVisitor(ASM5)
class EmptyFieldVisitor extends asm.FieldVisitor(ASM5)
class EmptyMethodVisitor extends asm.MethodVisitor(ASM5)
