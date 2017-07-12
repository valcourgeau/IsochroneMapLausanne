package ch.epfl.isochrone.tiledmap;

import static ch.epfl.isochrone.math.Math.divF;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class ColorTable {
	private int duree;
	private List<Color> colors;
	
	/**
	 * 
	 * @param duree		Durée en minutes de chaqune des tranches de couleur.
	 * @param colors	Liste des couleurs à utiliser.
	 */
	public ColorTable(int duree, List<Color> colors) {
		if (duree <= 0 || colors.isEmpty())
			throw new IllegalArgumentException();
		this.duree  = duree;
		this.colors = getColors(colors);
	}
	
	private List<Color> getColors(List<Color> colors) {
		LinkedList<Color> colorsList  = new LinkedList<>();
		Iterator<Color> colorIterator = colors.iterator();
		int i = 0;
		while (colorIterator.hasNext()) {
			Color currentColor = colorIterator.next();
			if (i++%2 == 0)
				colorsList.add(currentColor);
			else {
				++i;
				Color lastColor = colorsList.getLast();
				Color tempColor = computeColor(currentColor, lastColor) ;
				colorsList.add(tempColor);
				colorsList.add(currentColor);
			}
		}
		return colorsList;
	}
	
	private Color computeColor(Color currentColor, Color lastColor) {
		int red   = (lastColor.getRed() + currentColor.getRed()) / 2;
		int green = (lastColor.getGreen() + currentColor.getGreen()) / 2;
		int blue  = (lastColor.getBlue() + currentColor.getBlue()) / 2;
		
		return new Color(red, green, blue);
	}
	
	/**
	 * 
	 * @return	Durée en seconde de chaque tranche de couleur.
	 */
	public int getDuree() {
		return duree;
	}
	
	/**
	 * 
	 * @return Nombre de tranche de couleur.
	 */
	public int getNumberSlices() {
		return colors.size();
	}
	
	/**
	 * 
	 * @param time 	Temps en minutes pour lequel on cherche la couleur associée.
	 * @return		La couleur associée au temps passé en argument.
	 */
	public Color getColorForTime(int time) {
		if (time < 0)
			throw new IllegalArgumentException();
		if (divF(time, duree) > colors.size())
			return colors.get(colors.size() - 1);
		return colors.get(divF(time, duree));
	}
	
	/**
	 * 
	 * @param sliceNumber	Numéro de la tranche pour laquelle on cherche la couleur associée.
	 * @return				La couleur assocée au numéro de la tranche passée en argument.
	 */
	public Color getColorForSlice(int sliceNumber) {
		if (sliceNumber < 0)
			throw new IllegalArgumentException();
		if (sliceNumber > colors.size())
			return colors.get(colors.size()-1);
		return colors.get(sliceNumber);
	}
}