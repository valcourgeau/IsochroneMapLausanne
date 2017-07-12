package ch.epfl.isochrone.timetable;

import static ch.epfl.isochrone.math.Math.divF;
import static ch.epfl.isochrone.math.Math.modF;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
final class GraphEdge {
	private final Stop destination;
	private final int walkingTime;
	private final int[] packedTrips;
	
	/**
	 * 
	 * @param destination
	 * 						Arrêt de destination de l'arc du graphe.
	 * @param walkingTime
	 * 						Temps de marche entier en secondes pour atteindre l'arrêt de destination.
	 * @param packedTrips
	 * 						Set d'horaires de départ et d'arrivé compressés pour l'arrêt de destination.
	 * @throws IllegalArgumentException
	 * 						Si le temps de marche est strictement plus petit que -1.
	 */
	public GraphEdge(Stop destination, int walkingTime, Set<Integer> packedTrips) throws IllegalArgumentException {
		if (walkingTime < -1) 
			throw new IllegalArgumentException ("Illegal walkingTime");
		
		this.destination = destination;
		this.walkingTime = walkingTime;
		
		int[] temp = new int[packedTrips.size()];
		int i = 0;
		
		for (Integer packedTrip : packedTrips)
			temp[i++] = packedTrip;
		
		sort(temp);
		
		this.packedTrips = temp;
	}
	
	/**
	 * 
	 * @param departureTime
	 * 						Heure de départ à compresser.
	 * @param arrivalTime
	 * 						Heure d'arriver à compresser.
	 * @return
	 * 						Heure compresser correspondant aux deux entrés.
	 * @throws IllegalArgumentException
	 * 						Si l'heure de départ n'est pas dans l'intervalle ]0;10799[;
	 * 						Si l'heure d'arriver est antérieure à l'heure de départ;
	 * 						Si l'heure d'arriver est supérieure à l'heure de départ plus 9999 secondes = 2h 46min 39s.
	 */
	public static int packTrip(int departureTime, int arrivalTime) {
		if ((departureTime < 0 || departureTime > 107999) || (arrivalTime - departureTime < 0 || arrivalTime - departureTime > 9999))
			throw new IllegalArgumentException("Illegal departure/arrival time " + arrivalTime + " - " + departureTime);
		
		return (departureTime * 10000 + (arrivalTime - departureTime));
	}
	
	/**
	 * 
	 * @param packedTrip
	 * 					Heure compressée.
	 * @return
	 * 					Heure de départ extraite de l'heure compressée.
	 */
	public static int unpackTripDepartureTime (int packedTrip) {
		return divF(packedTrip, 10000);
	}
	
	/**
	 * 
	 * @param packedTrip
	 * 					Heure compressée.
	 * @return
	 * 					Durée du trajet.
	 */
	public static int unpackTripDuration (int packedTrip) {
		return modF(packedTrip, 10000);
	}
	
	/**
	 * 
	 * @param packedTrip
	 * 					Heure compressée.
	 * @return
	 * 					Heure d'arriver extraite de l'heure compressée.
	 */
	public static int unpackTripArrivalTime(int packedTrip) {
		return unpackTripDepartureTime(packedTrip) + unpackTripDuration(packedTrip);
	}
	
	/**
	 * 
	 * @return
	 * 			Destination de l'arc du graphe.
	 */
	public Stop destination() {
		return this.destination;
	}

	private int earliestNextPackedTripIndex (int departureTime) {
		int index = binarySearch(packedTrips, departureTime * 10000);
		
		return index < 0 ? abs(index + 1) : index;
	}

	/**
	 * 
	 * @param departureTime
	 * 						Heure de départ qui est l'heure minimum de départ du prochain trajet.
	 * @return
	 * 						Heure d'arriver du trajet partant après l'heure de départ et arrivant le plus tôt à la destination.
	 */
	public int earliestArrivalTime (int departureTime) {
		int indexOfPackedTrip;
		int walkingArrivalTime = walkingTime == -1 ? SecondsPastMidnight.INFINITE : departureTime + walkingTime;
		
		if(packedTrips.length == 0 || (indexOfPackedTrip = earliestNextPackedTripIndex(departureTime) )>= packedTrips.length )
			return walkingArrivalTime;
		
		int earliestArrivalTime = unpackTripArrivalTime(packedTrips[indexOfPackedTrip]);
		return min(walkingArrivalTime, earliestArrivalTime);	
	}
	
	/**
	 * 
	 * @author Virgile Neu (224138)
	 * @author Valentin Crougeau (225255)
	 * 
	 */
	public final static class Builder {
		private final Stop destination;
		private int walkingTime;
		private Set<Integer> packedTrips;
		
		/**
		 * 
		 * @param destination
		 * 					Arrêt de destination du l'arc en construction.
		 */
		public Builder(Stop destination) {
			this.destination = destination;
			this.walkingTime = -1;
			this.packedTrips = new HashSet<Integer>();
		}
		
		/**
		 * 
		 * @param newWalkingTime
		 * 						Nouveau temps de marche pour l'arrêt de Destination.
		 * @return
		 * 						Retourne le Builder lui-même pour permettre les appels chainés.
		 * @throws IllegalArgumentException
		 * 						Si le nouveau temps de marche est strictement inférieur à -1
		 */
		public GraphEdge.Builder setWalkingTime (int newWalkingTime) {
			if (newWalkingTime < 0 && newWalkingTime != -1) 
				throw new IllegalArgumentException ("Illegal WalkingTime");
			
			this.walkingTime = newWalkingTime;
			
			return this;
		}
		
		/**
		 * 
		 * @param departureTime
		 * 						Heure de départ du trajet à ajouter.
		 * @param arrivalTime
		 * 						Heure d'arriver du trajet à ajouter.
		 * @return
		 * 						Retourne le Builder lui-même pour permettre les appels chainés
		 * @throws IllegalArgumentException
		 						<br/>- Si l'heure de départ n'est pas dans l'intervalle ]0;10799[;
		 * 						<br/>- Si l'heure d'arrivée est antérieure à l'heure de départ;
		 * 						<br/>- Si l'heure d'arrivée est supérieure à l'heure de départ plus de 9999 secondes = 2h 46min 39s.
		 */
		public GraphEdge.Builder addTrip (int departureTime, int arrivalTime) {
			packedTrips.add(packTrip(departureTime, arrivalTime));
			
			return this;
		}
		
		/**
		 * 
		 * @return
		 * 			Arc de Graph créé à partir du builder.
		 */
		public GraphEdge build() {
			return new GraphEdge (destination, walkingTime, packedTrips);
		}
	}
}
