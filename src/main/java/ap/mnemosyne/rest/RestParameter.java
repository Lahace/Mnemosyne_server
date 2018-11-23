package ap.mnemosyne.rest;

import ap.mnemosyne.database.*;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.Message;
import ap.mnemosyne.resources.Parameter;
import ap.mnemosyne.resources.ResourceList;
import ap.mnemosyne.resources.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("parameter")
public class RestParameter
{

	@GET
	public void getParametersList(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		try
		{
			User u = (User) req.getSession().getAttribute("current");
			Map<ParamsName, Parameter> pdb = new GetUserDefinedParametersDatabase(getDataSource().getConnection(), u ).getUserDefinedParameters();
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			List<Parameter> plist = new ArrayList<>();
			for(Map.Entry<ParamsName, Parameter> e : pdb.entrySet())
			{
				plist.add(e.getValue());
			}
			new ResourceList<>(plist).toJSON(res.getOutputStream());
		}
		catch(IllegalArgumentException iae)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Parameter does not exists"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException | IOException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("{parameter}")
	public void getParameter(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("parameter") String param) throws IOException
	{
		try
		{
			ParamsName p = ParamsName.valueOf(param);
			User u = (User) req.getSession().getAttribute("current");
			Parameter pdb = new GetUserDefinedParameterByNameDatabase(getDataSource().getConnection(), u , p).getUserDefinedParameterByName();
			if(pdb == null)
			{
				ServletUtils.sendMessage(new Message("Bad Request",
						"400", "Parameter " + param + " is not defined for user " + u.getEmail()), res, HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			pdb.toJSON(res.getOutputStream());
		}
		catch(IllegalArgumentException iae)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Parameter does not exists"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException | IOException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}


	@POST
	public void createParameter(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
		try
		{
			Parameter p = Parameter.fromJSON(req.getInputStream());
			User u = (User) req.getSession().getAttribute("current");
			Parameter pdb = new CreateUserDefinedParameterDatabase(getDataSource().getConnection(), u , p).createUserDefinedParameter();
			res.setStatus(HttpServletResponse.SC_CREATED);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			pdb.toJSON(res.getOutputStream());
		}
		catch(IllegalArgumentException iae)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Parameter does not exists"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException | IOException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	public void updateParameter(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
		try
		{
			Parameter p = Parameter.fromJSON(req.getInputStream());
			User u = (User) req.getSession().getAttribute("current");
			Parameter pdb = new UpdateUserDefinedParameterDatabase(getDataSource().getConnection(), u , p).updateUserDefinedParameter();
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json; charset=utf-8");
			pdb.toJSON(res.getOutputStream());
		}
		catch(IllegalArgumentException iae)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Parameter does not exists"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException | IOException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	@Path("{parameter}")
	public void deleteParameter(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("parameter") String param) throws IOException
	{
		try
		{
			ParamsName p = ParamsName.valueOf(param);
			User u = (User) req.getSession().getAttribute("current");
			if(new DeleteUserDefinedParameterDatabase(getDataSource().getConnection(), u , p).deleteUserDefinedParameter())
			{
				ServletUtils.sendMessage(new Message("Ok"), res, HttpServletResponse.SC_OK);
			}
			else
			{
				ServletUtils.sendMessage(new Message("Ok",
						"400", "Parameter was not deleted, maybe it's not defined?"), res, HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		catch(IllegalArgumentException iae)
		{
			ServletUtils.sendMessage(new Message("Bad Request",
					"400", "Parameter does not exists"), res, HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException | IOException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
