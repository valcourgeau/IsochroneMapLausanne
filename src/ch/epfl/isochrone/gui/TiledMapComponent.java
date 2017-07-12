package ch.epfl.isochrone.gui;

import static ch.epfl.isochrone.geo.PointOSM.maxXY;
import static ch.epfl.isochrone.tiledmap.Tile.TILE_LENGTH;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ch.epfl.isochrone.tiledmap.TileProvider;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
@SuppressWarnings("serial")
public final class TiledMapComponent extends JComponent {
	private int zoom;
	private List<TileProvider> providers;
	
	/**
	 * 
	 * @param zoom	Niveau de zoom de la map.
	 */
	public TiledMapComponent(int zoom) {
		this.setZoom(zoom);
		providers = new ArrayList<>();
	}
	
	/**
	 * 
	 * @param zoom	Modifie le niveau de zoom de la map.
	 * @throws IllegalArgumentException	
	 * 				Si le niveau de zoom est en dehors des bornes ]10,19].
	 */
	public void setZoom(int zoom) {
		if(zoom < 10 || zoom > 19)
			throw new IllegalArgumentException();
		this.zoom = zoom;
		this.repaint();
	}
	
	/**
	 * 
	 * @return	Le niveau de zoom de la map.
	 */
	public int zoom(){
		return zoom;
	}
	
	/**
	 * 
	 * @param providers	Redéfinie la liste de fournisseurs de tuiles de la map.
	 */
	public void setTileProviders(List<TileProvider> providers) {
		if(providers == null)
			throw new IllegalArgumentException();
		this.providers = providers;
		this.repaint();
	}
	
	@Override
	public Dimension getPreferredSize(){
		int mapSize = maxXY(zoom);
		return new Dimension(mapSize, mapSize);
	}
	
	@Override
	public void paintComponent(Graphics g0) {
		Graphics2D g2D = (Graphics2D) g0;
		Rectangle visibleRect = this.getVisibleRect();
		
		int tileXmax = (int) Math.floor(visibleRect.getMaxX() / TILE_LENGTH);
		int	tileXmin = (int) Math.floor(visibleRect.getMinX() / TILE_LENGTH);
		int	tileYmin = (int) Math.floor(visibleRect.getMinY() / TILE_LENGTH);
		int	tileYmax = (int) Math.floor(visibleRect.getMaxY() / TILE_LENGTH);
		
		for(TileProvider provider : providers) {
			for(int i = tileXmin; i <= tileXmax; i++) {
				for(int j = tileYmin; j <= tileYmax; j++) {
					
					try {
						g2D.drawImage(provider.tileAt(zoom, i, j).image(), i * TILE_LENGTH, j * TILE_LENGTH, null);
					} catch (IOException e) {e.printStackTrace();
					}
				}
			}
		}
	}
}