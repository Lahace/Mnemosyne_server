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
import javafx.util.Pair;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseServlet extends AbstractDatabaseServlet
{

	private ParserITv2 parser;
	private PlacesManager pman;

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		pman = new PlacesManager();
		parser = new ParserITv2();
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
					"400", "Please specify the sentence to be parsed"), res, HttpServletResponse.SC_BAD_REQUEST);
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
					"400", "Please specify both lat and lon of your current position"), res, HttpServletResponse.SC_BAD_REQUEST);
			return;
		}


		TextualTask tt = parser.parseString(sentence);

		String user = ((User)req.getSession().getAttribute("current")).getEmail();
		String name = tt.getFullSentence();
		TaskConstraint constr = null;
		boolean possibleAtWork = false;
		boolean repeatable = false;
		boolean doneToday = false;
		boolean failed = false;
		Set<Place> placesToSatisfy = new HashSet<>();

		//Resolving Action
		try
		{
			ParamsName p = new GetPnameByTextualActionDatabase(getDataSource().getConnection(), tt.getTextualAction()).getPnameByTextualAction();
			if(p == null)
			{
				ServletUtils.sendMessage(new Message("Not implemented",
						"501", "Could not find a definition for action " + tt.getTextualAction().getVerb()
						+ " with subject " + tt.getTextualAction().getSubject()), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
				return;
			}
			Pair<Class, Object> param;
			Place my;
			Point point;
			switch (p)
			{
				case location_item:
					my = pman.getPlacesFromPoint(new Point(lat,lon));
					if(my == null) throw new NoDataReceivedException("No data received with current lat/lon");
					List<String> places = new SearchPlacesByItemDatabase(getDataSource().getConnection(), tt.getTextualAction().getSubject()).searchPlacesByItem();
					if(places.isEmpty())
					{
						ServletUtils.sendMessage(new Message("Not found",
								"404", "Could not find places where to find " + tt.getTextualAction().getSubject()), res, HttpServletResponse.SC_NOT_FOUND);
					}
					for(String s : places)
					{
						for (Place place : pman.getPlacesFromQuery(s + " in " + my.getTown()))
						{
							placesToSatisfy.add(place);
						}
					}
					break;

				case location_house:
					param = new GetUserDefinedParameterDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current"), p)
							.getUserDefinedParameter();
					if(param == null)
					{
						throw new ParameterNotDefinedException(p.toString());
					}

					if(param.getKey() == Point.class)
					{
						point = (Point) param.getValue();
						my = pman.getPlacesFromPoint(point);
						if(my == null) throw new NoDataReceivedException("No data received with lat/lon for parameter" + p.toString());
						placesToSatisfy.add(my);
					}
					else
					{
						throw new ServletException("Unexpected parameter declaration, needed time parameter, found " + param.getKey());
					}
					break;

				case location_work:
					param = new GetUserDefinedParameterDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current"), p)
							.getUserDefinedParameter();
					if(param == null)
					{
						throw new ParameterNotDefinedException(p.toString());
					}

					if(param.getKey() == Point.class)
					{
						point = (Point) param.getValue();
						my = pman.getPlacesFromPoint(point);
						if(my == null) throw new NoDataReceivedException("No data received with lat/lon for parameter" + p.toString());
						placesToSatisfy.add(my);
					}
					else
					{
						throw new ServletException("Unexpected parameter declaration, needed time parameter, found " + param.getKey());
					}
					break;
				default:
					break;
			}

			//Resolving Constraint
			//Getting only the first constraint
			//TODO: add multiple constraint support
			TextualConstraint current = tt.getTextualConstraints().get(0);
			boolean parsed;
			LocalTime specifiedtime = null;
			try
			{
				specifiedtime = LocalTime.parse(current.getConstraintWord());
				parsed = true;
			}
			catch (DateTimeParseException dtpe)
			{
				parsed = false;
			}

			if(parsed)
			{
				Pair<String, ConstraintTemporalType> pair = new GetConstraintMarkerFromMarkerDatabase(getDataSource().getConnection(), current.getConstraintMarker()).getConstraintFromMarker();
				if(pair.getValue() == null)
				{
					ServletUtils.sendMessage(new Message("Not implemented",
							"501", "Could not find a definition for constraint '" + tt.getTextualConstraints().get(0).getConstraintMarker()
							+ " " + tt.getTextualConstraints().get(0).getConstraintWord()), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
					return;
				}
				constr = new TaskTimeConstraint(specifiedtime, ParamsName.time_specified, pair.getValue());
			}
			else
			{
				Map<String, String> map = new GetConstraintResolveByTextualConstraintDatabase(getDataSource().getConnection(), current).getConstraintResolvesByTextualAction();
				if (map.isEmpty())
				{
					ServletUtils.sendMessage(new Message("Not implemented",
							"501", "Could not find a definition for constraint '" + tt.getTextualConstraints().get(0).getConstraintMarker()
							+ " " + tt.getTextualConstraints().get(0).getConstraintWord()), res, HttpServletResponse.SC_NOT_IMPLEMENTED);
					return;
				}
				else
				{
					switch (ParamsName.valueOf(map.get("parameter")))
					{
						case location_work:
							constr = this.solveLocationConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
							break;

						case location_house:
							constr = this.solveLocationConstraint(ParamsName.valueOf(map.get("parameter")), map, req);
							break;

						case location_item:
							throw new ParameterNotDefinedException(p.toString());

						case location_any:
							throw new ParameterNotDefinedException(p.toString());

						case time_bed:

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

						case time_closure:
							throw new ParameterNotDefinedException(p.toString());
					}
				}
			}

			Task t = new Task(-1, user, name ,constr, possibleAtWork, repeatable, doneToday, failed, placesToSatisfy);
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			t.toJSON(res.getOutputStream());

		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (ServletException bde)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", bde.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (NoDataReceivedException ndre)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", ndre.getMessage()), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch(ParameterNotDefinedException pnde)
		{
			ServletUtils.sendMessage(new Message("Not found",
					"404", pnde.getMessage()), res, HttpServletResponse.SC_NOT_FOUND);
		}

		return;
	}

	private TaskConstraint solveLocationConstraint(ParamsName location, Map<String, String> resolveMap, HttpServletRequest req) throws ServletException, SQLException, NoDataReceivedException
	{
		TaskConstraint toRet = null;
		Pair<Class, Object> param = new GetUserDefinedParameterDatabase(getDataSource().getConnection(),
				(User) req.getSession().getAttribute("current"), location).getUserDefinedParameter();
		if(param == null)
		{
			throw new ParameterNotDefinedException(location.toString());
		}

		if(param.getKey() == Point.class)
		{
			Point point = (Point) param.getValue();
			Place my = pman.getPlacesFromPoint(point);
			if(my == null) throw new NoDataReceivedException("No data received with lat/lon for parameter" + location.toString());
			toRet = new TaskPlaceConstraint(my, location,
					ConstraintTemporalType.valueOf(resolveMap.get("timing")), NormalizedActions.valueOf(resolveMap.get("normalized_action")));
		}
		else
		{
			throw new ServletException("Unexpected parameter declaration, needed time parameter, found " + param.getKey());
		}
		return toRet;
	}

	private TaskConstraint solveTimeConstraint(ParamsName timing, Map<String, String> resolveMap, HttpServletRequest req) throws ServletException, SQLException
	{
		TaskConstraint toRet = null;
		Pair<Class, Object> param = new GetUserDefinedParameterDatabase(getDataSource().getConnection(),
				(User) req.getSession().getAttribute("current"), timing).getUserDefinedParameter();
		if(param == null)
		{
			throw new ParameterNotDefinedException(timing.toString());
		}

		if(param.getKey() == LocalTime.class)
		{
			LocalTime time = (LocalTime) param.getValue();
			toRet = new TaskTimeConstraint(time, timing, ConstraintTemporalType.valueOf(resolveMap.get("timing")));
		}
		else
		{
			throw new ServletException("Unexpected parameter declaration, needed time parameter, found " + param.getKey());
		}

		return toRet;
	}
}
