package ap.mnemosyne.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

public class ParserIT extends Parser
{
	private TintPipeline pipeline;

	public ParserIT()
	{
		super(new ArrayList<>(Arrays.asList(
				"quando", "entro", "prima", "dopo", "per le"
		)));

		pipeline = new TintPipeline();
		try
		{
			pipeline.loadDefaultProperties();
		}
		catch(IOException ioe)
		{
			System.out.print(ioe.getMessage());
			return;
		}
		pipeline.load();
	}

	public Annotation parseString(String toParse)
	{
		Annotation annotation = null;

		try
		{
			//Phase 1: splitting
			SentenceStrips sp = splitActionConstraint(toParse);

			System.out.println(sp);

			InputStream stream = new ByteArrayInputStream(toParse.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			annotation = pipeline.run(stream, System.out, TintRunner.OutputFormat.JSON);
		}
		catch(IOException ioe)
		{
			System.out.print(ioe.getMessage());
			return null;
		}
		catch(MalformedNLStringException mnlse)
		{
			System.out.println(mnlse.getMessage());
		}

		return null;
	}

	private SentenceStrips splitActionConstraint(String toSplit) throws MalformedNLStringException
	{
		List<String> regexp = new ArrayList<>(Arrays.asList(
				"(" + String.join(".+|", CONSTRAINT_MARKERS) +".+)\\sdevo\\s(.+)",
				"devo\\s(.+)\\s("+ String.join(".+$|", CONSTRAINT_MARKERS) + ".+$)",
				"devo\\s(?!" +String.join(".+$|", CONSTRAINT_MARKERS) + ".+$)(.+)$"
		));

		SentenceStrips toReturn = null;
		toSplit = toSplit.toLowerCase();

		int numMatch;
		if ((numMatch = matchesList(toSplit, regexp)) < 0)
			throw new MalformedNLStringException("Badly formatted NLString");

		Pattern reg = Pattern.compile(regexp.get(numMatch));
		Matcher m = reg.matcher(toSplit);
		m.find();
		if(m.groupCount() == 2)
			toReturn = new SentenceStrips(m.group(0), m.group(1), m.group(2), this); //Supporting only one action and one constraint max
		else if(m.groupCount() == 1)
			toReturn = new SentenceStrips(m.group(0), m.group(1), "", this);

		return toReturn;
	}

	private int matchesList(String toMatch, List<String> regexps)
	{
		int result = -1;
		for(String e : regexps)
			if(toMatch.matches(e))
			{
				result = regexps.indexOf(e);
				break;
			}
		return result;
	}
}
