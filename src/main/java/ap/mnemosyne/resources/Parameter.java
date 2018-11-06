package ap.mnemosyne.resources;

import ap.mnemosyne.enums.ParamsName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
		@JsonSubTypes.Type(value = TimeParameter.class),
		@JsonSubTypes.Type(value = LocationParameter.class)
})
public abstract class Parameter extends Resource
{
	private ParamsName name;
	private String userEmail;

	public Parameter(@JsonProperty("name") ParamsName name, @JsonProperty("type") String userEmail)
	{
		this.name = name;
		this.userEmail = userEmail;
	}

	public ParamsName getName()
	{
		return name;
	}

	public String getUserEmail()
	{
		return userEmail;
	}

}
