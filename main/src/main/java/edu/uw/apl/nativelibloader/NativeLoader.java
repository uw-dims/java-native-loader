package edu.uw.apl.nativelibloader;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Stuart Maclean
 */

// This code is derived from Taro Saito's snappy-java project:
// https://github.com/xerial/snappy-java. See NOTICE.txt for details.


/**
 * <b>Internal only - Do not use this class.</b> This class loads a native
 * library of supplied name (NAME.dll, libNAME.so, etc.) according to the
 * user platform (<i>os.name</i> and <i>os.arch</i>). The artifact is
 * expected to be found on the classpath (e.g. bundled into a jar).
 *
 * TO DO: Document why use group,artifact,version
 *
 * TO DO: Document user configuration possibilities
 */
public class NativeLoader {

	/**
	 * A convenience helper for the primary entry point, where the
	 * group name is derived from a class's own package name
	 */
	static public synchronized void load( Class c,
										  String artifact,
										  String version ) {
		String group = c.getPackage().getName();
		load( group, artifact, version );
	}
	
	/*
	 * The primary entry point. Please load me a native library
	 * (presumably some JNI code associated with local Java classes)
	 * which has been packaged into an available jar as e.g.
	 *
	 * com/foo/bar/Linux/x86/libstuff-1.2.3.so
	 *
	 * for some supplied 'group' = com.foo.bar
	 * and some supplied 'artifact' = stuff
	 * and some supplied 'version' =  1.2.3
	 *
	 * These 3 strings would likely be available to the caller as
	 * info from a Maven-driven build.
	 *
	 * @see asResourceName
	 */
	static public synchronized void load( String group,
										  String artifact,
										  String version ) {
		if( isLoaded )
			return;
		try {
			loadNativeLibrary( group, artifact, version );
			isLoaded = true;
		} catch( Throwable t ) {
			log.error( t );
		}
	}

		
	static private synchronized void loadNativeLibrary( String group,
														String artifact,
														String version )
		throws IOException {

		log.debug( "Loading: " + group + " " + artifact + " " + version );
		
		String keyPrefix = group + "." + artifact;
		String prpResourceName = keyPrefix + ".properties";
		Properties p = loadConfiguration( prpResourceName );
		boolean useExternal = Boolean.parseBoolean
			( p.getProperty( keyPrefix + "." + "useExternal", "false" ) );
		if( useExternal ) {
			/*
			  Load external artifact (i.e. one found, hopefully, using
			  -Djava.library.path).  Do NOT proceed to load from a local
			  resource
			*/
			System.loadLibrary( artifact + "-" + version );
			return;
		}
		nativeLibFile = findNativeLibrary( group, artifact, version, p );
		if( nativeLibFile != null ) {
			System.load( nativeLibFile.getPath() );
		}
	}

	/*
	  Load an OS-dependent native library from the classpath
	  (typically from inside a jar file)
	*/
	static private File findNativeLibrary( String group,
										   String artifact,
										   String version,
										   Properties p )
		throws IOException {

		/*
		  We compose the final resourceName in a way similar to the
		  Maven convention for artifact file naming:
		  GROUP/ARTIFACT-VERSION.  This is also similar to how .so
		  files are named, except there the version trails the .so
		  suffix (see e.g. /lib/ on any Unix system).
		*/

		String nativeLibraryName = System.mapLibraryName( artifact +
														  "-" + version );
		String nativeLibraryPath = group.replaceAll( "\\.", "/" ) +
			"/" + OSInfo.getNativeLibFolderPathForCurrentOS();

		/*
		  Ensure the resourceName starts '/' so is not subject to any
		  'package name modification' during Class.getResource(),
		  Class.getResourceAsStream() calls (dots in the version
		  string could get replaced with slash!)
		*/
		String resourceName = "/" +	nativeLibraryPath +
			"/" + nativeLibraryName;
		log.debug( "ResourceName: " + resourceName );

		boolean haveNativeLib = haveResource( resourceName );
        if( !haveNativeLib ) {
            if( OSInfo.getOSName().equals("Mac") ) {
                // Fix for openjdk7 for Mac
                String altLibraryName =
					"lib" + (artifact + "-" + version) + ".jnilib";
				resourceName = "/" + nativeLibraryPath +
					"/" + altLibraryName;
				log.debug( "AltResourceName: " + resourceName );
                if( haveResource( resourceName ) ) {
                    haveNativeLib = true;
                }
            }
        }


		if( !haveNativeLib ) 
			throw new IllegalStateException( "Native library missing: " +
											 resourceName );
        /*
		  Temporary folder for the native library file.
		  Use a user value of $group.$artifact.tempdir, or java.io.tmpdir
		  by default
		*/
		String keyPrefix = group + "." + artifact;
		String prpKey = keyPrefix + ".tmpdir";
		String tmpPath = p.getProperty( prpKey,
										System.getProperty( "java.io.tmpdir"));
		File tmpDir = new File( tmpPath ).getCanonicalFile();
		log.debug( "Tempdir for native lib: " + tmpDir );
		return extractLibraryFile( resourceName, tmpDir );
	}
	
	
	// Extract a native library resource into a file in the target directory
	static private File extractLibraryFile( String resourceName,
											File outDir ) throws IOException {
        /*
		  Attach UUID to the native library file to essentially
		  randomize its name.  This ensures multiple class loaders can
		  read it multiple times.
		*/
        String uuid = UUID.randomUUID().toString();
		// In Maven terms, the uuid acts like a 'classifier'
        String extractedLibFileName = resourceName + "-" + uuid;
        File extractedLibFile = new File( outDir, extractedLibFileName);
		log.debug( "Extracting " + resourceName + " to " + extractedLibFile );

		InputStream is = NativeLoader.class.getResourceAsStream
			( resourceName );
		FileUtils.copyInputStreamToFile( is, extractedLibFile );
		is.close();

		// Set executable (x) flag to enable Java to load the native library
		extractedLibFile.setReadable(true);
		//extractedLibFile.setWritable(true, true);
		extractedLibFile.setExecutable(true);
		return extractedLibFile;
	}
	
	static private Properties loadConfiguration( String prpName ) {
		log.debug( "LoadConfiguration from " + prpName );
		
		// Inspired by log4j.debug, inspect the loading process...
		boolean debugging =
			Boolean.parseBoolean( System.getProperty( "nativelibloader.debug",
													  "false" ) );
		if( debugging ) {
			// todo: add System.err as an appender for our logger ??
		}

		/*
		  As per usual configuration 'hierarchies', stuff on the cmd line wins
		*/
		Properties p = new Properties();
		// TO DO: look for and load a prp file held as a resource
		return p;
	}
	
	static private boolean haveResource( String path ) {
        return NativeLoader.class.getResource(path) != null;
    }


	static private volatile boolean isLoaded = false;
    static private File nativeLibFile = null;

	static private Log log = LogFactory.getLog( NativeLoader.class );
}

// eof


