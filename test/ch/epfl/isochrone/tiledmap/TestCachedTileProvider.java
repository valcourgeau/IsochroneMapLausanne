package ch.epfl.isochrone.tiledmap;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class TestCachedTileProvider {

	@Test
	public void noExceptionConstructorTest() {
		URL toTest = null;
		try {
			toTest = new URL("toto");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		new CachedTileProvider(new OSMTileProvider(toTest));
	}
	
}
