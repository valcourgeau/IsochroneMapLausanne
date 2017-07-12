package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */
public class TestGraphEdge {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        int i1 = GraphEdge.packTrip(0, 0);
        i1 = GraphEdge.unpackTripDepartureTime(0);
        i1 = GraphEdge.unpackTripDuration(0);
        i1 = GraphEdge.unpackTripArrivalTime(0) + i1;
        Stop s = null;
        GraphEdge e = new GraphEdge(s, 0, Collections.<Integer>emptySet());
        s = e.destination();
        i1 = e.earliestArrivalTime(0);

        GraphEdge.Builder b = new GraphEdge.Builder(s);
        b.setWalkingTime(0);
        b.addTrip(0, 0);
        e = b.build();
    }
    /* Test Private
    @Test
    public void testEarliestNextPackedTrip() {
    	Stop stop = new Stop("stop", new PointWGS84(0,0));
    	Set<Integer> packedTrips = new HashSet<Integer>();
    	packedTrips.add(10000);
    	GraphEdge GE = new GraphEdge(stop, 10, packedTrips);
    	assertEquals(GE.earliestNextPackedTripIndex(0), 0);
    	assertEquals(GE.earliestNextPackedTripIndex(10000), 1);    	
    }*/
    
    @Test(expected = IllegalArgumentException.class)
    public void packTripConstructorExceptionArrivalBeforeDepartureTest(){
    	GraphEdge.packTrip(2, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void packTripConstructorExceptionNegativeDepartureTest(){
    	GraphEdge.packTrip(-2, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void packTripConstructorExceptionTooBigDepartureTest(){
    	GraphEdge.packTrip(108000, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void packTripConstructorExceptionArrivalLateAfterDepartureTest(){
    	GraphEdge.packTrip(1, 10001);
    }
    
    @Test
    public void packTripCalculTest() {
    	assertEquals(GraphEdge.packTrip(1,2),10001);
    }
    
    @Test
    public void unpackTripDepartureTimeTest() {
    	assertEquals(1, GraphEdge.unpackTripDepartureTime(10002));
    }
    
    @Test
    public void unpackTripDurationTimeTest() {
    	assertEquals(1, GraphEdge.unpackTripDuration(10001));
    }
    
    @Test
    public void unpackTripArrivalTime() {
    	assertEquals(2, GraphEdge.unpackTripArrivalTime(GraphEdge.packTrip(1,2)));
    }
    
    @Test
    public void earliestArrivalTimeTest() {
    	Stop s = new Stop("s", new PointWGS84(0,0));
    	GraphEdge.Builder builder = new GraphEdge.Builder(s);
    	builder.setWalkingTime(10);
    	builder.addTrip(11, 35);
    	builder.addTrip(7, 10);
    	builder.addTrip(3, 5);
    	builder.addTrip(1, 3);
    	GraphEdge gE = builder.build();
    	assertEquals(5 , gE.earliestArrivalTime(2));
    	assertEquals(5 , gE.earliestArrivalTime(3));
    	assertEquals(3 , gE.earliestArrivalTime(1));
    	assertEquals(21, gE.earliestArrivalTime(11));
    }
    
    @Test
    public void earliestArrivalTimeInfiniteTest() {
    	Stop s = new Stop("s", new PointWGS84(0,0));
    	Set<Integer> pT = new HashSet<Integer>();
    	pT.add(10002);
    	pT.add(30002);
    	pT.add(50002);
    	pT.add(70002);
    	pT.add(110002);
    	pT.add(130002);
    	GraphEdge gE = new GraphEdge(s, -1, pT);
    	assertEquals(SecondsPastMidnight.INFINITE , gE.earliestArrivalTime(14));	
    }
    
    @Test(expected = IllegalArgumentException.class)
	public void setWalkingTimeExceptionTest(){
		GraphEdge.Builder builder = new GraphEdge.Builder(null);
		
		builder.setWalkingTime(-3);
	}
	
	@Test
	public void builderBuildTest(){
		Stop stop = new Stop("lol", new PointWGS84(0.2,0.3));
		GraphEdge.Builder builder = new GraphEdge.Builder(stop);
		builder.setWalkingTime(1000);
		builder.addTrip(1000, 1020);
		builder.addTrip(1010, 1012);
		
		GraphEdge edge = builder.build();
		Set<Integer> packedTrips = new HashSet<Integer>();
		packedTrips.add(GraphEdge.packTrip(1000, 1020));
		packedTrips.add(GraphEdge.packTrip(1010, 1012));
		GraphEdge graphEdge = new GraphEdge(stop, 1000, packedTrips);
		assertEquals(graphEdge.earliestArrivalTime(1010), edge.earliestArrivalTime(1010));
	}
}
