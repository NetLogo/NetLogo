// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

// WHY CUSTOM ASSEMBLY?
//
// A primitive command that is custom assembled uses the methods in this interface to direct their
// own assembly.
//
// Assembly consists of taking the parse tree for a procedure, which may contain commands anywhere
// in the tree, and flattening it out into a linear array of commands.  Each resulting command has a
// tree of reporters inside it, but no other commands inside it.
//
// Here are the reasons some commands are custom assembled.  This is a complete list (as of 2/22/08
// anyway):
//
// - Any command which includes a command block must be custom assembled.  Examples: _ask, _ifelse.
//
// - A command may wish to drop out of existence entirely when assembled.  Example: _observercode
// and friends, which exist only to assist the AgentTypeChecker phase of compilation and are no longer
// needed after AgentTypeChecker has run.
//
// - A command may wish to cause other commands to be inserted, either instead of or in addition to
// the original.  For example _hatch replaces itself with _fasthatch if the optional command block
// is omitted.  _fd turns into _fd followed by _fdinternal.  And so on.
//
// - Any command that takes a Reference argument is custom assembled.  (This wouldn't be necessary
// if the compiler were a little smarter.)
//
// HOW CUSTOM ASSEMBLY WORKS
//
// At assembly time, the command's CustomAssembled.assemble() with an object implementing the
// following interface as an argument.  The command calls the following methods in order to direct
// its assembly.
//
// Typically an assemble() method begins with:
//    a.add( this ) ;
// which simply appends the command to the output, as would normally happen to any command if it
// weren't custom assembled.
//
// Calling block() inserts the contents of a command block.  You can supply an argument number, or
// if the number is omitted the last argument is used.
//
// Calling done() inserts a _done command.  Typically this comes after a block, for example in _ask.
// For example, _ask uses _done so that each new context created gets killed off at the end of the
// block.  a command like _if doesn't use _done because there is no new context to kill off.
//
// Reporter arguments can be accessed and manipulated via argCount(), arg(), and removeArg().
//
// Calling goTo() adds a _goto command.  The target of the command is the location where comeFrom()
// is called.  (Intercal fans will enjoy the name of the latter method.)  At present only one goto
// is permitted; if we ever write a prim needing multiple gotos, these methods would have to take
// label names.
//
// Most commands don't use goTo() because the compiler allows commands to include an implicit goto.
// The location jumped to is the "branch target"; the "offset" is the distance from the original
// command to the target.  Calling resume() tells the assembler, "the branch target is here."  The
// less frequently used offset() method is for command that need to do their own arithmetic with the
// offset (examples: _foreach, _repeat).
//
// This set of methods arose from the needs of our current set of primitives.  It's easy to imagine
// that future primitives may require additional methods to be added.
//
//  - ST 2/22/08

trait AssemblerAssistant {
  def add(cmd: Command)
  def block()
  def block(pos: Int)
  def done()
  def argCount: Int
  def arg(i: Int): Reporter
  def removeArg(i: Int)
  def goTo()
  def comeFrom()
  def resume()
  def offset(): Int
}
