package ap.mnemosyne.parser.resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TextualTask implements Serializable
{
	private TextualAction textualAction;
	private List<TextualConstraint> textualConstraints;
	private String fullSentence;

	public TextualTask(TextualAction textualAction, List<TextualConstraint> textualConstraints, String fullSentence)
	{
		this.textualAction = textualAction;
		this.textualConstraints = textualConstraints;
		this.fullSentence = fullSentence;
	}

	public TextualAction getTextualAction()
	{
		return textualAction;
	}

	public void setTextualAction(TextualAction textualAction)
	{
		this.textualAction = textualAction;
	}

	public List<TextualConstraint> getTextualConstraints()
	{
		return textualConstraints;
	}

	public void setTextualConstraints(List<TextualConstraint> textualConstraint)
	{
		this.textualConstraints = textualConstraint;
	}

	public String getFullSentence()
	{
		return fullSentence;
	}

	public void setFullSentence(String fullSentence)
	{
		this.fullSentence = fullSentence;
	}

	@Override
	public String toString()
	{
		return "TextualTask{" +
				"textualAction=" + textualAction +
				", textualConstraints=" + textualConstraints +
				", fullSentence='" + fullSentence + '\'' +
				'}';
	}

}
