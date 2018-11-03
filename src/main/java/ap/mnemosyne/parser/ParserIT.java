package ap.mnemosyne.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ap.mnemosyne.exceptions.MalformedNLStringException;
import ap.mnemosyne.parser.resources.*;
import com.google.common.collect.Iterables;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;

public class ParserIT extends Parser
{
	private TintPipeline pipeline;

	public ParserIT()
	{
		super(new ArrayList<>(Arrays.asList(
				new Marker("quando", new String[]{"V S"}),
				new Marker("entro", new String[]{"B"}),
				new Marker("prima", new String[]{"V S"}),
				new Marker("dopo", new String[]{"S", "V"}),
				new Marker("per le", new String[]{"N"}),
				new Marker("alle", new String[]{"N"})
		)));

		System.out.print("Loading pipeline.. ");
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
		System.out.println("Loaded.");
	}

	public String parseString(String toParse)
	{
		//TODO aggiungere ad un log permanente le frasi con gli errori
		try
		{
			//==============Phase 1: splitting
			SentenceStrips sp = splitActionConstraint(toParse);
			System.out.println("Phase 1 result: " + sp);

			//==============phase 2: processing
			TextualTask tt = textualProcessing(sp);
			System.out.println("Phase 2 results: " + tt.toString());

			//==============phase 3: resolving


			System.out.println("==================================");
		}
		catch(MalformedNLStringException mnlse)
		{
			System.out.println(mnlse.getMessage());
			return null;
		}

		return null;
	}

	private SentenceStrips splitActionConstraint(String toSplit) throws MalformedNLStringException
	{

		List<String> regexp = new ArrayList<>(Arrays.asList(
			"devo\\s(?!" +String.join(".+$|", CONSTRAINT_TOKENS) + ".+$)(.+)\\s("+ String.join(".+|", CONSTRAINT_TOKENS) + ".+)\\s("+ String.join(".+|", CONSTRAINT_TOKENS) + ".+)",
			"(" + String.join(".+|", CONSTRAINT_TOKENS) +".+)\\sdevo\\s(?!" +String.join(".+$|", CONSTRAINT_TOKENS) + ".+$)(.+)",
			"devo\\s(?!" +String.join(".+$|", CONSTRAINT_TOKENS) + ".+$)(.+)\\s("+ String.join(".+$|", CONSTRAINT_TOKENS) + ".+$)",
			"devo\\s(?!" +String.join(".+$|", CONSTRAINT_TOKENS) + ".+$)(.+)$"
		));

		SentenceStrips toReturn = null;
		toSplit = toSplit.toLowerCase();

		int numMatch;
		if ((numMatch = matchesList(toSplit, regexp)) < 0)
			throw new MalformedNLStringException("Badly formatted NL String");

		Pattern reg = Pattern.compile(regexp.get(numMatch));
		Matcher m = reg.matcher(toSplit);
		if(m.find())
		{
			if (m.groupCount() == 1)
				toReturn = new SentenceStrips(m.group(0), m.group(1), new ArrayList<>());
			else if (m.groupCount() > 1)
			{
				List<String> paramList = new ArrayList<>();
				String action="";
				for(int i=1; i<=m.groupCount(); i++)
				{
					boolean found = false;
					for (Marker e: CONSTRAINT_MARKERS)
					{
						if(m.group(i).contains(e.getMarker()))
						{
							paramList.add(m.group(i));
							found = true;
							break;
						}
					}
					if(!found) action = m.group(i);
				}
				toReturn = new SentenceStrips(m.group(0),action,paramList);
			}
		}
		else
		{
			//Should never happen
			throw new MalformedNLStringException("WOW you got visited by the lucky ERROR\n" +
					"this ERROR can be seen only once in a century.\n" +
					"Please text 'HELLO MR. ERROR' to your closest friends or get a century of bad burritos and weak doggos");
		}

		return toReturn;
	}

	private TextualTask textualProcessing(SentenceStrips sp) throws MalformedNLStringException
	{

		Annotation actionProcessed = pipeline.runRaw(sp.getAction());

		//Action Processing

		List<CoreMap> sentences = actionProcessed.get(CoreAnnotations.SentencesAnnotation.class);
		CoreMap sentence = sentences.get(0); //I only want a single sentence
		//System.out.println(sentence.toShorterString());
		List<String> verbList = new ArrayList<>();
		String subject = "";
		for(CoreLabel e: sentence.get(CoreAnnotations.TokensAnnotation.class))
		{
			//System.out.println("\t" + e.toString() + " is " + e.get(CoreAnnotations.PartOfSpeechAnnotation.class));
			if(e.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("V")) verbList.add(e.toString().split("-")[0]);
			else if(e.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals("S")) subject = e.toString().split("-")[0];
		}
		if(verbList.isEmpty()) throw new MalformedNLStringException("No verb found");
		if(subject.equals("")) throw new MalformedNLStringException("No subject found");
		TextualAction textAct = new TextualAction(
				String.join(
						"_",
						verbList.toArray(new String[verbList.size()])),
				subject);

		//Constraint Processing

		List<TextualConstraint> textConstr = new ArrayList<>();
		for (String c: sp.getConstraints())
		{
			Annotation a = pipeline.runRaw(c);
			sentences = a.get(CoreAnnotations.SentencesAnnotation.class);
			sentence = sentences.get(0); //I only want a single sentence
			//System.out.println("Sentence: " + sentence.toShorterString());
			for (Marker m : CONSTRAINT_MARKERS)
			{
				if (c.contains(m.getMarker()))
				{
					String check = "";
					for (CoreLabel e : Iterables.skip(sentence.get(CoreAnnotations.TokensAnnotation.class), 1))
					{
						//System.out.println("\t" + e.toString() + " is " + e.get(CoreAnnotations.PartOfSpeechAnnotation.class));
						if (e.get(CoreAnnotations.PartOfSpeechAnnotation.class).matches("N|V|S|B"))
							check += e.get(CoreAnnotations.PartOfSpeechAnnotation.class) + " ";
					}
					check = check.trim();
					boolean found = false;
					String snPattern = "";
					for (String e : m.getSyntacticNeeds())
					{
						if (e.equals(check))
						{
							found = true;
							snPattern = e;
							break;
						}
					}
					if (!found) throw new MalformedNLStringException("No matching pattern found in constraint \"" + c + "\"");

					String regexp = "(" + String.join("|", CONSTRAINT_TOKENS) + ")\\s(.+$)";

					Pattern reg = Pattern.compile(regexp);
					Matcher matcher = reg.matcher(c);
					if (matcher.find() && matcher.groupCount() == 2)
					{
						int sn = 0;
						String res = "";
						String[] snArray = snPattern.split(" ");
						for (CoreLabel e : sentence.get(CoreAnnotations.TokensAnnotation.class))
						{
							if (e.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals(snArray[sn]))
							{
								res += e.toString().split("-")[0] + "_";
								sn++;
							}
						}
						textConstr.add(new TextualConstraint(matcher.group(1), res.substring(0, res.length() - 1), "verb")); //placeholder verb, no time to implement this
					}
					else
					{
						throw new MalformedNLStringException("Malformed constraint");
					}
					break;
				}
			}
		}
		return new TextualTask(textAct, textConstr, sp.getFullSentence()); //TODO: mettere i verbi all'infinito
	}

	private void resolveTask(TextualTask tt)
	{

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
