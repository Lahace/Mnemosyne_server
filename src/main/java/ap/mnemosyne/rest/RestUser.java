package ap.mnemosyne.rest;

import ap.mnemosyne.database.*;
import ap.mnemosyne.resources.Message;
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

@Path("user")
public class RestUser
{
	@GET
	@Path("{email}")
	public void getUser(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("email") String email) throws IOException
	{
		if(!email.equals(((User) req.getSession().getAttribute("current")).getEmail()))
		{
			ServletUtils.sendMessage(new Message("Unauthorized",
					"401", "You cannot get other users' profile"), res, HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		try
		{
			User u = new SearchUserByEmailDatabase(getDataSource().getConnection(), email).searchUserByEmail();
			if(u == null)
			{
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			else
			{
				res.setStatus(HttpServletResponse.SC_OK);
				res.setHeader("Content-Type", "application/json; charset=utf-8");
				u.toJSON(res.getOutputStream());
			}
		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	//Currently not implemented, no use in this
	//Will be implemented as soon as User has some extra fields
	private void updateUser(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if(!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
		try
		{
			User u = User.fromJSON(req.getInputStream());
			if(new CheckUserCredentialsDatabase(getDataSource().getConnection(), u.getEmail(), u.getPassword()).checkUserCredentials() == null)
			{
				ServletUtils.sendMessage(new Message("Unauthorized",
						"401", "Wrong email/password"), res, HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			else if(!u.getEmail().equals(((User) req.getSession().getAttribute("current")).getEmail()))
			{
				ServletUtils.sendMessage(new Message("Unauthorized",
						"401", "You cannot update other users' profile"), res, HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			User ret = new UpdateUserDatabase(getDataSource().getConnection(), u).updateUser();
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
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("password")
	public void updateUserPassword(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if(!ServletUtils.checkContentType(MediaType.APPLICATION_FORM_URLENCODED, req, res)) return;
		try
		{
			String oldpsw = req.getParameter("old");
			String newpsw = req.getParameter("new");
			User u = (User) req.getSession().getAttribute("current");
			if(oldpsw == null || newpsw == null)
			{
				ServletUtils.sendMessage(new Message("Unauthorized",
						"400", "Please specify both new and old password"), res, HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			else if(new CheckUserCredentialsDatabase(getDataSource().getConnection(), u.getEmail(), oldpsw).checkUserCredentials() == null)
			{
				ServletUtils.sendMessage(new Message("Unauthorized",
						"401", "Wrong email/password"), res, HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			User ret = new UpdateUserPasswordDatabase(getDataSource().getConnection(), new User(null, u.getEmail(), newpsw)).updateUser();
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
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException se)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error",
					"500", se.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	public void deleteUser(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if(!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
		try
		{
			User u = User.fromJSON(req.getInputStream());

			if(new CheckUserCredentialsDatabase(getDataSource().getConnection(), u.getEmail(), u.getPassword()).checkUserCredentials() == null)
			{
				ServletUtils.sendMessage(new Message("Unauthorized",
						"401", "Wrong email/password"), res, HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			else if(!u.getEmail().equals(((User) req.getSession().getAttribute("current")).getEmail()))
			{
				ServletUtils.sendMessage(new Message("Unauthorized",
						"401", "You cannot delete other users' profile"), res, HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			User ret = new DeleteUserDatabase(getDataSource().getConnection(), u).deleteUser();
			if(ret == null)
			{
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
				res.setHeader("Content-Type", "application/json; charset=utf-8");
			}
			else
			{
				res.setStatus(HttpServletResponse.SC_OK);
				res.setHeader("Content-Type", "application/json; charset=utf-8");
				ret.toJSON(res.getOutputStream());
			}

		}
		catch (SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch(ServletException se)
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
