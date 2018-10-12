package ap.mnemosyne.parser.pResources;

public class TextualAction
{
	private String verb, subject;

	public TextualAction(String verb, String subject)
	{
		this.verb = verb;
		this.subject = subject;
	}

	public String getVerb()
	{
		return verb;
	}

	public void setVerb(String verb)
	{
		this.verb = verb;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	@Override
	public String toString()
	{
		return "TextualAction{" +
				"verb='" + verb + '\'' +
				", subject='" + subject + '\'' +
				'}';
	}

}
