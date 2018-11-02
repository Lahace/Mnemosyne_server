package ap.mnemosyne.servlets;

import ap.mnemosyne.database.GetPnameByTextualActionDatabase;
import ap.mnemosyne.database.GetUserDefinedParameterDatabase;
import ap.mnemosyne.database.SearchPlacesByItemDatabase;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.parser.ParserITv2;
import ap.mnemosyne.parser.resources.TextualTask;
import ap.mnemosyne.places.PlacesManager;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.util.ServletUtils;
import javafx.util.Pair;
import org.apache.http.HttpResponse;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
		List<Place> placesToSatisfy = new ArrayList<>();

		//Resolving Action
		try
		{
			ParamsName p = new GetPnameByTextualActionDatabase(getDataSource().getConnection(), tt.getTextualAction()).getPnameByTextualAction();
			Pair<Class, Object> param;
			Place my;
			switch (p)
			{
				case location_item:
					my = pman.getPlacesFromPoint(new Point(lat,lon));
					if(my == null) throw new NoDataReceivedException("No data received with current lat/lon");
					List<String> places = new SearchPlacesByItemDatabase(getDataSource().getConnection(), tt.getTextualAction().getSubject()).searchPlacesByItem();
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
					if(param.getKey() == Point.class)
					{
						Point point = (Point) param.getValue();
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
					if(param.getKey() == Point.class)
					{
						Point point = (Point) param.getValue();
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

		return;
	}
}
