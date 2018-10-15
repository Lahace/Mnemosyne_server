package ap.mnemosyne.parser.pResources;

import java.security.InvalidParameterException;

public class Marker
{
	String marker;
	String[] syntacticNeeds;

	public Marker(String marker, String[] syntacticNeeds)
	{
		this.marker = marker;
		for(String e: syntacticNeeds)
			if(!e.matches("((N|V|S)\\s)*(N|V|S)"))
				throw new InvalidParameterException("syntacticNeeds must satisfy regex: ((N|V|S)\\s)*(N|V|S) (found: " + e +")");
		this.syntacticNeeds = syntacticNeeds;
	}

	public String getMarker()
	{
		return marker;
	}

	public void setMarker(String marker)
	{
		this.marker = marker;
	}

	public String[] getSyntacticNeeds()
	{
		return syntacticNeeds;
	}

	public void setSyntacticNeeds(String[] syntacticNeeds)
	{
		this.syntacticNeeds = syntacticNeeds;
	}

	@Override
	public String toString()
	{
		return "Marker{" +
				"marker='" + marker + '\'' +
				", syntacticNeeds='" + syntacticNeeds + '\'' +
				'}';
	}

}
