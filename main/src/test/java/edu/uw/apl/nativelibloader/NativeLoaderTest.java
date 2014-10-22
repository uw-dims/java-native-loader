package edu.uw.apl.nativelibloader;

public class NativeLoaderTest extends junit.framework.TestCase {

	/**
	   The associated resource (under src/test/resources) is NOT a
	   'real' .so file, just some canned content.  But at least the
	   file is located on the 'classpath', exported to disk and then
	   loaded.  It's OK that the VM will complain bitterly about
	   crazy .so content ;)
	*/
	public void testBogus() {
		try {
			NativeLoader.load( NativeLoader.class, "artifact", "1.0.0" );
		} catch( Throwable t ) {
			fail( "" + t );
		}
	}
}

// eof
