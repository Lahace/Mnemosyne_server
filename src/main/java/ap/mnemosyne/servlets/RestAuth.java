package ap.mnemosyne.servlets;

import ap.mnemosyne.resources.Auth;
import ap.mnemosyne.resources.Message;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("auth")
public class RestAuth
{
	Message m;

	@GET
	public void getCurrentAuth(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if(!checkSessionValidity(req,res)) return;

		Auth a = (Auth) req.getSession().getAttribute("current");
		res.setStatus(HttpServletResponse.SC_OK);
		res.setHeader("Content-Type", "application/json");
		a.toJSON(res.getOutputStream());
		return;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void setCurrentAuth(@Context HttpServletRequest req, @Context HttpServletResponse res, @FormParam("email") String email, @FormParam("password") String pass) throws IOException
	{
		if(email == null || pass == null)
		{
			m = new Message("Login failed", "401", "Email or password not specified");
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
		}
		else if(email.equals("lol") && pass.equals("lol"))
		{
			Auth a = new Auth(req.getSession().getId(), email);
			req.getSession().setAttribute("current", a);
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Content-Type", "application/json");
			a.toJSON(res.getOutputStream());
		}
		else
		{
			m = new Message("Login failed", "401", "Wrong email or password");
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
		}
		return;
	}

	@DELETE
	public void invalidateCurrentAuth(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		if(!checkSessionValidity(req,res)) return;
		req.getSession().invalidate();
	}

	private boolean checkSessionValidity(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String JSESSIONID = null;
		Cookie[] cookies = req.getCookies();
		if(cookies != null)
			for(Cookie c: cookies)
				if(c.getName().equals("JSESSIONID")) JSESSIONID = c.getValue();

		if(req.getSession().getAttribute("current") == null && JSESSIONID != null)
		{
			m = new Message("No session Found", "401", "No session is available with ID: " + JSESSIONID);
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
			return false;
		}
		else if(req.getSession().getAttribute("current") == null && JSESSIONID == null)
		{
			m = new Message("No session Found", "401", "No JSESSIONID cookie found");
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
			return false;
		}

		return true;
	}
}
