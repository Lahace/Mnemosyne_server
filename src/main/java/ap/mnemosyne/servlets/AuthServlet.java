package ap.mnemosyne.servlets;

import ap.mnemosyne.database.CheckUserCredentialsDatabase;
import ap.mnemosyne.database.UpdateUserSessionDatabase;
import ap.mnemosyne.listeners.SessionListener;
import ap.mnemosyne.resources.User;
import ap.mnemosyne.resources.Message;
import ap.mnemosyne.util.ServletUtils;

import javax.servlet.http.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;

public class AuthServlet extends AbstractDatabaseServlet
{
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		if(!ServletUtils.checkContentType(MediaType.APPLICATION_FORM_URLENCODED, req, res)) return;
		if(SessionListener.map.get(req.getRequestedSessionId()) != null)
		{
			ServletUtils.sendMessage(new Message("Valid session found", "403", "Your session is still valid"),
					res, HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		//TODO maybe add device signature to avoid sessionID spoofing
		if(email == null || password == null)
		{
			ServletUtils.sendMessage(new Message("Login failed", "401", "Email or password not specified"),
					res, HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		try
		{
			User u = new CheckUserCredentialsDatabase(getDataSource().getConnection(), email, password).checkUserCredentials();
			if (u != null)
			{
				if(u.getSessionID() != null && SessionListener.map.get(u.getSessionID()) != null)
				{
					res.setStatus(HttpServletResponse.SC_OK);
					res.setHeader("Content-Type", "application/json; charset=utf-8");
					u.toJSON(res.getOutputStream());
				}
				else
				{
					HttpSession s = req.getSession();
					User newUser = new UpdateUserSessionDatabase(getDataSource().getConnection(),
							new User(s.getId(), u.getEmail(), u.getPassword())).updateUserSession();
					if(newUser == null)
					{
						ServletUtils.sendMessage(new Message("Login failed", "500", "Something went wrong while updating database records"),
								res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						return;
					}
					s.setAttribute("current", newUser);
					res.setStatus(HttpServletResponse.SC_OK);
					res.setHeader("Content-Type", "application/json; charset=utf-8");
					newUser.toJSON(res.getOutputStream());
				}
			}
			else
			{
				ServletUtils.sendMessage(new Message("Login failed", "401", "Wrong email or password"),
						res, HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return;
	}

	public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		if(SessionListener.map.get(req.getRequestedSessionId()) == null)
		{
			ServletUtils.sendMessage(new Message("No session Found", "401", "No session found")
					, res, HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		req.getSession().invalidate();
		res.setStatus(HttpServletResponse.SC_OK);
		ServletUtils.sendMessage(new Message("Session invalidated"), res, HttpServletResponse.SC_OK);
	}
}
