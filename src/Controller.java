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

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingWorker;

public class Controller extends SwingWorker implements Observer {
	private static Model model;
	private static View view;
	private Connector connector;
	private static final String KEY = "k839651";
	private static final String NICKNAME = "darrellshi";
	private static final String HOST = "pc.cs.purdue.edu";
	private static final int PORT = 1337;

	Controller(Model model, View view) {
		this.model = model;
		this.view = view;
		connector = new Connector(HOST, PORT, String.format("connect %s", KEY),
				this);
		execute();
	}

	class MessageHandler {
		public void handleMessage(String message) {
			String[] fields = message.split(" ");

			if (fields[0].equals("location"))
				handleLocationMessage(fields);
			else if (fields[0].equals("request"))
				handleRequestMessage(fields);
			else if (fields[0].equals("volunteer"))
				handleVolunteerMessage(fields);
			else if (fields[0].equals("moving"))
				handleMovingMessage(fields);
			else if (fields[0].equals("walking"))
				handleWalkingMessage(fields);
			else if (fields[0].equals("delete"))
				handleDeleteMessage(fields);
			else if (fields[0].equals("error"))
				handleErrorMessage(fields);
			else if (fields[0].equals("reset"))
				handleResetMessage(fields);
		}

		private void handleLocationMessage(String[] fields) {
			String buildingName = (String) fields[1];
			double x = Double.valueOf(fields[2]);
			double y = Double.valueOf(fields[3]);
			new Location(model, buildingName, x, y);
		}

		private void handleRequestMessage(String[] fields) {
			String name = fields[1];
			String fromBuildingName = fields[2];
			String toBuildingName = fields[3];
			int value = Integer.valueOf(fields[4]);
			new Request(model, name, model.getLocationByName(fromBuildingName),
					model.getLocationByName(toBuildingName), value);
		}

		private void handleVolunteerMessage(String[] fields) {
			String name = fields[1];
			String locationName = fields[2];
			int score = Integer.valueOf(fields[3]);
			double[] xy = model.getLocationByName(locationName).getXY();
			if(model.getVolunteers().contains(model.getVolunteerByName(name)))
				model.getVolunteerByName(name).setCurrentLocation(model.getLocationByName(locationName));
			else
				new Volunteer(model, name, score, new Location(model, locationName,
					xy[0], xy[1]));
		}

		private void handleMovingMessage(String[] fields) {
			String volunteerName = fields[1];
			String fromBuildingName = fields[2];
			String toBuildingName = fields[3];
			long transit = Long.valueOf(fields[4]);
			if (model.getVolunteerByName(volunteerName).getCurrentLocation()
					.equals(model.getLocationByName(fromBuildingName))) {
				model.getVolunteerByName(volunteerName).requester = null;
				model.getVolunteerByName(volunteerName).startMoving(
						model.getLocationByName(toBuildingName), transit);
 
			}
		}

		private void handleWalkingMessage(String[] fields) {
			String volunteerName = fields[1];
			String reqesterName = fields[2];
			String fromBuildingName = fields[3];
			String toBuildingName = fields[4];
			long transit = Long.valueOf(fields[5]);
			if (model.getVolunteerByName(volunteerName).getCurrentLocation()
					.equals(model.getLocationByName(fromBuildingName))) {
				model.getVolunteerByName(volunteerName).startWalking(
						model.getRequestByName(reqesterName), transit);

			}
		}

		private void handleDeleteMessage(String[] fields) {
			try {
				model.removeVolunteer(model.getVolunteerByName(fields[1]));
			} catch (Exception e) { // "Volunteer " + messages[1] +
				// " is not in the list."
			}
		}

		private void handleErrorMessage(String[] fields) {
			System.out.println("The previous request caused an error!");
			System.out.println("error text: " + fields[1]);
		}

		private void handleResetMessage(String[] fields) {
			System.out
					.println("The system has been reset. All clients should reset internal information about the state of the system.");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		synchronized (model.lock) {
			String message = (String) arg;
			MessageHandler messageHandler = new MessageHandler();
			messageHandler.handleMessage(message);
		}
	}

	@Override
	protected Object doInBackground() throws Exception {
		while (true) {
			Thread.sleep(100);
			view.repaint();
		}
	}

}
