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
										  String libName ) 
		throws IOException {
		String package_ = c.getPackage().getName();
		load( package_, libName );
	}
	
	/*
	 * The primary entry point. Please load me a native library
	 * (presumably some JNI code associated with local Java classes)
	 * which has been packaged into an available jar as e.g.
	 *
	 * com/foo/bar/native/Linux/x86/libstuff.so
	 *
	 * for some supplied 'prefix' = com.foo.bar
	 * and some supplied 'libName' = stuff
	 *
	 * The 'libName' name would likely be available to the caller as
	 * the perhaps a class name, or as the artifact value from a
	 * Maven-driven build.  Ditto the 'prefix', it could be a class's
	 * package or the groupId value from a Maven-driven build.
	 *
	 */
	static public synchronized void load( String prefix,
										  String libName )
		throws IOException {

		String key = prefix + "-" + libName;
		try {
			if( loaded.contains( key ) )
				return;
			loadNativeLibrary( prefix, libName );
			loaded.add( key );
		} catch( IOException ioe ) {
			log.error( ioe );
			throw ioe;
		}
	}
		
	static private synchronized void loadNativeLibrary( String prefix,
														String libName )
		throws IOException {

		log.debug( "Loading: " + prefix + " " + libName );

		Properties p = loadConfiguration( prefix, libName );
		
		if( isDefined( "disabled", prefix, libName, p ) ) {
			log.debug( "Loading disabled: " + prefix + "," + libName );
			return;
		}
		
		if( isDefined( "useExternal", prefix, libName, p ) ) {
			/*
			  Load external artifact (i.e. one found, hopefully, using
			  -Djava.library.path).  Do NOT proceed to load from a local
			  resource
			*/
			System.loadLibrary( libName );
			return;
		}

		File nativeLibFile = findNativeLibrary( prefix, libName, p );
		if( nativeLibFile != null ) {
			System.load( nativeLibFile.getPath() );
		}
	}

	/**
	 * Allow the loading process to be influenced by properties
	 * loaded from the classpath, as identified by a resource name
	 * built from prefix and libName
	 */
	static private Properties loadConfiguration( String prefix,
												 String libName ) {
		log.debug( "LoadConfiguration for " + prefix + "," + libName );
		
		Properties p = new Properties();
		try {
			String asResource = prefix.replaceAll( "\\.", "/" ) +
				"/" + libName + ".properties";
			InputStream is = NativeLoader.class.getResourceAsStream
				( asResource );
			if( is == null )
				return p;
			p.load( is );
			is.close();
		} catch( IOException e ) {
		}
		return p;
	}

	static private boolean isDefined( String key,
									  String prefix, String libName,
									  Properties p ) {
		String s = getValue( key, prefix, libName, p );
		return s != null;
	}

	/**
	 * We attempt a look up of a value for the supplied key, with look
	 * up first in System.property space, then in p space.  Further,
	 * we use two look up keys, in a 'hierarchical namespace' fashion
	 * (think log4j logger configuration, where a logger can be
	 * defined at e.g. class level, or package level)
	 *
	 * @param p a Properties object as returned from loadConfiguration
	 */
	static private String getValue( String key,
									String prefix, String libName,
									Properties p ) {
		log.debug( "getValue: " + key + "," + prefix + "," + libName );

		String keyP = prefix + "." + key;
		log.debug( "keyP: " + keyP );
		
		String s = System.getProperty( keyP );
		if( s != null )
			return s;
		s = p.getProperty( keyP );
		if( s != null )
			return s;
		
		String keyPL = prefix + "." + libName + "." + key;
		log.debug( "keyPL: " + keyPL );

		s = System.getProperty( keyPL );
		if( s != null )
			return s;
		s = p.getProperty( keyPL );
		if( s != null )
			return s;

		return null;
	}
	
	/*
	  Load an OS-dependent native library from the classpath
	  (typically from inside a jar file)
	*/
	static private File findNativeLibrary( String prefix,
										   String libName,
										   Properties p )
		throws IOException {

		/*
		  We compose the final resourceName in a way similar to the
		  Maven convention for artifact file naming: GROUP/ARTIFACT.
		  This is also similar to how .so files are named, except
		  there the version trails the .so suffix (see e.g. /lib/ on
		  any Unix system).
		*/

		String nativeLibraryName = System.mapLibraryName( libName );
		String nativeLibraryPath = prefix.replaceAll( "\\.", "/" ) +
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
					"lib" + libName + ".jnilib";
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
		  Use a user value of $prefix.$libName.tmpdir, or java.io.tmpdir
		  by default
		*/
		String tmpPath = getValue( "path", prefix, libName, p );
		if( tmpPath == null )
			tmpPath = System.getProperty( "java.io.tmpdir");
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
	
	
	static private boolean haveResource( String path ) {
        return NativeLoader.class.getResource(path) != null;
    }

	static private final Set<String> loaded = new HashSet<String>();

	static private final Log log = LogFactory.getLog( NativeLoader.class );
}

// eof


