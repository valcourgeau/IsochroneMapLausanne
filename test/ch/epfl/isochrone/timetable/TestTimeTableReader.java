package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.Date.DayOfWeek;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */
public class TestTimeTableReader {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() throws IOException {
        TimeTableReader r = new TimeTableReader("");
        TimeTable t = r.readTimeTable();
        Graph g = r.readGraphForServices(t.stops(), Collections.<Service>emptySet(), 0, 0d);
        System.out.println(g); // Evite l'avertissement que g n'est pas utilisé
    }
 
    //Test Private
    /*@Test 
    public void readStopsCSVTest() {
    	PointWGS84 titi = new PointWGS84(Math.toRadians(46.5367366879),Math.toRadians(6.58201906962));
    	Set<Stop> stops = new HashSet<Stop>();
    	Stop stop1 = new Stop("1er Août", titi);
    	Stop stop2 = new Stop("1er Mai", new PointWGS84(Math.toRadians(46.5407686803),Math.toRadians(6.58344370604)));
    	stops.add(stop1); stops.add(stop2);
    	
    	TimeTableReader toto = new TimeTableReader("/time-table/");
    	int i = 0;
    	try {
			Set<Stop> finalStops = toto.readStopsCSV();
			for(Stop stop : finalStops)
				if(stop.name().equals("1er Août") || stop.name().equals("1er Mai"))
					i++;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	assertEquals(2, i);
			
		
    }
    */
    
  /*  @Test test private
    public void decodeStringToDateTest() {
    	String s = "20130321";
    	assertEquals(new Date(21,03,2013), TimeTableReader.decodeStringToDate(s));
    }
    */
    
    @Test
    public void readGraphForServicesTest(){
		Stop stop1 = new Stop("1er Août", new PointWGS84(Math.toRadians(46.5367366879),Math.toRadians(6.58201906962)));
		Stop stop2 = new Stop("Avenir", new PointWGS84(Math.toRadians(46.5383398663),Math.toRadians(6.58346251012)));
    	
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	stops.add(stop2);
    	
    	Set<Service> services = new HashSet<>();
    	//35 2014_10V-VHU-Semaine-10;1;1;1;1;1;0;0;20131223;20140103
    	Set<Date.DayOfWeek> opD = new HashSet<>();
    	opD.add(DayOfWeek.MONDAY); opD.add(DayOfWeek.TUESDAY); opD.add(DayOfWeek.THURSDAY); 
    	opD.add(DayOfWeek.FRIDAY); opD.add(DayOfWeek.WEDNESDAY);
    	Set<Date> exD = new HashSet<>();
    	exD.add(new Date(14,12,2013));
    	Set<Date> inD = new HashSet<>();
    	inD.add(new Date(15,12,2013));
    	Service serv1 = new Service("2014_10V-VHU-Semaine-10", new Date(13, 12, 2013), new Date(01, 03, 2014), opD, exD, inD);
    	services.add(serv1);
    	
    	TimeTableReader tTR = new TimeTableReader("/time-table/");
    	Graph toto = null;
    	try {
			toto = tTR.readGraphForServices(stops, services, 300, 1.25);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//653 2014_10V-VHU-Semaine-10;1er Août;24660;Avenir;24720

    	FastestPathTree fPT = toto.fastestPaths(stop1, 24660);
    	assertEquals(fPT.arrivalTime(stop2), 24720);
    }
    
    @Test
	public void testReadTimeTable(){
		TimeTableReader reader = new TimeTableReader("/time-table/");
		TimeTable timetable = null;
		
		try{
			timetable = reader.readTimeTable();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Date interestingDate = new Date(13, 9, 2013);
		Set<Service> timetableServicesFor13092013 = timetable.servicesForDate(interestingDate);
		Set<String> serviceNames = new HashSet<>();
		
		{
			//Ajout des services actifs le VENDREDI 13 / 09 / 2013 :
			serviceNames.add("2013-SHU-Semaine-51");
			serviceNames.add("2013-SHU-Semaine-51-0000100");
			serviceNames.add("2013-SHU-Semaine-51-1101100");
			serviceNames.add("2013-SHU-Semaine-51-0001100");
		}
		
		int i = 0;
		for(Service service : timetableServicesFor13092013){
			if(serviceNames.contains(service.name())){
				assertTrue(service.isOperatingOn(interestingDate));
				i++;
			}
		}
		
		assertEquals(4, i);
		
		Date thrillingDate = new Date(14, 10, 2013);
		Set<Service> timetableServicesFor14102013 = timetable.servicesForDate(thrillingDate);
		Set<String> serviceNames2 = new HashSet<>();
		
		{
			//Ajout des services inactifs exceptionnelement ET non habituellement le LUNDI 14 / 10 / 2013 :
			serviceNames2.add("2013-SU-Semaine-50");
			serviceNames2.add("2013-SU-Semaine-50-1111000");
			serviceNames2.add("2013-SU-Semaine-50-1101000");
		}
		
		int j = 0;
		
		for(Service service : timetableServicesFor14102013){
			if(!serviceNames2.contains(service.name()))
				j++;
		}
		
		assertEquals(3, j);
	}
	
	//Test Private
    /*
	@Test
	public void decodeStringToDateTest(){
		Date date = TimeTableReader.decodeStringToDate("20130913");
		Date date2 = new Date(13, 9, 2013);
		assertEquals(date, date2);
	}*/
}
