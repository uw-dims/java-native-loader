# Java Native Loader

A Maven-based framework for managing the native (JNI) parts of Java
codebases.  The idea is to include any .dll/.dylib/.so files in a jar
as resources, and to load these via resource loading from the
classpath.  Generalising the approach taken by [Snappy-Java]
(https://github.com/xerial/snappy-java), the compiled library for all
available platforms is bundled into a single jar.  This gives the
illusion of platform independence, pure Java code and thus of a
regular Maven build/test/install cycle and seamless dependency
management of JNI-contaminated artifacts.

## Installation 

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

## Framework Use

* The [Device Files] (https://github.com/uw-dims/device-files)
  project uses the framework described here.  It is a much better
  example than the hello-world example above since requires different
  C sources and build procedures for different platforms.

## Framework Description

The examples/hello-world sample shows how to organize the management of JNI
resources in this framework:

* The [POM] (./examples/hello-world/pom.xml) shows how we use a Maven
  profile (called native) to drive the C compilation and linking
  steps.  A build without that profile activated results in a regular
  Java-only Maven build.  The POM also uses a set of profiles to
  'canonicalize' the local architecture string (result of os.arch) to
  match that assumed by the main [NativeLoader]
  (./main/src/main/java/edu/uw/apl/nativelibloader/NativeLoader.java)
  class.

* A platform-dependent [Makefile]
  (examples/hello-world/src/main/native/Linux/x86_64/Makefile) shows
  how the linked library (in this case an .so file) is transferred to
  src/main/resources for inclusion in the packaged Maven artifact.

* The sample [Hello.java]
  (examples/hello-world/src/main/java/greetings/Hello.java) shows use
  of the NativeLoader api located in the main artifact (see above).

The C code for the hello-world sample is simple enough that no
platform-specific parts are necessary, so we place all the C code in
[src/main/native] (examples/hello-world/src/main/native).  More
complicated C code may require platform and even bit-ness (32 vs 64)
sub-components.

We have compiled the JNI parts of the hello-world sample for Linux
only (both 64-bit and 32-bit variants).  The .so files end up as
[resources](./examples/hello-world/src/main/resources/greetings/native/Linux/).
Nevertheless, the Maven pom file, Makefiles and directory structure
promote a many-platform build (Windows, MacOS, etc).  The idea is to
create platform and bitness (32/64) specific build scripts
(e.g. Makefiles for Linux/Mac OS, .proj/NMAKE files for Windows), and
build C sources from those.  The resulting .so/.dylib/.dll files, once
transferred to src/main/resources, are then put under version control,
so need building only once per platform.  We treat the .so/.dylib/.dll
products essentially as source code components, with a Maven
profile-driven build of those platform-specific parts.  The net effect
is that the C parts of a split Java/C build become almost as easy to
manage as regular Java classes.

# Video/Slides

The ideas behind this work were presented at the Seattle Java User
Group meeting of 19 May 2015.  Slides and video available on the
[Seajug web site] (http://www.seajug.org).  A local copy of the slides
is also included [here](./doc/seajug-maven-jni.pdf).
