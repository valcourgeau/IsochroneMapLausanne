package ch.epfl.isochrone.gui;

import static ch.epfl.isochrone.timetable.Stop.getStopFromName;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.tiledmap.CachedTileProvider;
import ch.epfl.isochrone.tiledmap.ColorTable;
import ch.epfl.isochrone.tiledmap.IsochroneTileProvider;
import ch.epfl.isochrone.tiledmap.OSMTileProvider;
import ch.epfl.isochrone.tiledmap.TileProvider;
import ch.epfl.isochrone.tiledmap.TransparentTileProvider;
import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.Date.Month;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

/**
 * 
 * @author Virgile Neu (224138)
 * @author Valentin Courgeau (225255)
 *
 */
public final class IsochroneTL {
    private static final String OSM_TILE_URL = "http://b.tile.openstreetmap.org/";
    private static final int MIN_ZOOM = 10, INITIAL_ZOOM = 11, MAX_ZOOM = 19;
    private static final PointWGS84 INITIAL_POSITION = new PointWGS84(Math.toRadians(6.476), Math.toRadians(46.613));
    private static final String INITIAL_STARTING_STOP_NAME = "Lausanne-Flon";
    private static final int INITIAL_DEPARTURE_TIME = SecondsPastMidnight.fromHMS(6, 8, 0);
    private static final Date INITIAL_DATE = new Date(1, Month.OCTOBER, 2013);
    private static final int WALKING_TIME = 5 * 60;
    private static final int COLOR_SLICE_DURATION = 5 * 60;
    private static final double WALKING_SPEED = 1.25;
    private static final double ALPHA = 0.5;
    private static final TimeTableReader READER = new TimeTableReader("/time-table/");
    private static final TimeTable TIMETABLE = createTimeTable();
    private static final Vector<String> STOPS_NAMES = getOrderedStopsNamesInVector(TIMETABLE.stops());
    private static final ColorTable COLORTABLE = createColorTable();
    private static Graph graph;

    private final TiledMapComponent tiledMapComponent;
    private Point recentMousePosition, recentMapPosition;
    private TileProvider bgTileProvider;
    
    // Variables utilisées dans les updates : 
    private Date currentDate;
    private String currentStartingStopName;
    private FastestPathTree currentFastestPathTree;
    private int currentDepartureTime;
    
