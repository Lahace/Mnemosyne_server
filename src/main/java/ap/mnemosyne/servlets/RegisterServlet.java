package ap.mnemosyne.servlets;

import ap.mnemosyne.resources.Message;
import ap.mnemosyne.resources.User;
import ap.mnemosyne.util.ServletUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

public class RegisterServlet extends HttpServlet
{
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		try
		{
			if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
			User u = User.fromJSON(req.getInputStream());
			res.setStatus(HttpServletResponse.SC_OK);
			u.toJSON(res.getOutputStream());
		}
		catch (IOException ioe)
		{
			ServletUtils.sendMessage(new Message("IOException in RegisterServlet", "500", ioe.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}
}
