/**
 * Copyright © 2015, University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Washington nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UNIVERSITY OF WASHINGTON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * --------------------------------------------------------------------------------
 *
 * The source code in this codebase is inspired, and in some cases
 * directly re-implemented from, the Snappy Java project at
 * https://github.com/xerial/snappy-java.  The LICENSE for that work is
 * included [here] (./LICENSE.snappy-java)
 */
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
