package ch.epfl.isochrone.timetable;

import static ch.epfl.isochrone.math.Math.divF;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */

public final class SecondsPastMidnight {
	public static final int INFINITE = 200000;
	
	private SecondsPastMidnight() {}
	
	/**
	 * 
	 * @param hours
	 * 				Nombre d'heures après minuit à transformer en secondes
	 * @param minutes
	 * 				Nombre de minutes après minuit à transformer en secondes
	 * @param seconds
	 * 				Nombre de secondes après minuit.
	 * @return le nombre de secondes après minuit.
	 * 
	 * @throws IllegalArgumentException
	 * 				Si l'un des paramètres est négatif ou supérieure à 30 pour les heures, 60 pour les minutes et les secondes
	 */
	
	public static int fromHMS(int hours, int minutes, int seconds) throws IllegalArgumentException {
		if (hours < 0 || hours >= 30 || minutes < 0 || minutes >= 60 || seconds < 0 || seconds >= 60)
			throw new IllegalArgumentException("HMS incorrect");

		return 3600 * hours + 60 * minutes + seconds;
	}
	
	/**
	 * 
	 * @param date
	 * 				Date java à convertir en secondes après minuit
	 * @return le nombre de secondes après minuit d'après la date java fournie en argument
	 */
	@SuppressWarnings("deprecation")
	public static int fromJavaDate(java.util.Date date) {
		return 3600 * date.getHours() + 60 * date.getMinutes() + date.getSeconds();
	}
	
	/**
	 * 
	 * @param spm
	 * 				Nombre de secondes après minuit.
	 * @return le nombre d'heures arrondi à l'inférieur après minuit
	 * @throws IllegalArgumentException
	 * 				Si le nombre de seconde après minuit est négatif ou supérieur à 29:59:59 = 107999 secondes
	 */
	
	public static  int hours(int spm) throws IllegalArgumentException {
		valueInRange(spm);
		
		return divF(spm,3600);
	}
	
	/**
	 * 
	 * @param spm
	 * 				Nombre de secondes après minuit.
	 * @return le nombre de minutes arrondi à l'inférieur après minuit.
	 * @throws IllegalArgumentException
	 * 				Nombre de seconde après minuit est négatif ou supérieur à 29:59:59 = 107999 secondes
	 */
	
	public static int minutes(int spm) throws IllegalArgumentException {
		valueInRange(spm);
		
		return divF(spm-3600*hours(spm),60);
	}
	
	/**
	 * 
	 * @param spm
	 * 				Nombre de secondes après minuit.
	 * @return le nombre de secondes arrondi à l'inférieur après minuit.
	 * @throws IllegalArgumentException
	 * 				Si le nombre de seconde après minuit est négatif ou supérieur à 29:59:59 = 107999 secondes
	 */
	
	public static int seconds(int spm) throws IllegalArgumentException {
		valueInRange(spm);
		
		return spm - 60 * minutes(spm) - 3600 * hours(spm);
	}
	
	/**
	 * 
	 * @param spm
	 * 				Nombre de secondes après minuit
	 * @return un string pour afficher l'heure sous la forme hh:mm:ss
	 * @throws IllegalArgumentException
	 * 				Si le nombre de seconde après minuit est négatif ou supérieur à 29:59:59 = 107999 secondes
	 */
	public static String toString(int spm) throws IllegalArgumentException {
		valueInRange(spm);
			
		return String.format("%02d:%02d:%02d", hours(spm), minutes(spm), seconds(spm));
	}

	private static void valueInRange(int spm) {
		if (spm < 0 || spm > 107999)
			throw new IllegalArgumentException("Valeur en dehors des bornes");
	}
}
