package ch.epfl.isochrone.gui;


import static ch.epfl.isochrone.timetable.Date.daysInMonth;
import static ch.epfl.isochrone.timetable.Stop.getStopFromName;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.Date.Month;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

@SuppressWarnings("serial")
public class ItineraireComponent extends JPanel{
	private static final TimeTableReader READER = new TimeTableReader("/time-table/");
	private final int WALKING_TIME;
    private final double WALKING_SPEED;
	private final TimeTable TIMETABLE;
	private final JComboBox<Integer> minsBox, hoursBox, daysBox, monthsBox, yearsBox;
	private final JComboBox<String> departureCombo, arrivalCombo;
	private final JButton goButton;
	private final JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private final JLabel label = new JLabel();
	
	private Graph currentGraph;
	private FastestPathTree currentFastestPathTree;
	private int departureTime, fastestPathTreeBuidingTime, timeArriving;
	private Date startingDate, graphBuildingDate;
	private Stop startingStop, arrivalStop;
	private boolean departureStopChanged = false;
	private final MeteoPanel meteoPanel;
	
	public ItineraireComponent(TimeTable timetable, Vector<String> stopsNamesInVector, String initialStopName, Date initialStartingDate, int initialStartingTime, int WALKING_TIME, double WALKING_SPEED){
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(300, 300));
		scrollPane.setPreferredSize(new Dimension(200,150));

		this.WALKING_TIME = WALKING_TIME;
		this.WALKING_SPEED = WALKING_SPEED;
		TIMETABLE = timetable;
		startingDate = initialStartingDate;
		graphBuildingDate = initialStartingDate;
		startingStop = arrivalStop = getStopFromName(TIMETABLE.stops(), initialStopName);
		departureTime = initialStartingTime;
		
		meteoPanel = new MeteoPanel(startingStop, startingDate, SecondsPastMidnight.hours(departureTime), SecondsPastMidnight.minutes(departureTime), TIMETABLE.stops());
		
		// ComboBox :
		minsBox = createMinsBox();
		hoursBox = createHoursBox();
		daysBox = createDaysComboBox();
		monthsBox = createMonthsComboBox();
		yearsBox = createYearComboBox();
		
