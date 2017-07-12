package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.Date.DayOfWeek;
import ch.epfl.isochrone.timetable.Date.Month;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */
public class TestTimeTable {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    
	@Test
    public void namesAreOk() {
        TimeTable t = new TimeTable(Collections.<Stop> emptySet(),
                Collections.<Service> emptySet());
        t.stops();
        t.servicesForDate(new Date(1, Month.JANUARY, 2000));

        TimeTable.Builder b = new TimeTable.Builder();
        b.addStop(new Stop("s", new PointWGS84(0, 0)));
        Date d = new Date(1, Month.APRIL, 2000);
        b.addService(new Service("s", d, d, Collections.<DayOfWeek> emptySet(),
                Collections.<Date> emptySet(), Collections.<Date> emptySet()));
        b.build();
    }
    
    
    @Test
    public void testStops() {
    	TimeTable t1 = new TimeTable(Collections.<Stop> emptySet(), Collections.<Service> emptySet());
    	assertEquals(t1.stops(), Collections.<Stop> emptySet());
    	HashSet<Stop> setStop = new HashSet<Stop>();
    	setStop.add(new Stop("s",new PointWGS84(0,0)));
    	TimeTable t2 = new TimeTable(setStop, Collections.<Service> emptySet());
    	assertEquals(t2.stops(), setStop);
    }
    
    @Test
    public void testServicesForDate() {
    	TimeTable t1 = new TimeTable(Collections.<Stop> emptySet(), Collections.<Service> emptySet());
    	
    	assertEquals(t1.servicesForDate(new Date(1,Month.JANUARY, 2000)), Collections.<Service> emptySet());
    	
    	HashSet<Service> setService = new HashSet<Service>();
    	HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
    	
    	days.add(DayOfWeek.MONDAY);
    	
    	Service s = new Service("s", new Date(1,1,1), new Date(1,1,100), days, new HashSet<Date>(), new HashSet<Date>());
    	
    	setService.add(s);
    	
    	TimeTable t2 = new TimeTable(Collections.<Stop> emptySet(), setService);
    	
    	
    	HashSet<Service> setService2 = new HashSet<Service>(setService);
    	setService2.retainAll(t2.servicesForDate(new Date(1,1,1)));
    	
    	assertTrue(setService2.containsAll(setService));
    }
    
    @Test
    public void testAddStop() {
    	TimeTable.Builder b = new TimeTable.Builder();
    	Stop s = new Stop("s",new PointWGS84(0,0));
    	b.addStop(s);
    	assertEquals(b,b.addStop(s));
    }
    
    @Test
    public void testAddService() {
    	TimeTable.Builder b = new TimeTable.Builder();
    	Service s = new Service("s",new Date(1,1,1), new Date(1,1,100), new HashSet<Date.DayOfWeek>(), new HashSet<Date>(), new HashSet<Date>());
    	b.addService(s);
    	assertEquals(b,b.addService(s));
    }
    
	@Test
    public void testBuild() {
    	HashSet<Service> setService = new HashSet<Service>();
    	HashSet<Stop> setStop = new HashSet<Stop>();
    	Stop stop = new Stop("stop", new PointWGS84(0,0));
    	setStop.add(stop);
    	
    	HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
    	days.add(DayOfWeek.MONDAY);
    	Service service = new Service("s", new Date(1,1,1), new Date(1,1,100), days, new HashSet<Date>(), new HashSet<Date>());
    	setService.add(service);
    	
    	TimeTable.Builder b = new TimeTable.Builder();
    	b.addStop(stop);
    	b.addService(service);
    	
    	TimeTable t = new TimeTable(setStop, setService);
    	assertEquals(t.servicesForDate(new Date(1,Month.JANUARY, 2000)),b.build().servicesForDate(new Date(1,Month.JANUARY, 2000)));
    	HashSet<Stop> setStop2 = new HashSet<Stop>(setStop);
    	setStop2.retainAll(t.stops());
    	assertEquals(setStop2,setStop);
    	assertEquals(setStop2,t.stops());
	}
}