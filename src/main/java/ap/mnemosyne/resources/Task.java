package ap.mnemosyne.resources;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

public class Task extends Resource
{
	String name;

	public Task(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public final void toJSON(final OutputStream out) throws IOException
	{

		final JsonGenerator jg = JSON_FACTORY.createGenerator(out);

		jg.writeStartObject();

		jg.writeFieldName(Task.class.getSimpleName().toLowerCase());

		jg.writeStartObject();

		jg.writeStringField("name", name);

		jg.writeEndObject();

		jg.writeEndObject();

		jg.flush();
	}
}
