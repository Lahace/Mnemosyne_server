package apontini.mnemosyne.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.sql.SQLException;

import apontini.mnemosyne.database.SearchAnimalsDatabase;
import apontini.mnemosyne.resources.*;
import apontini.mnemosyne.database.*;

public class ResponseServlet extends AbstractDatabaseServlet
{

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		String categoria;

		ResourceList<Animale> list;
		Message m;

		categoria = req.getParameter("categoria");
		
		try
		{
			list = new ResourceList<Animale>(new SearchAnimalsDatabase(getDataSource().getConnection()).searchAnimals(categoria));
			m = new Message("OK");
		}
		catch(SQLException sqle)
		{
			list = null;
			m = new Message("ERROR", "qualche codice", sqle.getMessage());
		
		}
		
		res.setContentType("application/json; charset=utf-8");
		res.setStatus(HttpServletResponse.SC_OK);
		list.toJSON(res.getOutputStream());

	}
}
