package ch.epfl.isochrone.timetable;

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
public class TestGraph {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        // Graph n'a aucune méthode publique à ce stade...

        Set<Stop> stops = null;
        Stop stop = null;
        Graph.Builder gb = new Graph.Builder(stops);
        gb.addTripEdge(stop, stop, 0, 0);
        gb.addAllWalkEdges(0, 0);
        gb.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddTripEdgeExceptionTestFromStop() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Stop stop2 = new Stop("stop2",new PointWGS84(1.0,1.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addTripEdge(stop2, stop1, 0, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddTripEdgeExceptionTestToStop() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Stop stop2 = new Stop("stop2",new PointWGS84(1.0,1.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addTripEdge(stop1, stop2, 0, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddTripEdgeExceptionTestNegDepartureTime() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Stop stop2 = new Stop("stop2",new PointWGS84(1.0,1.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1); stops.add(stop2);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addTripEdge(stop2, stop1, -1, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddTripEdgeExceptionTestNegArrivalTime() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Stop stop2 = new Stop("stop2",new PointWGS84(1.0,1.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1); stops.add(stop2);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addTripEdge(stop2, stop1, 0, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddTripEdgeExceptionTestArrivalBeforeDeparture() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Stop stop2 = new Stop("stop2",new PointWGS84(1.0,1.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1); stops.add(stop2);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addTripEdge(stop2, stop1, 3, 1);
    }
    /* test private
    @Test
    public void BuilderAddTripEdgeTest() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Stop stop2 = new Stop("stop2",new PointWGS84(1.0,1.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1); stops.add(stop2);
    	Graph.Builder builder = new Graph.Builder(stops);
    	GraphEdge.Builder GEB = new GraphEdge.Builder(stop2);
    	GEB.addTrip(0, 1);
    	builder.addTripEdge(stop1, stop2, 0, 1);
    	assertEquals(GEB.build().earliestArrivalTime(0), builder.mapGraphEdgeBuilder.get(stop1).get(stop2).build().earliestArrivalTime(0));
    }*/
    
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddWalkEdgeExceptionTestNegWalkingTime() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addAllWalkEdges(-1, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddWalkEdgeExceptionTestNullWalkingSpeedTime() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addAllWalkEdges(1, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void BuilderAddWalkEdgeExceptionTestNegWalkingSpeedTime() {
    	Stop stop1 = new Stop("stop1",new PointWGS84(0.0,0.0));
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	Graph.Builder builder = new Graph.Builder(stops);
    	builder.addAllWalkEdges(1, -1);
    }
    /* Test private
    @Test
    public void fastestPathTest(){
    	Map<Stop, List<GraphEdge>> map = new HashMap<>();
    	GraphEdge gE;
    	List<GraphEdge> list;
    	Set<Integer> pT;
    	
    	Stop stop1 = new Stop("stop1", new PointWGS84(0.2, 0.1));
    	Stop stop2 = new Stop("stop2_1", new PointWGS84(0.1, 0.4));
    	Stop stop3 = new Stop("stop3_1", new PointWGS84(0.3, 0.4));
    	Stop stop4 = new Stop("stop4_2", new PointWGS84(0.6, 0.4));
    	Stop stop5 = new Stop("stop5_ARRIVEE", new PointWGS84(0.9, 0.4));
    	
    	Set<Stop> stops = new HashSet<Stop>();
    	stops.add(stop1);
    	stops.add(stop2);
    	stops.add(stop3);
    	stops.add(stop4);
    	stops.add(stop5);
    	
    	//Stop 1 -> Stop 2
    	pT = new HashSet<Integer>();
    	list = new ArrayList<GraphEdge>();
    	pT.add(10002);
    	gE = new GraphEdge(stop2, 10, pT);
    	list.add(gE);
    	
    	
    	//Stop 1 -> Stop 4
    	pT = new HashSet<Integer>();
    	pT.add(70002);
    	gE = new GraphEdge(stop4, 10, pT);
    	list.add(gE);
    	
    	map.put(stop1, list);
    	
    	
    	//Stop 2 -> Stop 3
    	pT = new HashSet<Integer>();
    	list = new ArrayList<GraphEdge>();
    	pT.add(30002);
    	gE = new GraphEdge(stop3, 10, pT);
    	list.add(gE);
    	map.put(stop2, list);
    	
    	//Stop 3 -> Stop 5
    	pT = new HashSet<Integer>();
    	list = new ArrayList<GraphEdge>();
    	pT.add(100002);    	
    	gE = new GraphEdge(stop5, 10, pT);
    	list.add(gE);
    	map.put(stop3, list);
    	
    	//Stop 4 -> Stop 5
    	pT = new HashSet<Integer>();
    	list = new ArrayList<GraphEdge>();
    	pT.add(200002);
    	gE = new GraphEdge(stop5, 10, pT);
    	list.add(gE);
    	map.put(stop4, list);
    	
    	//TODO ATTENTION REMETTRE CONSTRUCTEUR EN PRIVE !
    	Graph graph = new Graph(stops, map);
    	FastestPathTree fpt = graph.fastestPaths(stop1, 0);
    	List<Stop> listStop = new ArrayList<Stop>();
    	listStop.add(stop1);
    	listStop.add(stop2);
    	listStop.add(stop3);
    	listStop.add(stop5);
    	
    	List<Stop> listOfPathTo = fpt.pathTo(stop5);
    	System.out.println(listOfPathTo);
    	assertEquals(listOfPathTo, listStop);
    }*/
}
