package ap.mnemosyne.resources;

import com.fasterxml.jackson.core.*;

import java.io.*;

public class Animale extends Resource
{

	final String razza, categoria;
	final int id, anni;
	
	public Animale(int id, String razza, String categoria, int anni)
	{
		this.id = id;
		this.anni = anni;
		this.razza = razza;
		this.categoria = categoria;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getAnni()
	{
		return anni;
	}
	
	public String getRazza()
	{
		return razza;
	}
	
	public String getCategoria()
	{
		return categoria;
	}
	 
	@Override
	public final void toJSON(final OutputStream out) throws IOException {

		final JsonGenerator jg = JSON_FACTORY.createGenerator(out);

		jg.writeStartObject();

		jg.writeFieldName("animale");

		jg.writeStartObject();

		jg.writeNumberField("id", id);

		jg.writeStringField("razza", razza);

		jg.writeStringField("categoria", categoria);

		jg.writeNumberField("anni", anni);

		jg.writeEndObject();

		jg.writeEndObject();

		jg.flush();
	}

}
