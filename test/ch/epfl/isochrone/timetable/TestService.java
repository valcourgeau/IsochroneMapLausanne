package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.timetable.Date.DayOfWeek;
import ch.epfl.isochrone.timetable.Date.Month;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */
public class TestService {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Date d = new Date(1, Month.JANUARY, 2000);
        Service s = new Service("s",
                d, d,
                Collections.<Date.DayOfWeek> emptySet(),
                Collections.<Date> emptySet(),
                Collections.<Date> emptySet());
        s.name();
        s.isOperatingOn(d);

        Service.Builder sb = new Service.Builder("s", d, d);
        sb.name();
        sb.addOperatingDay(DayOfWeek.MONDAY);
        sb.addExcludedDate(d);
        sb.addIncludedDate(d);
        sb.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void builderConstructorInvalidDates(){
    	@SuppressWarnings("unused")
		Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void builderWrongAddIncludedDates(){
    	Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,100));
    	builder.addIncludedDate(new Date(1,1,1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void builderWrongAddExcludedDates(){
    	Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,100));
    	builder.addIncludedDate(new Date(1,1,1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void builderConflictAddIncludedDatesWithExcludedDates(){
    	Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,100));
    	builder.addExcludedDate(new Date(1,1,10));
    	builder.addIncludedDate(new Date(1,1,10));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void builderConflictAddExcludedDatesWithIncludedDates(){
    	Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,100));
    	builder.addIncludedDate(new Date(1,1,10));
    	builder.addExcludedDate(new Date(1,1,10));
    }
    
    @Test
    public void isOperatingOnTestWithOutOfBoundsDate(){
    	Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,100));
    	builder.addIncludedDate(new Date(1,1,10)).addExcludedDate(new Date(1,1,20));
    	Service service = builder.build();
    	assertTrue(service.isOperatingOn(new Date(1,1,10)));
    }
    
    @Test
    public void builderBuildTest(){
    	Service.Builder builder = new Service.Builder("name", new Date(1,1,2), new Date(1,1,100));
    	builder.addIncludedDate(new Date(1,1,10)).addExcludedDate(new Date(1,1,20));
    	Service service = builder.build();
    	assertTrue(service.isOperatingOn(new Date(1,1,10)));
    	assertFalse(service.isOperatingOn(new Date(1,1,20)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void constructorServiceWrongBeginEndDatesTest(){
    	Set<Date.DayOfWeek> operatingDays = new HashSet<Date.DayOfWeek>();
    	Set<Date> excludedDates = new HashSet<Date>();
    	Set<Date> includedDates = new HashSet<Date>();
    	new Service("name", new Date(1,1,2), new Date(1,1,1), operatingDays, excludedDates, includedDates);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void constructorServiceEmptyBeginEndTest(){
    	Set<Date.DayOfWeek> operatingDays = new HashSet<Date.DayOfWeek>();
    	Set<Date> excludedDates = new HashSet<Date>();
    	Set<Date> includedDates = new HashSet<Date>();
    	@SuppressWarnings("unused")
		Date date;
    	new Service("name", null, null, operatingDays, excludedDates, includedDates);
    }
    @Test
    public void immuableServiceExcludedDate(){
    	Date changingDate = new Date(1, Month.JANUARY, 2000);
    	
    	Service.Builder builder = new Service.Builder("testedService", new Date(1, Month.APRIL, 1900), new Date(12, Month.APRIL, 2020));
   
    	builder.addExcludedDate(changingDate);
    	
    	changingDate = new Date(1, 2, 2001);
    	
    	Service service = builder.build();
    	
    	assertFalse(service.isOperatingOn(new Date(1, Month.JANUARY, 2000)));
    }
   
    @Test
    public void immuableServiceIncludedDate(){
    	Date changingDate = new Date(1, Month.JANUARY, 2000);
    	Service.Builder builder = new Service.Builder("testedService", new Date(1, Month.APRIL, 1900), new Date(12, Month.APRIL, 2020));

    	builder.addIncludedDate(changingDate);
    	
    	changingDate = new Date(1, 2, 2001);
    	
    	Service service = builder.build();
    	
    	service.isOperatingOn(new Date(1, Month.JANUARY, 2000));
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testOutOfBoundsExcludedDates() {
    	Set<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
    	Set<Date> exclDates = new HashSet<Date>();
    	Set<Date> inclDates = new HashSet<Date>();
    	exclDates.add(new Date(1,1,2014));
    	
    	@SuppressWarnings("unused")
		Service service = new Service("service", new Date(1,1,1), new Date(31,12,2013), days, exclDates, inclDates);
    	
    }
    
    
    @Test (expected = IllegalArgumentException.class)
    public void testOutOfBoundsIncludedDates() {
    	Set<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
    	Set<Date> exclDates = new HashSet<Date>();
    	Set<Date> inclDates = new HashSet<Date>();
    	inclDates.add(new Date(1,1,2014));
    	
    	@SuppressWarnings("unused")
		Service service = new Service("service", new Date(1,1,1), new Date(31,12,2013), days, exclDates, inclDates);
    }
}
