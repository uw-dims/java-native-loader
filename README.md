# Java Native Loader

A Maven-based framework for managing the native (JNI) parts of Java
codebases.  The idea is to include any .dll/.so files in a jar, and to
load any such library from that jar.  Generalising the approach taken
by [Snappy-Java] (https://github.com/xerial/snappy-java), the compiled
library for all available platforms is bundled into a single jar.
This gives the illusion of platform independence, pure Java code and
thus of a regular Maven build/test/deploy cycle.

## Installation Requirements

The primary artifact for native library loading is under the main directory:

```
$ cd java-native-loader/main

$ mvn install
```

A sample Java codebase with JNI parts is at examples/hello-world:

```
$ cd java-native-loader/examples/hello-world

$ mvn test
```

The examples/hello-world sample shows how to organize the management of JNI
resources in this framework:

* The [POM] (./examples/hello-world/pom.xml) shows how we use a Maven
  profile (called native) to drive the C compilation and linking
  steps.  A build without that profile activated results in a regular
  Java-only Maven build.

* A platform-dependent [Makefile]
  (examples/hello-world/src/main/native/Linux/x86_64/Makefile) shows
  how the linked library (in this case an .so file) is placed under
  src/main/resources for inclusion in the packaged Maven artifact.

* The sample [Hello.java]
  (examples/hello-world/src/main/java/greetings/Hello.java) shows use
  of the NativeLoader api located in the main artifact (see above).

The C code for the hello-world sample is simple enough that no
platform-specific parts are necessary, so we place all the C code in
[src/main/native] (examples/hello-world/src/main/native).  More
complicated C code may require platform and even bit-ness
sub-components.

We have compiled the JNI parts of the hello-world sample for Linux
64-bit and 32-bit only (the .so files end up as
[resources](./examples/hello-world/src/main/resources/greetings/native/Linux/).
Nevertheless, the Maven pom files, Makefiles and directory structure
promote a many-platform build (Windows, MacOS, etc).  The idea is to create
platform and bitness (32/64) specific build scripts (e.g. Makefiles
for Linux/Mac OS, .proj/NMAKE files for Windows), and build C sources
from those.  The resulting .so/.dll files are then put under version
control, so need building only once per platform.  We treat the
.so/.dll products essentially as source code components, with a Maven
profile-driven build of those platform-specific parts.  The net effect
is that the C parts of a split Java/C build become almost as easy to
manage as regular Java classes.

# Video/Slides

The ideas behind this work were presented at the Seattle Java User
Group meeting of 19 May 2015.  Slides and video available on the
[Seajug web site] (http://www.seajug.org).  A local copy of the slides
is also included [here](./doc/seajug-maven-jni.pdf).