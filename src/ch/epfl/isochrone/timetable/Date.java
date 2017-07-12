package ch.epfl.isochrone.timetable;

import static ch.epfl.isochrone.math.Math.divF;
import static ch.epfl.isochrone.math.Math.modF;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 */

public final class Date implements Comparable<Date> {
	private final int day;
	private final int year;
	private final Month month;
	
	public enum DayOfWeek {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
	};
	
	public enum Month {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
	};
	
	/**
	 * 
	 * @param day Numéro du jour dans le mois.
	 * @param month Mois de la date.
	 * @param year Ann�e de la date.
	 * @throws IllegalArgumentException Vérifie que le jour est compris entre 1 et le nombre de jours du mois en question, et que
	 *  								le mois est compris entre 1 et 12.
	 */
	public Date(int day, Month month, int year) throws IllegalArgumentException {
		if (day < 1 || day > daysInMonth(month, year) 
				|| monthToInt(month) < 1 || monthToInt(month) > 12 ) {
			throw new IllegalArgumentException("Date non valide");
		} else {
			this.day=day;
			this.month=month;
			this.year=year;
		}	
	}
	
	/**
	 * 
	 * @param day 
	 * 				Numéro du jour dans le mois.
	 * @param month 
	 * 				Numéro du mois de la date.
	 * @param year 
	 * 				Année de la date.
	 * @throws IllegalArgumentException 
	 * 									Vérifie que le jour est compris entre 1 et le nombre de jours du mois en question, et que
	 * 									le mois est compris entre 1 et 12.
	 */
	public Date(int day, int month, int year) throws IllegalArgumentException {
		this(day, intToMonth(month), year);
	}
	
	/**
	 * 
	 * @param date 
	 * 				Date issue de l'implémentation de la date par Java.
	 * @throws IllegalArgumentException 
	 * 									Vérifie que le jour est compris entre 1 et le nombre de jours du mois en question, et que
	 * 									le mois est compris entre 1 et 12.
	 */

	@SuppressWarnings("deprecation")
	public Date(java.util.Date date) throws IllegalArgumentException {
		this(date.getDate(), intToMonth(1 + date.getMonth()), date.getYear() + 1900);
	}
	
	/**
	 * 
	 * @return Retourne le numéro du jour de la date à laquelle on l'applique.
	 */
	public int day() {
		return day;
	}
	
	/**
	 * 
	 * @return Retourne le mois de la date à laquelle on l'applique.
	 */
	public Month month() {
		return month;
	}
	
	/**
	 * 
	 * @return Retourne le numéro du mois de la date à laquelle on l'applique.
	 */
	@SuppressWarnings("unused")
	private int intMonth() {
		return this.month.ordinal() + 1;
	}
	
	/**
	 * 
	 * @return Retourne l'année de la date à laquelle on l'applique.
	 */
	public int year() {
		return year;
	}
	
	/**
	 * 
	 * @return Retourne le jour de la semaine asocié à la date à laquelle on l'applique.
	 */
	public DayOfWeek dayOfWeek() {
		return DayOfWeek.values()[modF(fixed() - 1, 7)];
	}
	
	/**
	 * 
	 * @param daysDiff
	 * 					Nombre de jour de décalage.
	 * @return retourne la Date à laquelle on l'applique décalée du nombre daysDiff de jours.
	 */
	public Date relative(int daysDiff) {
		return fixedToDate(this.fixed() + daysDiff);
	}
	
	/**
	 * 
	 * @return Retourne la JavaDate associée à la date à laquelle on l'applique.
	 */
	
	@SuppressWarnings("deprecation")
	public java.util.Date toJavaDate() {
		return new java.util.Date(this.year() -1900, monthToInt(this.month())-1, this.day());
	}
	
	/**
	 * Retourne la date à laquelle on l'applique selon un certain formatage: par exemple, 1983-06-17 pour le 17/06/1983.
	 */
	@Override
	public String toString() {
		return String.format("%04d-%02d-%02d", this.year(), monthToInt(this.month()), this.day());
	}
	
	/**
	 * @param that 
	 * 				Date utilisée pour vérifier l'égalité avec la date à laquelle on l'applique (Object est utilisé par convention).
	 * @return Retourne True si les deux dates sont égales, False sinon.
	 */
	@Override
	public boolean equals(Object that) {
		return (that instanceof Date && ((Date)that).year()==this.year() &&
				((Date)that).month()==this.month() && ((Date)that).day()==this.day());
	}
	
	/**
	 * @return Retourne le hashCode associé à la date à laquelle on l'applique.
	 */
	@Override
	public int hashCode() {
		return this.fixed();
	}
	
	/**
	 * 
	 * @param that 
	 * 				Date qu'on compare à la date à laquelle on l'applique.
	 * @return 	Retourne -1 si la Date à laquelle on l'applique est plus ancienne que la Date that,
	 * 			retourne 0 si la Date à laquelle on l'applique est égale que la Date that,
	 * 			retourne +1 si la Date à laquelle on l'applique est plus récente que la Date that.
	 */
	public int compareTo(Date that) {
		int thisDate = this.fixed();
		int thatDate = that.fixed();
		
		if (thisDate > thatDate) {
			return 1;
		} else{
			if (thisDate == thatDate)
				return 0;
			else 
				return -1;
		}
	}
	
