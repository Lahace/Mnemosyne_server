package ap.mnemosyne.util;

import ap.mnemosyne.resources.Message;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServletUtils
{
	public static String checkContentType(String[] admitted, HttpServletRequest req)
	{
		String found = null;
		String reqtype = req.getHeader("Content-Type").replaceAll(" ", "");
		for(String s : admitted)
			if(s.replaceAll(" ", "").equals(reqtype))
				found = s;

		return found;
	}

	public static boolean checkContentType(String admitted, HttpServletRequest req)
	{
		String reqtype = req.getHeader("Content-Type").replaceAll(" ", "");
        return admitted.replaceAll(" ", "").equals(reqtype);

    }

	public static String checkContentType(String[] admitted, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		String found = null;
		String reqtype = req.getHeader("Content-Type").replaceAll(" ", "");
		for(String s : admitted)
			if(s.replaceAll(" ", "").equals(reqtype))
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
		String reqtype = req.getHeader("Content-Type").replaceAll(" ", "");
		if(admitted.replaceAll(" ", "").equals(reqtype))
			return true;

		sendMessage(new Message("Content-Type not allowed", "415", reqtype + " Content-type is not allowed/supported in a " + req.getMethod() + " request for this URL"),
					res, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		return false;
	}

	public static void sendMessage(Message m, HttpServletResponse res, int status) throws IOException
	{
		res.setStatus(status);
		res.setHeader("Content-Type", "application/json; charset=utf-8");
		m.toJSON(res.getOutputStream());
	}

	public static String SHA256Hash(String toHash) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(toHash.getBytes());
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if(hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
