package ch.epfl.isochrone.tiledmap;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public abstract class FilteringTileProvider implements TileProvider {	
	/**
	 * 
	 * @param 	argb Couleur sous le foramt alpha red green blue codé sur un entier.
	 * @return 	La nouvelle couleur de la tuile.
	 */
	abstract public int transformARGB(int argb);
}
