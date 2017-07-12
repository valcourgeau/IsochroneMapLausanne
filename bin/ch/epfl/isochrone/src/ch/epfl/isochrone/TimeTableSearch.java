package ch.epfl.isochrone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

import static ch.epfl.isochrone.timetable.Stop.getStopFromName;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public class TimeTableSearch {

	/**
	 * 
	 * @param args 	Tableau de string passé lors de l'execution qui contient le nom, la date et l'heure dans le format suivant :  nom aaaa-mm-jj hh:mm:ss
	 */
	public static void main(String[] args) {
		String startingStopName = args[0];
		int startingYear  = Integer.parseInt((args[1].split("-"))[0]);
		int startingMonth = Integer.parseInt((args[1].split("-"))[1]);
		int startingDay   = Integer.parseInt((args[1].split("-"))[2]);
		int startingHour  = Integer.parseInt((args[2].split(":"))[0]);
		int startingMin   = Integer.parseInt((args[2].split(":"))[1]);
		int startingSec   = Integer.parseInt((args[2].split(":"))[2]);
		
		int departureTime = SecondsPastMidnight.fromHMS(startingHour, startingMin, startingSec);
		
		Date startingDate = new Date(startingDay, startingMonth, startingYear);
		
		int maxWalkingTime = 300;
		double walkingSpeed = 1.25;
		

		TimeTableReader reader = new TimeTableReader("/time-table/");
		TimeTable timeTable = null;
		try {
			timeTable = reader.readTimeTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Graph graph = null;
		try {
			graph = reader.readGraphForServices(timeTable.stops(), timeTable.servicesForDate(startingDate), maxWalkingTime, walkingSpeed);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		Stop startingStop = getStopFromName(timeTable.stops(), startingStopName);
		FastestPathTree fastestPathTree = graph.fastestPaths(startingStop, departureTime);
		List<String> alphabeticalOrder = new ArrayList<String>();
		
		for (Stop stop : fastestPathTree.stops())
			alphabeticalOrder.add(stop.name());
		
		Collections.sort(alphabeticalOrder);
		
		for (String stopName : alphabeticalOrder) {
			Stop arrivalStop = getStopFromName(fastestPathTree.stops(), stopName);
			System.out.println(stopName +" : "+ SecondsPastMidnight.toString(fastestPathTree.arrivalTime(arrivalStop)));
			System.out.println("\t via: " + fastestPathTree.pathTo(arrivalStop));
		}
		
	}
}	
