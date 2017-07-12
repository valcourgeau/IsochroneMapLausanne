package ch.epfl.isochrone.gui;

import static ch.epfl.isochrone.timetable.Stop.getStopFromName;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ch.epfl.isochrone.geo.PointMeteo;
import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.Stop;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
@SuppressWarnings("serial")
public class MeteoPanel extends JPanel {
	private PointMeteo meteoDeparture, meteoArrival;
	private JLabel departureIconAndTemp = new JLabel(), departureName = new JLabel();
	private JLabel arrivalIconAndTemp = new JLabel(), arrivalName = new JLabel();
	private JPanel departurePanel = new JPanel(new BorderLayout()), arrivalPanel = new JPanel(new BorderLayout());
	private MeteoCache cache;
	private final Set<Stop> stops;
	private Map<String, ImageIcon> icons = new HashMap<>();

	/**
	 * 
	 * @param stop		arrêt pour lequel on désire obtenir les informations météo.
	 * @param date		date pour laquelle on désire obtenir les informations météo.
	 * @param hours		heure pour laquelle on désire obtenir les informations météo.
	 * @param minutes	minutes pour lesquelles on désire obtenir les informations météo.
	 * @param stops		Set de stop qui permettra d'identifier les stops par leur noms.
	 */
	public MeteoPanel(Stop stop, Date date, int hours, int minutes,  Set<Stop> stops) {

		this.setLayout(new FlowLayout());
		cache = new MeteoCache(20);
		this.stops = stops;
		updateMeteoPanel(stop, stop, date, hours, minutes);
		this.add(departurePanel);
		this.add(new JSeparator(SwingConstants.HORIZONTAL));
		this.add(arrivalPanel);
	}
	
	/**
	 * 
	 * @param departureStop	stop de départ pour lequel on désire obtenir les informations météo.
	 * @param arrivalStop	stop d'arriver pour lequel on désire obtenir les informations météo.
	 * @param date			date pour laquelle on désire obtenir les informations météo.
	 * @param hours			heure pour laquelle on désire obtenir les informations météo.
	 * @param minutes		minutes pour lesquelles on désire obtenir les informations météo.
	 */
	public void updateMeteoPanel(Stop departureStop, Stop arrivalStop, Date date, int hours, int minutes) {
		String departureKey = departureStop.name() +'/'+ date.toString();
		String arrivalKey   = arrivalStop.name() +'/'+ date.toString();
		try {
			meteoDeparture = cache.get(departureKey);
			departureName.setText(departureStop.name());
			departurePanel.add(departureName, BorderLayout.NORTH);
			departurePanel.add(departureIconAndTemp, BorderLayout.CENTER);
			setDepartureIconAndTemp(departureKey, hours, minutes);
		} catch(IllegalArgumentException e) {
			departureName.setText("weather forecast unavailable");
		} try {
			meteoArrival = cache.get(arrivalKey);
			arrivalName.setText(arrivalStop.name());
			arrivalPanel.add(arrivalName, BorderLayout.NORTH);
			arrivalPanel.add(arrivalIconAndTemp, BorderLayout.CENTER);
			setArrivalIconAndTemp(arrivalKey, hours, minutes);
		} catch (IllegalArgumentException e) {
			arrivalName.setText("weather forecast unavailable");
		}
		
		this.repaint();
	}
	
	private void setDepartureIconAndTemp(String departureKey, int hours, int minutes) {
		String departureIconName = meteoDeparture.iconForTime(hours, minutes);
		departureIconAndTemp.setIcon(addIconToMap(departureIconName));
		departureIconAndTemp.setText(meteoDeparture.temperatureForTime(hours, minutes) + "°C");
	}
	
	private void setArrivalIconAndTemp(String arrivalKey, int hours, int minutes) {
		String arrivalIconName = meteoArrival.iconForTime(hours, minutes);		
		arrivalIconAndTemp.setIcon(addIconToMap(arrivalIconName));
		arrivalIconAndTemp.setText(meteoArrival.temperatureForTime(hours, minutes) + "°C");
	}
	
	private ImageIcon addIconToMap(String v) {
		ImageIcon icon = null;
		if (icons.get(v) == null) {
			try {
				icon = new ImageIcon(new URL("http://icons.wxug.com/i/c/a/"+ v +".gif"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			icons.put(v, icon);
			return icon;
		}
		return icons.get(v);
	}
	
	/**
	 * 
	 * @author Virgile Neu (224138)
	 * @author Valentin Courgeau (225255)
	 *
	 */
	private class MeteoCache extends LinkedHashMap<String, PointMeteo> {
		private final int MAX_SIZE;
		private LinkedHashMap<String, PointMeteo> cache = new LinkedHashMap<String, PointMeteo>() {
	        @Override
	        protected boolean removeEldestEntry(Map.Entry<String, PointMeteo> e){
	            return size() > MAX_SIZE;
	        }
		};
		
		/**
		 * 
		 * @param MAX_SIZE	Taille maximal du cache.
		 */
		public MeteoCache(int MAX_SIZE) {
			this.MAX_SIZE = MAX_SIZE;
		}
		
		@Override
		public PointMeteo put(String s, PointMeteo point) {
			if(!point.meteoAvailable())
				throw new IllegalArgumentException("Meteo not available)");
			Iterator<String> it = cache.keySet().iterator();
			if (it.hasNext()) {
				String eldestKey = it.next();
				if (removeEldestEntry(new Entry<String, PointMeteo>(eldestKey, cache.get(eldestKey))))
					cache.remove(eldestKey);
			}
			PointMeteo toto = cache.put(s, point);
			return toto;
		}
		
		/**
		 * 
		 * @param s clé du pointMétéo à retourner.
		 * @return	pointMétéo correspondant à la clé. Il est créé et ajouté au cache s'il n'y était pas.
		 */
		public PointMeteo get(String s){
			if (!containsKey(s)) {
				put(s, pointMeteoFromString(s));
			}
			return cache.get(s);
		}
		
		private PointMeteo pointMeteoFromString(String s) {
			String[] substrings = s.split("/");
			Stop stop = getStopFromName(stops, substrings[0]);
			String[] dateSubstrings = substrings[1].split("-");
			Date date = new Date(Integer.parseInt(dateSubstrings[2]), Integer.parseInt(dateSubstrings[1]), Integer.parseInt(dateSubstrings[0]));
			return new PointMeteo(stop.position(), date);
		}

		/**
		 * 
		 * @author Virgile Neu (224138)
		 * @author Valentin Courgeau (225255)
		 * 
		 */
		@SuppressWarnings("hiding")
		private final class Entry<String, PointMeteo> implements Map.Entry<String, PointMeteo> {
			private String key;
			private PointMeteo value;

			private Entry(String key, PointMeteo pointMeteo) {
				this.key = key;
				this.value = pointMeteo;
			}
			
			@Override
			public String getKey() {
				return this.key;
			}

			@Override
			public PointMeteo getValue() {
				return this.value;
			}

			@Override
			public PointMeteo setValue(PointMeteo arg0) {
				return this.value = arg0;
			}
		}
	}
}
