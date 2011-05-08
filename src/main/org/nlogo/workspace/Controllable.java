package org.nlogo.workspace;

import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;

/**
 * Interface provides access to the NetLogo controlling API's report and 
 * command methods found in methods independent of App.app and
 * headless.HeadlessWorkspace.  This is useful for making java software that
 * can run NetLogo in both GUI and Headless mode.
 **/

public interface Controllable 
{
	 void command(String source) throws LogoException, CompilerException;
	 Object report(String source) throws LogoException, CompilerException;
	 void open( String path ) throws LogoException, CompilerException, java.io.IOException;
}
