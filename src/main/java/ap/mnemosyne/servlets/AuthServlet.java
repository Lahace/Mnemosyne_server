package ap.mnemosyne.servlets;

import ap.mnemosyne.resources.Auth;
import ap.mnemosyne.resources.Message;

import javax.servlet.http.*;
import java.io.IOException;

public class AuthServlet extends HttpServlet
{
	Message m;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		if(!checkSessionValidity(req,res)) return;

		Auth a = (Auth) req.getSession().getAttribute("current");
		res.setStatus(HttpServletResponse.SC_OK);
		res.setHeader("Content-Type", "application/json");
		a.toJSON(res.getOutputStream());
		return;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String email = req.getParameter("email");;
		String pass = req.getParameter("password");;
		//TODO maybe add device signature to avoid sessionID spoofing
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

	public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		if(!checkSessionValidity(req,res)) return;

		req.getSession().invalidate();
		res.setStatus(HttpServletResponse.SC_OK);
	}

	private boolean checkSessionValidity(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		Message m;
		String JSESSIONID = null;
		Cookie[] cookies = req.getCookies();
		if(cookies != null)
			for(Cookie c: cookies)
				if(c.getName().equals("JSESSIONID")) JSESSIONID = c.getValue();

		HttpSession session = req.getSession(false);

		if(session == null && JSESSIONID != null)
		{
			m = new Message("No session Found", "401", "No session is available with ID: " + JSESSIONID);
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
			return false;
		}
		else if(session == null && JSESSIONID == null)
		{
			m = new Message("No session Found", "401", "No JSESSIONID cookie found");
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
			return false;
		}
		else if(session != null && session.getAttribute("current") == null)
		{
			m = new Message("No session Found", "401", "Corrupted session with ID: " + JSESSIONID);
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Content-Type", "application/json");
			m.toJSON(res.getOutputStream());
			return false;
		}

		return true;
	}
}
