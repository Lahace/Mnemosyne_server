package ap.mnemosyne.util;

import ap.mnemosyne.resources.Message;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletUtils
{
	public static String checkContentType(String[] admitted, HttpServletRequest req)
	{
		String found = null;
		String reqtype = req.getHeader("Content-Type");
		for(String s : admitted)
			if(s.equals(reqtype))
				found = s;

		return found;
	}

	public static boolean checkContentType(String admitted, HttpServletRequest req)
	{
		String reqtype = req.getHeader("Content-Type");
		if(admitted.equals(reqtype))
			return true;

		return false;
	}

	public static String checkContentType(String[] admitted, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String found = null;
		String reqtype = req.getHeader("Content-Type");
		for(String s : admitted)
			if(s.equals(reqtype))
				found = s;
		if(found == null)
		{
			sendMessage(new Message("Content-Type not allowed", "415", reqtype + " Content-type is not allowed/supported in a " + req.getMethod() + " request for this URL"),
					res,HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE );
		}

		return found;
	}

	public static boolean checkContentType(String admitted, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String reqtype = req.getHeader("Content-Type");
		if(admitted.equals(reqtype))
			return true;

		sendMessage(new Message("Content-Type not allowed", "415", reqtype + " Content-type is not allowed/supported in a " + req.getMethod() + " request for this URL"),
					res, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		return false;
	}

	public static void sendMessage(Message m, HttpServletResponse res, int status) throws IOException
	{
		res.setStatus(status);
		res.setHeader("Content-Type", "application/json");
		m.toJSON(res.getOutputStream());
	}
}
