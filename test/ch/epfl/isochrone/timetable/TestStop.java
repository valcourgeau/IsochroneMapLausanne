package ch.epfl.isochrone.timetable;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Crougeau (225255)
 * 
 */
public class TestStop {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Stop s = new Stop("invalid", new PointWGS84(0.17, 0.12));
        s.name();
        s.position();
    }
    
    @Test
    public void positionCheck(){
    	PointWGS84 testedPoint = new PointWGS84(0.1, 0.1);
    	Stop testedStop = new Stop("Ok", testedPoint);
    	testedPoint = new PointWGS84(0.2, 0.3);
    	assertNotEquals((testedStop).position(), testedPoint);
    }
    
    @Test
    public void nameCheck(){
    	assertEquals((new Stop("Ok", new PointWGS84(0.1, 0.1))).name(), "Ok");
    }
    
    
    // A compléter avec de véritables méthodes de test...
}
