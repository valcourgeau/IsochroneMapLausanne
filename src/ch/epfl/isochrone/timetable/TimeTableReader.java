package ch.epfl.isochrone.timetable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.Date.DayOfWeek;


/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class TimeTableReader {
	private final String prefix;
	private Map<String, Stop> nameToStops;

	/**
	 * 
	 * @param baseResourceName Adresse du dossier où se trouve les fichiers de donnés .csv.
	 */
	public TimeTableReader(String baseResourceName) {
		prefix = baseResourceName;
		nameToStops = new HashMap<String, Stop>();
	}
	
	/**
	 * 
	 * @return				La timeTable construite à l'aide des fichiers de donnés.
	 * 
	 * @throws IOException 	s'il y a une erreur de lecture avec le stream.
	 */
	public TimeTable readTimeTable() throws IOException {
		TimeTable.Builder timeTableBuilder = new TimeTable.Builder();
		Set<Service.Builder> buildersFromCalendar = readCalendarCSV();
		
		Set<Service.Builder> builders = new HashSet<Service.Builder>(buildersFromCalendar);
		Set<Stop> stops = readStopsCSV();

		addStopsToTimeTable(timeTableBuilder, stops);	
		addServicestoTimeTable(timeTableBuilder, builders);
		addCalendarDates(builders);
		
		return timeTableBuilder.build();
	}
	
	/**
	 * 
	 * @param stops				Set des stops du graph.
	 * @param services			Set des services du graph.
	 * @param walkingTime		Temp de marche maximal pour ce graph.
	 * @param walkingSpeed		Vitesse de marche pour ce graph.
	 * @return					Un graph qui lie chaque arret de stops avec les autres d'après les donnés.
	 * @throws IOException		S'il y a une erreur de lecture avec le stream.
	 */
	public Graph readGraphForServices(Set<Stop> stops, Set<Service> services, int walkingTime, double walkingSpeed) throws IOException {
		Graph.Builder graphBuilder = new Graph.Builder(stops);
		InputStream stream = getClass().getResourceAsStream(prefix + "stop_times.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		String line;
		
		while ((line = reader.readLine()) != null) {
			String[] lineSplit = line.split(";");
			String fromStopName = lineSplit[1], toStopName = lineSplit[3], serviceName = lineSplit[0];
			int departureTime = Integer.parseInt(lineSplit[2]), arrivalTime = Integer.parseInt(lineSplit[4]);
			Stop fromStop = stopFromName(stops, fromStopName), toStop = stopFromName(stops, toStopName);
			
			if (fromStop != null && toStop != null && nameIsInSetOfService(services, serviceName))
				graphBuilder.addTripEdge(fromStop, toStop, departureTime, arrivalTime);
		}
		
		graphBuilder.addAllWalkEdges(walkingTime, walkingSpeed);
		
		reader.close();
		stream.close();
		
		return graphBuilder.build();
	}
	
	private boolean nameIsInSetOfService(Set<Service> services, String name) {
		for (Service service : services) {
			if (service.name().equals(name))
				return true;
		}
		
		return false;
	}
	
	private Stop stopFromName(Set<Stop> stops, String name) {
		for (Stop stop : stops) {
			if (stop.name().equals(name)) 
				return stop;
		}
		
		return null;
	}
	
	private void addServicestoTimeTable(TimeTable.Builder timeTableBuilder, Set<Service.Builder> builders) {
		for (Service.Builder serviceBuilder : builders)
			timeTableBuilder.addService(serviceBuilder.build());
	}
	
	private  void addStopsToTimeTable(TimeTable.Builder timeTableBuilder, Set<Stop> stops) {
		for (Stop stop : stops) 
			timeTableBuilder.addStop(stop);
	}
	
	private Set<Stop> readStopsCSV() throws IOException{
		Set<Stop> stops = new HashSet<Stop>();
		InputStream stream = getClass().getResourceAsStream(prefix + "stops.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		String line;
		
		while ((line = reader.readLine()) != null) {
			String[] lineSplit = line.split(";");
			String stopName = lineSplit[0];
			PointWGS84 position = new PointWGS84(Math.toRadians(Double.parseDouble(lineSplit[2])), Math.toRadians(Double.parseDouble(lineSplit[1])));
			nameToStops.put(stopName, new Stop(stopName, position));
			stops.add(nameToStops.get(stopName));
		}
		
		reader.close();
		stream.close();
		
		return stops;
	}
	
	
	private Set<Service.Builder> readCalendarCSV() throws IOException {
		Set<Service.Builder> setBuilder = new HashSet<Service.Builder>();
		InputStream stream = getClass().getResourceAsStream(prefix + "calendar.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		String line;

		while ((line = reader.readLine()) != null) {
			String[] lineSplit = line.split(";");
			Service.Builder builder = new Service.Builder(lineSplit[0], decodeStringToDate(lineSplit[8]), decodeStringToDate(lineSplit[9]));
			addOperatingDays(builder, lineSplit);
			setBuilder.add(builder);
		}
		
		reader.close();
		stream.close();
		
		return setBuilder;
	}
	
	
	private void addCalendarDates (Set<Service.Builder> builders) throws IOException{
		InputStream stream = getClass().getResourceAsStream(prefix + "calendar_dates.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		String line;
		
		while ((line = reader.readLine()) != null) {
			String[] lineSplit = line.split(";");
			Date decodedDate = decodeStringToDate(lineSplit[1]);
			for (Service.Builder builder : builders) {
				if (builder.name().equals(lineSplit[0])) {
					if (lineSplit[2].equals("1"))
						builder.addIncludedDate(decodedDate);
					else 
						builder.addExcludedDate(decodedDate);
				}
			}
		}
		
		reader.close();
		stream.close();
	}
	
	private static void addOperatingDays(Service.Builder builder, String[] lineSplit) {
		for (int i = 1; i <= 7; ++i) {
			if (Integer.parseInt(lineSplit[i]) == 1)
				builder.addOperatingDay((DayOfWeek.values())[i-1]);
		}
	}
	
	private static Date decodeStringToDate(String string) {
		int day = Integer.parseInt(string.substring(6, 8));
		int month = Integer.parseInt(string.substring(4, 6));
		int year = Integer.parseInt(string.substring(0, 4));
		return new Date(day, month, year);
	}
}
