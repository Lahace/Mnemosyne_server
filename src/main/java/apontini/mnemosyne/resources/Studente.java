package apontini.mnemosyne.resources;

import com.fasterxml.jackson.core.*;

import java.io.*;
import java.sql.Date;

public class Studente extends Resource
{
	final String email;
	final String name;
	final Date bday; //Date birthday = new Date(dateFormat.parse(bdate).getTime());
	
	
	public Studente(String email, String name, Date bday)
	{
		this.email = email;
		this.name = name;
		this.bday = bday;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Date getBday()
	{
		return bday;
	}
	
	@Override
	public final void toJSON(final OutputStream out) throws IOException
	{

		final JsonGenerator jg = JSON_FACTORY.createGenerator(out);

		jg.writeStartObject();

		jg.writeFieldName("studente");

		jg.writeStartObject();

		jg.writeStringField("email", email);

		jg.writeStringField("name", name);

		jg.writeStringField("bday", bday.toString());

		jg.writeEndObject();

		jg.writeEndObject();

		jg.flush();
	}
}
