package ch.epfl.isochrone.geo;
import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.atan;
import static java.lang.Math.sinh;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 * 
 */

public final class PointOSM {
	
	private final int zoom;
	private final double x;
	private final double y;
	
	/**
	 * 
	 * @param zoom
	 * 				Valeur du zoom à utiliser.
	 * @return Retourne la taille maximal de la carte avec le zoom indiqué en paramètre.
	 * @throws IllegalArgumentException
	 * 				Si la valeur du zoom est strictement négative.
	 */
	public static int maxXY(int zoom) throws IllegalArgumentException {
		if (zoom < 0)
			throw  new IllegalArgumentException();
		else
			return (int) pow(2, 8 + zoom);
	}
	
	/**
	 * 
	 * @param zoom
	 * 				Valeur du zoom à utiliser.
	 * @param x
	 * 				Position en X du point auquel on l'applique.
	 * @param y
	 * 				Position en Y du point auquel on l'applique.
	 * @throws IllegalArgumentException
	 * 				Vérifie que le zoom est positif et que les arguments X et Y
	 * 				sont compris entre 0 et la taille maximal de la carte pour le zoom.
	 */
	public PointOSM(int zoom, double x, double y) throws IllegalArgumentException {
		if (zoom < 0 || x < 0 || y < 0 || x > maxXY(zoom) || y > maxXY(zoom)) {
			throw new IllegalArgumentException();
		} else {
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}
	}
	
	/**
	 * 
	 * @return Retourne la position en X du point  auquel on l'applique.
	 */
	public double x() {
		return x;
	}
	
	/**
	 * 
	 * @return Retourne la position en Y du point auquel on l'applique.
	 */
	public double y() { 
		return y; 
	}
	
	/**
	 * 
	 * @return Retourne le niveau de zoom du point courant.
	 */
	public int zoom() {
		return zoom; 
	}
	
	/**
	 * 
	 * @return Retourne l'entier le plus proche de la coordonnée X du point auquel on l'applique.
	 */
	public int roundedX() {
		return (int) round(x);
	}
	
	/**
	 * 
	 * @return Retourne l'entier le plus proche de la coordonnée Y du point auquel on l'applique.
	 */
	public int roundedY() {
		return (int) round(y);
	}
	
	/**
	 * 
	 * @param newZoom 
	 * 				Nouvelle valeur de zoom.
	 * @return Retourne un nouveau pointOSM aux coordonnées liées du niveau de zoom newZoom.
	 * @throws IllegalArgumentException
	 * 				Vérifie que la valeur du zoom est bien positive.
	 */
	public PointOSM atZoom(int newZoom) throws IllegalArgumentException{
		if (newZoom < 0 ) {
			throw new IllegalArgumentException();
		} else {
			return new PointOSM(newZoom, x() * pow(2, newZoom - zoom()), y() * pow(2, newZoom - zoom()));
		}
	}
	
	/**
	 * 
	 * @return Convertit le pointOSM auquel on l'applique en pointWGS84.
	 */
	public PointWGS84 toWGS84() {
		return new PointWGS84(2 * PI * x / maxXY(zoom) - PI, atan(sinh(PI - 2 * PI * y / maxXY(zoom))));
	}
	
	/**
	 * @return Affiche le niveau de zoom et les coordonnées, X puis Y, associées au pointOSM auquel on l'applique. (Ex: (2, 85, 125)).
	 */
	@Override
	public String toString() {
		return "(" + zoom() + "," + x() + "," + y() + ")";
	}
}
