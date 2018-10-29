package ap.mnemosyne.rest;

import ap.mnemosyne.database.CreateTaskDatabase;
import ap.mnemosyne.database.GetTaskByIDDatabase;
import ap.mnemosyne.database.GetTaskByUserDatabase;
import ap.mnemosyne.enums.ConstraintTemporalType;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import ap.mnemosyne.servlets.AbstractDatabaseServlet;
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
import java.util.List;

@Path("task")
public class RestTask
{
	@GET
	public void getTasks(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException, ServletException
	{
		try
		{
			List<Task> tl = new GetTaskByUserDatabase(getDataSource().getConnection(), (User) req.getSession(false).getAttribute("current")).getTaskByUser();
			if(tl != null)
			{
				res.setStatus(HttpServletResponse.SC_OK);
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
			ArrayList<Point> plist = new ArrayList<>();
			plist.add(new Point(48.44, -123.37));
			new CreateTaskDatabase(getDataSource().getConnection(),
					new Task(12,"asd@asd.it" , "Nome", new TaskTimeConstraint(LocalTime.of(16,0), ParamsName.time_bed, ConstraintTemporalType.dopo),
							false, false, false, false, plist), (User) req.getSession().getAttribute("current")).createTask();
			new CreateTaskDatabase(getDataSource().getConnection(),
					new Task(12,"asd@asd.it" , "Nome", new TaskPlaceConstraint(new Point(12,54), ParamsName.location_house, ConstraintTemporalType.prima),
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
