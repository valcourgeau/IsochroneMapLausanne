package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.LinkedList;

import org.junit.Test;

public class testColorTable {

	@Test (expected = IllegalArgumentException.class)
	public void ConstructorExceptionDuree() {
		@SuppressWarnings("unused")
		ColorTable toto = new ColorTable(0, null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void ConstructorExceptionList() {
		@SuppressWarnings("unused")
		ColorTable toto = new ColorTable (42, new LinkedList<Color>());
	}
	
	@Test
	public void getDuree() {
		LinkedList<Color> colorList = new LinkedList<Color>();
		colorList.add(new Color(0,0,0));
		ColorTable toto = new ColorTable(42, colorList);
		assertEquals(42, toto.getDuree());
	}
	
	@Test
	public void getNumberSlices() {
		LinkedList<Color> colorList = new LinkedList<Color>();
		colorList.add(new Color(0,0,0));
		ColorTable toto = new ColorTable(42, colorList);
		assertEquals(1, toto.getNumberSlices());
	}
	
	
	@Test
	public void getColorForTime() {
		LinkedList<Color> colorList = new LinkedList<Color>();
		colorList.add(new Color(0,0,0));
		colorList.add(new Color(1,0,0));
		colorList.add(new Color(0,1,0));
		colorList.add(new Color(0,0,1));
		ColorTable toto = new ColorTable(10, colorList);
		assertEquals(new Color(0,0,0).getRGB(), toto.getColorForTime(1).getRGB());
		assertEquals(new Color(1,0,0).getRGB(), toto.getColorForTime(11).getRGB());
		assertEquals(new Color(0,1,0).getRGB(), toto.getColorForTime(21).getRGB());
		assertEquals(new Color(0,0,1).getRGB(), toto.getColorForTime(31).getRGB());
	}
	
	
}
