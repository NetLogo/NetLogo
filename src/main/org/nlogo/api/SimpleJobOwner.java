package org.nlogo.api ;

import org.nlogo.util.MersenneTwisterFast;

public strictfp class SimpleJobOwner
        implements JobOwner
{
    private final String name ;
    private final MersenneTwisterFast random ;
    private final Class<?> agentClass ;

    public SimpleJobOwner( String name, MersenneTwisterFast random, Class<?> agentClass)
    {
        this.name = name ;
        this.random = random ;
        this.agentClass = agentClass ;
    }

    public String displayName() { return name ; }
    public boolean isButton() { return false ; }
    public boolean isTurtleForeverButton() { return false ; }
    public boolean isLinkForeverButton() { return false ; }
    public boolean ownsPrimaryJobs() { return false ; }
    public boolean isCommandCenter() { return false ; }

    public String classDisplayName() { return agentClass.getSimpleName() ; }
    public Class<?> agentClass() { return agentClass ; }
    public String headerSource() { return "" ; }
    public String innerSource() { return "" ; }
    public String source() { return "" ; }
    public void innerSource( String s ) { }

    public MersenneTwisterFast random() { return random ; }
}
