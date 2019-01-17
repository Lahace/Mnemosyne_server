package ap.mnemosyne.servlets;

import ap.mnemosyne.database.*;
import ap.mnemosyne.enums.NormalizedActions;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.places.PlacesManager;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.util.ServletUtils;
import ap.mnemosyne.util.TimeUtils;
import ap.mnemosyne.util.Tuple;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.util.MapEntry;
import org.joda.time.LocalTime;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Null;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HintsServlet extends AbstractDatabaseServlet
{
	public final int LOCATION_RADIUS_METERS = 150;
	public final int LOCATION_RADIUS_BEFORE_METERS = 2000;
	public final int LOCATION_RADIUS_BEFORE_URGENT_METERS = 500;
	public final int LOCATION_INTEREST_DISTANCE_METERS = 50;
	public final int LOCATION_INTEREST_DISTANCE_METERS_CRITICAL = 500;
	public final int TIME_NOTICE_MINUTES = 20;
	public final int TIME_NOTICE_MINUTES_CRITICAL = 30;
	public final int TIME_MAX_SLACK_MINUTES = 10;
	public final int TIME_EVENTS_BEFORE_BED_MINUTES = 20;
	public final int TIME_EVENTS_BEFORE_BED_MINUTES_CRITICAL = 5;
	public final double MULTIPLIER_TIME_NOTICE = 0.2;

	private final Logger LOGGER = Logger.getLogger(HintsServlet.class.getName());
	private PlacesManager pman;
	private Map<Integer, Boolean> confirmMap = new HashMap<>();

	public void init(ServletConfig config) throws ServletException
	{
		//CHECKING CONSTANTS CORRECTNESS (to avoid human (me) error)
		if(!(LOCATION_RADIUS_METERS<LOCATION_RADIUS_BEFORE_URGENT_METERS))
		{
			throw new InvalidParameterException("LOCATION_RADIUS_METERS must be < than LOCATION_RADIUS_BEFORE_URGENT_METERS");
		}
		if(!(LOCATION_RADIUS_BEFORE_URGENT_METERS<LOCATION_RADIUS_BEFORE_METERS))
		{
			throw new InvalidParameterException("LOCATION_RADIUS_BEFORE_URGENT_METERS must be < than LOCATION_RADIUS_BEFORE_METERS");
		}

		LOGGER.setLevel(Level.INFO);
		super.init(config);
		LOGGER.info("Initializing HintsServlet..");
		pman = new PlacesManager();
		LOGGER.info("Initializing HintsServlet.. Done");
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
		Map<ParamsName, Parameter> userParametersMap;
		try
		{
			 userParametersMap = new GetUserDefinedParametersDatabase(getDataSource().getConnection(), user)
					.getUserDefinedParameters();
			if (lat >= 0 && lon >= 0)
			{
				position = resolvePosition(userParametersMap, new Point(lat, lon), phoneTime);
			}
			else
			{
				position = resolvePosition(userParametersMap, ssid, cellID, phoneTime);
				if (position == null)
				{
					LOGGER.info("Not Acceptable: Parameters sent are not sufficient (position is null), specify lat and lon");
					ServletUtils.sendMessage(new Message("Not Acceptable",
							"406", "Parameters sent are not sufficient (position is null), specify lat and lon"), res, HttpServletResponse.SC_NOT_ACCEPTABLE);
					return;
				}
			}

			List<Task> tasks = new GetTasksByUserDatabase(getDataSource().getConnection(), user).getTasksByUser();
			Set<Hint> doable = new HashSet<>();
			Point myPoint;
			if(position != null)
				myPoint = ((LocationParameter) userParametersMap.get(position)).getLocation();
			else
				myPoint = new Point(lat,lon);

			LocationParameter prevPlaceParameter = (LocationParameter) new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(), user, ParamsName.location_previous)
					.getUserDefinedParameterByName();

			ParamsName prevPosition = null;
			if(prevPlaceParameter != null)
			{
				prevPosition = resolvePosition(userParametersMap, prevPlaceParameter.getLocation(), phoneTime);
			}

			for(Task t : tasks)
			{
				LOGGER.info("Examining task: " + t.getName());
				if(confirmMap.get(t.getId()) != null) synchronized (confirmMap){confirmMap.put(t.getId(), false);}
				if(!t.isDoneToday() && !t.isFailed() && !t.isIgnoredToday())
				{
					LocalTime fromLunch = ((TimeParameter) userParametersMap.get(ParamsName.time_lunch)).getFromTime();
					LocalTime toLunch = ((TimeParameter) userParametersMap.get(ParamsName.time_lunch)).getToTime();
					if(position != ParamsName.location_work || t.isPossibleAtWork() || TimeUtils.isTimeBetween(phoneTime, fromLunch, toLunch))
					{
						Tuple<Place, Integer> nearest = null;
						int timeToNearest;
						Place latestClosing;
						int timeToLatest;
						LocalTime fromBed;
						LocalTime toBed;
						int intrestDistanceMeters, timeNoticeMinutes, timeEventsBeforeBed;
						if(confirmMap.get(t.getId()) != null) synchronized (confirmMap){confirmMap.put(t.getId(), true);} //i've seen this task, it has not been deleted and it supposedly still requires confirmation
						if(t.isCritical())
						{
							intrestDistanceMeters = LOCATION_INTEREST_DISTANCE_METERS_CRITICAL;
							timeNoticeMinutes = TIME_NOTICE_MINUTES_CRITICAL;
							timeEventsBeforeBed = TIME_EVENTS_BEFORE_BED_MINUTES_CRITICAL;
						}
						else
						{
							intrestDistanceMeters = LOCATION_INTEREST_DISTANCE_METERS;
							timeNoticeMinutes = TIME_NOTICE_MINUTES;
							timeEventsBeforeBed = TIME_EVENTS_BEFORE_BED_MINUTES;
						}

						if(t.getConstr() == null)
						{
							ParamsName taskPos = null;
							LocalTime toWork = ((TimeParameter) userParametersMap.get(ParamsName.time_work)).getToTime();
							if(t.getPlacesToSatisfy().isEmpty())
							{
								taskPos = ParamsName.location_any;
							}
							else if(t.getPlacesToSatisfy().size() == 1)
							{
								taskPos = resolvePosition(userParametersMap, t.getPlacesToSatisfy().iterator().next().getCoordinates(), phoneTime);
								if(taskPos == null)
								{
									taskPos = ParamsName.location_item;
								}
							}
							else
							{
								//For now, this does not support multiple work places/houses
								taskPos = ParamsName.location_item;
							}

							switch (taskPos)
							{
								case location_any:
									//Tricky to manage
									LOGGER.info("Found: " + taskPos);
									fromBed = ((TimeParameter) userParametersMap.get(ParamsName.time_bed)).getFromTime();
									if(fromBed.isBefore(phoneTime.plusMinutes(timeEventsBeforeBed)))
									{
										LOGGER.info("Task has failed (Asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}

									if(fromBed.isBefore(phoneTime.plusMinutes(timeEventsBeforeBed).plusMinutes(timeNoticeMinutes)))
									{
										LOGGER.info("Adding to doable, urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null ,true));
									}
									else
									{
										LOGGER.info("Adding to doable, non urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null ,false));
									}
									break;

								case location_item:
									LOGGER.info("Found: " + taskPos);
									nearest = null;
									latestClosing = TimeUtils.findLatestOpenedPlace(t.getPlacesToSatisfy());
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) userParametersMap.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(nearest != null) timeToNearest = pman.getMinutesToDestination(myPoint, nearest.getLeft().getCoordinates());
									else timeToNearest = 0;

									if((phoneTime.isAfter(toWork) && position == ParamsName.location_house && prevPosition != ParamsName.location_house) ||
											(phoneTime.isAfter(latestClosing.getClosing())))
									{
										LOGGER.info("Task has failed (Asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}

									if((phoneTime.isAfter(toWork) && distanceInMeters(myPoint, ((LocationParameter) userParametersMap.get(ParamsName.location_house)).getLocation())
											<= LOCATION_RADIUS_BEFORE_METERS) || (phoneTime.plusMinutes(timeNoticeMinutes).plusMinutes(timeToNearest).isAfter(latestClosing.getClosing())))
									{
										LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), nearest.getLeft() ,true));
									}
									else if(distanceInMeters(myPoint, nearest.getLeft().getCoordinates()) <= intrestDistanceMeters)
									{
										LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), nearest.getLeft() ,false));
									}

									break;

								case location_work:
									LOGGER.info("Found: " + taskPos);
									if((phoneTime.isAfter(toWork) && position != ParamsName.location_work))
									{
										LOGGER.info("Task has failed (Asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}

									if(toWork.isBefore(phoneTime.plusMinutes(timeNoticeMinutes)))
									{
										LOGGER.info("Adding to doable, urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null ,true));
									}
									else if(position == ParamsName.location_work)
									{
										LOGGER.info("Adding to doable, non urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null ,false));
									}
									break;

								case location_house:
									LOGGER.info("Found: " + taskPos);
									fromBed = ((TimeParameter) userParametersMap.get(ParamsName.time_bed)).getFromTime();
									if(fromBed.isBefore(phoneTime.plusMinutes(timeEventsBeforeBed)))
									{
										LOGGER.info("Task has failed (Asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}

									if(fromBed.isBefore(phoneTime.plusMinutes(timeEventsBeforeBed).plusMinutes(timeNoticeMinutes)))
									{
										LOGGER.info("Adding to doable, urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null ,true));
									}
									else if(position == ParamsName.location_house)
									{
										LOGGER.info("Adding to doable, non urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null ,false));
									}
									break;
							}
						}
						else if (t.getConstr() instanceof TaskTimeConstraint)
						{
							LocalTime toTime = ((TaskTimeConstraint) t.getConstr()).getToTime();
							LocalTime fromTime = ((TaskTimeConstraint) t.getConstr()).getFromTime();
							switch (t.getConstr().getType())
							{
								case at:
									LOGGER.info("Found: " + t.getConstr().getType() + " (time)");
									if((toTime != null && toTime.plusMinutes(TIME_MAX_SLACK_MINUTES).isBefore(phoneTime))
											|| (toTime == null && fromTime.plusMinutes(TIME_MAX_SLACK_MINUTES).isBefore(phoneTime)))
									{
										LOGGER.info("Task has failed (Asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}
									//computing nearest place where to satisfy this task
									//At this point, position == null means that we already asked for lat/lon
									nearest = null;
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) userParametersMap.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(nearest != null) timeToNearest = pman.getMinutesToDestination(myPoint, nearest.getLeft().getCoordinates());
									else timeToNearest = 0;

									if((toTime != null && toTime.isBefore(phoneTime))
											|| (toTime == null && fromTime.isBefore(phoneTime.plusMinutes(timeToNearest)))) //not totally sure about this
									{
										if(nearest != null)
										{
											LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), nearest.getLeft(), true));
										}
										else
										{
											LOGGER.info("Adding to doable, urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), null, true));
										}
									}
									else if((toTime != null && fromTime.isBefore(phoneTime))
											|| (toTime == null && phoneTime.plusMinutes(timeToNearest).isAfter(fromTime.plusMinutes(TIME_MAX_SLACK_MINUTES).minusMinutes(timeNoticeMinutes))))
									{
										if(nearest != null)
										{
											LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), nearest.getLeft(), false));
										}
										else
										{
											LOGGER.info("Adding to doable, non urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), null, false));
										}
									}

									break;

								case before:
									LOGGER.info("Found: " + t.getConstr().getType());
									if(fromTime.isBefore(phoneTime))
									{
										LOGGER.info("Task has failed (Asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}

									nearest = null;
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) userParametersMap.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(nearest != null) timeToNearest = pman.getMinutesToDestination(myPoint, nearest.getLeft().getCoordinates());
									else timeToNearest = 0;

									if(fromTime.isBefore(phoneTime.plusMinutes(timeNoticeMinutes).plusMinutes(timeToNearest)))
									{
										if(nearest != null)
										{
											LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), nearest.getLeft(), true));
										}
										else
										{
											LOGGER.info("Adding to doable, urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), null, true));
										}
									}
									else if(nearest != null && nearest.getRight()<=intrestDistanceMeters)
									{
										LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), nearest.getLeft(), false));
									}
									else if(nearest == null)
									{
										LOGGER.info("Adding to doable, non urgent");
										synchronized (confirmMap){confirmMap.remove(t.getId());}
										doable.add(new Hint(t.getId(), null, false));
									}

									break;

								case after:
									LOGGER.info("Found: " + t.getConstr().getType());
									latestClosing = TimeUtils.findLatestOpenedPlace(t.getPlacesToSatisfy());
									fromBed = ((TimeParameter) userParametersMap.get(ParamsName.time_bed)).getFromTime();
									if((latestClosing != null && latestClosing.getClosing().isBefore(phoneTime)) || (fromBed.isBefore(phoneTime.plusMinutes(timeEventsBeforeBed))))
									{
										LOGGER.info("Task has failed (asking for confirmation)");
										synchronized (confirmMap){confirmMap.put(t.getId(), true);}
										doable.add(new Hint(t.getId(), null ,true, true));
										//setTaskFailed(t, user);
										break;
									}

									nearest = null;
									if(position != null)
									{
										nearest = getClosestToMeFromList(((LocationParameter) userParametersMap.get(position)).getLocation(), t.getPlacesToSatisfy());
									}
									else
									{
										nearest = getClosestToMeFromList(new Point(lat,lon), t.getPlacesToSatisfy());
									}

									if(nearest != null) timeToLatest = pman.getMinutesToDestination(myPoint, nearest.getLeft().getCoordinates());
									else timeToLatest = 0;

									if((latestClosing != null && latestClosing.getClosing().isBefore(phoneTime.plusMinutes(timeNoticeMinutes).plusMinutes(timeToLatest))) ||
											(fromBed.isBefore(phoneTime.plusMinutes(timeEventsBeforeBed).plusMinutes(timeNoticeMinutes))))
									{
										if(nearest != null)
										{
											LOGGER.info("Adding to doable, urgent with place : " + nearest.getLeft());
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), nearest.getLeft(), true));
										}
										else
										{
											LOGGER.info("Adding to doable, urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), null, true));
										}
									}
									else if(phoneTime.isAfter(((TaskTimeConstraint) t.getConstr()).getFromTime()))
									{
										if(nearest != null && nearest.getRight()<=intrestDistanceMeters)
										{
											LOGGER.info("Adding to doable, non urgent with place : " + nearest.getLeft());
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), nearest.getLeft(), false));
										}
										else if(nearest == null)
										{
											LOGGER.info("Adding to doable, non urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), null, false));
										}
									}
									break;
							}
						}
						else if (t.getConstr() instanceof TaskPlaceConstraint)
						{
							//This is only possible with known places (eg. work or house)
							switch (t.getConstr().getType())
							{
								case at:
									LOGGER.info("Found: " + t.getConstr().getType() + " (place)");
									if(((TaskPlaceConstraint) t.getConstr()).getNormalizedAction().equals(NormalizedActions.get))
									{
										if(prevPosition != null && prevPosition.equals(t.getConstr().getParamName()) && (position == null || !position.equals(t.getConstr().getParamName())))
										{
											LOGGER.info("Task has failed (asking for confirmation)");
											synchronized (confirmMap){confirmMap.put(t.getId(), true);}
											doable.add(new Hint(t.getId(), null ,true, true));
											//setTaskFailed(t, user);
											break;
										}

										if (position != null && position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Adding to doable, non urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), false));
										}
									}
									else if(((TaskPlaceConstraint) t.getConstr()).getNormalizedAction().equals(NormalizedActions.leave))
									{
										if(prevPosition != null && !prevPosition.equals(t.getConstr().getParamName()) && position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Task has failed (asking for confirmation)");
											synchronized (confirmMap){confirmMap.put(t.getId(), true);}
											doable.add(new Hint(t.getId(), null ,true, true));
											//setTaskFailed(t, user);
											break;
										}

										if (position == null || !position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Adding to doable, non urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), false));
										}
									}
									break;

								case before:
									LOGGER.info("Found: " + t.getConstr().getType() + " (place)");
									if(((TaskPlaceConstraint) t.getConstr()).getNormalizedAction().equals(NormalizedActions.get))
									{
										if(position != null && position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Task has failed (asking for confirmation)");
											synchronized (confirmMap){confirmMap.put(t.getId(), true);}
											doable.add(new Hint(t.getId(), null ,true, true));
											//setTaskFailed(t, user);
											break;
										}

										if(prevPosition == null || !prevPosition.equals(t.getConstr().getParamName()))
										{
											if(distanceInMeters(myPoint, ((TaskPlaceConstraint)t.getConstr()).getConstraintPlace().getCoordinates()) <= LOCATION_RADIUS_BEFORE_URGENT_METERS)
											{
												LOGGER.info("Adding to doable, urgent");
												synchronized (confirmMap){confirmMap.remove(t.getId());}
												doable.add(new Hint(t.getId(), true));
											}
											else if(distanceInMeters(myPoint, ((TaskPlaceConstraint)t.getConstr()).getConstraintPlace().getCoordinates()) <= LOCATION_RADIUS_BEFORE_METERS)
											{
												LOGGER.info("Adding to doable, non urgent");
												synchronized (confirmMap){confirmMap.remove(t.getId());}
												doable.add(new Hint(t.getId(), false));
											}
										}
									}
									else if(((TaskPlaceConstraint) t.getConstr()).getNormalizedAction().equals(NormalizedActions.leave))
									{
										//NOT possible for now
									}
									break;

								case after:
									//Same as AT case
									LOGGER.info("Found: " + t.getConstr().getType() + " (place)");
									if(((TaskPlaceConstraint) t.getConstr()).getNormalizedAction().equals(NormalizedActions.get))
									{
										if(prevPosition != null && prevPosition.equals(t.getConstr().getParamName()) && (position == null || !position.equals(t.getConstr().getParamName())))
										{
											LOGGER.info("Task has failed (asking for confirmation)");
											synchronized (confirmMap){confirmMap.put(t.getId(), true);}
											doable.add(new Hint(t.getId(), null ,true, true));
											//setTaskFailed(t, user);
											break;
										}

										if (position != null && position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Adding to doable, non urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), false));
										}
									}
									else if(((TaskPlaceConstraint) t.getConstr()).getNormalizedAction().equals(NormalizedActions.leave))
									{
										if(prevPosition != null && !prevPosition.equals(t.getConstr().getParamName()) && position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Task has failed (asking for confirmation)");
											synchronized (confirmMap){confirmMap.put(t.getId(), true);}
											doable.add(new Hint(t.getId(), null ,true, true));
											//setTaskFailed(t, user);
											break;
										}

										if (position == null || !position.equals(t.getConstr().getParamName()))
										{
											LOGGER.info("Adding to doable, non urgent");
											synchronized (confirmMap){confirmMap.remove(t.getId());}
											doable.add(new Hint(t.getId(), false));
										}
									}
									break;
							}
						}
					}
				}
			}

			synchronized (confirmMap)
			{
				for(Map.Entry<Integer, Boolean> e : confirmMap.entrySet())
				{
					if(!e.getValue()) confirmMap.remove(e.getKey());
					else doable.add(new Hint(e.getKey(), null, true, true));
				}
			}
			

			//updating user's last known position
			if(position == null)
				if(prevPlaceParameter == null)
					new CreateUserDefinedParameterDatabase(getDataSource().getConnection(), user,
							new LocationParameter(ParamsName.location_previous, user.getEmail(), new Point(lat,lon), -1, null))
							.createUserDefinedParameter();
				else
					new UpdateUserDefinedParameterDatabase(getDataSource().getConnection(), user,
							new LocationParameter(ParamsName.location_previous, user.getEmail(), new Point(lat,lon), -1, null))
							.updateUserDefinedParameter();
			else
				if(prevPlaceParameter == null)
					new CreateUserDefinedParameterDatabase(getDataSource().getConnection(), user,
							new LocationParameter(ParamsName.location_previous, user.getEmail(), ((LocationParameter)userParametersMap.get(position)).getLocation(), -1, null))
							.createUserDefinedParameter();
				else
					new UpdateUserDefinedParameterDatabase(getDataSource().getConnection(), user,
							new LocationParameter(ParamsName.location_previous, user.getEmail(), ((LocationParameter)userParametersMap.get(position)).getLocation(), -1, null))
							.updateUserDefinedParameter();

			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
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
		catch (NullPointerException e)
		{
			LOGGER.severe("NullPointerException: " + e.getMessage());
			e.printStackTrace();
			ServletUtils.sendMessage(new Message("NullPointerException",
					"500", e.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		LOGGER.info("Incoming request.. Completed");
	}

	private ParamsName resolvePosition(Map<ParamsName, Parameter> plist, String ssid, int cellID, LocalTime givenTime)
	{
		LocationParameter house;
		LocationParameter work;
		TimeParameter workTime = (TimeParameter) plist.get(ParamsName.time_work);

		LOGGER.info("Identifying user's position with parameters ssid: " + ssid + " cellID: " + cellID + " givenTime: " + givenTime);

		try{ house = (LocationParameter) plist.get(ParamsName.location_house); }catch(NullPointerException npe){ LOGGER.info("User did not set its home location"); return null;}
		try{ work = (LocationParameter) plist.get(ParamsName.location_work); }catch(NullPointerException npe){ LOGGER.info("User did not set its work location"); return null;}

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
		Point housePoint = null;
		Point workPoint = null;
		TimeParameter workTime = (TimeParameter) plist.get(ParamsName.time_work);

		LOGGER.info("Identifying user's position with parameters givenPoint: " + givenPoint + " givenTime: " + givenTime);

		try{ housePoint = ((LocationParameter) plist.get(ParamsName.location_house)).getLocation(); }catch(NullPointerException npe){ LOGGER.info("User did not set its home location"); return null;}
		try{ workPoint = ((LocationParameter) plist.get(ParamsName.location_work)).getLocation(); }catch(NullPointerException npe){ LOGGER.info("User did not set its work location"); return null;}

		if(workTime == null)
		{
			//just set an impossible work time
			//AKA starts and finishes at the same time
			workTime = new TimeParameter(ParamsName.time_work, null,
					new LocalTime(0,0), new LocalTime(0,0));
		}

		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");

		int houseDistance = distanceInMeters(givenPoint, housePoint);
		int workDistance = distanceInMeters(givenPoint, workPoint);

		if(workDistance <= LOCATION_RADIUS_METERS && TimeUtils.isTimeBetween(givenTime, workTime.getFromTime(), workTime.getToTime()))
		{
			LOGGER.info("Position corresponds to workplace (parameters are lat: " + givenPoint.getLat() + " lon: " + givenPoint.getLon() +
					" distance from house: " + houseDistance + "m, time: " + givenTime + ")");
			return ParamsName.location_work;
		}
		else if(houseDistance <= LOCATION_RADIUS_METERS)
		{
			LOGGER.info("Position corresponds to house (parameters are lat: " + givenPoint.getLat() + " lon: " + givenPoint.getLon() +
					" distance from work: " + workDistance + "m, time: " + givenTime + ")");
			return ParamsName.location_house;
		}

		LOGGER.info("Position is not a known place (parameters are lat: " + givenPoint.getLat() + " lon: " + givenPoint.getLon() +
				" distance from house: " + houseDistance + "m, distance from work: " + workDistance + "m, time: " + givenTime + ")");
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
		Task tNew = new Task(t.getId(), t.getUser(),t.getName(),t.getConstr(), t.isPossibleAtWork(), t.isCritical(), t.isRepeatable(), t.isDoneToday(), true, t.isIgnoredToday(),t.getPlacesToSatisfy());
		new UpdateTaskDatabase(getDataSource().getConnection(), tNew, u).updateTask();
	}
}
