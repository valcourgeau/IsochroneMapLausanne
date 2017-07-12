package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
public class TestTemp {
	
	@Test
	public void testReadTimeTable(){
		TimeTableReader reader = new TimeTableReader("/time-table/");
		TimeTable timetable = null;
		
		try{
			timetable = reader.readTimeTable();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		Set<Service> timetableServicesFor13092013 = timetable.servicesForDate(new Date(13, 9, 2013));
		int i = 0;
		Set<String> serviceNames = new HashSet<>();
		
		{
			//Ajout des services actifs le VENDREDI 13 / 09 / 2013 :
			serviceNames.add("2013-SHU-Semaine-51");
			serviceNames.add("2013-SHU-Semaine-51-0000100");
			serviceNames.add("2013-SHU-Semaine-51-1101100");
			serviceNames.add("2013-SHU-Semaine-51-0001100");
		}
		
		for(Service service : timetableServicesFor13092013){
			if(serviceNames.contains(service.name()))
				i++;
		}
		
		assertEquals(4, i);
	}
	
}
