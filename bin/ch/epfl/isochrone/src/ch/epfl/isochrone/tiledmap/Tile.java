package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;

import ch.epfl.isochrone.geo.PointOSM;

public final class Tile {
	@SuppressWarnings("unused")
	private int zoom, xTile, yTile;
	private BufferedImage tile;
	public static final int TILE_LENGTH = 256;
	
	/**
	 * 
	 * @param image Image java qui représente la tuile en elle-même
	 * @param point Point géographique qui correspond à la tuile.
	 */
	public Tile (BufferedImage image, PointOSM point) {
		this.zoom = point.zoom();
		this.xTile = (int) Math.round(point.x());
		this.yTile = (int) Math.round(point.y());
		this.tile = image;
	}

	/**
	 * 
	 * @return L'image de la tuile.
	 */
	public BufferedImage image() {
		return tile;
	}
	
}
