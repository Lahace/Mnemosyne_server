package ap.mnemosyne.servlets;

import ap.mnemosyne.database.*;
import ap.mnemosyne.enums.ConstraintTemporalType;
import ap.mnemosyne.enums.NormalizedActions;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.exceptions.ParameterNotDefinedException;
import ap.mnemosyne.parser.ParserITv2;
import ap.mnemosyne.parser.resources.TextualConstraint;
import ap.mnemosyne.parser.resources.TextualTask;
import ap.mnemosyne.places.PlacesManager;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.util.ServletUtils;
import ap.mnemosyne.util.TimeUtils;
import ap.mnemosyne.util.Tuple;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParseServlet extends AbstractDatabaseServlet
{

	private ParserITv2 parser;
	private PlacesManager pman;
	private final Logger LOGGER = Logger.getLogger(ParseServlet.class.getName());

	public void init(ServletConfig config) throws ServletException
	{
		LOGGER.setLevel(Level.INFO);
		super.init(config);
		LOGGER.info("Initializing ParseServlet..");
		pman = new PlacesManager();
		parser = new ParserITv2();
		LOGGER.info("Initializing ParseServlet.. Done");
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		LocalTime standardOpening = LocalTime.of(9,0);
		LocalTime standardClosing = LocalTime.of(19,30);

		if(!ServletUtils.checkContentType(MediaType.APPLICATION_FORM_URLENCODED, req, res)) return;
		String sentence = req.getParameter("sentence");
		if(sentence == null)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"PRSR01", "Please specify the sentence to be parsed"), res, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		double lat,lon;

		try
		{
			lat = Double.parseDouble(req.getParameter("lat"));
			lon = Double.parseDouble(req.getParameter("lon"));
		}
		catch (NullPointerException npe)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"PRSR02", "Please specify both lat and lon of your current position"), res, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		LOGGER.info("Parsing \"" + sentence + "\" with lat: " + lat + " and lon: " + lon);

		TextualTask tt = parser.parseString(sentence);

		if(tt.getTextualAction() == null)
		{
			LOGGER.warning("FAILED: No action retrieved from sentence");
			ServletUtils.sendMessage(new Message("Not implemented",
					"PRSR03", "Parser did not recognized sentence " + tt.getFullSentence()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		String user = ((User)req.getSession().getAttribute("current")).getEmail();
		String name = tt.getFullSentence();
		TaskConstraint constr = null;
		boolean possibleAtWork = false;
		boolean repeatable = false;
		boolean doneToday = false;
		boolean failed = false;
		Set<Place> placesToSatisfy = new HashSet<>();

		try
		{

			LOGGER.info("Checking action validity..");
			ParamsName p = new GetPnameByTextualActionDatabase(getDataSource().getConnection(), tt.getTextualAction()).getPnameByTextualAction();
			if(p == null)
			{
				LOGGER.warning("FAILED: No action definition for: " + tt.getTextualAction().getVerb() + " " + tt.getTextualAction().getSubject());
				ServletUtils.sendMessage(new Message("Not implemented",
						"PRSR04", "Could not find a definition for action " + tt.getTextualAction().getVerb()
						+ " with subject " + tt.getTextualAction().getSubject()), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
				return;
			}
			Parameter param;
			Place my;
			Point point;
			boolean findPlacesForItem = false;
			LOGGER.info("Resolving parameter " + p.toString() + " for actions");
			switch (p)
			{
				case location_item:
					/*Deferring operation since it's quite heavy, we can then return error messages in (for examples) constraints before
					searching for places
					*/
					LOGGER.info("Found location_item, resolving later..");
					findPlacesForItem = true;
					break;

				case location_house:
					LOGGER.info("Found location_house");
					param = new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current"), p)
							.getUserDefinedParameterByName();
					if(param == null)
					{
						throw new ParameterNotDefinedException(p.toString());
					}

					if(param instanceof LocationParameter)
					{
						point = ((LocationParameter) param).getLocation();
						my = pman.getPlacesFromPoint(point);
						LOGGER.info("House is at: " + my.toString());
						placesToSatisfy.add(my);
					}
					else
					{
						throw new ServletException("Unexpected parameter declaration, needed place parameter, found " + param.getClass());
					}
					break;

				case location_work:
					LOGGER.info("Found location_work");
					possibleAtWork = true;
					param = new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current"), p)
							.getUserDefinedParameterByName();
					if(param == null)
					{
						throw new ParameterNotDefinedException(p.toString());
					}

					if(param instanceof LocationParameter)
					{
						point = ((LocationParameter) param).getLocation();
						my = pman.getPlacesFromPoint(point);
						LOGGER.info("Workplace is at: " + my.toString());
						placesToSatisfy.add(my);
					}
					else
					{
						throw new ServletException("Unexpected parameter declaration, needed place parameter, found " + param.getClass());
					}
					break;

				case location_any:
					//Do nothing, no need to add places
					break;

				default:
					throw new ServletException("Could not find parameter " + p);
			}

			LOGGER.info("Checking constraints validity.. ");

			//Resolving Constraint
			//Getting only the first constraint
			//TODO: add multiple constraint support
			boolean parsed = false;
			LocalTime specifiedTime = null;
			Tuple<String, ConstraintTemporalType> pair = null;
			if(!tt.getTextualConstraints().isEmpty())
			{
				TextualConstraint current = tt.getTextualConstraints().get(0);
				String toParse = null;
				try
				{
					//TODO: Add time interval support
					if (current.getConstraintWord().length() == 2)
						toParse = current.getConstraintWord() + ":00";
					else if(current.getConstraintWord().length() == 1)
						toParse = "0" + current.getConstraintWord() + ":00";
					else if(current.getConstraintWord().length() == 5)
						toParse = current.getConstraintWord();

					specifiedTime = LocalTime.parse(toParse);
					parsed = true;
				}
				catch (NullPointerException | DateTimeParseException dtpe)
				{
					LOGGER.info("Could not parse time: " + toParse);
					parsed = false;
				}

				if (!parsed)
				{
					LOGGER.info("Getting resolver record..");
					Map<String, String> map = new GetConstraintResolveByTextualConstraintDatabase(getDataSource().getConnection(), current).getConstraintResolvesByTextualAction();
					if (map.isEmpty())
					{
						LOGGER.warning("FAILED: No constraint definition for: " + current.getConstraintMarker() + " " + current.getVerb() + " " + current.getConstraintWord());
						ServletUtils.sendMessage(new Message("Not implemented",
								"PRSR06", "Could not find a definition for constraint '" + current.getConstraintMarker()
								+ " " + current.getVerb() + " " + current.getConstraintWord()), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
						return;
					}
					else
					{
						LOGGER.info("Resolving " + ParamsName.valueOf(map.get("parameter")));
						switch (ParamsName.valueOf(map.get("parameter")))
						{
							case location_work:
								possibleAtWork = true;
								constr = this.solveLocationConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
								break;

							case location_house:
								if(map.get("timing").equals(ConstraintTemporalType.before) && map.get("normalized_action").equals(NormalizedActions.leave))
								{
									LOGGER.warning("FAILED: Leave is not implemented for location_house ");
									ServletUtils.sendMessage(new Message("Not implemented",
											"PRSR13", "This constraint is not supported yet"), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
								}
								constr = this.solveLocationConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
								break;

							case location_item:
								throw new ParameterNotDefinedException(p.toString());

							case location_any:
								//Still not supported
								throw new ParameterNotDefinedException(p.toString());

							case time_bed:
								constr = this.solveTimeConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
								break;

							case time_work:
								constr = this.solveTimeConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
								break;

							case time_lunch:
								constr = this.solveTimeConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
								break;

							case time_dinner:
								constr = this.solveTimeConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
								break;

							default:
								throw new ServletException("Could not find parameter " + p);
						}
					}
				}
				else
				{
					LOGGER.info("Found specific time in constraint: " + specifiedTime);
					pair = new GetConstraintMarkerFromMarkerDatabase(getDataSource().getConnection(), current.getConstraintMarker()).getConstraintFromMarker();
					if(pair.getRight() == null)
					{
						LOGGER.warning("FAILED: No constraint definition for: " + current.getConstraintMarker());
						ServletUtils.sendMessage(new Message("Not implemented",
								"PRSR06", "Could not find a definition for constraint '" + tt.getTextualConstraints().get(0).getConstraintMarker()
								+ " " + tt.getTextualConstraints().get(0).getConstraintWord()), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
						return;
					}
				}
			}
			else
			{
				LOGGER.info("No constraints found");
			}

			if(findPlacesForItem)
			{
				LOGGER.info("Computing places to satisfy action");
				//TODO: UPDATE places with user's current position
				my = pman.getPlacesFromPoint(new Point(lat,lon));
				LOGGER.info("My place is: " + my.toString());
				List<String> places = new SearchPlacesByItemDatabase(getDataSource().getConnection(), tt.getTextualAction().getSubject()).searchPlacesByItem();
				if(places.isEmpty())
				{
					LOGGER.warning("FAILED: No places found for item " + tt.getTextualAction().getSubject());
					ServletUtils.sendMessage(new Message("Not found",
							"PRSR05", "Could not find places where to find " + tt.getTextualAction().getSubject()), res, HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				for(String s : places)
				{
					for (Place place : pman.getPlacesFromQuery(s + " in " + my.getTown() + " in " + my.getState()))
					{
						//TODO: add check for closing/opening time
						placesToSatisfy.add(new Place(place.getCountry(), place.getState(), place.getTown(), place.getSuburb(), place.getHouseNumber(),
								place.getName(), place.getPlaceType(), place.getCoordinates(), standardOpening, standardClosing));
					}

					//TODO: add query caching for PlaceManager & reduce query number
					//Adding also places around known places to satisfy every constraint
					//eg. if user works 100km away from his/her house, we must find places near his/her house in case constraint is something like "when i get back home"
					Point housePoint = ((LocationParameter) new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current"), ParamsName.location_house)
							.getUserDefinedParameterByName()).getLocation();
					Place myHouse = pman.getPlacesFromPoint(housePoint);
					for (Place place : pman.getPlacesFromQuery(s + " in " + myHouse.getTown() + " in " + my.getState()))
					{
						placesToSatisfy.add(new Place(place.getCountry(), place.getState(), place.getTown(), place.getSuburb(), place.getHouseNumber(),
								place.getName(), place.getPlaceType(), place.getCoordinates(), standardOpening, standardClosing));
					}

					Point workPoint = ((LocationParameter) new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current"), ParamsName.location_work)
							.getUserDefinedParameterByName()).getLocation();
					Place myWorkplace = pman.getPlacesFromPoint(workPoint);
					for (Place place : pman.getPlacesFromQuery(s + " in " + myWorkplace.getTown() + " in " + my.getState()))
					{
						placesToSatisfy.add(new Place(place.getCountry(), place.getState(), place.getTown(), place.getSuburb(), place.getHouseNumber(),
								place.getName(), place.getPlaceType(), place.getCoordinates(), standardOpening, standardClosing));
					}
				}
				if(placesToSatisfy.isEmpty())
					throw new NoDataReceivedException("No data was received searching for places where to find: " + tt.getTextualAction().getSubject());

				if(parsed)
				{

					if(p.equals(ParamsName.location_item))
					{
						//Remove every place that's not open at the time specified
						TimeParameter bed = (TimeParameter) new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(),
								(User) req.getSession().getAttribute("current"), ParamsName.time_bed).getUserDefinedParameterByName();
						Iterator<Place> iter = placesToSatisfy.iterator();
						Set<Place> toReplace = new HashSet<>();
						while(iter.hasNext())
						{
							//TODO: maybe also add time to destination to the condition? -> specifiedTime + timeToDestination
							Place place = iter.next();
							if ((pair.getRight().equals(ConstraintTemporalType.at) && !TimeUtils.isTimeBetween(specifiedTime, place.getOpening(), place.getClosing())) ||
									(pair.getRight().equals(ConstraintTemporalType.before) && TimeUtils.isTimeBetween(specifiedTime, bed.getToTime(), place.getOpening())) ||
									(pair.getRight().equals(ConstraintTemporalType.after) && TimeUtils.isTimeBetween(specifiedTime, place.getClosing(), bed.getFromTime())))
							{
								toReplace.add(place);
							}
						}

						placesToSatisfy.removeAll(toReplace);

						if (placesToSatisfy.isEmpty())
						{
							LOGGER.warning("FAILED: placesToSatisfy was emptied");
							ServletUtils.sendMessage(new Message("Bad request",
									"PRSR07", "No places found that's still/already open at: " + specifiedTime), res, HttpServletResponse.SC_BAD_REQUEST);
							return;
						}

						//changing constraint to match with places' closing/opening time
						//getting earliest opening place and latest closing place

						Place maxClosing = TimeUtils.findLatestOpenedPlace(placesToSatisfy);
						Place minOpening = TimeUtils.findEarliestOpeningPlace(placesToSatisfy);

						if (pair.getRight().equals(ConstraintTemporalType.before) && specifiedTime.isAfter(maxClosing.getClosing()))
						{
							constr = new TaskTimeConstraint(maxClosing.getClosing(), null, ParamsName.time_specified, pair.getRight());
						}
						else if (pair.getRight().equals(ConstraintTemporalType.after) && specifiedTime.isBefore(minOpening.getOpening()))
						{
							constr = new TaskTimeConstraint(minOpening.getOpening(), null, ParamsName.time_specified, pair.getRight());
						}
						else
						{
							constr = new TaskTimeConstraint(specifiedTime, null, ParamsName.time_specified, pair.getRight());
						}
					}
					else
					{
						constr = new TaskTimeConstraint(specifiedTime, null, ParamsName.time_specified, pair.getRight());
					}
				}
			}

			LOGGER.info("Creating task..");

			Task t = new Task(-1, user, name ,constr, possibleAtWork, repeatable, doneToday, failed, placesToSatisfy);
			new CreateTaskDatabase(getDataSource().getConnection(), t, (User) req.getSession(false).getAttribute("current")).createTask();
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			t.toJSON(res.getOutputStream());
			LOGGER.info("Creating Task.. Done");
		}
		catch (SQLException sqle)
		{
			LOGGER.severe("SQLException: " + sqle.getMessage() + " -> Code: " + sqle.getErrorCode() + " State: " + sqle.getSQLState());
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"PRSR08", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (ServletException bde)
		{
			LOGGER.severe("ServletException: " + bde.getMessage());
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"PRSR09", bde.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (NoDataReceivedException ndre)
		{
			LOGGER.severe("NoDataReceivedException: " + ndre.getMessage());
			ServletUtils.sendMessage(new Message("Bad Request",
					"PRSR10", ndre.getMessage()), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch(ParameterNotDefinedException pnde)
		{
			LOGGER.severe("ParameterNotDefinedException: " + pnde.getMessage());
			ServletUtils.sendMessage(new Message("Not found",
					"PRSR11", pnde.getMessage()), res, HttpServletResponse.SC_NOT_FOUND);
		}
		catch(NullPointerException npe)
		{
			LOGGER.severe("NullPointerException: " + npe.getMessage());
			ServletUtils.sendMessage(new Message("NullPointerException on server-side",
					"PRSR12", "Something is clearly wrong, please send a warning to the monkeys who created this application"), res, HttpServletResponse.SC_NOT_FOUND);
			npe.printStackTrace();
		}

		return;
	}

	private TaskConstraint solveLocationConstraint(ParamsName location, Map<String, String> resolveMap, HttpServletRequest req) throws ServletException, SQLException, NoDataReceivedException
	{
		TaskConstraint toRet = null;
		Parameter param = new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(),
				(User) req.getSession().getAttribute("current"), location).getUserDefinedParameterByName();
		if(param == null)
		{
			throw new ParameterNotDefinedException(location.toString());
		}

		if(param instanceof LocationParameter)
		{
			Point point = ((LocationParameter) param).getLocation();
			Place my = pman.getPlacesFromPoint(point);
			LOGGER.info("My place is: " + my.toString());
			toRet = new TaskPlaceConstraint(my, location,
					ConstraintTemporalType.valueOf(resolveMap.get("timing")), NormalizedActions.valueOf(resolveMap.get("normalized_action")));
		}
		else
		{
			throw new ServletException("Unexpected parameter declaration, needed time parameter, found " + param.getClass());
		}
		return toRet;
	}

	private TaskConstraint solveTimeConstraint(ParamsName timing, Map<String, String> resolveMap, HttpServletRequest req) throws ServletException, SQLException
	{
		TaskConstraint toRet = null;
		Parameter param = new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(),
				(User) req.getSession().getAttribute("current"), timing).getUserDefinedParameterByName();
		if(param == null)
		{
			throw new ParameterNotDefinedException(timing.toString());
		}

		if(param instanceof TimeParameter)
		{
			TimeParameter time = (TimeParameter) param;
			toRet = new TaskTimeConstraint(time.getFromTime(), time.getToTime(), timing, ConstraintTemporalType.valueOf(resolveMap.get("timing")));
		}
		else
		{
			throw new ServletException("Unexpected parameter declaration, needed time parameter, found " + param.getClass());
		}

		return toRet;
	}
}
