package ch.epfl.isochrone.tiledmap;

import java.io.IOException;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public interface TileProvider {
	
	/**
	 * 
	 * @param zoom	Zoom de la tuile à retourner.
	 * @param x		X de la tuile à retourner.
	 * @param y		Y de la tuile à retourner.
	 * @return		Tuile des coordonnées passées en arguments
	 * @throws IOException
	 * 				Si il y a une erreure avec l'image.
	 */
	public Tile tileAt(int zoom, int x, int y) throws IOException;
}
