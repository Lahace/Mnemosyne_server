package ap.mnemosyne.resources;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

public class Auth extends Resource
{
	private String cookieID;
	private String mail;

	public Auth(String cookieID, String mail)
	{
		this.cookieID = cookieID;
		this.mail = mail;
	}

	public String getCookieID()
	{
		return cookieID;
	}

	public String getMail()
	{
		return mail;
	}

	@Override
	public final void toJSON(final OutputStream out) throws IOException
	{

		final JsonGenerator jg = JSON_FACTORY.createGenerator(out);

		jg.writeStartObject();

		jg.writeFieldName("auth");

		jg.writeStartObject();

		jg.writeStringField("email", mail);

		jg.writeStringField("cookieID", cookieID);

		jg.writeEndObject();

		jg.writeEndObject();

		jg.flush();
	}
}