		departureCombo = createStopsCombo(stopsNamesInVector, initialStopName);
		arrivalCombo   = createStopsCombo(stopsNamesInVector, initialStopName);
		departureCombo.setPreferredSize(new Dimension(120, 20));
		arrivalCombo.setPreferredSize(new Dimension(120, 20));
		departureCombo.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				startingStop = getStopFromName(TIMETABLE.stops(), (String)departureCombo.getSelectedItem());
				departureStopChanged = true;
			}
			
		});
		arrivalCombo.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				arrivalStop = getStopFromName(TIMETABLE.stops(), (String)arrivalCombo.getSelectedItem());
			}
			
		});
		
		updateGraph();
		goButton = createSearchButton(this);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		
		topPanel.add(placeItems());
		topPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		topPanel.add(meteoPanel);
		
		this.add(topPanel, BorderLayout.NORTH);
	}	
	
	/**
	 * 
	 * @return
	 * 			Fourni un JPanel où les composants sont placés (notamment les ComboBox);
	 */
	private JPanel placeItems() {
		GridBagLayout layout = new GridBagLayout();
		JPanel itineraireConstructionPanel = new JPanel();
		itineraireConstructionPanel.setLayout(layout);
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel 	depart 		= new JLabel("Départ :"), 
				arrivee 	= new JLabel("Arrivée :"),
				dateText	= new JLabel("Date :"),
				heureText	= new JLabel("Heure :");
		
		JPanel datePanel = new JPanel();
		datePanel.add(dateText);
		datePanel.add(daysBox);
		datePanel.add(monthsBox);
		datePanel.add(yearsBox);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		
		itineraireConstructionPanel.add(datePanel, gbc);
		
		JPanel heurePanel = new JPanel();
		heurePanel.add(heureText);
		heurePanel.add(hoursBox);
		heurePanel.add(minsBox);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		itineraireConstructionPanel.add(heurePanel, gbc);
	
		JPanel departPanel = new JPanel();
		departPanel.add(depart);
		departPanel.add(departureCombo);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		itineraireConstructionPanel.add(departPanel, gbc);
		
		JPanel arriveePanel = new JPanel();
		arriveePanel.add(arrivee);
		arriveePanel.add(arrivalCombo);
		
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		
		itineraireConstructionPanel.add(arriveePanel, gbc);
		
		gbc.gridx = 6;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		
		itineraireConstructionPanel.add(goButton, gbc);
		return itineraireConstructionPanel;
	}
	
	/**
	 * 
	 * @return
	 * 			Crée la ComboBox des heures;
	 */
	private JComboBox<Integer> createHoursBox() {
		Vector<Integer> hourVector = new Vector<>();
		final JComboBox<Integer> hoursBox;
		
		for(int i = 0; i < 24; i++){
			hourVector.add(i);
		}
		
		hoursBox = new JComboBox<>(hourVector);
		hoursBox.setSelectedItem(SecondsPastMidnight.hours(departureTime));
		hoursBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setTime(SecondsPastMidnight.fromHMS((int)hoursBox.getSelectedItem(), SecondsPastMidnight.minutes(departureTime), SecondsPastMidnight.seconds(departureTime)));
			}
			
		});
		
		return hoursBox;
	}

	/**
	 * 
	 * @return
	 * 			Crée la ComboBox des minutes;
	 */
	private JComboBox<Integer> createMinsBox() {
		Vector<Integer> minVector = new Vector<>();
		final JComboBox<Integer> minsBox;
		
		for(int i = 0; i < 60; i++){
			minVector.add(i);
		}
		
		minsBox = new JComboBox<>(minVector);
		minsBox.setSelectedItem(SecondsPastMidnight.minutes(departureTime));
		minsBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setTime(SecondsPastMidnight.fromHMS(SecondsPastMidnight.hours(departureTime), (Integer)minsBox.getSelectedItem(), SecondsPastMidnight.seconds(departureTime)));
			}
			
		});
		
		return minsBox;
	}
	
	/**
	 * 
	 * @return
	 * 			Crée la ComboBox des Stop;
	 */
	private JComboBox<String> createStopsCombo(Vector<String> stopsNamesInVector, String initialStopName) {
		JComboBox<String> stopsBox = new JComboBox<>(stopsNamesInVector);
		stopsBox.setSelectedItem(initialStopName);
		
		return stopsBox;
	}
	

	/**
	 * 
	 * @param date
	 * 				Date à mettre en paramètres pour le calcul du trajet. 
	 */
	public void setDate(Date date){
		startingDate = date;
		updateComboBoxes();
	}
	
	/**
	 * 
	 * @param time
	 * 				Heure du départ à mettre en paramètre.
	 */
	public void setTime(int time){
		departureTime = time;
	}
	
	/**
	 * 	Mises à jours des boîtes en fonction de la date (nombre de jours dans le mois, etc.).
	 */
	private void updateComboBoxes() {
		int daysInMonth = daysInMonth(startingDate.month(), startingDate.year());
		
		if(daysBox.getItemCount() != daysInMonth){
			if(daysBox.getItemCount() == 31){
				for(int i = 30; i >= daysInMonth; i--){
					daysBox.removeItemAt(i);
				}
			}else{
				if(daysBox.getItemCount() == 30){
					if(startingDate.month() != Month.FEBRUARY){
						daysBox.addItem(31);
					}else{
						daysBox.addItem(29);
						daysBox.addItem(30);
						daysBox.addItem(31);
					}
				}else{
					if(startingDate.month() != Month.FEBRUARY)
						for(int i = daysBox.getItemCount() + 1; i <= daysInMonth; i++)
							daysBox.addItem(i);
				}
			}
		}
		
		monthsBox.setSelectedItem(startingDate.intMonth());
		yearsBox.setSelectedItem(startingDate.year());
	}
	
	/**
	 * 
	 * @param newGraph
	 * 					Nouveau graphe à utiliser.
	 */
	public void setGraph(Graph newGraph){
		currentGraph = newGraph;
		graphBuildingDate = startingDate;
	}
	
	/**
	 * 
	 * @return
	 * 			Retourne la ComboBox ayant un nombre de jours adéquat pour le mois et l'année sélectionnée.
	 */
	private JComboBox<Integer> createDaysComboBox(){
		Vector<Integer> dayVector = new Vector<>();
		final JComboBox<Integer> daysBox;
		startingDate.month();
		int daysInThisMonth = daysInMonth(startingDate.month(), startingDate.year());
		
		for(int i = 1; i <= daysInThisMonth; i++){
			dayVector.add(i);
		}
		
		daysBox = new JComboBox<>(dayVector);
		daysBox.setSelectedItem(startingDate.day());
		daysBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int daysInMonth = daysInMonth(startingDate.month(), startingDate.year());
				
				if((int) daysBox.getSelectedItem() > daysInMonth){
					startingDate = new Date(daysInMonth, startingDate.month(),startingDate.year());
					daysBox.setSelectedItem(daysInMonth);
					setDate(new Date(daysInMonth, startingDate.month(), startingDate.year()));
				}else{
					setDate(new Date((int) daysBox.getSelectedItem(), startingDate.month(), startingDate.year()));
				}
			}
			
		});
		return daysBox;
	}
	
	/**
	 * 
	 * @return
	 * 			Retourne la ComboBox des mois, en chiffres.
	 */
	private JComboBox<Integer> createMonthsComboBox(){
		Vector<Integer> monthVector = new Vector<>();

		for(int i = 1; i <= 12; i++){
			monthVector.add(i);
		}
		
		final JComboBox<Integer> monthBox = new JComboBox<>(monthVector);
		monthBox.setSelectedItem(startingDate.intMonth());
		monthBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Month selectedMonth = Date.intToMonth((int) monthsBox.getSelectedItem());
				int daysInMonth = daysInMonth(selectedMonth, startingDate.year());
				
				if(startingDate.day() > daysInMonth){
					daysBox.setSelectedItem(daysInMonth);
					setDate(new Date(daysInMonth, selectedMonth, startingDate.year()));
				}else{
					setDate(new Date((int) daysBox.getSelectedItem(), selectedMonth, startingDate.year()));
				}
				setDate(new Date(startingDate.day(), selectedMonth, startingDate.year()));
			}
			
		});
		return monthBox;
	}
	
	/**
	 * 
	 * @return
	 * 			Retourne la ComboBox des années (ici de l'an 1 à 2500, 
	 * 			ce qui n'est pas très utile mais c'est pour montrer que le code est valable quelque soit la date).
	 */
	private JComboBox<Integer> createYearComboBox(){
		Vector<Integer> yearVector = new Vector<>();
		final JComboBox<Integer> yearsBox;
		
		for(int i = 0; i < 2500; i++){
			yearVector.add(i);
		}
		
		yearsBox = new JComboBox<>(yearVector);
		yearsBox.setSelectedItem(startingDate.year());
		yearsBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedYear = (int)yearsBox.getSelectedItem();
				int daysInMonth = daysInMonth(startingDate.month(), selectedYear);
				if(startingDate.day() > daysInMonth){
					daysBox.setSelectedItem(daysInMonth);
					setDate(new Date(daysInMonth , startingDate.intMonth(), selectedYear));
				}else
					setDate(new Date(startingDate.day() , startingDate.intMonth(), selectedYear));
			}
			
		});
		
		return yearsBox;
	}
	
	/**
	 * 
	 * @param iti
	 * 				iti est "this" en pratique, ce qui permet d'avoir accès à la méthode launchSearch();
	 * @return
	 */
	private JButton createSearchButton(final ItineraireComponent iti){
		JButton goButton = new JButton("Go!");
		goButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				iti.launchSearch();
			}

		});
		
		return goButton;
	}
	
	/**
	 *  Lance une recherche d'itinéraire le plus proche de l'heure indiquée (création du graphe, du FastestPathTree).
	 *  Ecrit dans une JScrollPane les résultats.
	 */
	private void launchSearch() {
		boolean datesAreEquals = startingDate.equals(graphBuildingDate);
		boolean timesAreEquals = (departureTime == fastestPathTreeBuidingTime);
		
		if(!datesAreEquals || departureStopChanged){
			updateGraph();
			updateFastestPathTree();
			departureStopChanged = false;
		}else{
			if(!timesAreEquals)
				updateFastestPathTree();
		}
		
		JTextArea textPanel   = new JTextArea();
		
		try {
			timeArriving = currentFastestPathTree.arrivalTime(arrivalStop);
			
			List<Stop> pathTo = currentFastestPathTree.pathTo(arrivalStop);
			for (Stop stop : pathTo) {
				int stopArrivalTime = currentFastestPathTree.arrivalTime(stop);
				String arrivalTimeString = "";
				if (stopArrivalTime > 24*3600)
					arrivalTimeString = SecondsPastMidnight.toString(stopArrivalTime - 24 * 3600);
				else
					arrivalTimeString = SecondsPastMidnight.toString(stopArrivalTime);
				textPanel.append(stop.name() + " arrival at : " + arrivalTimeString + "\n");
			}
			label.setText("Heure d'arrivée estimée : " + SecondsPastMidnight.toString(timeArriving));
		} catch(IllegalArgumentException e) {
			label.setText("Stop non atteignable");
		}
		textPanel.revalidate();
		scrollPane.setViewportView(textPanel);
		scrollPane.revalidate();
		
		if (timeArriving > 24 * 3600) {
			timeArriving -= 24 * 3600;
		}
		

		updateMeteoPanels();
		
		this.add(label, BorderLayout.CENTER);
		this.add(scrollPane, BorderLayout.SOUTH);
		this.revalidate();
		
	}
	
	/**
	 * Met à jour le panneau Météo avec les nouveaux paramètres.
	 */
	private void updateMeteoPanels() {
		meteoPanel.updateMeteoPanel(startingStop, arrivalStop, startingDate, SecondsPastMidnight.hours(departureTime), SecondsPastMidnight.minutes(departureTime));
	}
	
	/**
	 * Met à jour le FastestPathTree avec les nouveaux paramètres.
	 */
	private void updateFastestPathTree() {
		currentFastestPathTree = currentGraph.fastestPaths(startingStop, departureTime);
		fastestPathTreeBuidingTime = departureTime;
	}
	
	/**
	 * Met à jour le Graph avec les nouveaux paramètres.
	 */
	private void updateGraph() {
		try {
			currentGraph = READER.readGraphForServices(TIMETABLE.stops(), TIMETABLE.servicesForDate(startingDate), WALKING_TIME, WALKING_SPEED);
			graphBuildingDate = startingDate;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Rafraichit le visualiseur de trajet.
	 */
	public void refresh() {
		scrollPane.repaint();
	}
}
