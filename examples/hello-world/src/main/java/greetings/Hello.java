package greetings;

import edu.uw.apl.nativelibloader.NativeLoader;

public class Hello {

	static public native void world();

	static final String prefix = Hello.class.getPackage().getName();
	static final String libName = "Hello";
	
	static {
		try {
			NativeLoader.load( prefix, libName );
		} catch( java.io.IOException ioe ) {
			throw new ExceptionInInitializerError( ioe );
		}
	}
}

// eof

