package edu.uw.apl.nativelibloader;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Stuart Maclean
 */

/*
  This code is derived from Taro Saito's snappy-java project:
  https://github.com/xerial/snappy-java. See NOTICE.txt for details.
*/


/**
 * This class loads a native library of supplied name (NAME.dll,
 * libNAME.so, etc.) according to the user platform (<i>os.name</i>
 * and <i>os.arch</i>). The artifact is expected to be found on the
 * classpath (e.g. bundled into a jar).
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
										  String artifact ) 
		throws IOException {
		String package_ = c.getPackage().getName();
		load( package_, artifact );
	}
	
	/*
	 * The primary entry point. Please load me a native library
	 * (presumably some JNI code associated with local Java classes)
	 * which has been packaged into an available jar as e.g.
	 *
	 * com/foo/bar/Linux/x86/libstuff.so
	 *
	 * for some supplied 'package' = com.foo.bar
	 * and some supplied 'artifact' = stuff
	 *
	 * The 'artifact' name would likely be available to the caller as
	 * the artifact value from a Maven-driven build.  Ditto the
	 * 'package', it could be the group vale from a Maven-driven
	 * build.
	 *
	 * @see asResourceName
	 */
	static public synchronized void load( String group,
										  String artifact )
		throws IOException {

		String key = group + "-" + artifact;
		try {
			if( loaded.contains( key ) )
				return;
			loadNativeLibrary( group, artifact );
			loaded.add( key );
		} catch( IOException ioe ) {
			log.error( ioe );
			throw ioe;
		}
	}
		
	static private synchronized void loadNativeLibrary( String group,
														String artifact )
		throws IOException {

		log.debug( "Loading: " + group + " " + artifact );
		
		String keyPrefix = group + "." + artifact;
		String prpResourceName = keyPrefix + ".properties";
		Properties p = loadConfiguration( prpResourceName );
		boolean useExternal = Boolean.parseBoolean
			( p.getProperty( keyPrefix + "." + "useLibraryPath", "false" ) );
		if( useExternal ) {
			/*
			  Load external artifact (i.e. one found, hopefully, using
			  -Djava.library.path).  Do NOT proceed to load from a local
			  resource
			*/
			System.loadLibrary( artifact );
			return;
		}
		File nativeLibFile = findNativeLibrary( group, artifact, p );
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
										   Properties p )
		throws IOException {

		/*
		  We compose the final resourceName in a way similar to the
		  Maven convention for artifact file naming: GROUP/ARTIFACT.
		  This is also similar to how .so files are named, except
		  there the version trails the .so suffix (see e.g. /lib/ on
		  any Unix system).
		*/

		String nativeLibraryName = System.mapLibraryName( artifact );
		String nativeLibraryPath = group.replaceAll( "\\.", "/" ) +
			"/native/" + OSInfo.getNativeLibFolderPathForCurrentOS();

		/*
		  Ensure the resourceName starts '/' so is not subject to any
		  'package name modification' during Class.getResource(),
		  Class.getResourceAsStream() calls (dots in the version
		  string could get replaced with slash!)
		*/
		String resourceName = "/" +	nativeLibraryPath +	"/" + nativeLibraryName;
		log.debug( "ResourceName: " + resourceName );

		boolean haveNativeLib = haveResource( resourceName );
        if( !haveNativeLib ) {
            if( OSInfo.getOSName().equals("Mac") ) {
                // Fix for openjdk7 for Mac
                String altLibraryName =
					"lib" + artifact + ".jnilib";
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
        String extractedLibFileName =
			resourceName.substring(1).replaceAll( "/", "." );
		// In Maven terms, the uuid acts like a 'classifier'
		extractedLibFileName += "-" + uuid;
        File extractedLibFile = new File( outDir, extractedLibFileName );
		log.debug( "Extracting " + resourceName + " to " + extractedLibFile );

		InputStream is = NativeLoader.class.getResourceAsStream
			( resourceName );
		FileUtils.copyInputStreamToFile( is, extractedLibFile );
		is.close();
		extractedLibFile.deleteOnExit();

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

	static private final Set<String> loaded = new HashSet<String>();
	static private final Log log = LogFactory.getLog( NativeLoader.class );
}

// eof


