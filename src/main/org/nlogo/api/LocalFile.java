package org.nlogo.api ;

// we shouldn't need "File." lampsvn.epfl.ch/trac/scala/ticket/1409 - ST 3/6/11

public strictfp class LocalFile extends File
{

	private final String filepath ;
	private String suffix = null;
	private File.Mode mode = File.Mode.NONE;
	private java.io.PrintWriter w = null;
	private java.io.BufferedReader buffReader;
	
	public LocalFile(String filepath)
	{
		this.filepath = filepath;
	}

	public LocalFile(String filepath, String suffix)
	{
		if(filepath != null && suffix != null)
		{
			String tmpf = filepath.toLowerCase();
			String tmps = suffix.toLowerCase();
			if(tmpf.endsWith(tmps))
			{
				this.filepath = filepath.substring(0, tmpf.lastIndexOf(tmps));
			}
			else
			{
				this.filepath = filepath;
			}
		}
		else
		{
			this.filepath = filepath;
		}
		this.suffix = suffix;
	}

	@Override
	public File.Mode getMode()
	{
		return mode ;
	}


	@Override
	public java.io.PrintWriter getPrintWriter()
	{
		return w ;
	}
	
	@Override
	public java.io.BufferedReader getBufferedReader()
	{
		return buffReader;
	}

	@Override
	public java.io.InputStream getInputStream()
		throws java.io.IOException
	{
		return new java.io.FileInputStream( getPath() ) ;
	}

	@Override
	public void open(File.Mode mode)
		throws java.io.IOException
	{
		if(w != null || buffReader != null )
		{
			throw new java.io.IOException("Attempted to open an already open file");
		}
		// Comment out this line and swich lines below to enable renaming code
		String fullpath = suffix == null ? filepath : filepath + suffix;
		switch(mode)
		{
			case READ:
				pos = 0 ;
				eof = false ;
				buffReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(
						new java.io.BufferedInputStream(
							new java.io.FileInputStream(new java.io.File(fullpath)))));
				this.mode = mode;
				break;
			case WRITE:
				w = new java.io.PrintWriter(new java.io.FileWriter(fullpath));
				//w = new org.nlogo.util.PrintWriter(new java.io.FileWriter(filepath + TMPSUFFIX));
				this.mode = mode;
				break;
			case APPEND:
				w = new java.io.PrintWriter(new java.io.FileWriter(fullpath, true));
				//w = new org.nlogo.util.PrintWriter(new java.io.FileWriter(filepath + TMPSUFFIX, true));
				this.mode = mode;
				break;
			default:
				break;
		}
	}

	@Override
	public void print(String str)
		throws java.io.IOException
	{
		if( w == null )
		{
			throw new java.io.IOException( "Attempted to print to an unopened File" ) ;
		}
		w.print( str ) ;
	}
	
	@Override
	public void println(String line)
		throws java.io.IOException
	{
		if( w == null )
		{
			throw new java.io.IOException( "Attempted to println to an unopened File" ) ;
		}
		w.println( line ) ;
	}
	
	@Override
	public void println()
		throws java.io.IOException
	{
		if( w == null )
		{
			throw new java.io.IOException( "Attempted to println to an unopened File" ) ;
		}
		w.println() ;
	}

	@Override
	public void flush()
	{
		if(w != null )
		{
			w.flush() ;
		}
	}
	
	@Override
	public void close(boolean ok)
		throws java.io.IOException
	{
		if(w == null && buffReader == null)
		{
			return ;  // not an error
		}
		switch( mode )
		{
			case WRITE:
			case APPEND:
				w.close() ;
				w = null ;
				break ;
			case READ:
				buffReader.close() ;
				buffReader = null ;
				break ;
			default:
				break ;
		}
		mode = File.Mode.NONE;

	}
	
	@Override
	public String getAbsolutePath()
	{
		return suffix == null
			? new java.io.File( filepath ).getAbsolutePath()
			: new java.io.File( filepath + suffix ).getAbsolutePath() ;
	}

	@Override
	public String getPath()
	{
		return suffix == null
			? new java.io.File( filepath ).getPath()
			: new java.io.File( filepath + suffix ).getPath() ;
	}
	
    // Read in a file
	public static String readFile(java.io.File file)
		throws java.io.IOException
	{
		StringBuilder buffer = new StringBuilder() ;
		java.io.BufferedReader in = new java.io.BufferedReader(
			new java.io.InputStreamReader(
				new java.io.BufferedInputStream (
					new java.io.FileInputStream(file))));
		while( true )
		{
			String line = in.readLine();
			if( line == null )
			{
				break ;
			}
			buffer.append(line + "\n") ;
		}
		return buffer.toString() ;
	}

	
}
