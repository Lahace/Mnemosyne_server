package ap.mnemosyne.resources;

import ap.mnemosyne.enums.ConstraintTemporalType;
import ap.mnemosyne.enums.ParamsName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalTime;

@JsonTypeName("task-time-constraint")
public class TaskTimeConstraint extends TaskConstraint
{
	private LocalTime constraintTime;

	@JsonCreator
	public TaskTimeConstraint(@JsonProperty("constraintTime") LocalTime constraintTime, @JsonProperty("paramName") ParamsName paramsName , @JsonProperty("type") ConstraintTemporalType type)
	{
		super(paramsName, type);
		this.constraintTime = constraintTime;
	}

	public LocalTime getConstraintTime()
	{
		return constraintTime;
	}

	@Override
	public final void toJSON(final OutputStream out) throws IOException
	{
		PrintWriter pw = new PrintWriter(out);
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
		om.findAndRegisterModules();
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
}
