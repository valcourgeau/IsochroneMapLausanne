package ch.epfl.isochrone.tiledmap;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Test;

import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.Date.Month;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

public class TestIsochroneTileProvider {
	@Test
	public void tileAt() {
		int departureTime = SecondsPastMidnight.fromHMS(6, 8, 0);
		
		Date startingDate = new Date(1, Month.OCTOBER, 2013);
		
		int maxWalkingTime = 300;
		double walkingSpeed = 1.25;
		
		TimeTableReader reader = new TimeTableReader("/time-table/");
		TimeTable timeTable = null;
		try {
			timeTable = reader.readTimeTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Graph graph = null;
		try {
			graph = reader.readGraphForServices(timeTable.stops(), timeTable.servicesForDate(startingDate), maxWalkingTime, walkingSpeed);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		Stop startingStop = getStopFromName(timeTable.stops(), "Lausanne-Flon");
		FastestPathTree fastestPathTree = graph.fastestPaths(startingStop, departureTime);
		
		List<Color> colors = new ArrayList<Color>();
		
		colors.add(new Color(255,0,0));
		colors.add(new Color(255,255,0));
		colors.add(new Color(0,255,0));
		colors.add(new Color(0,0,255));
		colors.add(new Color(0,0,0));
		
		ColorTable colorTable = new ColorTable(5, colors);
		
		IsochroneTileProvider isoTileProvider = new IsochroneTileProvider(fastestPathTree, colorTable, walkingSpeed);
		Tile tileToProduce = null;
		try {
			tileToProduce = isoTileProvider.tileAt(11, 1061, 724);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ImageIO.write(tileToProduce.image(), "png", new File("image.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static Stop getStopFromName(Set<Stop> stops, String name) {
		for (Stop stop : stops) {
			if (stop.name().equals(name)) return stop;
		}
		return null;
	}
}
