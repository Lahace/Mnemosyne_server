package ap.mnemosyne.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User extends Resource
{
	private String sessionID;
	private String email;
	private String password;

	public User() {} //mandatory for the fromJSON method

	public User(String sessionID, String email)
	{
		this.sessionID = sessionID;
		this.email = email;
	}

	public User(String sessionID, String email, String password)
	{
		this.sessionID = sessionID;
		this.email = email;
		this.password = password;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	public String getEmail()
	{
		return email;
	}

	public String getPassword()
	{
		return password;
	}

	@Override
	public final void toJSON(final OutputStream out) throws IOException
	{
		PrintWriter pw = new PrintWriter(out);
		ObjectMapper om = new ObjectMapper();
		String temp = this.password;
		this.password = null;
		pw.print("{\"user\":" + om.writeValueAsString(this) + "}"); //hashed password will not be sent via JSON
		this.password = temp;
		pw.flush();
		pw.close();
	}

	public final String toJSON() throws JsonProcessingException
	{
		ObjectMapper om = new ObjectMapper();
		String temp = this.password;
		this.password = null;
		String toRet = "{\"user\":" + om.writeValueAsString(this) + "}"; //hashed password will not be sent via JSON
		this.password = temp;
		return toRet;
	}

	public static final User fromJSON(InputStream in) throws IOException
	{
		String regex = "\\{\"user\":(\\{.*\\})\\}";
		StringBuilder textBuilder = new StringBuilder();
		Reader reader = new BufferedReader(new InputStreamReader(in));
		int c = 0;
		while ((c = reader.read()) != -1)
		{
			textBuilder.append((char) c);
		}

		if(!textBuilder.toString().matches(regex)) throw new IOException("No User object found (needed: " + regex + ", found: " + textBuilder.toString() + ")");

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(textBuilder.toString());
		matcher.find();

		ObjectMapper om = new ObjectMapper();
		User u = om.readValue(matcher.group(1), User.class);
		return u;
	}
}
