package ap.mnemosyne.parser;

class SentenceStrips
{
	private String fullSentence, action, constraint;

	public SentenceStrips(String fullSentence, String first, String second, Parser parserInstance)
	{
		this.fullSentence = fullSentence;
		action = first;
		for(String e : parserInstance.CONSTRAINT_MARKERS)
			if(first.contains(e))
			{
				constraint = first;
				action = second;
				break;
			}

		for(String e : parserInstance.CONSTRAINT_MARKERS)
			if(second.contains(e))
			{
				constraint = second;
				action = first;
				break;
			}
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

	public String getConstraint()
	{
		return constraint;
	}

	public void setConstraint(String constraint)
	{
		this.constraint = constraint;
	}

	@Override
	public String toString()
	{
		return "SentenceStrips{" +
				"fullSentence='" + fullSentence + '\'' +
				", action='" + action + '\'' +
				", constraint='" + constraint + '\'' +
				'}';
	}

}
