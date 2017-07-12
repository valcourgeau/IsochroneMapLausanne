package ch.epfl.isochrone.tiledmap;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Stop;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class IsochroneTileProvider implements TileProvider {
	private FastestPathTree fastestPathTree;
	private ColorTable colorTable;
	private double walkingSpeed;
	
	/**
	 * 
	 * @param fastestPathTree 	Arbres des trajets les plus rapides pour dessiner la carte.
	 * @param colorTable	  	Table des couleurs pour colorer la carte.
	 * @param walkingSpeed		Vitesse de marche pour la carte isochrone.
	 */
	public IsochroneTileProvider(FastestPathTree fastestPathTree, ColorTable colorTable, double walkingSpeed) {
		if (walkingSpeed <= 0)
			throw new IllegalArgumentException();
		this.fastestPathTree = fastestPathTree;
		this.colorTable      = colorTable;
		this.walkingSpeed    = walkingSpeed;
	}
	
	@Override
	public Tile tileAt(int zoom, int x, int y) throws IOException {
		BufferedImage image = new BufferedImage(Tile.TILE_LENGTH,Tile.TILE_LENGTH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
				
		g.setColor(colorTable.getColorForSlice(colorTable.getNumberSlices()-1));
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		double ratio = getRatio(zoom, x, y);
		
		for (int i = colorTable.getNumberSlices() - 1; i >= 0 ; --i) {
			for (Stop stop : fastestPathTree.stops()) {
				int time = (i + 1) * colorTable.getDuree() - (fastestPathTree.arrivalTime(stop) - fastestPathTree.startingTime());
				if (time > 0 ) {
					double radius = time * walkingSpeed * ratio;
					
					PointOSM stopPositionOSM = stop.position().toOSM(zoom);
					double xStop = stopPositionOSM.x();
					double yStop = stopPositionOSM.y();
					
					g.setColor(colorTable.getColorForSlice(i));
					
					g.fill(new Ellipse2D.Double((xStop - x * Tile.TILE_LENGTH - radius), (yStop - y * Tile.TILE_LENGTH - radius), 2.0 * radius, 2.0 * radius));
				}
			}
		}
		
		return new Tile (image, new PointOSM(zoom, x, y));
	}
	
	private double getRatio(int zoom, int x, int y) {
		PointOSM 	x0 = new PointOSM(zoom, x * Tile.TILE_LENGTH, y * Tile.TILE_LENGTH),
					x1 = new PointOSM(zoom, x * Tile.TILE_LENGTH, y * Tile.TILE_LENGTH + 1);
		
		return 1 / x0.toWGS84().distanceTo(x1.toWGS84());
	}
	
}
