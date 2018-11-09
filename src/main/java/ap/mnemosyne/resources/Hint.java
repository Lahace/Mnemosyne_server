package ap.mnemosyne.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@JsonTypeName("taskID")
public class Hint extends Resource
{
	private int taskID;
	private boolean urgent;

	@JsonCreator
	public Hint(@JsonProperty("task-id") int taskID, @JsonProperty("urgent") boolean urgent)
	{
		this.taskID = taskID;
		this.urgent = urgent;
	}

	public int getTaskID()
	{
		return taskID;
	}

	public boolean isUrgent()
	{
		return urgent;
	}

	@Override
	public final void toJSON(final OutputStream out) throws IOException
	{
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
		ObjectMapper om = new ObjectMapper();
		om.findAndRegisterModules();
		pw.print(om.writeValueAsString(this));
		pw.flush();
		pw.close();
	}

	@Override
	public final void toJSON(final PrintWriter pw) throws IOException
	{
		ObjectMapper om = new ObjectMapper();
		pw.print(om.writeValueAsString(this));
		pw.flush();
	}

	@Override
	public final String toJSON() throws JsonProcessingException
	{
		ObjectMapper om = new ObjectMapper();
		om.findAndRegisterModules();
		return om.writeValueAsString(this);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Hint hint = (Hint) o;
		return taskID == hint.taskID &&
				urgent == hint.urgent;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(taskID, urgent);
	}

	@Override
	public String toString()
	{
		return "Hint{" +
				"taskID=" + taskID +
				", urgent=" + urgent +
				'}';
	}

}
