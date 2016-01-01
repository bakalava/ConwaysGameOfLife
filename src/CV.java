import java.awt.Color;
import java.awt.Graphics;

// Displays current status of entire board 
import javax.swing.JPanel;

class CV extends JPanel
{
	boolean[][] currentGrid; // colony
	
	// Default constructor
	public CV (boolean[][] currentGrid)
	{
		this.currentGrid = currentGrid;
		repaint();
	}

	// Setter method for grid (colony)
	public void setGrid (boolean[][] currentGrid)
	{
		this.currentGrid = currentGrid;
	}
	
	// Custom paint onto the JPanel
	public void paintComponent (Graphics g)
	{
		super.paintComponent (g); // override default paintComponent
		// Draws lifeforms/empty spaces
		for (int row = 0; row < currentGrid.length; row++)
			for (int col = 0; col < currentGrid[0].length; col++) {
				if (currentGrid[row][col]) // life
				g.setColor(Color.green); // life is green!
				else
					g.setColor(Color.black); // Death. is black.
				g.fillRect(col * 2 + 7, row * 2 + 5, 2, 2); // draw appropriately coloured rectangle
			}
		
		// Draw grid
		g.setColor(Color.black);
		for (int i = 0; i < currentGrid.length; i++)
		{
			g.drawLine (0 + 7, i * 2 + 5, currentGrid.length * 2 + 7, i * 2 + 5);
			g.drawLine(i * 2 + 7, 0 + 5, i * 2 + 7, currentGrid.length * 2 + 5);
		}

	}
}
