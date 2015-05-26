#include <stdio.h>

#include "greetings_Hello.h"

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


