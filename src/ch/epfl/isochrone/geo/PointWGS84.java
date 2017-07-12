package ch.epfl.isochrone.geo;

import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.tan;
import static ch.epfl.isochrone.math.Math.asinh;
import static ch.epfl.isochrone.math.Math.haversin;
import static ch.epfl.isochrone.geo.PointOSM.maxXY;


/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 * 
 */

public final class PointWGS84 {
	final public static double R=6378137;

	private final double longitude;
	private final double latitude;
	
	/**
	 * 
	 * @param longitude
	 * 				Longitude du point à créer (angle au Méridien de Greenwich).
	 * @param latitude
	 * 				Latitude du point à créer (angle à l'équateur).
	 * @throws IllegalArgumentException
	 * 				Vérifie que longitude se trouve bien entre -PI et +PI, et que latidude est entre -PI/2 et +PI/2.
	 */
	public PointWGS84(double longitude, double latitude) {
		if (java.lang.Math.abs(longitude) > PI || java.lang.Math.abs(latitude) > PI / 2) 
			throw new IllegalArgumentException();

		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	/**
	 * 
	 * @return Retourne la longitude du point auquel on l'applique.
	 */
	public double longitude() {
		return longitude;
	}
	
	/**
	 * 
	 * @return Retourne la latidude du point auquel on l'applique.
	 */
	public double latitude() {
		return latitude;
	}
	
	/**
	 * 
	 * @param that
	 * 				PointWGS84 par rapport auquel on calcule la distance au pointWGS84 auquel on l'applique. 
	 * @return Retourne la distance entre le pointWGS84 auquel on l'applique et le pointWGS84 that.
	 */
	public double distanceTo(PointWGS84 that) {
		return 2 * R * asin(sqrt(haversin(latitude() - that.latitude()) + cos(latitude()) * cos(that.latitude()) * haversin(longitude() - that.longitude())));
	}
	
	/**
	 * 
	 * @param zoom
	 * 				Valeur du zoom à utiliser pour construire le pointOSM correspondant au pointWGS84 auquel on l'applique.
	 * @return Convertit le pointWGS84 auquel on l'applique en un pointOSM correspondant.
	 * @throws IllegalArgumentException
	 * 				Vérifie que la valeur du zoom est bien positive.
	 */
	public PointOSM toOSM(int zoom) throws IllegalArgumentException {
		if (zoom < 0)
			throw new IllegalArgumentException();
		
		return new PointOSM(zoom, maxXY(zoom) * (longitude() + PI) / (2 * PI), maxXY(zoom) * (PI - asinh(tan(latitude()))) / (2*PI));
	}
	
	/**
	 * @return Affiche la longitude et la latitude du pointWGS84 auquel on l'applique (Ex: (84.5, 50.2)).
	 */
	@Override
	public String toString() {
		return "("+java.lang.Math.toDegrees(longitude())+","+java.lang.Math.toDegrees(latitude())+")";
	}
}
