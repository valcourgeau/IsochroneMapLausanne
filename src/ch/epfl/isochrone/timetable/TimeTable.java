package ch.epfl.isochrone.timetable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */

public final class TimeTable {
	private final Set<Stop> stops;
	private final Set<Service> services;
	
	/**
	 * 
	 * @param stops
	 * 				Liste des arrêt de la TimeTable.
	 * @param services
	 * 				Liste des services de la TimeTable.
	 */
	public TimeTable(Set<Stop> stops, Collection<Service> services) {
		this.stops = Collections.unmodifiableSet(new HashSet<Stop>(stops));
		this.services = Collections.unmodifiableSet(new HashSet<Service>(services));
	}
	
	/**
	 * 
	 * @return Retourne une copie non modifiable de la liste des arrêts.
	 */
	public Set<Stop> stops() {
		return stops;
	}
	
	/**
	 * 
	 * @param date
	 * 				Date pour laquelle on souhaite obtenir le set des services actifs.
	 * @return Retourne le set des services actifs pour la date passée en arguments.
	 */
	public Set<Service> servicesForDate(Date date) {
		Set<Service> temp = new HashSet<Service>();
		
		for(Service service : services){
			if(service.isOperatingOn(date))
				temp.add(service);
		}
		
		return temp;
	}
	
	 
	/**
	 * 
	 * @author Virgile Neu (224138)
	 * @author Valentin Courgeau (225255)
	 *
	 */
	public static final class Builder {
		private Set<Stop> stops;
		private Set<Service> services;
		
		public Builder(){
			stops = new HashSet<Stop>();
			services = new HashSet<Service>();
		}
		/**
		 * 
		 * @param newStop
		 * 				Arrêt à ajouter au builder.
		 * @return Retourne le builder modifier permettant les appel chaînnés.
		 */
		public Builder addStop(Stop newStop) {
			stops.add(newStop);
			
			return this;
		}
		
		/**
		 * 
		 * @param newService
		 * 				Service à ajouter au builder.
		 * @return Retourne le builder modifier permettant les appel chaînnés.
		 */
		public Builder addService(Service newService) {
			services.add(newService);
			
			return this;
		}
		
		/**
		 * 
		 * @return Retourne la TimeTable créer à partir du builder.
		 */
		public TimeTable build() {
			return new TimeTable(stops, services);
		}
	}
}