    public static void main(String[] args) {
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	            try {
	                new IsochroneTL().start();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}

    /**
     * 
     * @throws IOException S'il y a une erreure de lecture avec la tuile d'erreure.
     */
	public IsochroneTL() throws IOException {
        bgTileProvider = new CachedTileProvider(new OSMTileProvider(new URL(OSM_TILE_URL)));
        tiledMapComponent = new TiledMapComponent(INITIAL_ZOOM);
        
        currentDate = INITIAL_DATE;
        currentStartingStopName = INITIAL_STARTING_STOP_NAME;
        currentDepartureTime = INITIAL_DEPARTURE_TIME;
        
        updateGraph();
    	updateFastestPathTree();
        updateTiledMapComponent();
    }

	private void start() {
	    JFrame frame = new JFrame("Isochrone TL");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	    frame.getContentPane().setLayout(new BorderLayout());
	    frame.getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
	    frame.getContentPane().add(createTopPanel(), BorderLayout.PAGE_START);
	    frame.pack();
	    frame.setVisible(true);
	}

	private JComponent createCenterPanel() {
        final JViewport viewPort = new JViewport();
        viewPort.setView(tiledMapComponent);
        PointOSM startingPosOSM = INITIAL_POSITION.toOSM(tiledMapComponent.zoom());
        viewPort.setViewPosition(new Point(startingPosOSM.roundedX(), startingPosOSM.roundedY()));

        final JPanel copyrightPanel = createCopyrightPanel();

        final JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(400, 300));

        layeredPane.add(viewPort, new Integer(0));
        layeredPane.add(copyrightPanel, new Integer(1));

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                final Rectangle newBounds = layeredPane.getBounds();
                viewPort.setBounds(newBounds);
                copyrightPanel.setBounds(newBounds);

                viewPort.revalidate();
                copyrightPanel.revalidate();
            }
        });

        //	Détecteur de clic gauche :
        layeredPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            	recentMousePosition = e.getPoint();
            	recentMapPosition = viewPort.getViewPosition();
            }
        });
        
        // Détecteur de dragging (mouvement cliqué) :
        layeredPane.addMouseMotionListener(new MouseAdapter() {
        	@Override
            public void mouseDragged(MouseEvent e) {
                viewPort.setViewPosition(new Point(recentMapPosition.x - (e.getPoint().x - recentMousePosition.x), recentMapPosition.y - (e.getPoint().y - recentMousePosition.y)));
            }
        });
        
        // zoom de la carte à la souris (molette)
        layeredPane.addMouseWheelListener(new MouseWheelListener(){
			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				int wheelRotation = arg0.getWheelRotation();
            	recentMapPosition = viewPort.getViewPosition();
            	Point pointToAim = SwingUtilities.convertPoint(layeredPane, arg0.getPoint(), tiledMapComponent);
				
            	int nextZoom = tiledMapComponent.zoom() - wheelRotation;
				
				if(nextZoom >= MIN_ZOOM && nextZoom <= MAX_ZOOM) {
					double ratio = Math.pow(2,nextZoom - tiledMapComponent.zoom());
					tiledMapComponent.setZoom(nextZoom);
					
					viewPort.setViewSize(tiledMapComponent.getPreferredSize());
					
					double positionX = Math.round(pointToAim.x * ratio - arg0.getX());
					double positionY = pointToAim.y * ratio - arg0.getY();
					
					viewPort.setViewPosition(new Point((int) positionX, (int) positionY ));
				} else if(nextZoom < MIN_ZOOM)
					tiledMapComponent.setZoom(MIN_ZOOM);
				  else
					tiledMapComponent.setZoom(MAX_ZOOM);
			}
        });
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(layeredPane, BorderLayout.CENTER);
        return centerPanel;
    }
    
    private JPanel createTopPanel(){
    	JPanel topPanel = new JPanel();
    	topPanel.setLayout(new FlowLayout());
    	JLabel departureLabel = new JLabel("Départ : "), dateAndTimeLabel = new JLabel("Date et heure : ");
    	
    	// JSpinner :
    	final JSpinner dateAndTimeSpinner = createDefaultJSpinner();
    	dateAndTimeSpinner.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {	
				setDate((java.util.Date)dateAndTimeSpinner.getValue());
			}
    	
    	});
    	
    	//ComboBox : 
    	final JComboBox<String> stopBox = new JComboBox<>(STOPS_NAMES);
    	stopBox.setSelectedItem(INITIAL_STARTING_STOP_NAME);
    	stopBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setStartingStop((String)stopBox.getSelectedItem());
			}
    		
    	});
    	JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
    	
    	
    	//Ajout des components :
    	topPanel.add(departureLabel);
    	topPanel.add(stopBox);
    	topPanel.add(separator);
    	topPanel.add(dateAndTimeLabel);
    	topPanel.add(dateAndTimeSpinner);
    	
    	return topPanel;
    }
    
    @SuppressWarnings("deprecation")
	private JSpinner createDefaultJSpinner(){
		SpinnerModel model = new SpinnerDateModel(INITIAL_DATE.toJavaDate(), null, null, Calendar.MINUTE);
		JSpinner spinner = new JSpinner(model);
		java.util.Date javaDate = INITIAL_DATE.toJavaDate();
		javaDate.setHours(SecondsPastMidnight.hours(INITIAL_DEPARTURE_TIME));
		javaDate.setMinutes(SecondsPastMidnight.minutes(INITIAL_DEPARTURE_TIME));
		javaDate.setSeconds(SecondsPastMidnight.seconds(INITIAL_DEPARTURE_TIME));
		
		spinner.setValue(javaDate);
		return new JSpinner(model);
	}

	private JPanel createCopyrightPanel() {
	    Icon tlIcon = new ImageIcon(getClass().getResource("/images/tl-logo.png"));
	    String copyrightText = "Données horaires 2013. Source : Transports publics de la région lausannoise / Carte : © contributeurs d'OpenStreetMap";
	    JLabel copyrightLabel = new JLabel(copyrightText, tlIcon, SwingConstants.CENTER);
	    copyrightLabel.setOpaque(true);
	    copyrightLabel.setForeground(new Color(1f, 1f, 1f, 0.6f));
	    copyrightLabel.setBackground(new Color(0f, 0f, 0f, 0.4f));
	    copyrightLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
	
	    JPanel copyrightPanel = new JPanel(new BorderLayout());
	    copyrightPanel.add(copyrightLabel, BorderLayout.PAGE_END);
	    copyrightPanel.setOpaque(false);
	    return copyrightPanel;
	}

	private static ColorTable createColorTable() {
		List<Color> colors = new ArrayList<Color>();
		
		colors.add(new Color(255,0,0));
		colors.add(new Color(255,255,0));
		colors.add(new Color(0,255,0));
		colors.add(new Color(0,0,255));
		colors.add(new Color(0,0,0));
		
		ColorTable colorTable = new ColorTable(COLOR_SLICE_DURATION, colors);
		return colorTable;
	}

	private void updateDate() {
		updateGraph();
		updateStartingTime();
	}

	private void updateStartingTime() {
		updateFastestPathTree();
		updateTiledMapComponent();
	}

	private void updateGraph(){
		try {
			graph = READER.readGraphForServices(TIMETABLE.stops(), TIMETABLE.servicesForDate(currentDate), WALKING_TIME, WALKING_SPEED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateFastestPathTree(){
		Stop startingStop = getStopFromName(TIMETABLE.stops(), currentStartingStopName);
		currentFastestPathTree = getFastestPathTree(startingStop, currentDepartureTime);
	}

	private void updateTiledMapComponent(){
		 List<TileProvider> tileProvidersList = new LinkedList<>();
	     TileProvider isochroneTileProvider = new IsochroneTileProvider(currentFastestPathTree, COLORTABLE, WALKING_SPEED);
	     
	     TileProvider transparentIsochrone = new CachedTileProvider(new TransparentTileProvider(isochroneTileProvider, ALPHA));
	     
	     tileProvidersList.add(bgTileProvider);
	     tileProvidersList.add(transparentIsochrone);
	     
	     tiledMapComponent.setTileProviders(tileProvidersList);
	}

	private void updateStartingStop() {
		updateFastestPathTree();
		updateTiledMapComponent();
	}

    private void setDate(java.util.Date date) {
    	Date changedDate = new Date(date);
    	int time = SecondsPastMidnight.fromJavaDate(date);
    	int FOUR_O_CLOCK = 4 * 3600;
    	int MIDNIGHT = 24 * 3600;
    	
    	if (!currentDate.equals(changedDate) && time >= FOUR_O_CLOCK) {
    		currentDepartureTime = time;
    		currentDate = changedDate;
    		updateDate();
    	} else if (time < FOUR_O_CLOCK) {
    		Date dayBefore = changedDate.relative(-1);
    		currentDepartureTime = time + MIDNIGHT;
    		if (!currentDate.equals(dayBefore)) {
    			currentDate = dayBefore;
    			updateDate();
    		} else {
    			updateStartingTime();
    		}
    	} else {
    		currentDepartureTime = time;
    		updateStartingTime();
    	}
	}
    
    private void setStartingStop(String selectedItem) {
		currentStartingStopName	= selectedItem;
		updateStartingStop();
	}
    
    private static TimeTable createTimeTable(){
    	TimeTable timeTable = null;
		
		try {
			timeTable = READER.readTimeTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return timeTable;
    }
    
    private FastestPathTree getFastestPathTree(Stop startingStop, int departureTime){
    	return graph.fastestPaths(startingStop, departureTime);
    }
    
    private static Vector<String> getOrderedStopsNamesInVector(Set<Stop> stops){
    	List<String> stopsNames = new ArrayList<>();
    	
    	for (Stop stop : stops)
			stopsNames.add(stop.name());
		
		Collections.sort(stopsNames);
		
		return new Vector<String>(stopsNames);
    }
}
