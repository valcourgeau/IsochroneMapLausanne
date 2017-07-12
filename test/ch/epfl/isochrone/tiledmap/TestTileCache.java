package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import ch.epfl.isochrone.geo.PointOSM;

public class TestTileCache {
	
	@Test
	public void constructorTest() {
		@SuppressWarnings("unused")
		TileCache tileCache1 = new TileCache();
		@SuppressWarnings("unused")
		TileCache tileCache2 = new TileCache(100);
		@SuppressWarnings("unused")
		TileCache tileCache3 = new TileCache(3);
	}
	
	/*	TEST PRIVATE
	@Test
	public void intsToKeyTest() {
		TileCache tc = new TileCache();
		int zoom = 1;
		int x = 1;
		int y = 1;
		long toto = 1000000101;
		assertEquals(toto, tc.intsToKey(zoom,x,y));
	}
	*/
	
	@Test
	public void putTest() throws IOException {
		TileCache tc = new TileCache(3);
		PointOSM point1 = new PointOSM(1,1,1);
		Tile tile1 = new Tile(ImageIO.read(getClass().getResource("/images/error-tile.png")), point1);
		PointOSM point2 = new PointOSM(2,1,1);
		Tile tile2 = new Tile(ImageIO.read(getClass().getResource("/images/error-tile.png")), point2);
		PointOSM point3 = new PointOSM(3,1,1);
		Tile tile3 = new Tile(ImageIO.read(getClass().getResource("/images/error-tile.png")), point3);
		PointOSM point4 = new PointOSM(4,1,1);
		Tile tile4 = new Tile(ImageIO.read(getClass().getResource("/images/error-tile.png")), point4);
		tc.put(1,1,1, tile1);
		tc.put(2,1,1, tile2);
		tc.put(3,1,1, tile3);
		tc.put(4,1,1, tile4);
		assertTrue(tc.cache.containsValue(tile2));
		assertTrue(tc.cache.containsValue(tile3));
		assertTrue(tc.cache.containsValue(tile4));
		assertTrue(!tc.cache.containsValue(tile1));

		
	}
	
}
