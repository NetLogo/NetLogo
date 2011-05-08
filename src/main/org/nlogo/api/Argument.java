package org.nlogo.api ;

/**
 * Interface provides access to arguments passed to the <code>perform</code> 
 * or <code>report</code> methods of a primitive at run-time.
 * <p>
 * <code>Arguments</code> are created by NetLogo and passed to the 
 * <code>perform</code> or <code>report</code> methods of your 
 * primitive. 
 * @see Command#perform(Argument[], Context)
 * @see Reporter#report(Argument[], Context)
 */
public interface Argument
{	
	/**
	 * Returns the argument as an <code>Object</code> without type checking.
	 * @throws ExtensionException
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	Object get() throws ExtensionException, LogoException;
	
	/**
	 * Returns the argument as an <code>org.nlogo.api.AgentSet</code>.
	 * @throws ExtensionException if the argument is not an <code>AgentSet</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	AgentSet getAgentSet() throws ExtensionException, LogoException;

	/**
	 * Returns the argument as an <code>Agent</code>.
	 * @throws ExtensionException if the argument is not an <code>Agent</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	Agent getAgent() throws ExtensionException, LogoException;

	/**
	 * Returns the argument as a <code>Boolean</code>
	 * @throws ExtensionException if the argument is not a <code>Boolean</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	Boolean getBoolean() throws ExtensionException, LogoException;

	/**
	 * Returns the value of the argument as a boolean
	 * @throws ExtensionException if the argument is not a <code>boolean</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	boolean getBooleanValue() throws ExtensionException, LogoException;

	/**
	 * Returns the value of the argument as an <code>int</code>.
	 * Any fractional part is discarded.
	 * @throws ExtensionException if the argument is not a number.
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	int getIntValue() throws ExtensionException, LogoException;

	/**
	 * Returns the value of the argument as a <code>double</code>.
	 * @throws ExtensionException if the argument is not a number.
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	double getDoubleValue() throws ExtensionException, LogoException;

	/**
	 * Returns the argument as a <code>org.nlogo.api.LogoList</code>
	 * @throws ExtensionException if the argument is not a <code>LogoList</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	org.nlogo.api.LogoList getList() throws ExtensionException, LogoException;

	/**
	 * Returns the argument as an <code>org.nlogo.api.Patch</code>
	 * @throws ExtensionException if the argument is not a <code>Patch</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	Patch getPatch() throws ExtensionException, LogoException;

	/**
	 * Returns the argument as a <code>String</code>
	 * @throws ExtensionException if the argument cannot be cast to a <code>String</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	String getString() throws ExtensionException, LogoException;

	/**
	 * Returns the argument as a <code>org.nlogo.api.Turtle</code>.
	 * @throws ExtensionException if the argument is not a <code>Turtle</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	Turtle getTurtle() throws ExtensionException, LogoException;	

	/**
	 * Returns the argument as a <code>org.nlogo.api.Link</code>.
	 * @throws ExtensionException if the argument is not a <code>Link</code>
	 * @throws LogoException if a LogoException occurred while evaluating this argument
	 */
	Link getLink() throws ExtensionException, LogoException;	

}
