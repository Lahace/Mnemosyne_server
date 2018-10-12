package ap.mnemosyne.parser.pResources;

public class Marker
{
	String marker;
	String[] syntacticNeeds;

	public Marker(String marker, String[] syntacticNeeds)
	{
		this.marker = marker;
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
