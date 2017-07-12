package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class TestFastestPathTree {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Stop stop = null;
        Map<Stop, Integer> arrivalTimes = null;
        Map<Stop, Stop> predecessors = null;
        FastestPathTree f = new FastestPathTree(stop, arrivalTimes, predecessors);
        Stop s = f.startingStop();
        int i = f.startingTime();
        Set<Stop> ss = f.stops();
        i = f.arrivalTime(stop);
        List<Stop> p = f.pathTo(stop);
        System.out.println("" + s + i + ss + p);

        FastestPathTree.Builder fb = new FastestPathTree.Builder(stop, 0);
        fb.setArrivalTime(stop, 0, stop);
        i = fb.arrivalTime(stop);
        f = fb.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void fastestPathTreeConstructorExceptionMissingPredecessorStop(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("debat", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("debzt", new PointWGS84(0.1, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, 1);
    	arrivalTime.put(stop2, 2);
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop2, stop1);
    	
    	
    	@SuppressWarnings("unused")
		FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    }
    
    @Test
    public void testStartingStop(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("debat", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("debzt", new PointWGS84(0.1, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, 1);
    	arrivalTime.put(stop2, 2);
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop2, stop1);
    	predecessor.put(stop1, startingStop);
    	
    	FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    	
    	assertEquals(startingStop, testTree.startingStop());
    }
    
    @Test
    public void testStartingTime(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("debat", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("debzt", new PointWGS84(0.1, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, 1);
    	arrivalTime.put(stop2, 2);
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop2, stop1);
    	predecessor.put(stop1, startingStop);
    	
    	FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    	
    	assertEquals(0, testTree.startingTime());
    }
    
    @Test
    public void testStops(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("debat", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("debzt", new PointWGS84(0.1, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, null);
    	arrivalTime.put(stop2, 2);
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop2, stop1);
    	predecessor.put(stop1, startingStop);
    	
    	FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    	
    	Set<Stop> temp = new HashSet<Stop>();
    	temp.add(stop2);
    	temp.add(stop1);
    	temp.add(startingStop);
    	assertEquals(temp, testTree.stops());
    }
    
    @Test
    public void testArrivalTime(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("debat", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("debzt", new PointWGS84(0.1, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, 1);
    	arrivalTime.put(stop2, 2);
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop2, stop1);
    	predecessor.put(stop1, startingStop);
    	
    	FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    	
    	assertEquals(testTree.arrivalTime(stop2), 2);
    	assertEquals(testTree.arrivalTime(new Stop("Rockcreek Park", new PointWGS84(0.2,0.3))), SecondsPastMidnight.INFINITE);
    }
    
    @Test
    public void testPathTo(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("stop1-", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("stop2-", new PointWGS84(0.1, 0.4));
    	Stop stop3 = new Stop("stop3-", new PointWGS84(0.3, 0.4));
    	Stop stop4 = new Stop("stop4", new PointWGS84(0.6, 0.4));
    	Stop stop5 = new Stop("stop5-", new PointWGS84(0.9, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, 1);
    	arrivalTime.put(stop2, 2);
    	arrivalTime.put(stop3, 4);
    	arrivalTime.put(stop4, 5);
    	arrivalTime.put(stop5, 80);
    	
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop5, stop3);
    	predecessor.put(stop4, stop2);
    	predecessor.put(stop3, stop2);
    	predecessor.put(stop2, stop1);
    	predecessor.put(stop1, startingStop);
    	
    	
    	FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    	List<Stop> temp = new ArrayList<>();
    	temp.add(startingStop);
    	temp.add(stop1);
    	temp.add(stop2);
    	temp.add(stop3);
    	temp.add(stop5);
    	
    	List<Stop> temp2 = new ArrayList<>();
    	temp2.add(startingStop);
    	
    	assertEquals(temp2, testTree.pathTo(startingStop));
    	assertEquals(temp, testTree.pathTo(stop5));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPathToExceptionStopMissing(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("stop1-", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("stop2-", new PointWGS84(0.1, 0.4));
    	Stop stop3 = new Stop("stop3-", new PointWGS84(0.3, 0.4));
    	
    	Map<Stop, Integer> arrivalTime = new HashMap<>();
    	arrivalTime.put(startingStop, 0);
    	arrivalTime.put(stop1, 1);
   
    	Map<Stop, Stop> predecessor = new HashMap<>();
    	predecessor.put(stop2, stop1);
    	predecessor.put(stop1, startingStop);
    	
    	FastestPathTree testTree = new FastestPathTree(startingStop, arrivalTime, predecessor);
    	
    	testTree.pathTo(stop3);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderExceptionNegativeStartingTime(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	
    	@SuppressWarnings("unused")
		FastestPathTree.Builder builder = new FastestPathTree.Builder(startingStop, -5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetArrivalTimeBuilderExceptionBadNewArrivalTime(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("stop1-", new PointWGS84(0.2, 0.1));
    	
    	int startingTime = 2;
    	
    	FastestPathTree.Builder builder = new FastestPathTree.Builder(startingStop, startingTime);
    	builder.setArrivalTime(startingStop, 1, stop1);
    }
    
    @Test
    public void testArrivalTimeBuilder(){
    	Stop startingStop = new Stop("debut", new PointWGS84(0.1, 0.1));
    	Stop stop1 = new Stop("stop1-", new PointWGS84(0.2, 0.1));
    	
    	int startingTime = 2;
    	
    	FastestPathTree.Builder builder = new FastestPathTree.Builder(startingStop, startingTime);
    	assertEquals(builder.arrivalTime(stop1), SecondsPastMidnight.INFINITE);
    	assertEquals(builder.arrivalTime(startingStop), startingTime);
    }
    
    
}
