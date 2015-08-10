package greetings;

import org.junit.*;
import static org.junit.Assert.*;

public class HelloTest {

	@Ignore
	public void test1() {
		//		Hello.world();
	}

	@Test
	public void disabled() {
		/*
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
