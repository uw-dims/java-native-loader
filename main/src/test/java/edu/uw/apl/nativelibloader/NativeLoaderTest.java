package edu.uw.apl.nativelibloader;

import java.io.IOException;

public class NativeLoaderTest extends junit.framework.TestCase {

	/**
	   The associated resource (under src/test/resources) is NOT a
	   'real' .so file, just some canned content.  But at least the
	   file is located on the 'classpath', exported to disk and then
	   loaded.  It's OK that the VM will complain bitterly about crazy
	   .so content.  The test is 'can the resource be loaded?'
	*/
	public void testBogusButPresent() {
		try {
			NativeLoader.load( NativeLoader.class, "artifact" );
			fail();
		} catch( UnsatisfiedLinkError ule ) {
		} catch( Throwable t ) {
			fail( "" + t );
		}
	}

	public void testMissing() {
		String thisPackageIsNotHere = "hello";
		String thisArtifactIsNotHere = "world";
		try {
			NativeLoader.load( thisPackageIsNotHere, thisArtifactIsNotHere );
			fail();
		} catch( IllegalStateException iae ) {
			// Expected
		} catch( IOException ioe ) {
			fail( "" + ioe );
		}
	}
}

// eof
