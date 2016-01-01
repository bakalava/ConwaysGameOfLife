import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Colony 
{
    private boolean grid[] []; // stores information of entire colony
    private double popEradRate; // how successful population/eradication the marquee tool is for population/eradication

    // Default constructor
    public Colony (double density, double popEradRate)
    {
		grid = new boolean [100] [100]; // set size of grid to 100 * 100
		this.popEradRate = popEradRate;
		
		// Fill with cells/spaces
		for (int row = 0 ; row < grid.length ; row++) // go through colony with nested for loop
		    for (int col = 0 ; col < grid [0].length ; col++)
		    	grid [row] [col] = Math.random () < density; // if the float returned is less than the density, put in a cell
    }

    // Setter method for population/eradication rate
    public void setPopEradRate (double popEradRate)
    {
    	this.popEradRate = popEradRate;
    }
    
    // Returns number of cells on board
    public int getCellNum ()
    {
    	int counter = 0;
    	for (int i = 0; i < grid.length; i++) // go through colony with nested for loop
    		for (int j = 0; j < grid[0].length; j++)
    			if (grid [i][j]) // found a cell
    				counter++;
    	return counter;
    }
    
    // Checks if there are any cells left on the board
    public boolean anyCellsLeft ()
    {
    	boolean anyCells = false;
    	for (int i = 0; i < grid.length&& anyCells == false; i++) // go through colony with nested for loop
    		for (int j = 0; j < grid[0].length && anyCells == false; j++)
    			if (grid [i][j]) // found a cell
    				anyCells = true;
    	return anyCells;
    }
    
    // Determine if a cell will live in the next generation
    public boolean live (int row, int column)
    {
    	boolean isAlive = false; 
    	int counter = 0; // Counter for number of neighbours
    	
    	// Check above
    	if (column > 0)
    	{
    		// Upper left
    		if (row > 0)
    			if (grid [row - 1] [column - 1])
    				counter++;
    		// Upper right
    		if (row < grid.length - 1)
    			if (grid [row + 1] [column - 1])
    				counter++;
    		// Directly above
    		if (grid [row][column - 1])
    			counter++;
    	}
    	
    	// Check below
    	if (column < (grid[0].length - 1))
    	{
    		// Lower left
    		if (row > 0)
    			if (grid [row - 1][column + 1])
    				counter++;
    		// Lower right
    		if (row < grid.length - 1)
    			if (grid [row + 1] [column + 1])
    				counter++;
    		// Directly below
    		if (grid [row][column + 1])
    			counter++;
    	}
    	
    	// Check left
    	if (row > 0)
    		if (grid [row - 1][column])
    			counter++;
    	
    	// Check right
    	if (row < grid.length - 1)
    		if (grid [row + 1][column])
    			counter++;
    	
    	// Determine fate of the cell
    	if (!grid [row][column]) // originally dead
    	{
    		if (counter == 3)
    			isAlive = true;
    	}
    	else
    	{
    		if ((counter == 2) || (counter == 3)) // if 2 or 3 neighbours, a living cell keeps living
    			isAlive = true;
    	}
    	
    	return isAlive;
    }
    
    // Add cells to an area
    public void populate (int x1, int x2, int y1, int y2)
    {
    	// Go through part of array/grid selected
    	for (int i = y1; i <= y2; i++) 
    		for (int j = x1; j <= x2; j++)
    		{
    			if (!grid[i][j]) // if the array element does not have a cell
    				grid [i] [j] = Math.random () * 100 < popEradRate; 
    		}
    }
    
    // Delete cells in an area
    public void eradicate (int x1, int x2, int y1, int y2)
    {
    	// Go through part of array/grid selected
    	for (int i = y1; i <= y2; i++)
    		for (int j = x1; j <= x2; j++)
    		{
    			if (grid [i] [j]) // if the array element contains a cell 
    				grid [i] [j] = Math.random () * 100 > popEradRate;
    		}
    }
    
    // Populates one square (for freestyle population)
    public void populateOne (int y, int x) 
    {
    	grid [x] [y] = true;
    }
    
    // Produce next generation of cells in colony
    public void advance ()
    {
    	boolean[][] temp = new boolean [grid.length][grid[0].length]; // temporary array to store new generation
    	
    	// Copy grid to temp
    	for (int i = 0; i < grid.length; i++) // go through colony with nested for loop
    		for (int j = 0; j < grid[0].length; j++)
    			temp [i] [j] = grid [i] [j];
    	
    	// Determine new generation
    	for (int i = 0; i < grid.length; i++) // go through colony with nested for loop
    		for (int j = 0; j < grid[i].length; j++)
    		{
    			if (live (i, j)) // the cell lives
    				temp [i] [j] = true;
    			else // the cell dies
    				temp [i][j] = false;
    		}
    			
    	grid = temp; // update grid
    }
    
    // Update colony
    public void setColony (boolean[][] temp)
    {
    	grid = temp;
    }
    
    // Getter method for colony's array
    public boolean[][] getGrid ()
    {
    	return grid;
    }
    
    // Converts colony to a text representation to store in text file later
    public String toString ()
    {
    	String arrayString = "";
    	
    	// Produce text representation
    	for (int i = 0; i < grid.length; i++) // go through colony with nested for loop
    		for (int j = 0; j < grid[i].length; j++)
    		{
    			// 1s represent cells and 0s represent spaces
    			if (grid[i][j])
    				arrayString += " 1"; // space to separate numbers (so that Scanner's nextInt function can read it)
    			else
    				arrayString += " 0";
    		}
    	
    	return arrayString;
    }
}
