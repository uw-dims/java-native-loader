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
package greetings;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * @author Stuart Maclean
 *
 * Unit testing the correctness of loading and using the Hello Java/C
 * class, while influencing the NativeLoader.
 *
 * @see Hello
 */

public class HelloTest {

	/*
	  We have to ignore this test (i.e. not run it) else it will bring
	  the native library into the VM, and then the 'disabled' test,
	  which expects the library to be unavailable, will fail!
	*/
	@Ignore
	public void test1() {
		Hello.world();
	}

	@Test
	public void disabled() {
		/*
		  By defining this system property, we can force the
		  NativeLoader to not load the native library, thus causing a
		  ULE.  This is done so we can see how such a split Java/C
		  codebase would perform on a platform when the C library is
		  not available in the jar.

		  Cannot refer to 'Hello.prefix' since that will cause a load
		  of the Hello class BEFORE we can set our sys property!
		*/
		System.setProperty( "greetings.Hello.disabled",	"true" );
		try {
			Hello.world();
			fail();
		} catch( UnsatisfiedLinkError ule ) {
		}
	}
}

// eof
