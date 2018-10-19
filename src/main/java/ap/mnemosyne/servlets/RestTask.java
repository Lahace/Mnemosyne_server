package ap.mnemosyne.servlets;

import ap.mnemosyne.resources.Task;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.io.IOException;

@Path("task")
public class RestTask
{
	@GET
	public void getTasks(@Context HttpServletRequest req, @Context HttpServletResponse res) throws IOException
	{
		new Task("Pippo").toJSON(res.getOutputStream());
	}

	@GET
	@Path("task/{id}")
	public void getTaskByID(@Context HttpServletRequest req, @Context HttpServletResponse res, @PathParam("id") int id) throws IOException
	{
		new Task(new Integer(id).toString()).toJSON(res.getOutputStream());
	}
}
