package org.nlogo.api ;

public interface EditorAreaInterface
{
	int getSelectionStart() ;
	int getSelectionEnd() ;
	void setSelectionStart( int pos ) ;
	void setSelectionEnd( int pos ) ;
	int offsetToLine( int pos ) ;
	int lineToStartOffset( int pos ) ;
	int lineToEndOffset( int pos ) ;
	String getText( int start , int len ) ;
	String getLineOfText( int lineNum ) ;
	void insertString( int pos , String spaces ) ;
	void replaceSelection( String text ) ;
	void remove( int start , int len ) ;
}
