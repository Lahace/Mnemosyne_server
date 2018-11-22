package ap.mnemosyne.rest;

import ap.mnemosyne.database.*;
import ap.mnemosyne.enums.ConstraintTemporalType;
import ap.mnemosyne.enums.NormalizedActions;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.util.ServletUtils;
import org.joda.time.LocalTime;

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
			List<Task> tl = new GetTasksByUserDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current")).getTasksByUser();
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
	public void getTaskByID(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("id") int id) throws IOException
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
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
	public void createTask(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		try
		{
			if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
			Task t = Task.fromJSON(req.getInputStream());
			Task ret = new CreateTaskDatabase(getDataSource().getConnection(), t, (User) req.getSession(false).getAttribute("current")).createTask();
			res.setStatus(HttpServletResponse.SC_CREATED);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			ret.toJSON(res.getOutputStream());

		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	public void updateTask(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		try
		{
			if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
			Task t = Task.fromJSON(req.getInputStream());
			Task ret = new UpdateTaskDatabase(getDataSource().getConnection(), t, (User) req.getSession(false).getAttribute("current")).updateTask();
			if(ret == null)
			{
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			else
			{
				res.setStatus(HttpServletResponse.SC_OK);
				res.setHeader("Content-Type", "application/json; charset=utf-8");
				ret.toJSON(res.getOutputStream());
			}

		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("test")
	public void createTestTask(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		//Class to serialize and create example tasks
		try
		{
			Set<Place> plist = new HashSet<>();
			plist.add(new Place("italy", "veneto", "schio", "magré", 2, "Famila",
					"supermarket", new Point(45.714012, 11.353281), new LocalTime(9,0), new LocalTime(20,30)));

			new CreateTaskDatabase(getDataSource().getConnection(),
					new Task(12,((User)req.getSession().getAttribute("current")).getEmail() , "Prova task time", new TaskTimeConstraint(new LocalTime(16,0), null, ParamsName.time_bed, ConstraintTemporalType.after),
							false, false, false, false, plist), (User) req.getSession().getAttribute("current")).createTask();

			new CreateTaskDatabase(getDataSource().getConnection(),
					new Task(12,((User)req.getSession().getAttribute("current")).getEmail() , "prova task place", new TaskPlaceConstraint(
							new Place("italy", "veneto", "schio", "magré", 2, "casa", "housing",
									new Point(45.703336, 11.356497), null, null), ParamsName.location_house, ConstraintTemporalType.before, NormalizedActions.get),
							false, false, false, false, plist), (User) req.getSession().getAttribute("current")).createTask();
			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	@Path("{id}")
	public void deleteTask(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("id") int id) throws IOException
	{
		try
		{
			User u = (User) req.getSession(false).getAttribute("current");
			if(new DeleteTaskDatabase(getDataSource().getConnection(), id, u).deleteTask())
			{
				ServletUtils.sendMessage(new Message("Ok"), res, HttpServletResponse.SC_OK);
			}
			else
			{
				ServletUtils.sendMessage(new Message("Error while deleting task",
						"400", "Task was not deleted"), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
