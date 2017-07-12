package ch.epfl.isochrone.tiledmap;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import ch.epfl.isochrone.geo.PointOSM;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public class OSMTileProvider implements TileProvider {
	
	private URL adresse;
	
	/**
	 * 
	 * @param adresse
	 * 					adresse du site où l'on va récupérer les tuiles.
	 */
	public OSMTileProvider(URL adresse){
		this.adresse = adresse;
	}
	
	/**
	 * 
	 * @return l'adresse du site où l'on va récupérer les tuiles.
	 */
	public String adresse() {
		return adresse.toString();
	}
	
	@Override
	public Tile tileAt(int zoom, int x, int y) throws IOException{
		// TODO Coordonnées du point 
		PointOSM point = new PointOSM(zoom, x, y);
		try {
			URL urlOfTile = new URL(adresse.toString() + zoom + "/" + x + "/" + y + ".png");
			return new Tile(ImageIO.read(urlOfTile), point);
		} catch (IOException e) {
			return new Tile(ImageIO.read(getClass().getResource("/images/error-tile.png")), point);
		}
	}

}
