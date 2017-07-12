package ch.epfl.isochrone.timetable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */

public final class Service {
	private final String name;
	private final Date startingDate;
	private final Date endingDate;
	private final Set<Date.DayOfWeek> operatingDays;
	private final Set<Date> excludedDates;
	private final Set<Date> includedDates;
	
	/**
	 * 
	 * @param name
	 * 				Nom à donner à l'instance.
	 * @param startingDate
	 * 				Date de début du service.	
	 * @param endingDate
	 * 				Date de fin du service.
	 * @param operatingDays
	 * 				Set de jours de la semaine où le service sera habituellement opérant.
	 * @param excludedDates
	 * 				Set de dates exclues exceptionnelement.
	 * @param includedDates
	 * 				Set de dates incluses exceptionnelement.
	 * @throws IllegalArgumentException Lance l'exception dans les cas où: <br/>- La date de début ou de fin n'est pas définie; <br/>
	 * 									- La date de fin est antérieure à la date de début; <br/>- Si l'intersection des ensembles de dates incluses et excluses n'est pas vide; <br/>
	 * 									- Si les dates incluses ne sont pas toutes dans l'intervalle [date de début, date de fin]; <br/>
	 * 									- Si les dates excluses ne sont pas toutes dans l'intervalle [date de début, date de fin]; <br/>
	 */
	public Service(String name, Date startingDate, Date endingDate, Set<Date.DayOfWeek> operatingDays, Set<Date> excludedDates, Set<Date> includedDates){
		if (startingDate == null || endingDate == null || startingDate.compareTo(endingDate) == 1  || haveDatesInCommon(includedDates, excludedDates) || !SetInBetween(includedDates, startingDate, endingDate) || !SetInBetween(excludedDates, startingDate, endingDate))
			throw new IllegalArgumentException("Dates invalides");
		
		this.name = name;
		this.startingDate = startingDate;
		this.endingDate = endingDate;
		this.operatingDays = Collections.unmodifiableSet(new HashSet<Date.DayOfWeek>(operatingDays));
		this.excludedDates = Collections.unmodifiableSet(new HashSet<Date>(excludedDates));
		this.includedDates = Collections.unmodifiableSet(new HashSet<Date>(includedDates));
	}

	private boolean haveDatesInCommon(Set<Date> dateSet1, Set<Date> dateSet2){
		if(dateSet1 == null || dateSet2 == null || dateSet1.isEmpty() || dateSet2.isEmpty()) 
			return false;
		
		for(Date date : dateSet2){
			if(dateSet1.contains(date))
				return true;
		}
		
		return false;
	}
	
	private boolean SetInBetween(Set<Date> set1, Date startingDate, Date endingDate){
		if(set1 == null || set1.isEmpty())
			return true;
		
		for(Date date : set1){
			if(!DateInInterval(date, startingDate, endingDate))
				return false;
		}
		
		return true;
	}
	
	private static boolean DateInInterval(Date date, Date startingDate, Date endingDate){
		return !(date == null || startingDate.compareTo(date) == 1 || endingDate.compareTo(date) == -1);
	}
	
	private boolean isInOperatingDays(Date date){
		if(date == null || operatingDays == null || operatingDays.isEmpty())
			return false;
		
		return operatingDays.contains(date.dayOfWeek());
	}
	
	private boolean isInExcludedDates(Date date){
		if(date == null || excludedDates == null || excludedDates.isEmpty())
			return false;
		
		return excludedDates.contains(date);
	}
	
	private boolean isInIncludedDates(Date date){
		if(date == null || includedDates == null || includedDates.isEmpty())
			return false;
	
		return includedDates.contains(date);
	}
	
	/**
	 * 
	 * @return Retourne le nom du service.
	 */
	public String name() {
		return name;
	}
	
	/**
	 * 
	 * @param date
	 * 				Date pour laquelle on veut savoir si le service op�re.
	 * @return Retourne True si le service opère lors de la date renseignée, False sinon.
	 */
	
