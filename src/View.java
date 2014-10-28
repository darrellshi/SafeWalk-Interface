/**
 * Project 7 -- SafeWalk Monitor
 *
 * @author hu247 (Hanxiao Hu)
 * @author shi180 (Qinxin Shi)
 *
 * @lab section number: L13
 *
 * @date April 15, 2014
 *
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;

import javax.swing.*;

import java.util.Iterator;

public class View extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Model model;
	private Image mapImage;
	private Image volunteerImage;
	private Image requesterImage;

	final static int DIAMETER = 16; // diameter of circle at each location

	final static float dash1[] = { 10.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
	Graphics2D g2;
	float scaleWidth; // Width Parameter for resizing the image
	float scaleHeight; // Height Parameter for resizing the image

	public View(Model model) {
		this.model = model;

		mapImage = loadImage("CampusCropped-Faded.jpg");
		volunteerImage = loadImage("volunteer.jpg");
		requesterImage = loadImage("request.jpg");

		JFrame frame = new JFrame("SafeWalkView");
		JPanel mainPanel = new JPanel(new BorderLayout());

		// Adding the canvas (this) to the main panel at the CENTER...
		mainPanel.add(this, BorderLayout.CENTER);
		frame.add(mainPanel);

		JScrollPane pane = new JScrollPane(this);
		frame.add(pane);

		frame.getContentPane().add(pane);
		frame.setSize(675,700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void paintComponent(Graphics gr) {
		synchronized (model.lock) {
			g2 = (Graphics2D) gr;

			scaleWidth = this.getWidth() / (float) mapImage.getWidth(null);
			scaleHeight = this.getHeight() / (float) mapImage.getHeight(null);

			g2.drawImage(mapImage, 0, 0, getWidth(), getWidth(), null);

			drawLocation();
		    drawIntransit();
		}
	}

	private void drawLocation() {
		int x = 0;
		int y = 0;
		Iterator<Location> it = (model.getLocations()).iterator();

		while (it.hasNext()) {
			Location location = (Location) it.next();
			x = (int) Math.round(location.getXY()[0]);
			y = (int) Math.round(location.getXY()[1]);

			g2.setColor(Color.BLACK);
			g2.fillOval(Math.round(x * scaleWidth),
					    Math.round(y * scaleHeight), DIAMETER, DIAMETER);
			g2.setColor(Color.YELLOW);
			g2.fillOval(Math.round(x * scaleWidth) + 2,
					    Math.round(y * scaleHeight) + 2, DIAMETER - 4, DIAMETER - 4);
			g2.setColor(Color.BLACK);

			g2.setFont(new Font("Arial", Font.PLAIN, 12));
			
			int dy = g2.getFontMetrics().getHeight() * 2;
			y += dy / 2;

			y += dy;
			Iterator<Request> requests = location.getRequests().iterator();
			while (requests.hasNext()) {
				Request req = (Request) requests.next();
				drawName(requesterImage, req.getName() + " " + 
				         req.getDestination().getName() + " " + 
						req.getValue(), x, y);
				y += dy;
			}

			y += dy;
			Iterator<Volunteer> volunteers = location.getVolunteers().iterator();
			while (volunteers.hasNext()) {
				Volunteer vol = (Volunteer) volunteers.next();
				drawName(volunteerImage, vol.getName(), x, y);
				y += dy;
			}
		}
	}

	private void drawName(Image image, String name, int x, int y) {
		int gap = g2.getFontMetrics().getHeight();

		g2.drawImage(image, Math.round(x * scaleWidth) - gap,
				Math.round(y * scaleHeight), 12, 12, null);
		g2.drawString(name, Math.round(x * scaleWidth),
				Math.round(y * scaleHeight) + gap - 3);
	}

	private void drawIntransit() {
		
		int xStart = 0;
		int yStart = 0;
		int xEnd = 0;
		int yEnd = 0;
		
		Iterator<Volunteer> volunteers = model.getVolunteers().iterator();
		
		while (volunteers.hasNext()) {
			Volunteer volunteer = volunteers.next();
			// find the volunteer that is in the movement
			if (volunteer.getCurrentLocation() == null) {
				
				double[] start = volunteer.getStart();
				double[] end = volunteer.getDestination();
				xStart = (int) Math.round(start[0]);
				yStart = (int) Math.round(start[1]);
				xEnd = (int) Math.round(end[0]);
				yEnd = (int) Math.round(end[1]);
				
				// Moving
				if (volunteer.getRequester() == null) {
					// Draw diagonal dashed line between start and end positions...
		            g2.setStroke(dashed);
		            g2.drawLine(Math.round(xStart * scaleWidth) + DIAMETER / 2,
		                        Math.round(yStart * scaleHeight) + DIAMETER / 2,
		                        Math.round(xEnd * scaleWidth) + DIAMETER / 2,
		                        Math.round(yEnd * scaleHeight) + DIAMETER / 2);

		            double[] xy = volunteer.getCurrentPosition();
		            int x = (int) Math.round(xy[0]);
		            int y = (int) Math.round(xy[1]);
		            
		         // draw the volunteer name
		            g2.setFont(new Font("Arial", Font.PLAIN, 16));
					g2.setColor(Color.BLACK);
		            int gap = g2.getFontMetrics().getHeight();
		            g2.drawString(volunteer.getName(),
		            		Math.round(x * scaleWidth) + gap,
		            		Math.round(y * scaleHeight) + gap);
		         // Draw filled circle at mover (volunteer) location on dashed
		            // line...
		            g2.setColor(Color.RED);
		            g2.fillOval(Math.round(x * scaleWidth),
		                        Math.round(y * scaleHeight), DIAMETER, DIAMETER);
				}
					
				// Walking
				else {
					// Draw diagonal dashed line between start and end positions...
		            g2.setStroke(dashed);
		            g2.drawLine(Math.round(xStart * scaleWidth) + DIAMETER / 2,
		                        Math.round(yStart * scaleHeight) + DIAMETER / 2,
		                        Math.round(xEnd * scaleWidth) + DIAMETER / 2,
		                        Math.round(yEnd * scaleHeight) + DIAMETER / 2);

		            double[] xy = volunteer.getCurrentPosition();
		            int x = (int) Math.round(xy[0]);
		            int y = (int) Math.round(xy[1]);
		            
		         // draw the volunteer and walker name
		            g2.setFont(new Font("Arial", Font.PLAIN, 16));
					g2.setColor(Color.BLACK);
		            int gap = g2.getFontMetrics().getHeight();
		            g2.drawString(volunteer.getName() + " walking with " + volunteer.getRequester(),
		            		Math.round(x * scaleWidth) + gap,
		            		Math.round(y * scaleHeight) + gap);
		         // Draw filled circle at mover (volunteer) location on dashed
		            // line...
		            g2.setColor(Color.GREEN);
		            g2.fillOval(Math.round(x * scaleWidth),
		                        Math.round(y * scaleHeight), DIAMETER, DIAMETER);
					
				}

			}
		}


	}

	private Image loadImage(String name) {
		URL url = getClass().getResource(name);
		if (url == null)
			throw new RuntimeException("Could not find " + name);
		return new ImageIcon(url).getImage();
	}

}
