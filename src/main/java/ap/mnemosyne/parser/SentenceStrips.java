package ap.mnemosyne.parser;

import java.util.List;

class SentenceStrips
{
	private String fullSentence, action;
	private List<String> constraints;

	public SentenceStrips(String fullSentence, String first, List<String> second)
	{
		this.fullSentence = fullSentence;
		action = first;
		constraints = second;
	}

	public String getFullSentence()
	{
		return fullSentence;
	}

	public void setFullSentence(String fullSentence)
	{
		this.fullSentence = fullSentence;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public List<String> getConstraints()
	{
		return constraints;
	}

	public void setConstraints(List<String> constraint)
	{
		this.constraints = constraint;
	}

	@Override
	public String toString()
	{
		return "SentenceStrips{" +
				"fullSentence='" + fullSentence + '\'' +
				", action='" + action + '\'' +
				", constraint='" + constraints + '\'' +
				'}';
	}

}