	public boolean isOperatingOn(Date date) {
		return DateInInterval(date, this.startingDate, this.endingDate) && ((!isInExcludedDates(date) && isInOperatingDays(date)) || isInIncludedDates(date));
	}
	
	/**
	 * @return Retourne la nom du service.
	 */
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 *
	 * @author Virgile Neu (224138)
	 * @author Valentin Courgeau (225255)
	 *
	 */
	public static class Builder {
		private final String name;
		private final Date startingDate;
		private final Date endingDate;
		private Set<Date.DayOfWeek> operatingDays;
		private Set<Date> excludedDates;
		private Set<Date> includedDates;
		
		/**
		 * 
		 * @param name
		 * 				Nom à donner au futur Service;
		 * @param startingDate
		 * 				Date de début pour le futur Service;
		 * @param endingDate
		 * 				Date de fin pour le futur Service;
		 * @throws IllegalArgumentException
		 * 									Lance l'exception si la date de fin est antérieure à celle de début.
		 */
		public Builder(String name, Date startingDate, Date endingDate) throws IllegalArgumentException{
			if (startingDate.compareTo(endingDate) == 1) 
				throw new IllegalArgumentException("Dates invalides");
			
			this.name = name;
			this.startingDate = startingDate;
			this.endingDate = endingDate;
			this.operatingDays = new HashSet<Date.DayOfWeek>();
			this.includedDates = new HashSet<Date>();
			this.excludedDates = new HashSet<Date>();
		}
		
		/**
		 * 
		 * @return Retourne la nom du service.
		 */
		public String name() {
			return name;
		}
		
		/**
		 * 
		 * @param day
		 * 				Jour de la semaine que l'on veut ajouter à l'instance du Builder.
		 * @return Retourne l'instance du Builder à laquelle on l'applique afin de pouvoir faire des appels chaînés.
		 */
		public Builder addOperatingDay(Date.DayOfWeek day) {
			operatingDays.add(day);
			
			return this;
		}
		
		/**
		 * 
		 * @param date
		 * 				Date à ajouter aux jours exclus de l'instance à laquelle on l'applique.
		 * @return	Retourne l'instance du Builder à laquelle on l'applique afin de pouvoir faire des appels cha�n�s.
		 * @throws IllegalArgumentException
		 * 									Lance l'exception dans les cas où la Date date est hors de l'intervalle de validité de l'instance de Builder,
		 * 									ou que la date est déjà incluse dans les jours inclus exceptionnelement.
		 */
		public Builder addExcludedDate(Date date) throws IllegalArgumentException {
			if (startingDate.compareTo(date) == 1 || endingDate.compareTo(date) == -1 || includedDates.contains(date))
				throw new IllegalArgumentException("Dates invalides");
	
			excludedDates.add(date);
			
			return this;
		}
		
		/**
		 * 
		 * @param date
		 * 				Date à ajouter aux jours inclus exceptionnelement.
		 * @return	Retourne l'instance du Builder à laquelle on l'applique afin de pouvoir faire des appels chaînés.
		 * @throws IllegalArgumentException
		 * 									Lance l'exception dans les cas où la Date date est hors de l'intervalle de validité de l'instance de Builder,
		 * 									ou que la date est déjà incluse dans les jours exclus exceptionnelement.
		 */
		public Builder addIncludedDate(Date date) throws IllegalArgumentException {
			if (startingDate.compareTo(date) == 1 || endingDate.compareTo(date) == -1 || excludedDates.contains(date))
				throw new IllegalArgumentException("Dates invalides");

			includedDates.add(date);
			
			return this;
		}
		
		/**
		 * 
		 * @return Retourne une instance de service, possédant les attributs de l'instance à laquelle on l'applique.
		 */
		public Service build() {
			return new Service(name, startingDate, endingDate, operatingDays, excludedDates, includedDates);
		}
	}
}
