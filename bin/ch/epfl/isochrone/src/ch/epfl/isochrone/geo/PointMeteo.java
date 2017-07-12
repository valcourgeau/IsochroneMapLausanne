package ch.epfl.isochrone.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.isochrone.timetable.Date;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class PointMeteo {
	private static final String KEY = "061d6c02193a97d7"; // weather underground;
	private boolean METEO_AVAILABLE;
	private PointWGS84 point;
	private Date date;
	private Map<Double, Integer> temperatures = new HashMap<>();
	private Map<Double, String> iconsURL = new HashMap<>();
	private URL url;
	private final String baseUrl = "http://api.wunderground.com/api";
	
	/**
	 * 
	 * @param point	Coordonnées du point pour récupéré les informations météos.
	 * @param date	Date cible pour récupéré les informations météos.
	 */
	public PointMeteo(PointWGS84 point, Date date){
		this.point = point;
		this.date = date;
		url = null;
		try {
			url = new URL(baseUrl + '/' + KEY + '/' + "history_" + date.toString().replace("-","") + '/' + "q/" + Math.toDegrees(point.latitude()) + ',' + Math.toDegrees(point.longitude())  + ".json");
			METEO_AVAILABLE = true;
			setInformations();
		} catch (MalformedURLException e) {
			METEO_AVAILABLE = false;
		}
	}
	
	/**
	 * 
	 * @return Le pointWGS84 du pointMétéo.
	 */
	public PointWGS84 point() {
		return point;
	}
	
	/**
	 * 
	 * @return	La date du pointMétéo.
	 */
	public Date date() {
		 return date;
	}
	
	/**
	 * 
	 * @return	True si les informations météo ont été récupérés correctement, false sinon.
	 */
	public boolean meteoAvailable() {
		return METEO_AVAILABLE;
	}
	
	/**
	 * 
	 * @param hours		Heure cible pour récupéré l'icon.
	 * @param minutes	Minutes cible pour récupéré l'icon.
	 * @return			L'icon pour les informations de température les plus proches des arguments passés, ou bien si aucune icone n'est documenté ("unknown"), une recherche dans un interval de 2 heures autour de la cible est lancé pour trouver une icone.
	 */
	public String iconForTime(int hours, int minutes) {
		String iconName = iconsURL.get(getClosest(hours, minutes));
		if (iconName.equals("unknown")) {
			for (int i = 0; i <= 2; ++i) {
				for (int j = 0; j < 6; ++j) {
					for (int k = 0; k < 2; ++k) {
						int ratioHours = (int) (i * Math.pow(-1,k));
						int ratioMinutes = (int) (j*10 * Math.pow(-1,k));
						String closestKnownIcon = iconsURL.get(getClosest(hours + ratioHours, minutes + ratioMinutes));
						if (!closestKnownIcon.equals("unknown"))
							return closestKnownIcon;
					}
				}
			}
		}
		return iconName;
	}
	
	
	/**
	 * 
	 * @param hours		Heure cible pour récupéré l'icon.
	 * @param minutes	Minutes cible pour récupéré l'icon.
	 * @return			La temprérature la plus proches pour les arguments passés en arguments.
	 */
	public int temperatureForTime(int hours, int minutes) {
		return temperatures.get(getClosest(hours, minutes));
	}
	
	private double getClosest(int hours, int minutes) {
		double searchedKey = getKey(hours, minutes);
		double index = 0.0;
		double ecart = Math.abs(searchedKey);
		for (double key : temperatures.keySet()) {
			if (ecart > Math.abs(searchedKey - key)) {
				ecart = Math.abs(searchedKey - key);
				index = key;
			}
		}
		return index;
	}
	
	private double getKey(int hours, int minutes) {
		return (double)(hours + minutes/60.0);
	}
	private void setInformations() {
		if (!METEO_AVAILABLE)
			return;
		try {
			try {
			boolean timeChanelOK = true;
			int currentLoopHours = 0;
			int currentLoopMinutes = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {

				if (timeChanelOK) {
					if (line.contains("hour")) {
						int index = line.indexOf("hour") + 8;
						currentLoopHours = Integer.parseInt(line.substring(index, index + 2));
					}
					if (line.contains("min")) {
						int index = line.indexOf("min") + 7;
						currentLoopMinutes = Integer.parseInt(line.substring(index, index + 2));
						timeChanelOK = false;
					}
				
				}
				
				if (line.contains("tempm")) {
					int tempBeginIndex = line.indexOf("tempm") + 8;
					int tempEndingIndex = tempBeginIndex + 1;
					while (line.charAt(tempEndingIndex) != '"')
						++tempEndingIndex;
					temperatures.put(getKey(currentLoopHours, currentLoopMinutes), (int)Double.parseDouble(line.substring(tempBeginIndex, tempEndingIndex)));
				}
				
				if (line.contains("icon")) {
					int iconBeginIndex = line.indexOf("icon") + 7;
					int iconEndingIndex = iconBeginIndex + 1;
					while (line.charAt(iconEndingIndex) != '"')
						++iconEndingIndex;
					String iconsString;
					
					if (line.substring(iconBeginIndex, iconEndingIndex).length() < 3 )
						iconsString = "unknown";
					else
						iconsString = line.substring(iconBeginIndex, iconEndingIndex);
					
					iconsURL.put(getKey(currentLoopHours, currentLoopMinutes), iconsString);
					timeChanelOK = true;
				}
			}
			} catch(NumberFormatException e1) {
				METEO_AVAILABLE = false;
			}
		} catch (IOException e) {
			METEO_AVAILABLE = false;
		}
	}
}
