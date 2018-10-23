package ap.mnemosyne.resources;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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
		PrintWriter pw = new PrintWriter(out);
		ObjectMapper om = new ObjectMapper();
		pw.print("{\"task\":" + om.writeValueAsString(this) + "}");
		pw.flush();
		pw.close();
	}

	public final String toJSON() throws JsonProcessingException
	{
		ObjectMapper om = new ObjectMapper();
		return "{\"task\":" + om.writeValueAsString(this) + "}";
	}
}
