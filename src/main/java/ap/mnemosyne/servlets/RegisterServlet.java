package ap.mnemosyne.servlets;

import ap.mnemosyne.database.CreateUserDatabase;
import ap.mnemosyne.resources.Message;
import ap.mnemosyne.resources.User;
import ap.mnemosyne.util.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;

public class RegisterServlet extends AbstractDatabaseServlet
{
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		try
		{
			if (!ServletUtils.checkContentType(MediaType.APPLICATION_JSON, req, res)) return;
			User u = User.fromJSON(req.getInputStream());
			User created = new CreateUserDatabase(getDataSource().getConnection(), u).CreateUser();
			res.setStatus(HttpServletResponse.SC_OK);
			created.toJSON(res.getOutputStream());
		}
		catch(SQLException sqle)
		{
			ServletUtils.sendMessage(new Message("Internal Server Error (SQL State: " + sqle.getSQLState() + ", error code: " + sqle.getErrorCode() + ")",
					"500", sqle.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (IOException ioe)
		{
			ServletUtils.sendMessage(new Message("IOException in RegisterServlet", "500", ioe.getMessage()), res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
