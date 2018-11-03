package ap.mnemosyne.rest;

import ap.mnemosyne.database.CreateTaskDatabase;
import ap.mnemosyne.database.GetTaskByIDDatabase;
import ap.mnemosyne.database.SearchTaskByUserDatabase;
import ap.mnemosyne.enums.ConstraintTemporalType;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.util.ServletUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("task")
public class RestTask
{
	@GET
	public void getTasks(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException, ServletException
	{
		try
		{
			List<Task> tl = new SearchTaskByUserDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current")).searchTaskByUser();
			if(tl != null)
			{
				res.setStatus(HttpServletResponse.SC_OK);
				res.setHeader("Content-Type", "application/json; charset=utf-8");
				new ResourceList<>(tl).toJSON(res.getOutputStream());
			}
			else
			{
				ServletUtils.sendMessage(new Message("Internal Server Error",
						"500", "An error occurred (wow!), list returned null"), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (ClassNotFoundException ex)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", ex.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("{id}")
	public void getTaskByID(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("id") int id) throws IOException, ServletException
	{
		try
		{
			Task t = new GetTaskByIDDatabase(getDataSource().getConnection(), id).getTaskByID();
			if(t != null)
			{
				res.setStatus(HttpServletResponse.SC_OK);
				res.setHeader("Content-Type", "application/json; charset=utf-8");
				t.toJSON(res.getOutputStream());
			}
			else
			{
				ServletUtils.sendMessage(new Message("Not Found",
						"400", "Task was not found"), res, HttpServletResponse.SC_NOT_FOUND);
			}
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (ClassNotFoundException ex)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", ex.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	public void createTask(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException, ServletException
	{
		try
		{
			if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
			Task t = Task.fromJSON(req.getInputStream());
			Task ret = new CreateTaskDatabase(getDataSource().getConnection(), t, (User) req.getSession(false).getAttribute("current")).createTask();
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			ret.toJSON(res.getOutputStream());

		}
		catch (IOException ioe)
		{
			ServletUtils.sendMessage(new Message("IOException in RestTask", "500", ioe.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	public void createTestTask(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException, ServletException
	{
		//Class to serialize and create example tasks
		try
		{
			Set<Place> plist = new HashSet<>();
			plist.add(new Place("italy", "veneto", "schio", "magré", 2, "Famila",
					"supermarket", new Point(45.714012, 11.353281), LocalTime.of(9,0), LocalTime.of(20,30)));
			new CreateTaskDatabase(getDataSource().getConnection(),
					new Task(12,"asd@asd.it" , "Nome", new TaskTimeConstraint(LocalTime.of(16,0), ParamsName.time_bed, ConstraintTemporalType.after),
							false, false, false, false, plist), (User) req.getSession().getAttribute("current")).createTask();
			new CreateTaskDatabase(getDataSource().getConnection(),
					new Task(12,"asd@asd.it" , "Nome", new TaskPlaceConstraint(
							new Place("italy", "veneto", "schio", "magré", 2, "casa", "housing",
									new Point(45.703336, 11.356497), null, null), ParamsName.location_house, ConstraintTemporalType.before),
							false, false, false, false, plist), (User) req.getSession().getAttribute("current")).createTask();
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private DataSource getDataSource() throws ServletException
	{
		InitialContext cxt;
		DataSource ds;

		try {
			cxt = new InitialContext();
			ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/mnemosyne");
		} catch (NamingException e) {
			ds = null;

			throw new ServletException(
					String.format("Impossible to access the connection pool to the database: %s",
							e.getMessage()));
		}
		return ds;
	}
}
