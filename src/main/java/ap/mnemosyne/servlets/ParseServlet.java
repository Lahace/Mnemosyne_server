package ap.mnemosyne.servlets;

import ap.mnemosyne.parser.ParserITv2;
import ap.mnemosyne.parser.resources.TextualTask;
import ap.mnemosyne.util.ServletUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

public class ParseServlet extends AbstractDatabaseServlet
{

	private ParserITv2 parser;
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		parser = new ParserITv2();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		if(!ServletUtils.checkContentType(MediaType.APPLICATION_FORM_URLENCODED, req, res)) return;
		String sentence = req.getParameter("sentence");
		TextualTask tt = parser.parseString(sentence);

		//TODO: Resolve task

	}
}
