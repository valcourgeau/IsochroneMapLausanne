package ch.epfl.isochrone.math;

import static java.lang.Integer.signum;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 * 
 */

final public class Math {
	
	private Math() {}
	
	/**
	 * 
	 * @param x
	 * 			Réel dont on veut calculer la valeur par la fonction argument sinus hyperbolique.
	 * @return
	 * 			Retourne la valeur de la fonction argument sinus hyperbolique pour la valeur passée en argument.
	 */
	public static double asinh(double x) {
		 return java.lang.Math.log(x + java.lang.Math.sqrt(1 + x*x));
	}
	
	/**
	 * 
	 * @param x
	 * 			Réel dont on veut calculer la valeur par la fonction haversinus (sin(x/2)^2).
	 * @return
	 * 			Retourne la valeur de la fonction haversinus (sin(x/2)^2) pour la valeur passée en argument.
	 */
	public static double haversin(double x) {
		return java.lang.Math.pow(java.lang.Math.sin(x/2),2);
	}
	
	/**
	 * 
	 * @param n
	 * 			Entier à diviser.
	 * @param d
	 * 			Entier diviseur.
	 * @return  
	 * 			Résultat de la division entière.
	 */ 
	public static int divF (int n, int d) {
		if (d == 0) 
			return 0;
	
		int i,  qt = n / d, rt = n % d;
		
		if (signum(rt) == - signum(d))
			i=1;
		else
			i=0;
		
		return qt-i;
	}
	
	/**
	 * 
	 * @param n
	 * 			entier qui va subir le modulo
	 * @param d
	 * 			modulo
	 * @return résultat de n modulo d
	 */
	public static int modF (int n, int d) {
		if (d == 0)
			return n;

		int i, rt = n % d;
			
		if (signum(rt) == - signum(d))
			i=1;
		else 
			i=0;
		
		return rt+i*d;
	}
}
