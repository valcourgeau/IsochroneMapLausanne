package ch.epfl.isochrone.timetable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */
public final class FastestPathTree {
	private final Stop startingStop;
	private final Map<Stop, Integer> arrivalTime;
	private final Map<Stop, Stop> predecessor;
	
	/**
	 * 
	 * @param startingStop
	 * 						Arret de départ de l'arbre.
	 * @param arrivalTime
	 * 						Map qui a pour clés les differents Stop visités lors des différents trajets, et pour value les heures d'arrivés à ces dits Stop,
	 * 						en SecondsPastMidnight
	 * @param predecessor
	 * 						Map qui a pour clé un Stop s0, et en value le Stop précédent à ce Stop s0.
	 * @throws IllegalArgumentException
	 * 									Lance l'exception lors que l'ensemble des Stop dont on connait les heures d'arrivées n'est pas égal à l'ensemble
	 * 									des Stop dont on connait les prédécesseurs.
	 */
	public FastestPathTree(Stop startingStop, Map<Stop, Integer> arrivalTime, Map<Stop, Stop> predecessor) throws IllegalArgumentException{
		Set<Stop> predecessorKeysExtended = new HashSet<Stop>(predecessor.keySet());
		predecessorKeysExtended.add(startingStop);
	
		if (!(arrivalTime.keySet().equals(predecessorKeysExtended))) 
			throw new IllegalArgumentException("invalid keyMapping");
		
		
		this.startingStop = startingStop;
		this.arrivalTime = Collections.unmodifiableMap(new HashMap<Stop, Integer>(arrivalTime));
		this.predecessor = Collections.unmodifiableMap(new HashMap<Stop, Stop>(predecessor));
	}
	
	/**
	 * 
	 * @return	Retourne le Stop de départ.
	 * 			
	 */
	public Stop startingStop() {
		return startingStop;
	}
	
	/**
	 * 
	 * @return Retourne l'heure de départ.
	 */
	public int startingTime() {
		return arrivalTime(startingStop);
	}
	
	/**
	 * 
	 * @return Retourne le Set des Stop parcourus lors du trajet.
	 */
	public Set<Stop> stops() {
		return Collections.unmodifiableSet(new HashSet<Stop>(arrivalTime.keySet()));
	}
	
	/**
	 * 
	 * @param stop
	 * 				Stop duquel on veut connaitre le temps d'arrivée.
	 * @return	Retourne le temps d'arrivée au Stop passé en argument, stop.
	 */
	public int arrivalTime(Stop stop) {
		if (arrivalTime.get(stop) != null)
			return arrivalTime.get(stop);
		
		return SecondsPastMidnight.INFINITE;
	}
	
	/**
	 * 
	 * @param stop
	 * 				Stop d'arrivée du trajet depuis startingStop.
	 * @return Retourne la Liste de Stop parcourus lors du trajet startingStop jusqu'à stop.
	 * @throws IllegalArgumentException
	 * 									Lance l'exception lorsque stop n'appartient pas à l'ensemble des Stop dont connait l'heure d'arrivée,
	 * 									c'est-à-dire lorsqu'il n'appartient pas à l'ensemble des Stop parcourus.
	 */
	public List<Stop> pathTo(Stop stop) throws IllegalArgumentException {
		if (!arrivalTime.keySet().contains(stop))
	 		throw new IllegalArgumentException("Stop not in path");
	 	
	 	LinkedList<Stop> path = new LinkedList<Stop>();	 	
	 	
	 	for (Stop stopTemp = stop; stopTemp != null; stopTemp = predecessor.get(stopTemp)) 
	 		path.addFirst(stopTemp);
	 	
	 	return path;
	}
	
	/**
	 * @author Virgile Neu (224138)
	 * @author Valentin Courgeau (225255)
	 *
	 */
	public static final class Builder {
		private Stop startingStop;
		private Map<Stop, Integer> arrivalTime;
		private Map<Stop, Stop> predecessor;
		
		/**
		 * 
		 * @param startingStop	
		 * 						Arret de départ de l'arbre à construire.
		 * @param startingTime
		 * 						Map qui a pour clés les differents Stop visités lors des différents trajets de l'arbre à construire,
		 * 						et pour value les heures d'arrivés à ces dits Stop, en SecondsPastMidnight.
		 * @throws IllegalArgumentException
		 * 									Lance l'exception lors que l'ensemble des Stop dont on connait les heures d'arrivées n'est pas égal à l'ensemble
		 * 									des Stop dont on connait les prédécesseurs.
		 */
		public Builder(Stop startingStop, int startingTime) throws IllegalArgumentException {
			if (startingTime < 0) throw new IllegalArgumentException("negative startingTime");
			this.startingStop = startingStop;
			
			this.arrivalTime = new HashMap<Stop, Integer>();
			arrivalTime.put(this.startingStop, startingTime);
			
			this.predecessor = new HashMap<Stop, Stop>();
		}
		
		/**
		 * 
		 * @param stop
		 * 				Stop duquel on veut modifier l'heure d'arrivée.
		 * @param time
		 * 				Heure d'arrivée à mettre pour stop. 
		 * @param predecessor
		 * 					Stop de provenance. Permet donc de préciser que l'on parle de l'heure d'arrivée du trajet predecessor jusqu'à stop.
		 * @return	
		 * 			Retourne le Builder lui-même afin de pouvoir faire des appels chaînés.
		 * @throws IllegalArgumentException
		 * 									Lance l'exception lorsque l'heure d'arrivée indiquée est strictement inférieure à celle déjà
		 * 									paramétrée.
		 */
		//TODO
		public Builder setArrivalTime(Stop stop, int time, Stop predecessor) throws IllegalArgumentException {
			if ( time < arrivalTime.get(startingStop))
				throw new IllegalArgumentException("time < startingTime");
			
			this.arrivalTime.put(stop, time);
			this.predecessor.put(stop, predecessor);
			
			return this;
		}
		
		/**
		 * 
		 * @param stop
		 * 				Stop dont on veut connaitre l'heure d'arrivée.
		 * @return 
		 * 				Retourne l'heure d'arrivée au stop passé en argument.
		 * 
		 */
		public int arrivalTime(Stop stop) {
			if (!arrivalTime.containsKey(stop)) 
				return SecondsPastMidnight.INFINITE;
			
			return arrivalTime.get(stop);
		}
		
		/**
		 * 
		 * @return 
		 * 			Retourne un FastestPathTree à l'aide du Stop startingStop de départ, de la Map arrivalTime, et de la Map predecessor.
		 */
		public FastestPathTree build() {
			return new FastestPathTree(startingStop, arrivalTime, predecessor);
		}
	}
}
