package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class TestOSMTileProvider {

	@Test
	public void constructorAndAdressTest() {
		URL toTest = null;
		try {
			toTest = new URL("toto");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		assertEquals("toto", new OSMTileProvider(toTest).adresse());
	}
}
