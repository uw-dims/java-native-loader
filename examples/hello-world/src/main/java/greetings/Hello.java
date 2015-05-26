package greetings;

import edu.uw.apl.nativelibloader.NativeLoader;

public class Hello {

	static public native void world();

	static {
		/**
		 * Look: use Maven 'groupId' and 'artifactId' in place of magic
		 * literals ?
		 */
		try {
			NativeLoader.load( "greetings", "hello-world" );
		} catch( java.io.IOException ioe ) {
			throw new ExceptionInInitializerError( ioe );
		}
		
	}
	
}

// eof