	/**
	 * 
	 * @param date 
	 * 				Date à transformer en nombre de jours.
	 * @return Retourne le nombre de jour écoulé depuis le 01/01/0001 (inclus).
	 */
	private static int dateToFixed(Date date) {
		int c;
		if (monthToInt(date.month()) <= 2) {
			c=0;
		} else if(monthToInt(date.month()) > 2 && (isLeapYear(date.year()))) {
			c = -1;
		} else {
			c = -2;
		}
		return 365*(date.year()-1)+divF(date.year()-1,4)-divF(date.year()-1,100)+divF(date.year()-1,400)+divF((367*monthToInt(date.month())-362),12)+c+date.day();
	}
	
	/**
	 * 
	 * @param n 
	 * 			Nombre de jours à convertir en date.
	 * @return Retourne la date associée au nombre de jours n.
	 */
	private static Date fixedToDate(int n) {
		int d0 		= n - 1;	
		int n400 	= divF(d0, 146097);
		int d1 		= modF(d0, 146097);
		int n100 	= divF(d1, 36524);
		int d2 		= modF(d1, 36524);
		int n4 		= divF(d2, 1461);
		int d3 		= modF(d2, 1461);
		int n1 		= divF(d3, 365);
		int y0 		= 400 * n400 + 100 * n100 + 4 * n4 + n1;	
		int y;
		
		
		if (n100 == 4 || n1 == 4) 
			y = y0;
		else 
			y = y0 + 1;
		
		int temp1 	= dateToFixed(new Date(1, 1, y));
		int p 		= n - temp1;
		
		int temp2	= dateToFixed(new Date(1, 3, y));
		int c;
		
		if (n < temp2) 
			c = 0;
		else if (n >= temp2 &&  isLeapYear(y)) 
			c = 1;
		else 
			c = 2;
		
		
		int m 		= divF((12 * (p + c) + 373), 367);
		int temp3 	= dateToFixed(new Date(1, m, y));
		int d 		= n - temp3 + 1;
		
		return new Date(d, m, y);
	}
	
	/**
	 * 
	 * @param month 
	 * 				Mois à utiliser pour calculer le nombre de jours dans ce mois-ci.
	 * @param year 
	 * 				Année à utiliser pour calculer le nombre de jours dans le mois month de cette année.
	 * @return Retourne le nombre de jours dans le mois month au cours de l'année year.
	 */
	public static int daysInMonth(Month month, int year) {
			if ((monthToInt(month) < 8 && monthToInt(month) % 2 == 1) || (monthToInt(month) > 7 && monthToInt(month) % 2 == 0)) {
				assert monthToInt(month)==1 || monthToInt(month)==3 || monthToInt(month)== 5 || monthToInt(month)==7 || monthToInt(month)==8 || monthToInt(month)==10 || monthToInt(month)==12;
				return 31;
			} else if (monthToInt(month)==2) {
				if (isLeapYear(year))
					return 29;
				else
					return 28;
			} else {
				assert monthToInt(month)==4 || monthToInt(month)== 6 || monthToInt(month)==9 || monthToInt(month)==11;
				return 30;
			}
		}
	
	/**
	 * 
	 * @param year 
	 * 				Année à utiliser pour savoir si elle est bissextile ou non.
	 * @return Retourne True si l'année est bissextile, False sinon.
	 */
	private static boolean isLeapYear(int year) {
		return (modF(year, 4) == 0 && modF(year, 100) != 0) || modF(year, 400) == 0;
	}
	
	/**
	 * 
	 * @param month 
	 * 				Number Le numéro du mois à retourner.
	 * @return	Retourne le mois associé à monthNumber.
	 */
	public static Month intToMonth(int monthNumber) throws IllegalArgumentException {
		if (monthNumber > 12 || monthNumber < 1) 
			throw new IllegalArgumentException("mois invalide");
		
		return Month.values()[monthNumber-1];
	}
	
	/**
	 * 
	 * @param month 
	 * 				Mois dont on veut le num�ro.
	 * @return Retourne le numéro du mois month.
	 */
	private static int monthToInt(Month month) throws IllegalArgumentException {
		if (month.ordinal() + 1 > 12 || month.ordinal() + 1 < 1)
			throw new IllegalArgumentException("Mois invalide");
		
		return (month.ordinal() + 1);
	}
	
	/**
	 * 
	 * @return Retourne le nombre de jours écoulés entre le 01/01/0001 (inclus) et la date à laquelle on l'applique.
	 * 		   Cette méthode est une simple réécriture de dateToFixed appliqué à l'instance actuelle.
	 */
	private int fixed() {
		return dateToFixed(this);
	}
}