package ch.epfl.isochrone.gui;

import static ch.epfl.isochrone.timetable.Stop.getStopFromName;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
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
import ch.epfl.isochrone.timetable.CachedGraph;
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
	private static final double WALKING_SPEED = 1.25;
	private static final double ALPHA = 0.5;
	private static final int COLOR_SLICE_DURATION = 5 * 60;
	private static final TimeTableReader READER = new TimeTableReader("/time-table/");
	private static final TimeTable TIMETABLE = createTimeTable();
	private static final ColorTable DEFAULT_COLORTABLE = createColorTable();
	private static final JTabbedPane TABBEDPANE = new JTabbedPane();
	private static Graph graph;
	private final TiledMapComponent tiledMapComponent;
	private Point recentMousePosition, recentMapPosition;
	private TileProvider bgTileProvider;

	// Cache pour les Graphs
	private static final int CACHED_GRAPH_SIZE = 10;
	private static CachedGraph cachedGraph;
	
	// Stop avec clic :
	private boolean customStopRecentlyChanged = false, customStopActive = false;
	private static Stop customStop = new Stop("CustomStop", INITIAL_POSITION);
	private static Set<Stop> activeSetOfStops = createSetOfStops();

	// ComboBox pour les stops
	private static final Vector<String> STOPS_NAMES = getOrderedStopsNamesInVector(activeSetOfStops);
	private static final JComboBox<String> TOP_STOP_BOX = new JComboBox<>(STOPS_NAMES);
	
	// Jspinner
	private static final JSpinner dateAndTimeSpinner = createDefaultJSpinner();;
	private final int MAX_UPDATES = 200;
	private int sleepTime = 0;
	private boolean animationEnable = false;

	// Customisation des couleurs :
	private static ColorTable activeColorTable = DEFAULT_COLORTABLE;
	private static final JPanel colorSettingsTab = createColorSettingsTab();
	
	// Onglet Itinéraire
	private final ItineraireComponent itinerairePanel = createItineraire();
	
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

	private static Set<Stop> createSetOfStops() {
		Set<Stop> setStops = new HashSet<>(TIMETABLE.stops());
		setStops.add(customStop);
		return setStops;
	}

	/**
	 * 
	 * @throws IOException
	 *             S'il y a une erreur de lecture avec la tuile d'erreur.
	 */
	public IsochroneTL() throws IOException {
		bgTileProvider = new CachedTileProvider(new OSMTileProvider(new URL(OSM_TILE_URL)));
		tiledMapComponent = new TiledMapComponent(INITIAL_ZOOM);
		cachedGraph = new CachedGraph(CACHED_GRAPH_SIZE);

		currentDate = INITIAL_DATE;
		currentStartingStopName = INITIAL_STARTING_STOP_NAME;
		currentDepartureTime = INITIAL_DEPARTURE_TIME;

		dateAndTimeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				setDate((java.util.Date) dateAndTimeSpinner.getValue());
			}});

		updateFastestPathTree();
		updateTiledMapComponent();
	}

	private void start() {
		JFrame frame = new JFrame("Isochrone TL");
		frame.setJMenuBar(createMenuBar());
		JPanel mapPanel = new JPanel(), slidePanel = new JPanel(), firstTab = new JPanel();

		final JSlider slide = createDefaultJSlider();
		final JButton animationButton = new JButton(new ImageIcon("imagesData/play.png"));
		
		animationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				animationEnable = !animationEnable;
				if (!slide.isEnabled())
					animationButton.setIcon(new ImageIcon("imagesData/pause.png"));
				else
					animationButton.setIcon(new ImageIcon("imagesData/play.png"));

				slide.setEnabled(animationEnable);
			}});

		slidePanel.setLayout(new BorderLayout());
		slidePanel.add(slide);
		slidePanel.add(animationButton, BorderLayout.NORTH);

		mapPanel.setLayout(new BorderLayout());
		mapPanel.add(createCenterPanel(), BorderLayout.CENTER);
		mapPanel.add(createTopPanel(), BorderLayout.PAGE_START);
		mapPanel.add(slidePanel, BorderLayout.WEST);
		mapPanel.setMinimumSize(new Dimension(400, 300));

		firstTab.setLayout(new BorderLayout());
		firstTab.add(slidePanel, BorderLayout.WEST);
		firstTab.add(mapPanel, BorderLayout.CENTER);

		TABBEDPANE.addTab("Map", firstTab);
		TABBEDPANE.addTab("Itineraire", itinerairePanel);
		TABBEDPANE.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (slide.isEnabled())
					animationButton.doClick();
			}});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(new ImageIcon("imagesData/iconMS48.jpg").getImage());
		frame.add(TABBEDPANE);
		frame.pack();
		frame.setVisible(true);
		animation();
	}

	private ItineraireComponent createItineraire() {
		return new ItineraireComponent(TIMETABLE, getOrderedStopsNamesInVector(TIMETABLE.stops()), INITIAL_STARTING_STOP_NAME, INITIAL_DATE, INITIAL_DEPARTURE_TIME, WALKING_TIME, WALKING_SPEED);
	}
	
	/**
	 * 
	 * @return Crée la barre de menus.
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu options = new JMenu("Options");
		JMenu settings = new JMenu("Settings");
		final JCheckBoxMenuItem activeCustomStop = new JCheckBoxMenuItem("Activate Custom Stop");
		JMenuItem close = new JMenuItem("Close");
		close.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to close IsochroneTL ?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if(option == JOptionPane.YES_OPTION)
					System.exit(0);
			}
			
		});
		activeCustomStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (activeCustomStop.isSelected())
					customStopActive = true;
				else {
					customStopActive = false;
					TOP_STOP_BOX.setSelectedItem(INITIAL_STARTING_STOP_NAME);
				}}});

		
		final JMenuItem colorSettings = new JMenuItem("Colors");
		
		colorSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TABBEDPANE.addTab("Settings", colorSettingsTab);
				TABBEDPANE.setSelectedComponent(colorSettingsTab);
			}});

		options.add(activeCustomStop);
		options.addSeparator();
		options.add(close);
		
		settings.add(colorSettings);
		
		menuBar.add(options);
		menuBar.add(settings);
		
		return menuBar;
	}
	
	/**
	 * 
	 * @return Retourne le panneau principal d'édition des couleurs.
	 */
	private static JPanel createColorSettingsTab() {
		final int numberSlices = activeColorTable.getNumberSlices();
		final int numberOfColors = (int) ((double) numberSlices / 2 + 1);
		final JPanel colorSettingsTab = new JPanel();
		final JPanel colorSetter = new JPanel();
		final Dimension rectDimension = new Dimension(400, 50);
		final Set<JComponent> rectColorSet = new HashSet<>();
		final Map<JButton, Integer> buttonsIndexMap = new HashMap<>();
		final JRadioButton defaultRadioButton = new JRadioButton("Use default colors.", true), customizedRadioButton = new JRadioButton("Use customized colors : ", false);
		
		colorSettingsTab.setLayout(new BorderLayout());
		
		// Les deux boutons radios du haut du panneau :
		defaultRadioButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (defaultRadioButton.isSelected()) {
					for (JButton button : buttonsIndexMap.keySet())
						button.setEnabled(false);
					for (JComponent rect : rectColorSet)
						rect.setEnabled(false);
				}}});

		customizedRadioButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (customizedRadioButton.isSelected())
					for (JButton button : buttonsIndexMap.keySet())
						button.setEnabled(true);
			}});

		final List<Color> newColorList = new ArrayList<>();

		GridLayout gridLayout = new GridLayout(numberOfColors, 3);

		ButtonGroup bg = new ButtonGroup();
		bg.add(defaultRadioButton);
		bg.add(customizedRadioButton);

		colorSetter.setLayout(gridLayout);
		
		for (int i = 0; i < numberSlices; i += 2) {
			final int j = i;
			final JLabel colorLabel = new JLabel("Color n°" + (j / 2 + 1) + ":");
			newColorList.add(activeColorTable.getColorForSlice(i));

			@SuppressWarnings("serial")
			final JComponent rectColor = new JComponent() {
				public void paint(Graphics g) {
					g.setColor(newColorList.get(j / 2));
					g.fillRect(0, 0, rectDimension.width, rectDimension.height);
				}};
				
			rectColorSet.add(rectColor);
			JButton colorButton = new JButton("Click to set color");
			colorButton.setEnabled(false);
			buttonsIndexMap.put(colorButton, j / 2);
			final Color COLOR = newColorList.get(j / 2);
			/**
			 * Ce qui suit crée l'éditeur de couleurs :
			 */
			
			colorButton.addActionListener(new ActionListener() {
				@SuppressWarnings("serial")
				@Override
				public void actionPerformed(ActionEvent e) {
					final JPanel panelToAdd = new JPanel() {
						private Color color;
						private JComponent samplingRectColor;
						private final JSlider redBar, greenBar, blueBar;
						private Stack<int[]> colorStack;
						private boolean redRecentlySet, greenRecentlySet, blueRecentlySet;
						private final JButton reset = new JButton("Reset to default"), cancel = new JButton("Cancel"), saveAndClose = new JButton("Save & close");
						
						{
							color = newColorList.get(j / 2);
							redBar = createJSlider(color.getRed());
							greenBar = createJSlider(color.getGreen());
							blueBar = createJSlider(color.getBlue());
							colorStack = new Stack<>();
							int[] colorArray = {color.getRed(), color.getGreen(), color.getBlue()};
							colorStack.push(colorArray);
							redRecentlySet = greenRecentlySet = blueRecentlySet = false;
							this.setBackground(new Color(230, 230, 230));
							
							samplingRectColor = new JComponent() {
								public void paint(Graphics g) {
									g.setColor(color);
									g.fillRect(0, 0, TABBEDPANE.getSelectedComponent().getWidth(), TABBEDPANE.getSelectedComponent().getHeight() / 5);
								}};

							samplingRectColor.setPreferredSize(new Dimension(TABBEDPANE.getSelectedComponent().getWidth(), TABBEDPANE.getSelectedComponent().getHeight() / 5));
							this.setLayout(new BorderLayout());
							this.add(samplingRectColor, BorderLayout.NORTH);
							this.add(buildFrame(), BorderLayout.CENTER);
							this.add(addPaneWithButtons(), BorderLayout.SOUTH);
							this.setVisible(true);
						}
						
						/**
						 * 
						 * @return Retourne le centre de l'éditeur de couleurs (avec les Sliders, etc).
						 */
						private JPanel buildFrame() {
							JPanel slidersPanel = new JPanel();
							slidersPanel.setLayout(new GridLayout(6, 1));

							redBar.addChangeListener(new ChangeListener() {
								@Override
								public void stateChanged(ChangeEvent arg0) {
									redRecentlySet = true;
									int[] colorArray = {redBar.getValue(), color.getGreen(), color.getBlue()};
									
									if(blueRecentlySet || greenRecentlySet){
										if(colorStack.size() > 0){
											int[] colorPeek = colorStack.peek();
											
											if(!colorsAreEquals(colorPeek, colorArray))
												colorStack.push(colorArray);
										}else{
											colorStack.push(colorArray);
										}
										
										cancel.setEnabled(true);
										greenRecentlySet = blueRecentlySet = false;
									}
									
									color = new Color(colorArray[0], colorArray[1], colorArray[2]);
									samplingRectColor.repaint();
								}});
							
							greenBar.addChangeListener(new ChangeListener() {
								@Override
								public void stateChanged(ChangeEvent arg0) {
									greenRecentlySet = true;
									int[] colorArray = {color.getRed(), greenBar.getValue(), color.getBlue()};
									
									if(blueRecentlySet || redRecentlySet){
										if(colorStack.size() > 0){
											int[] colorPeek = colorStack.peek();
											
											if(!colorsAreEquals(colorPeek, colorArray))
												colorStack.push(colorArray);
										}else{
											colorStack.push(colorArray);
										}
										
										cancel.setEnabled(true);
										redRecentlySet = blueRecentlySet = false;
									}
									
									color = new Color(colorArray[0], colorArray[1], colorArray[2]);
									samplingRectColor.repaint();
								}});
							
							blueBar.addChangeListener(new ChangeListener() {
								@Override
								public void stateChanged(ChangeEvent arg0) {
									blueRecentlySet = true;
									int[] colorArray = {color.getRed(), color.getGreen(), blueBar.getValue()};
									
									if(greenRecentlySet || redRecentlySet){
										if(colorStack.size() > 0){
											int[] colorPeek = colorStack.peek();
											
											if(!colorsAreEquals(colorPeek, colorArray))
												colorStack.push(colorArray);
										}else{
											colorStack.push(colorArray);
										}
										
										cancel.setEnabled(true);
										greenRecentlySet = redRecentlySet = false;
									}
									
									color = new Color(colorArray[0], colorArray[1], colorArray[2]);
									samplingRectColor.repaint();
								}});
							
							slidersPanel.add(new JLabel("Red :"));
							slidersPanel.add(redBar);
							slidersPanel.add(new JLabel("Green :"));
							slidersPanel.add(greenBar);
							slidersPanel.add(new JLabel("Blue :"));
							slidersPanel.add(blueBar);

							return slidersPanel;
						}
						
						/**
						 * 
						 * @param colorValue
						 * 					Valeur initiale du slider.
						 * @return
						 * 					Retourne un slider avec la valeur colorValue comme valeur initiale.
						 */
						private JSlider createJSlider(int colorValue) {
							JSlider slider = new JSlider(0, 255, colorValue);
							slider.setPaintTicks(true);
							slider.setMinorTickSpacing(5);
							slider.setMajorTickSpacing(100);
							slider.setPreferredSize(new Dimension(200, 35));
							slider.setPaintLabels(true);

							return slider;
						}
						
						/**
						 * 
						 * @return Le panel contenant les boutons de l'éditeur de couleurs.
						 */
						private JPanel addPaneWithButtons() {
							JPanel buttonsPanel = new JPanel();
							
							reset.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									color = COLOR;
									
									redBar.setValue(COLOR.getRed());
									greenBar.setValue(COLOR.getGreen());
									blueBar.setValue(COLOR.getBlue());
									
									if(colorStack.size() == 0){
										int[] colorArray = {color.getRed(), color.getGreen(), color.getBlue()};
										colorStack.push(colorArray);
									}
									
									samplingRectColor.repaint();
								}
							});
							
							cancel.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									if(colorStack.size() > 0){
										int[] colorArray = colorStack.pop();
										if(colorStack.size() > 0){
											int[] colorArrayPeek = colorStack.peek();
											
											while(colorsAreEquals(colorArrayPeek, colorArray) && colorStack.size() > 0){
												colorStack.pop();
												colorArrayPeek = colorStack.peek();
											}
										}else{
											greenRecentlySet = blueRecentlySet = redRecentlySet = true;
											cancel.setEnabled(false);
										}
										
										color = new Color(colorArray[0], colorArray[1], colorArray[2]);
										redBar.setValue(colorArray[0]);
										greenBar.setValue(colorArray[1]);
										blueBar.setValue(colorArray[2]);
										newColorList.set(j / 2, color);
										
										rectColor.repaint();
									}else{
										cancel.setEnabled(false);
									}
								}
								
								
							});
							
							
							
							saveAndClose.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent arg0) {
										newColorList.set(j / 2, color);
										TABBEDPANE.remove(TABBEDPANE.getSelectedComponent());
										rectColor.repaint();
									}
							});

							buttonsPanel.add(reset);
							buttonsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
							buttonsPanel.add(cancel);
							buttonsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
							buttonsPanel.add(saveAndClose);

							return buttonsPanel;
						}};
						
					TABBEDPANE.add(panelToAdd, colorLabel.getText());
					TABBEDPANE.setSelectedComponent(panelToAdd);
				}});

			colorSetter.add(colorLabel);
			colorSetter.add(rectColor);
			colorSetter.add(colorButton);
		}
		// Fin de la boucle for
		
		JPanel buttonsPanelMain = new JPanel();
		JButton resetMain = new JButton("Reset to default."), saveAndCloseMain = new JButton("Save & close");

		resetMain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int i = 0, k = 0;
				while (i < numberOfColors) {
					newColorList.set(i, DEFAULT_COLORTABLE.getColorForSlice(k));
					i++;
					k += 2;
				}
				for (JComponent rect : rectColorSet)
					rect.repaint();
			}});

		saveAndCloseMain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (defaultRadioButton.isSelected())
					activeColorTable = DEFAULT_COLORTABLE;
				else
					activeColorTable = new ColorTable(activeColorTable.getDuree(), newColorList);

				TABBEDPANE.remove(TABBEDPANE.getSelectedComponent());
				updateViewer();
				TABBEDPANE.setSelectedIndex(0);
			}});

		buttonsPanelMain.add(resetMain);
		buttonsPanelMain.add(new JSeparator(SwingConstants.HORIZONTAL));
		buttonsPanelMain.add(saveAndCloseMain);

		JPanel radioButtonsPanel = new JPanel();
		radioButtonsPanel.add(defaultRadioButton);
		radioButtonsPanel.add(customizedRadioButton);

		colorSettingsTab.add(radioButtonsPanel, BorderLayout.NORTH);
		colorSettingsTab.add(colorSetter, BorderLayout.CENTER);
		colorSettingsTab.add(buttonsPanelMain, BorderLayout.SOUTH);

		return colorSettingsTab;
	}

	private JComponent createCenterPanel() {
		final JViewport viewPort = new JViewport();
		final JPopupMenu popMenu = new JPopupMenu();
		final JMenuItem placeStop = new JMenuItem("Place your custom stop here");

		placeStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!customStopActive)
					JOptionPane.showMessageDialog(null, "Please tick \"Activate CustomStop\" (Options > Activate CustomStop).", "Error", JOptionPane.ERROR_MESSAGE);
				else {
					TOP_STOP_BOX.setSelectedItem(customStop.name());
					updateCustomStop();
					updateFastestPathTree();
					updateTiledMapComponent();
				}}});
		
		popMenu.add(placeStop);

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
			}});

		// Détecteur de clic gauche :
		layeredPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				recentMousePosition = e.getPoint();
				recentMapPosition = viewPort.getViewPosition();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					popMenu.show(layeredPane, e.getX(), e.getY());
				}}
		});

		// Détecteur de dragging (mouvement cliqué) :
		layeredPane.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				viewPort.setViewPosition(new Point(recentMapPosition.x - (e.getPoint().x - recentMousePosition.x), recentMapPosition.y - (e.getPoint().y - recentMousePosition.y)));
			}});

		// zoom de la carte à la souris (molette)
		layeredPane.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				int wheelRotation = arg0.getWheelRotation();
				recentMapPosition = viewPort.getViewPosition();
				Point pointToAim = SwingUtilities.convertPoint(layeredPane, arg0.getPoint(), tiledMapComponent);
				
				int nextZoom = tiledMapComponent.zoom() - wheelRotation;
				
				if (nextZoom >= MIN_ZOOM && nextZoom <= MAX_ZOOM) {
					double ratio = Math.pow(2, nextZoom - tiledMapComponent.zoom());
					tiledMapComponent.setZoom(nextZoom);

					viewPort.setViewSize(tiledMapComponent.getPreferredSize());

					double positionX = Math.round(pointToAim.x * ratio - arg0.getX());
					double positionY = pointToAim.y * ratio - arg0.getY();

					viewPort.setViewPosition(new Point((int) positionX, (int) positionY));
				} else if (nextZoom < MIN_ZOOM)
					tiledMapComponent.setZoom(MIN_ZOOM);
				else
					tiledMapComponent.setZoom(MAX_ZOOM);
			}});

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(layeredPane, BorderLayout.CENTER);
		return centerPanel;
	}

	private JPanel createTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		JLabel departureLabel = new JLabel("Départ : "), dateAndTimeLabel = new JLabel("Date et heure : ");

		// ComboBox :
		TOP_STOP_BOX.setSelectedItem(INITIAL_STARTING_STOP_NAME);
		TOP_STOP_BOX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selectedStop = (String) TOP_STOP_BOX.getSelectedItem();
				if (selectedStop.equals(customStop.name()) && !customStopActive)
					JOptionPane.showMessageDialog(null, "Please tick \"Activate CustomStop\" (Options > Activate CustomStop).", "Error", JOptionPane.ERROR_MESSAGE);
				else
					setStartingStop(selectedStop);
			}});
		
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

		// Ajout des components :
		topPanel.add(departureLabel);
		topPanel.add(TOP_STOP_BOX);
		topPanel.add(separator);
		topPanel.add(dateAndTimeLabel);
		topPanel.add(dateAndTimeSpinner);

		return topPanel;
	}

	@SuppressWarnings("deprecation")
	private static JSpinner createDefaultJSpinner() {
		SpinnerModel model = new SpinnerDateModel(INITIAL_DATE.toJavaDate(), null, null, Calendar.MINUTE);
		JSpinner spinner = new JSpinner(model);
		java.util.Date javaDate = INITIAL_DATE.toJavaDate();
		javaDate.setHours(SecondsPastMidnight.hours(INITIAL_DEPARTURE_TIME));
		javaDate.setMinutes(SecondsPastMidnight.minutes(INITIAL_DEPARTURE_TIME));
		javaDate.setSeconds(SecondsPastMidnight.seconds(INITIAL_DEPARTURE_TIME));
		spinner.setValue(javaDate);
		return new JSpinner(model);
	}

	private JSlider createDefaultJSlider() {
		final JSlider slide = new JSlider(JSlider.VERTICAL, -MAX_UPDATES, MAX_UPDATES, 0);
		slide.setPaintTicks(true);
		slide.setMinorTickSpacing(50);
		slide.setMajorTickSpacing(100);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("Stop"));
		labelTable.put(new Integer(MAX_UPDATES), new JLabel("Forward"));
		labelTable.put(new Integer(-MAX_UPDATES), new JLabel("Reverse"));
		slide.setLabelTable(labelTable);
		slide.setPaintLabels(true);
		slide.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				sleepTime = slide.getValue();
			}});
		slide.setEnabled(animationEnable);
		return slide;
	}

	private JPanel createCopyrightPanel() {
		Icon tlIcon = new ImageIcon(getClass().getResource( "/images/tl-logo.png"));
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

		colors.add(new Color(255, 0, 0));
		colors.add(new Color(255, 255, 0));
		colors.add(new Color(0, 255, 0));
		colors.add(new Color(0, 0, 255));
		colors.add(new Color(0, 0, 0));

		ColorTable colorTable = new ColorTable(COLOR_SLICE_DURATION, colors);
		return colorTable;
	}

	private void updateCustomStop() {
		PointWGS84 point = new PointOSM(tiledMapComponent.zoom(), recentMapPosition.x + recentMousePosition.x, recentMapPosition.y + +recentMousePosition.y).toWGS84();
		customStop = new Stop("CustomStop", point);
		activeSetOfStops = createSetOfStops();
		customStopRecentlyChanged = true;
	}

	private void updateDate() {
		graph = getGraph(currentDate);
		updateStartingTime();
	}

	private void updateStartingTime() {
		updateFastestPathTree();
		updateTiledMapComponent();
	}

	private void updateGraph() {
		try {
			graph = READER.readGraphForServices(activeSetOfStops, TIMETABLE.servicesForDate(currentDate), WALKING_TIME, WALKING_SPEED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateFastestPathTree() {
		Stop startingStop = getStopFromName(activeSetOfStops, currentStartingStopName);
		currentFastestPathTree = getFastestPathTree(startingStop, currentDepartureTime);
		customStopRecentlyChanged = false;
	}

	private void updateTiledMapComponent() {
		List<TileProvider> tileProvidersList = new LinkedList<>();
		TileProvider isochroneTileProvider = new IsochroneTileProvider( currentFastestPathTree, activeColorTable, WALKING_SPEED);
		TileProvider transparentIsochrone = new CachedTileProvider( new TransparentTileProvider(isochroneTileProvider, ALPHA));
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
		if (!currentDate.equals(changedDate) && time >= 4 * 3600) {
			currentDepartureTime = time;
			currentDate = changedDate;
			updateDate();
		} else if (time < 4 * 3600) {
			Date dayBefore = changedDate.relative(-1);
			currentDepartureTime = time + 24 * 3600;
			if (!currentDate.equals(dayBefore)) {
				currentDate = dayBefore;
				updateDate();
			} else
				updateStartingTime();
		} else {
			currentDepartureTime = time;
			updateStartingTime();
		}
	}

	private void setStartingStop(String selectedItem) {
		currentStartingStopName = selectedItem;
		updateStartingStop();
	}

	private static TimeTable createTimeTable() {
		TimeTable timeTable = null;
		try {
			timeTable = READER.readTimeTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return timeTable;
	}

	private void animation() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (Math.abs(sleepTime) > 5 && animationEnable) {
						if (sleepTime > 0)
							dateAndTimeSpinner.setValue(dateAndTimeSpinner.getNextValue());
						else
							dateAndTimeSpinner.setValue(dateAndTimeSpinner.getPreviousValue());
						setDate((java.util.Date) dateAndTimeSpinner.getValue());
					}
					try {
						Thread.sleep(200 - Math.abs(sleepTime));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}}).start();
	}

	private Graph getGraph(Date date) {
		if (!cachedGraph.containsKey(date) || customStopRecentlyChanged) {
			updateGraph();
			cachedGraph.put(date, graph);
		}
		return cachedGraph.get(date);
	}

	private FastestPathTree getFastestPathTree(Stop startingStop, int departureTime) {
		return getGraph(currentDate).fastestPaths(startingStop, departureTime);
	}

	private static Vector<String> getOrderedStopsNamesInVector(Set<Stop> stops) {
		List<String> stopsNames = new ArrayList<>();

		for (Stop stop : stops)
			stopsNames.add(stop.name());

		Collections.sort(stopsNames);

		return new Vector<String>(stopsNames);
	}

	private static void updateViewer() {
		dateAndTimeSpinner.setValue(dateAndTimeSpinner.getNextValue());
		dateAndTimeSpinner.setValue(dateAndTimeSpinner.getPreviousValue());
	}
	
	private static boolean colorsAreEquals(int[] color1, int[] colorRef){
		return (colorRef[0] - 5 <= color1[0] && color1[0] <= colorRef[0] + 5)
				&& (colorRef[1] - 5 <= color1[1] && color1[1] <= colorRef[1] + 5)
				&& (colorRef[2] - 5 <= color1[2] && color1[2] <= colorRef[2] + 5);
	}
}
