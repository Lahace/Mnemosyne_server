package ap.mnemosyne.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

@Path("parameter")
public class RestParameter
{
	@GET
	public void getParameter(@Context HttpServletRequest req, @Context HttpServletResponse res)
	{
		//TODO
	}

	@POST
	public void createParameter(@Context HttpServletRequest req, @Context HttpServletResponse res)
	{
		//TODO
	}

	@PUT
	public void updateParameter(@Context HttpServletRequest req, @Context HttpServletResponse res)
	{
		//TODO
	}

	@DELETE
	public void deleteParameter(@Context HttpServletRequest req, @Context HttpServletResponse res)
	{
		//TODO
	}
}
