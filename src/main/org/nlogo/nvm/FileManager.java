package org.nlogo.nvm ;

import org.nlogo.api.CompilerException ;
import org.nlogo.api.File ;
import org.nlogo.agent.World ;

public interface FileManager
{
	String getPrefix() ;

	// Prefix is 
	String attachPrefix( String filename )
		throws java.net.MalformedURLException ;
	void setPrefix( String newPrefix ) ;
	void setPrefix( java.net.URL newPrefix ) ;

	boolean eof()
		throws java.io.IOException ;
	boolean hasCurrentFile() ;
	void closeCurrentFile()
		throws java.io.IOException ;
	void flushCurrentFile()
		throws java.io.IOException ;
	void deleteFile( String filename )
		throws java.io.IOException ;
	void closeAllFiles()
		throws java.io.IOException ;
	boolean fileExists( String filePath ) 
		throws java.io.IOException ;
	void openFile( String newFileName ) 
		throws java.io.IOException ;
	File getFile( String newFileName ) ;
	void ensureMode( File.Mode openMode )
		throws java.io.IOException ;
	String getErrorInfo()
		throws java.io.IOException ;
	Object read( World world )
		throws java.io.IOException , CompilerException ;
	String readLine()
		throws java.io.IOException ;
	String readChars( int num )
		throws java.io.IOException ;
}
