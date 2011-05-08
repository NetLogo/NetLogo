package org.nlogo.api ;

// it's very tempting to get rid of ride entirely but for the interface
// "riding turtle 0" I supposed we still need it. ev 4/29/05
// In the old days this was an integer instead of an Enum, so in exported
// worlds it's still represented as an integer, hence the code here to
// convert back and forth to an integer at import or export time. - ST 3/18/08
public enum Perspective {
	OBSERVE , RIDE , FOLLOW , WATCH ;
	// now for import/export support
	public int export() {
		switch( this ) {
			case OBSERVE: return OBSERVE_INT ;
			case RIDE: return RIDE_INT ;
			case FOLLOW: return FOLLOW_INT ;
			case WATCH: return WATCH_INT ;
			default : throw new IllegalStateException() ;
		}
	}
	public static Perspective load( int perspectiveAsInteger ) {
		switch( perspectiveAsInteger ) {
			case OBSERVE_INT : return OBSERVE ;
			case RIDE_INT : return RIDE ;
			case FOLLOW_INT : return FOLLOW ;
			case WATCH_INT : return WATCH ;
			default : throw new IllegalStateException() ;
		}
	}
	private static final int OBSERVE_INT = 0 ;
	private static final int RIDE_INT 	 = 1 ;
	private static final int FOLLOW_INT	 = 2 ;
	private static final int WATCH_INT   = 4 ;
}
