package ch.epfl.isochrone.tiledmap;

import java.io.IOException;


/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public class CachedTileProvider implements TileProvider {
	private TileCache tilecache;
	private TileProvider tileProvider;
	
	/**
	 * 
	 * @param tileProvider Fournisseur de tuile à utiliser avec un cache.
	 */
	public CachedTileProvider(TileProvider tileProvider) {
		this.tileProvider = tileProvider;
		tilecache = new TileCache();
	}
	
	@Override
	public Tile tileAt(int zoom, int x, int y) throws IOException {
		if(tilecache.get(zoom, x, y) == null)
			tilecache.put(zoom, x, y, tileProvider.tileAt(zoom, x, y));
		
		return tilecache.get(zoom, x, y);	
	}
}
