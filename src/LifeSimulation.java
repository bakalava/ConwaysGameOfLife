// Program Name : Conway's Game of Life
// Author       : Sally Hui
// Course       : ICS4U1
// Teacher      : Jay, S.
// Date Created : December 24, 2013
// Description  : This program simulates lifeforms in a colony according to the 
//				  rules outlined by Conway's Game of Life 
//				  (available at http://www.math.com/students/wonders/life/life.html).
//                It provides tools such as rectangular marquee population/eradication
//                and freestyle drawing to allow the user to interact with the colony,
//                as well as preset colonies that can be loaded. The user can choose to
//                run the simulation or advance it manually. They can also save and load
//                text files that store a colony's cell information. Various settings can 
//                be changed--zoom, population/eradication success rate, and 
//                simulation speed. 

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class LifeSimulation extends JFrame implements ActionListener, ChangeListener
{   
	private ColonyPanel cPanel = new ColonyPanel (0.6);
	private Timer timer = new Timer (500, this); 
	private JButton advanceBtn = new JButton ("Advance");
	private JButton playBtn = new JButton ("Play");
	private JButton pauseBtn = new JButton ("Pause");
	private JButton loadBtn = new JButton ("Load File");
	private JButton saveBtn = new JButton ("Save File");
	private JSlider genSpeed = new JSlider (100, 900, 500);
	private JButton credit = new JButton ("Click me");
	private JSlider zoom = new JSlider (5, 100, 7);
	private JSlider popErad = new JSlider (0, 100, 85);
	private JPanel scrollCPanePanel = new JPanel ();
	private JScrollPane scrollCPane = new JScrollPane();
	private JButton[][] toolsButtons = new JButton [3][3];
	private CV CVPanel = new CV (cPanel.getColony().getGrid());
	private boolean startedFree = false, startedErad = false, startedPop = false;
	private int genNum = 0, cellNum = cPanel.getColony().getCellNum();
	private JLabel stats;
	private JToolBar topTBar = new JToolBar ();
	private JPanel panelWithToolbar = new JPanel ();
    private JToolBar bottomTBar = new JToolBar ();
	
	// Default constructor
	public LifeSimulation ()
	{
		// Set up window
		this.setTitle("Conway's Game of Life");
		setResizable(false);  
		setPreferredSize(new Dimension (900, 680));
		setMinimumSize(new Dimension (900, 680));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // open in middle of screen
		this.getContentPane().setLayout(new BorderLayout());  
		
		// Add action listeners to buttons
		advanceBtn.addActionListener(this);	
		playBtn.addActionListener(this);	
		pauseBtn.addActionListener(this);
		loadBtn.addActionListener(this);
		saveBtn.addActionListener(this);
		credit.addActionListener(this);
	    
		// Set up (top) options toolbar
		topTBar.setFloatable(false); // disable dragging toolbar off
		topTBar.add (saveBtn); // add buttons
		topTBar.add (loadBtn);
		topTBar.add (credit);
		
		// Tools pane 
		JPanel toolBox = new JPanel ();
		toolBox.setLayout(new GridLayout (0, 1));
		
		// Play controls
		JPanel controls = new JPanel (); 
		controls.add(advanceBtn);
		controls.add(playBtn);
		controls.add(pauseBtn);
				
		// General Settings (sliders)
		JPanel settings = new JPanel ();
		settings.setLayout(new BoxLayout (settings, BoxLayout.Y_AXIS)); // set alignment
	  
		genSpeed.setMajorTickSpacing(200); // Tick marks for generation speed slider
		genSpeed.setMinorTickSpacing(100);
		genSpeed.setPaintTicks(true);
		genSpeed.setPaintLabels(true);
		
		popErad.setMajorTickSpacing (10); // tick marks for population/eradication slider
		popErad.setMinorTickSpacing(5);
		popErad.setPaintTicks(true);
		popErad.setPaintLabels(true);
		
		genSpeed.addChangeListener (this); // add change listeners (what sliders use) to sliders
		popErad.addChangeListener(this);
		
		JPanel genLblPnl = new JPanel (); // JPanels (for formatting) of text in the left toolbox
		JPanel popEradLblPnl = new JPanel ();
		genLblPnl.setLayout (new BorderLayout ());
		popEradLblPnl.setLayout (new BorderLayout ());
		genLblPnl.add(new JLabel ("    Milliseconds per Gen. (update speed)"), BorderLayout.CENTER);
		popEradLblPnl.add (new JLabel ("    Population/Eradication Success %"), BorderLayout.CENTER);
		
		settings.add (genLblPnl); // add settings components of toolbox to settings
		settings.add (genSpeed);
		settings.add (popEradLblPnl);
		settings.add (popErad);
		settings.add(controls);  
		settings.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)); // add border
		
		// Brushes
		JPanel brushes = new JPanel ();
		brushes.setLayout (new GridLayout(3, 3));
		// Text in the JButtons
		toolsButtons [0][0] = new JButton ("Pop"); // Rectangular marquee for population
		toolsButtons [0][1] = new JButton ("Erad"); // Rectangular marquee for eradication
		toolsButtons [0][2] = new JButton ("Free"); // Freestyle drawing for population
		toolsButtons [1][0] = new JButton ("Clr"); // Clear screen 
		toolsButtons [1][1] = new JButton ("R-P"); // R-Pentomino (PATTERN)
		toolsButtons [1][2] = new JButton ("Gun"); // Glider Gun (PATTERN)
		toolsButtons [2][0] = new JButton ("Plsr"); // Pulsar (PATTERN)
		toolsButtons [2][1] = new JButton ("Die"); // Die Hard (PATTERN)
		toolsButtons [2][2] = new JButton ("Pffr"); // Puffers (PATTERN)
		
		for (int i = 0; i < toolsButtons.length; i++) 
		    for (int j = 0; j < toolsButtons[0].length; j++)
		    {
		    	toolsButtons [i][j].addActionListener (this); // add action listeners to buttons
		    	brushes.add (toolsButtons [i] [j]); // add buttons to the jpanel
		    }   	
		
		// create compound border (empty and etched) for brushes
		Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20); // create empty border
		Border raisedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED); // create etched border
		Border compound = BorderFactory.createCompoundBorder(emptyBorder, raisedBorder); // create compound border
		brushes.setBorder(compound);
		
		// Current View panel
		CVPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)); //add border to it
				
		// Add components to tools jpanel
		toolBox.add (CVPanel);
		toolBox.add (brushes);
		toolBox.add (settings);
			  
		// Add colony panel to a scrollpane which is added to a to a jpanel which is added to the main borderlayout's center
		scrollCPane = new JScrollPane (cPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCPane.setPreferredSize(new Dimension(675, 580));
		
		scrollCPanePanel = new JPanel ();
		scrollCPanePanel.add(scrollCPane);
			    
		// Add current status bar
		zoom.addChangeListener (this); // add change listener to zoom (a slider)
		panelWithToolbar = new JPanel (); // jpanel to hold the toolbar and the colony scrollpane
	    bottomTBar = new JToolBar (); // bottom toolbar (displays cell/gen # and zoom slider)
	    // set up toolbar
		bottomTBar.setFloatable(false); // disable dragging toolbar off
		bottomTBar.setLayout (new FlowLayout(FlowLayout.RIGHT));
		bottomTBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)); // add border
		
		stats = new JLabel ("Cells: " + cellNum + ", Generation: " + genNum); // text for toolbar
		bottomTBar.add(stats); // add the text
		bottomTBar.add (new JLabel ("| Zoom")); 
		bottomTBar.add(zoom); // add zoom slider
		
		panelWithToolbar.setLayout (new BorderLayout ()); // add things to the panel that includes the toolbar
		panelWithToolbar.add(scrollCPanePanel, BorderLayout.CENTER);
		panelWithToolbar.add(bottomTBar, BorderLayout.SOUTH);
				
		// Add components to JFrame
		this.getContentPane().add (topTBar, BorderLayout.NORTH);
		this.getContentPane().add (toolBox, BorderLayout.WEST);
		this.getContentPane().add(panelWithToolbar, BorderLayout.CENTER);
	}
	
	// Update screen (used to update current view box and jlabels in the stats bar)
	public void updateCmpt ()
	{
		CVPanel.setGrid (cPanel.getColony().getGrid()); // update colony/grid in current view box
		cellNum = cPanel.getColony().getCellNum(); // get updated number of cells for stats bar
		stats.setText("Cells: " + cellNum + " | Generation: " + genNum); // update the stats bar
		
		// Set buttons enabled if currently being used
		if (startedPop)
			toolsButtons[0][0].setEnabled(false);
		else
			toolsButtons[0][0].setEnabled(true);
		
		if (startedErad)
			toolsButtons[0][1].setEnabled(false);
		else
			toolsButtons[0][1].setEnabled(true);
		
		if (startedFree)
			toolsButtons[0][2].setEnabled(false);
		else
			toolsButtons[0][2].setEnabled(true);
		
		// update
		revalidate();
		repaint();
	}
	
	// Listens to ChangeEvents made by sliders
	public void stateChanged(ChangeEvent e) {
		Object origin = e.getSource(); // to check which slider made the ChangeEvent
		if (origin.equals (genSpeed)) // generation speed slider was moved
			timer.setDelay (genSpeed.getValue()); // set timer speed
		
		else if (origin.equals(zoom)) // zoom slider was moved
		{
			cPanel.setZoom(zoom.getValue()); // zoom in/out
			cPanel.show();
			cPanel.revalidate();
			scrollCPane.revalidate();
			scrollCPanePanel.revalidate();
			scrollCPanePanel.repaint();
		}
		
		else if (origin.equals (popErad)) // population/eradication rate slider was moved
		{
			Colony temp = cPanel.getColony(); // get colony from cPanel
			temp.setPopEradRate(popErad.getValue()); // set rate
			cPanel.setColony (temp); // update colony in cPanel
		}
	}
	
	// Customize file filter to only accept text files
	class TxtFilter extends FileFilter
	{
		 // Check if file is a text file or directory
		 public boolean accept(File check)  
		 {  
		  return check.getName().toLowerCase().endsWith(".txt") || check.isDirectory();  
		 }  
		 
		 // Set description for text file option
		 public String getDescription()  
		 {  
		  return ".txt Files";  
		 } 
	}
	
	// Allow user to save files
	public void save ()
    {
    	try
    	{
    		// Open a JFileChooser dialog to allow user to choose directory to save file
    		JFileChooser save = new JFileChooser ();
    		save.setFileFilter(new TxtFilter ()); // Set file filter (which only accepts text files)
    		int result = save.showSaveDialog (this); // to find out which button was clicked
    		
    		if (result == JFileChooser.APPROVE_OPTION) // OK was clicked
    		{
    			// check if file had appropiate file name ending; add if not
    			String fileName = save.getSelectedFile().getAbsolutePath(); 
    			if (!fileName.endsWith(".txt"))
    					fileName += ".txt";
    			File file = new File (fileName); // get file 
    			
	    		if (file.exists()) // file (or something with the same name) exists in that directory
	    		{
	    			// ask if user wants to save over
	    			int event = JOptionPane.showConfirmDialog(null, "The file you are trying to write already exists. Would you like to overwrite the file?", "Error", JOptionPane.YES_NO_OPTION);
	    			if (event == JOptionPane.YES_OPTION) // yep! 
	    			{
		    			String fileContent = cPanel.getColony().toString(); // produce file content
		    			// write file
			    		FileWriter fileWriter = new FileWriter (file.getAbsoluteFile());
			    		BufferedWriter bufferedWriter = new BufferedWriter (fileWriter);
			    		bufferedWriter.write (fileContent); // write contents of text file
			    		bufferedWriter.close (); // close buffered writer
	    			}
	    		}
	    		else // file does not already exist in that directory
	    		{
	    			String fileContent = cPanel.getColony().toString(); // produce contents of the text file
		    		FileWriter fileWriter = new FileWriter (file.getAbsoluteFile());
		    		BufferedWriter bufferedWriter = new BufferedWriter (fileWriter);
		    		bufferedWriter.write (fileContent); // write contents of text file
		    		bufferedWriter.close (); // close buffered writer
	    		}
    		}
    	}
    	catch (IOException e) // catch IOException
    	{
    		e.printStackTrace(); // help diagnose problem
    	}
    }
    
	// Allow user to load previously saved files
    public void load ()
    {
    	// Open a JFileChooser dialog to allow user to select a file to load
    	JFileChooser open = new JFileChooser ();
		open.setAcceptAllFileFilterUsed(false);
		open.setFileFilter(new TxtFilter ());
		int result = open.showOpenDialog(this); // find out which button was clicked
		
		boolean[][] temp = new boolean [100][100]; // temporary array to store loaded file's colony
		
		if (result == JFileChooser.APPROVE_OPTION) // OK was clicked
		{
			File file = new File (open.getSelectedFile().getAbsolutePath()); // get file from JFileChooser
			try 
			{
				int counter = 0; // Check if text file is legal
				Scanner readFile = new Scanner(new FileInputStream (file)); // initialize scanner to read file
				for (int i = 0;  i < temp.length && readFile.hasNextInt(); i++)
					for (int j = 0; j < temp[0].length && readFile.hasNextInt(); j++)
					{
						if (readFile.nextInt() == 1) // 1s represent true/has cell
							temp [i][j] = true;
						else // 0s represent false/no cells
							temp [i][j] = false;
						
						counter++;						
					}
				readFile.close(); // close scanner
				
				// Check if file is legal and update
				if (counter == temp.length * temp[0].length)
				{
					cPanel.setColony(temp); 
					cPanel.show();
				}
				else // file is illegal - inform user
					JOptionPane.showMessageDialog(null, "The file you have selected could be loaded.", "Error", JOptionPane.INFORMATION_MESSAGE);
			} 
			catch (FileNotFoundException e) // catch IOException
			{
				e.printStackTrace(); // help diagnose problem
			}	
		
			// Set generation number back to 0
			genNum = 0; 
		}		
    }
    
    // Load preset stencils
    public void loadStencil (int ID)
    {
    	try
    	{
    		File file = new File ("./stencils\\" + ID + ".txt"); // load file
    		System.out.println (this.getClass ().getResource ("/stencils/" + ID + ".txt").toString());
    		
    		boolean[][] temp = new boolean [100][100]; // temporary array to hold new colony
    		
    		Scanner readFile = new Scanner(new FileInputStream (file)); // initialize scanner to read file
			for (int i = 0;  i < temp.length && readFile.hasNextInt(); i++)
				for (int j = 0; j < temp[0].length && readFile.hasNextInt(); j++)
				{
					if (readFile.nextInt() == 1) // 1s represent true/has cell
						temp [i][j] = true;
					else // 0s represent false/no cells
						temp [i][j] = false;
				}
			readFile.close(); // close scanner
			
			cPanel.setColony(temp); // update colony
    	}
    	catch (FileNotFoundException e) // catch FileNotFoundException
    	{
    		e.printStackTrace();
    	} 
    	
    	// Set generation number back to 0
    	genNum = 0;
    	
    	if (ID == 4) // Clear was pressed; stop timer
    		timer.stop();
    }
    
    // Define what happens when action events are fired
	public void actionPerformed (ActionEvent e)
    {
        Object temp = (Object) e.getSource (); // which button was pressed
        
        if (temp.equals(advanceBtn)) // Advance was pressed
        {
			if (cPanel.getColony().anyCellsLeft())
			{
				genNum++;
				Colony tempColony = cPanel.getColony(); // get colony from colony jpanel
	        	tempColony.advance(); // advance the colony
	        	cPanel.setColony (tempColony); // update the colony in the colony jpanel
	        	cPanel.show();
			}
        }
        
        else if (temp.equals (playBtn)) // Play was pressed
        {
        	timer.start(); // start timer, which will fire action events for each advance
        }
        
        else if (temp.equals (pauseBtn)) // Pause was pressed
        {
        	timer.stop(); // stop timer
        }
        
        else if (temp.equals (saveBtn)) // Save was pressed
        {
        	save (); // Directs to save method
        }
        
        else if (temp.equals (credit)) // "Click me" was pressed
        {
        	loadStencil (0); // load credits
        }
        	
        else if (temp.equals (loadBtn)) // Load was pressed
        {
        	load (); // Directs to load method
        }
        
        else if (temp.equals (timer)) // Timer was pressed
        {
        	if (cPanel.getColony().anyCellsLeft())
        	{
        		Colony tempColony = cPanel.getColony(); // get colony from colony JPanel
        		tempColony.advance(); // advance colony
        		cPanel.setColony (tempColony); // update colony in colony JPanel
        		cPanel.show();
        		genNum++; // increment generation number
        		if (!cPanel.getColony().anyCellsLeft()) // no cells left; stop timer
        			timer.stop();
        	}
        }
        
        else if (temp.equals (toolsButtons [0] [0])) // Populate was pressed
        {
        	startedFree = false;
        	cPanel.startPopulate(); // tell colony JPanel to start tracking coordinates
        }
        
        else if (temp.equals (toolsButtons [0][1])) // Eradicate was pressed
        { 
        	startedFree = false;
        	cPanel.startEradicate(); // tell colony JPanel to start tracking coordinates
        }
        
        else if (temp.equals (toolsButtons [0][2])) // Freestyle was pressed
        { 
        	// start tracking coordinates for freestyle
        	if (startedFree)
        		startedFree = false; 
        	else
        		startedFree = true;
        	
        	// stop tracking for population/eradication if already tracking for those
        	startedPop = false;
        	startedErad = false;
        }
        
        else // one of the stencils were chosen
        {
        	startedFree = false; // stop tracking for freestyle/population/eradication
        	startedPop = false;
        	startedErad = false;
        	
        	// Find button that was pressed
        	boolean locBtn = false; // boolean to tell for loop to stop when button matched
        	for (int i = 0; i < toolsButtons.length && locBtn == false; i++)
        		for (int j = 0; j < toolsButtons[0].length && locBtn == false; j++)
        			if (temp.equals (toolsButtons[i][j]))
        			{
        				locBtn = true; // stop the for loop
        				loadStencil (i * 3 + j + 1); // load appropriate stencil
        			}
        }
    }
	
	// (main) JPanel that displays the colony
	class ColonyPanel extends JPanel implements MouseMotionListener, MouseListener {
		
		private Colony colony;
		private int popX1 = 0, popY1 = 0, popX2 = 0, popY2 = 0, zoom = 7;
		private boolean firstCoordinate;
		
		// Default constructor
		public ColonyPanel(double density) {
			
			// Initialize variables
			colony = new Colony (density, 85);
			
			// Set up JPanel
			setPreferredSize (new Dimension (zoom * colony.getGrid().length, zoom * colony.getGrid()[0].length));
			addMouseListener(this);
			addMouseMotionListener (this);
			setVisible(true);
			repaint();	
		}

		// Setter method for zoom
		public void setZoom (int zoom)
		{
			this.zoom = zoom;
			// set preferred size so the JScrollPane that holds the panel adjusts
			setPreferredSize (new Dimension (zoom * colony.getGrid().length, zoom * colony.getGrid()[0].length));
		}
		
		// Start tracking coordinates for populating
		public void startPopulate ()
		{
			startedPop = true;
			startedErad = false; // not tracking for eradication anymore
			firstCoordinate = true;
		}
		
		// Start tracking coordinates for eradication
		public void startEradicate ()
		{
			startedErad = true;
			startedPop = false; // not tracking for population anymore
			firstCoordinate = true;
		}
		
		// Getter method to return colony
		public Colony getColony ()
		{
			return colony;
		}
		
		// Setter method to set colony
		public void setColony (Colony newColony)
		{
			colony = newColony;
		}
		
		// Setter method to set colony (still in array form)
		public void setColony (boolean[][] temp)
	    {
	    	colony.setColony(temp);
	    }
		
		// Call repaint
		public void show() {
			repaint();
		}
	    
		// Override paintComponent to draw graphics
		public void paintComponent(Graphics g) {
			super.paintComponent(g); // override paintComponent
			
			boolean[][] colonyInfo = colony.getGrid();
			
			// Draw life forms/empty spaces
			for (int row = 0; row < colonyInfo.length; row++) // go through colony with nested for loop
				for (int col = 0; col < colonyInfo[0].length; col++) {
					if (colonyInfo[row][col]) // life
						g.setColor(Color.green);
					else
						g.setColor(Color.black); // empty space
					g.fillRect(col * zoom, row * zoom, zoom, zoom); // draw appropiately coloured rectangle
				}
			
			// Draw grid
			g.setColor(Color.black);
			for (int i = 0; i < colonyInfo.length; i++)
			{
				g.drawLine (0, i * zoom, colonyInfo.length * zoom, i * zoom);
				g.drawLine(i * zoom, 0, i * zoom, colonyInfo.length * zoom);
			}

			// Rectangle that shows where the user is populating/eradicating
			if (startedPop || startedErad) 
			{
				// Find start/end coordinates of rectangle
				int startX = Math.min(popX1, popX2);  
	            int startY = Math.min(popY1, popY2);
	            int endX = Math.max(popX1, popX2);
	            int endY = Math.max(popY1, popY2);
	            
	            if (startedPop) // Red colour scheme for highlighting box (eradicating)
	            {
		            g.setColor(new Color(149,231,185, 100)); // set colour of highlighting box
		            g.fillRect (startX, startY, endX - startX, endY - startY); // draw rectangle
		            
		            g.setColor(new Color(11,93,107, 220)); // set colour outer highlighting rectangle
		            g.drawRect (startX, startY, endX - startX, endY - startY); // draw outer highlighting rectangle
	            }
	            else // Blue colour scheme for highlighting box (populating)
	            {
		            g.setColor(new Color(237,108,76, 100)); // set colour of highlighting box
		            g.fillRect (startX, startY, endX - startX, endY - startY); // draw rectangle
		            
		            g.setColor(new Color(151,38,34,220)); // set colour outer highlighting rectangle
		            g.drawRect (startX, startY, endX - startX, endY - startY); // draw outer highlighting rectangle
	            }
			}
			
			updateCmpt(); // update current view box and JLabel
			
		}

		// Mouse dragged event
		public void mouseDragged(MouseEvent e) {
			if (startedPop || startedErad) // if populating/eradicating, keep track of coordinates
			{
				if (firstCoordinate) // where user started dragging
				{
					popX1 = e.getX();
					popY1 = e.getY();
					firstCoordinate = false;
				}
				// where user is currently at
				popX2 = e.getX();
				popY2 = e.getY();		
				repaint();
			}
			else if (startedFree)
			{
				// Check for if user has gone out of bounds
				if ((e.getX() / zoom >= 0) && (e.getX()/ zoom < colony.getGrid().length) && (e.getY() / zoom >= 0) && (e.getY()/ zoom < colony.getGrid().length))
				{
					colony.populateOne (e.getX() / zoom, e.getY() / zoom); // put cell into location
					revalidate();
					repaint();
				}
			}
		}
		
		// Mouse moved event
		public void mouseMoved(MouseEvent e) {
			
		}

		// Mouse clicked event
		public void mouseClicked(MouseEvent e) {
		}
		
		// Mouse entered event
		public void mouseEntered(MouseEvent arg0) {

		}

		// Mouse exited the tracking area event
		public void mouseExited(MouseEvent arg0) {

		}

		// Mouse pressed event
		public void mousePressed(MouseEvent arg0) {

		}
		
		// Mouse released from dragging event
		public void mouseReleased(MouseEvent arg0) {
			if (startedPop || startedErad) // if populating/eradicating, this is the final position of rectangle
			{			
				// Determine start/end points of population/eradication area
				int startX = Math.min(popX1 / zoom, popX2 / zoom);  
	            int startY = Math.min(popY1 / zoom, popY2 / zoom);
	            int endX = Math.max(popX1 / zoom, popX2 / zoom);
	            int endY = Math.max(popY1 / zoom, popY2 / zoom);
	            
	            // Check if the user is completely out of bounds to the right or below (do nothing)
	            if (startX < colony.getGrid().length && endX > 0 && startY < colony.getGrid()[0].length && endY > 0)
	            {	
		            // Check and correct for if the user has one end of the rectangle out of bounds
		            if (startX < 0)
		            	startX = 0;
		            else if (startX >= colony.getGrid().length)
		            	startX = colony.getGrid().length - 1;
		            
		            if (endX < 0)
		            	endX = 0;
		            else if (endX >= colony.getGrid().length)
		            	endX = colony.getGrid().length - 1;
		            
		            if (startY < 0)
		            	startY = 0;
		            else if (startY >= colony.getGrid().length)
		            	startY = colony.getGrid().length - 1;
		            
		            if (endY < 0)
		            	endY = 0;
		            else if (endY >= colony.getGrid().length)
		            	endY = colony.getGrid().length - 1;
		            
		            if (startedPop) // populate area
		            {
						colony.populate(startX, endX, startY, endY); 
		            }
		            
		            else // eradicate area
		            {
			            colony.eradicate(startX, endX, startY, endY);
		            }            
		            
					repaint();
	            }
	            
	            // Reset values to keep populating/eradicating
	            firstCoordinate = true;
	            popX1 = 0; 
	            popY1 = 0; 
	            popX2 = 0; 
	            popY2 = 0;      
			}

		}
	}
	
	// Main method
    public static void main (String [] args)
    {
    	// Set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// do nothing
		} catch (ClassNotFoundException e) {
			// do nothing
		} catch (InstantiationException e) {
			// do nothing
		} catch (IllegalAccessException e) {
			// do nothing
		}
    	LifeSimulation window = new LifeSimulation (); // new window
    	window.setVisible (true);
    }
}
