package ch.epfl.isochrone.timetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class Graph {
	private final Set<Stop> stops;
	private final Map<Stop, List<GraphEdge>> outgoingEdges;
	
	// Constructeur privé, les unmodifiableSet et unmodifiableMap sont dans le Builder (instructions des assistants).
	private Graph(Set<Stop> stops, Map<Stop, List<GraphEdge>> outgoingEdges) {
		this.stops = stops;
		this.outgoingEdges = outgoingEdges;
	}
	/**
	 * 
	 * @param startingStop
	 * 						Stop de départ pour le calcul des plus courts trajets avec l'algorithme de Dijkstra. 
	 * @param departureTime
	 * 						Heure de départ pour le calcul des plus courts trajets avec Dijkstra.
	 * @return
	 * 						L'arbre des plus courts trajets avec comme racine le stop de départ à l'heure de départ
	 * 						et à chaques feuilles un stop avec son arrivé la plus proche.
	 */
	public FastestPathTree fastestPaths(Stop startingStop, int departureTime) {
		if (departureTime < 0 || !(stops.contains(startingStop)))
			throw new IllegalArgumentException("Illegal Argument in fastestPaths (stop or departureTime");
		
		final FastestPathTree.Builder pathBuilder = new FastestPathTree.Builder(startingStop, departureTime);
		
		PriorityQueue<Stop> priorityQueue = new PriorityQueue<Stop>(stops.size(), new Comparator<Stop>(){
			
			@Override
			public int compare(Stop stop1, Stop stop2) {
				return Integer.compare(pathBuilder.arrivalTime(stop1), pathBuilder.arrivalTime(stop2));
			}});
			
		priorityQueue.addAll(stops);

		while(!(priorityQueue.isEmpty())) {
			Stop closestStop = priorityQueue.remove();
					
			if (pathBuilder.arrivalTime(closestStop) == SecondsPastMidnight.INFINITE)
				break;
			
			for (GraphEdge graphEdge : outgoingEdges.get(closestStop)) {
				if (graphEdge.destination() != null) {//TODO correction efficace?
					Stop graphEdgeDestination = graphEdge.destination();
					int edgeArrivalTime = graphEdge.earliestArrivalTime(pathBuilder.arrivalTime(closestStop));
						
					if (edgeArrivalTime < pathBuilder.arrivalTime(graphEdgeDestination)) {
						priorityQueue.remove(graphEdgeDestination);
						pathBuilder.setArrivalTime(graphEdgeDestination, edgeArrivalTime, closestStop);
						priorityQueue.add(graphEdgeDestination);
					}
				}
			}
		}

		return pathBuilder.build();
	}
	
	
	/**
	 * 
	 * @author Virgile Neu (224138)
	 * @author Valentin Courgeau (225255)
	 * 
	 */
	public final static class Builder {
		private final Set<Stop> stops;
		private Map<Stop, Map<Stop, GraphEdge.Builder>> mapGraphEdgeBuilder = new HashMap<Stop, Map<Stop, GraphEdge.Builder>>();
		
		/**
		 * 
		 * @param stops
		 * 				Set initial de Stop passé au builder manipulables par les méthodes.
		 */
		public Builder(Set<Stop> stops) {
			this.stops = Collections.unmodifiableSet(stops);
		}
		
		private GraphEdge.Builder getGraphEdgeBuilder(Stop fromStop, Stop toStop) {
			if (mapGraphEdgeBuilder.get(fromStop) == null) 
				mapGraphEdgeBuilder.put(fromStop, new HashMap<Stop, GraphEdge.Builder>());
			
			if (mapGraphEdgeBuilder.get(fromStop).get(toStop) == null)
				mapGraphEdgeBuilder.get(fromStop).put(toStop, new GraphEdge.Builder(toStop));

			return (mapGraphEdgeBuilder.get(fromStop)).get(toStop);
		}
		
		/**
		 * 
		 * @param fromStop
		 * 					Arrêt de départ du trajet à ajouter.
		 * @param toStop
		 * 					Arrêt d'arriver du trajet à ajouter.
		 * @param departureTime
		 * 					Heure de depart du trajet à ajouter en SecondsPastMidnight. 
		 * @param arrivalTime
		 * 					Heure d'arriver du trajet à ajouter en SecondsPastMidnight.
		 * @return			
		 * 					Retourne le Builder lui-même pour permettre les appel chainés.
		 * @throws IllegalArgumentException
		 * 					Si : <br/>- au moins l'un des deux arrêt ne fait pas parti des Stop passés dans le constructeur;
		 * 						 <br/>- au moins l'un des deux horaires est négatif;
		 * 						 <br/>- l'horaire d'arrivée est antérieure à celle de départ.
		 */
		public Builder addTripEdge(Stop fromStop, Stop toStop, int departureTime, int arrivalTime) throws IllegalArgumentException {
			if (!(stops.contains(fromStop)) || !(stops.contains(toStop)) || departureTime < 0 || arrivalTime < 0 || arrivalTime < departureTime) 
				throw new IllegalArgumentException("invalid argument for addTripEdge");
			
			getGraphEdgeBuilder(fromStop, toStop).addTrip(departureTime, arrivalTime);
			
			return this;
		}
		
		/**
		 * 
		 * @param maxWalkingTime
		 * 						Temps de marche maximal autorisé.
		 * @param walkingSpeed
		 * 						Vitesse de marche.
		 * @return
		 * 						Retourne le Builder lui-même pour permettre les appels chainés.
		 * @throws IllegalArgumentException
		 * 						Si le temps de marche maximal est négatif ou si la vitesse est négative ou nulle.
		 */
		public Builder addAllWalkEdges (int maxWalkingTime, double walkingSpeed) throws IllegalArgumentException {
			if (maxWalkingTime < 0 || walkingSpeed <= 0)
				throw new IllegalArgumentException("invalid walking");
			
			ArrayList<Stop> stopsArray = new ArrayList<Stop>(stops);
			
			for (int i = 0; i < stops.size(); i++) {
				for (int j = 0 ; j < stops.size(); j++) {
					if (i!=j) {
						int timeToWalk = (int) Math.round((stopsArray.get(i).position().distanceTo(stopsArray.get(j).position())) / walkingSpeed);
	
						if (timeToWalk <= maxWalkingTime)
							getGraphEdgeBuilder(stopsArray.get(i), stopsArray.get(j)).setWalkingTime(timeToWalk);
					}
				}
			}
			
			return this;
		}

		/**
		 * 
		 * @return
		 * 			Le Graph créé à l'aide du Builder.
		 */
		public Graph build() {
			Map<Stop, List<GraphEdge>> outgoingEdges = new HashMap<Stop, List<GraphEdge>>();
			List<GraphEdge> graphEdgeList;
			
			for (Stop s1 : mapGraphEdgeBuilder.keySet()) {
				graphEdgeList = new ArrayList<GraphEdge>();
				
				for (Stop s2 : mapGraphEdgeBuilder.get(s1).keySet())
					graphEdgeList.add(mapGraphEdgeBuilder.get(s1).get(s2).build());
				
				outgoingEdges.put(s1, graphEdgeList);
			}
			
			return new Graph(stops, Collections.unmodifiableMap(new HashMap<Stop, List<GraphEdge>>(outgoingEdges)));
		}
	}
}
