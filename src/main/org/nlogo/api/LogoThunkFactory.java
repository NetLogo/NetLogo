package org.nlogo.api;

public interface LogoThunkFactory
{
    ReporterLogoThunk makeReporterThunk( String code , String jobOwnerName )
		throws org.nlogo.api.CompilerException ;
    CommandLogoThunk makeCommandThunk( String code , String jobOwnerName )
		throws org.nlogo.api.CompilerException ;
}
