package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;
import java.io.IOException;

import ch.epfl.isochrone.geo.PointOSM;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class TransparentTileProvider extends FilteringTileProvider {
	private TileProvider tileProvider;
	private double alpha;
	
	/**
	 * 
	 * @param tileProvider Provider qui va être utiliser par ce transformateur de fourniseur
	 * @param alpha transparence qui va être appliquée par ce transformateur.
	 */
	public TransparentTileProvider(TileProvider tileProvider, double alpha) {
		this.tileProvider = tileProvider;
		this.alpha = alpha;
	}
	
	
	@Override
	public Tile tileAt(int zoom, int x, int y) throws IOException {
		BufferedImage image = new BufferedImage(Tile.TILE_LENGTH, Tile.TILE_LENGTH, BufferedImage.TYPE_INT_ARGB);
		BufferedImage biOfTile = tileProvider.tileAt(zoom, x, y).image();
		for(int i = 0; i < Tile.TILE_LENGTH; i++){
			for(int j = 0; j < Tile.TILE_LENGTH; j++){
				image.setRGB(i, j, transformARGB(biOfTile.getRGB(i, j)));
			}
		}
		return new Tile(image, new PointOSM(zoom, x, y));
	}

	@Override
	public int transformARGB(int argb) {
		int rgb = argb << 8 >>> 8;
		int roundedAlpha = (int) Math.round(255 * alpha);
		int alphaAnchor = (int) Math.pow(256,3);
		
		return roundedAlpha * alphaAnchor + rgb;
	}

}
