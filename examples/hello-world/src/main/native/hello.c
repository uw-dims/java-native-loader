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
#include <stdio.h>

#include "greetings_Hello.h"

/**
 * @author Stuart Maclean
 *
 * Some trivial C code showing the native parts of the greetings.Hello
 * Java class.  
 *
 * Uses only 'standard C' constructs, so lives in src/main/native.  A
 * more platform-specfic example might contain different C sources for
 * e.g. Linux and Windows, and even for 32 vs 64 bit builds.  In those
 * cases, place C sources in e.g. src/main/native/Linux/x86/code.c
 */

/*
 * Class:     greetings_Hello
 * Method:    world
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_greetings_Hello_world
(JNIEnv *env, jclass clazz) {
  printf( "Hello world\n" );
}

// eof


