package ch.epfl.isochrone.timetable;

import java.util.Set;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */

public final class Stop {

	private final String name;
	private final PointWGS84 position;
	
	/**
	 * 
	 * @param name
	 * 				Nom de l'arrêt à créer
	 * @param position
	 * 				Position WGS84 de l'arrêt à créer
	 */
	
	public Stop(String name, PointWGS84 position) {
		this.name = name;
		this.position = position;
	}
	
	/**
	 * 
	 * @return Retourne le nom de l'arrêt.
	 */
	
	public String name() {
		return name;
	}
	
	
	/**
	 * 
	 * @return Retourne la position de l'arrêt.
	 */
	public PointWGS84 position() {
		return position;
	}
	
	/**
	 * 
	 * @return Affiche le nom de l'arrêt.
	 */
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * 
	 * @param stops
	 * 				Set de Stop sur lequel on veut rechercher un Stop par son nom.
	 * @param name
	 * 				Nom du Stop à rechercher.
	 * @return
	 * 				Retourne le Stop de stops ayant pour nom le nom passé en argument.
	 */
	public static Stop getStopFromName(Set<Stop> stops, String name) {
		for (Stop stop : stops) {
			if (stop.name().equals(name)) return stop;
		}
		return null;
	}
}
