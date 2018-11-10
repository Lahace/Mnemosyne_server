package ap.mnemosyne.servlets;

import ap.mnemosyne.database.GetTasksByUserDatabase;
import ap.mnemosyne.database.GetUserDefinedParametersDatabase;
import ap.mnemosyne.database.UpdateTaskDatabase;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.places.PlacesManager;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.util.ServletUtils;
import ap.mnemosyne.util.TimeUtils;
import ap.mnemosyne.util.Tuple;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HintsServlet extends AbstractDatabaseServlet
{
	private final int LOCATION_RADIUS_METERS = 150;
	private final int TIME_NOTICE_MINUTES = 30;
	private final int TIME_MAX_SLACK_MINUTES = 15;
	private final int LOCATION_INTEREST_DISTANCE_METERS = 800;
	//private final int
	private final Logger LOGGER = Logger.getLogger(HintsServlet.class.getName());
	private PlacesManager pman;

	public void init(ServletConfig config) throws ServletException
	{
		LOGGER.setLevel(Level.INFO);
		super.init(config);
		LOGGER.info("Initializing ParseServlet..");
		pman = new PlacesManager();
		LOGGER.info("Initializing ParseServlet.. Done");
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		if(!ServletUtils.checkContentType(MediaType.APPLICATION_FORM_URLENCODED, req, res)) return;
		LOGGER.info("Incoming request..");
		//Checking for parameters correctness
		User user = (User) req.getSession(false).getAttribute("current");
		int cellID = -1;
		LocalTime phoneTime = null;
		try
		{
			phoneTime = LocalTime.parse(req.getParameter("time"));
			cellID = Integer.parseInt(req.getParameter("cellID"));
		}
		catch (DateTimeParseException | NullPointerException dtpe)
		{
			LOGGER.info("Bad request: Phone time not specified or badly formatted (Use HH:MM format)");
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Phone time not specified or badly formatted (Use HH:MM format)"), res, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		catch(NumberFormatException nfe)
		{
			if(req.getParameter("cellID") != null)
			{
				LOGGER.info("Bad request: cellID must be an integer");
				ServletUtils.sendMessage(new Message("Bad Request",
						"400", "cellID must be an integer"), res, HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		double lat = -1;
		double lon = -1;
		try
		{
			lat = Double.parseDouble(req.getParameter("lat"));
		}
		catch (NumberFormatException nfe)
		{
			LOGGER.info("Bad request: \"lat\" must be a number");
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "\"lat\" must be a number"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (NullPointerException npe) {}

		try
		{
			lon = Double.parseDouble(req.getParameter("lon"));
		}
		catch (NumberFormatException nfe)
		{
			LOGGER.info("Bad request: \"lon\" must be a number");
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "\"lon\" must be a number"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (NullPointerException npe) {}

		String ssid = req.getParameter("ssid");

		if(lat <0 && lon < 0 && ssid == null && cellID < 0)
		{
			LOGGER.info("Bad request: Please send lat and lon and/or one (or both) of ssid and cellID");
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Please send lat and lon and/or one (or both) of ssid and cellID"), res, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		else if(lat < 0 && lon < 0 && (ssid == null  || cellID  < 0))
		{
			LOGGER.info("Not Acceptable: Parameters sent are not sufficient, specify lat and lon (ssid: " + ssid + " cellID: " + cellID + ")");
			ServletUtils.sendMessage(new Message("Not Acceptable",
					"406", "Parameters sent are not sufficient, specify lat and lon"), res, HttpServletResponse.SC_NOT_ACCEPTABLE);
			return;
		}
		else if((lat >= 0 && lon < 0) || (lat < 0 && lon >= 0))
		{
			LOGGER.info("Bad Request: Please specify both lat and lon");
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Please specify both lat and lon"), res, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		//END check

		ParamsName position = null;
		try
		{
			Map<ParamsName, Parameter> plist = new GetUserDefinedParametersDatabase(getDataSource().getConnection(), user)
					.getUserDefinedParameters();
			if (lat >= 0 && lon >= 0)
			{
				position = resolvePosition(plist, new Point(lat, lon), phoneTime);
			}
			else
			{
				position = resolvePosition(plist, ssid, cellID, phoneTime);
				if (position == null)
				{
					LOGGER.info("Not Acceptable: Parameters sent are not sufficient (position is null), specify lat and lon");
					ServletUtils.sendMessage(new Message("Not Acceptable",
							"406", "Parameters sent are not sufficient (position is null), specify lat and lon"), res, HttpServletResponse.SC_NOT_ACCEPTABLE);
					return;
				}
			}

			List<Task> tasks = new GetTasksByUserDatabase(getDataSource().getConnection(), user).getTasksByUser();
			List<Hint> doable = new ArrayList<>();

			for(Task t : tasks)
			{
				LOGGER.info("Examining task: " + t.getName());
				if(!t.isDoneToday() && !t.isFailed())
				{
					if(position != ParamsName.location_work || (position == ParamsName.location_work && t.isPossibleAtWork()))
					{
						if(t.getConstr() == null)
						{
							//TODO
						}
						else if (t.getConstr() instanceof TaskTimeConstraint)
						{
							Tuple<Place, Integer> nearest = null;
							int timeToNearest;
							Point my;

							switch (t.getConstr().getType())
							{
								case at:
									LOGGER.info("Found: " + t.getConstr().getType());
									if(((TaskTimeConstraint) t.getConstr()).getFromTime().plusMinutes(TIME_MAX_SLACK_MINUTES).isBefore(phoneTime))
									{
										LOGGER.info("Task has failed");
										setTaskFailed(t, user);
										break;
									}
									//computing nearest place where to satisfy this task
									//At this point, position == null means that we already asked for lat/lon
									nearest = null;
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) plist.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(position != null)
										my = ((LocationParameter) plist.get(position)).getLocation();
									else
										my = new Point(lat,lon);

									timeToNearest = pman.getMinutesToDestination(my, nearest.getLeft().getCoordinates());

									if(((TaskTimeConstraint) t.getConstr()).getFromTime().plusMinutes(TIME_MAX_SLACK_MINUTES).isBefore(phoneTime.plusMinutes(timeToNearest)))
									{
										LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
										doable.add(new Hint(t.getId(), nearest.getLeft() ,true));
									}
									else if(phoneTime.isAfter(((TaskTimeConstraint) t.getConstr()).getFromTime().plusMinutes(TIME_MAX_SLACK_MINUTES)
											.minusMinutes(timeToNearest).minusMinutes(TIME_NOTICE_MINUTES)))
									{
										LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
										doable.add(new Hint(t.getId(), nearest.getLeft(), false));
									}

									break;

								case before:
									LOGGER.info("Found: " + t.getConstr().getType());
									if(((TaskTimeConstraint) t.getConstr()).getFromTime().isBefore(phoneTime))
									{
										LOGGER.info("Task has failed");
										setTaskFailed(t, user);
										break;
									}

									nearest = null;
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) plist.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(position != null)
										my = ((LocationParameter) plist.get(position)).getLocation();
									else
										my = new Point(lat,lon);

									timeToNearest = pman.getMinutesToDestination(my, nearest.getLeft().getCoordinates());

									if(((TaskTimeConstraint) t.getConstr()).getFromTime().isBefore(phoneTime.plusMinutes(TIME_NOTICE_MINUTES).plusMinutes(timeToNearest)))
									{
										LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
										doable.add(new Hint(t.getId(), nearest.getLeft(), true));
									}
									else if(nearest.getRight()<=LOCATION_INTEREST_DISTANCE_METERS)
									{
										LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
										doable.add(new Hint(t.getId(), nearest.getLeft(), false));
									}

									break;

								case after:
									LOGGER.info("Found: " + t.getConstr().getType());
									Place last = TimeUtils.findLatestOpenedPlace(t.getPlacesToSatisfy());
									if(last.getClosing().isBefore(phoneTime))
									{
										LOGGER.info("Task has failed");
										setTaskFailed(t, user);
										break;
									}

									nearest = null;
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) plist.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(position != null)
										my = ((LocationParameter) plist.get(position)).getLocation();
									else
										my = new Point(lat,lon);

									timeToNearest = pman.getMinutesToDestination(my, nearest.getLeft().getCoordinates());

									if(last.getClosing().isBefore(phoneTime.plusMinutes(TIME_NOTICE_MINUTES).plusMinutes(timeToNearest)))
									{
										LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
										doable.add(new Hint(t.getId(), nearest.getLeft(), true));
									}
									else if(nearest.getRight()<=LOCATION_INTEREST_DISTANCE_METERS && phoneTime.isAfter(((TaskTimeConstraint) t.getConstr()).getFromTime()))
									{
										LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
										doable.add(new Hint(t.getId(), nearest.getLeft(), false));
									}

									break;
							}
						}
						else if (t.getConstr() instanceof TaskPlaceConstraint)
						{
							//TODO
						}
					}
				}
			}

			new ResourceList<>(doable).toJSON(res.getOutputStream());
		}
		catch (SQLException sqle)
		{
			LOGGER.severe("SQLException: " + sqle.getMessage() + " -> Code: " + sqle.getErrorCode() + " State: " + sqle.getSQLState());
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		catch (TransformException e)
		{
			LOGGER.severe("TransformException: " + e.getMessage());
			ServletUtils.sendMessage(new Message("TransformException",
					"500", e.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		catch (FactoryException e)
		{
			LOGGER.severe("FactoryException: " + e.getMessage());
			ServletUtils.sendMessage(new Message("FactoryException",
					"500", e.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.severe("ClassNotFoundException: " + e.getMessage());
			ServletUtils.sendMessage(new Message("ClassNotFoundException",
					"500", e.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		LOGGER.info("Incoming request.. Completed");
	}

	private ParamsName resolvePosition(Map<ParamsName, Parameter> plist, String ssid, int cellID, LocalTime givenTime)
	{
		LocationParameter house = (LocationParameter) plist.get(ParamsName.valueOf("location_house"));
		LocationParameter work = (LocationParameter) plist.get(ParamsName.valueOf("location_work"));
		TimeParameter workTime = (TimeParameter) plist.get(ParamsName.valueOf("time_work"));

		if(house.getSSID().equals(ssid) && house.getCellID() == cellID)
		{
			LOGGER.info("User is at its house (parameters are ssid: " + ssid + " cellID: " + cellID + " time: " + givenTime + ")");
			return ParamsName.location_house;
		}
		else if(work.getSSID().equals(ssid) && work.getCellID() == cellID && TimeUtils.isTimeBetween(givenTime, workTime.getFromTime(), workTime.getToTime()))
		{
			LOGGER.info("User is at its workplace (parameters are ssid: " + ssid + " cellID: " + cellID + " time: " + givenTime + ")");
			return ParamsName.location_work;
		}

		return null;
	}

	private ParamsName resolvePosition(Map<ParamsName, Parameter> plist, Point givenPoint, LocalTime givenTime) throws TransformException, FactoryException
	{
		org.locationtech.jts.geom.Point gisGivenPoint = givenPoint.toJTSPoint();
		Point housePoint = ((LocationParameter) plist.get(ParamsName.valueOf("location_house"))).getLocation();
		Point workPoint = ((LocationParameter) plist.get(ParamsName.valueOf("location_work"))).getLocation();
		TimeParameter workTime = (TimeParameter) plist.get(ParamsName.valueOf("time_work"));

		if(workTime == null)
		{
			//just set an impossible work time
			//AKA starts and finishes at the same time
			workTime = new TimeParameter(ParamsName.valueOf("time_work"), null,
					LocalTime.of(0,0), LocalTime.of(0,0));
		}

		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

		int houseDistance = distanceInMeters(givenPoint, housePoint);
		int workDistance = distanceInMeters(givenPoint, workPoint);

		if(workDistance <= LOCATION_RADIUS_METERS && TimeUtils.isTimeBetween(givenTime, workTime.getFromTime(), workTime.getToTime()))
		{
			LOGGER.info("User is at its workplace (parameters are lat: " + givenPoint.getLat() + " lon: " + givenPoint.getLon() +
					" distance: " + houseDistance + " time: " + givenTime + ")");
			return ParamsName.location_work;
		}
		else if(houseDistance <= LOCATION_RADIUS_METERS)
		{
			LOGGER.info("User is at its house (parameters are lat: " + givenPoint.getLat() + " lon: " + givenPoint.getLon() +
					" distance: " + houseDistance + " time: " + givenTime + ")");
			return ParamsName.location_house;
		}

		LOGGER.info("User seems to be outside (parameters are lat: " + givenPoint.getLat() + " lon: " + givenPoint.getLon() +
				" distance from house: " + houseDistance + " distance from work: " + workDistance + " time: " + givenTime + ")");
		return null;
	}

	private int distanceInMeters(Point one, Point two) throws FactoryException, TransformException
	{
		org.locationtech.jts.geom.Point gisPointOne = one.toJTSPoint();
		org.locationtech.jts.geom.Point gisPointTwo = two.toJTSPoint();
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

		return (int) JTS.orthodromicDistance(gisPointOne.getCoordinate(), gisPointTwo.getCoordinate(), sourceCRS);
	}

	private Tuple<Place, Integer> getClosestToMeFromList(Point myPosition, Set<Place> plist) throws FactoryException, TransformException
	{
		Tuple<Place, Integer> pair = null;

		if(plist.isEmpty()) return null;

		int dist = 0;
		for(Place p : plist)
		{
			dist = distanceInMeters(p.getCoordinates(), myPosition);

			if(pair == null || dist<pair.getRight())
			{
				pair = new Tuple<>(p, dist);
			}
		}
		return pair;
	}

	private void setTaskFailed(Task t, User u) throws SQLException, IOException
	{
		Task tNew = new Task(t.getId(), t.getUser(),t.getName(),t.getConstr(), t.isPossibleAtWork(), t.isRepeatable(), t.isDoneToday(), true, t.getPlacesToSatisfy());
		new UpdateTaskDatabase(getDataSource().getConnection(), tNew, u).updateTask();
	}
}
