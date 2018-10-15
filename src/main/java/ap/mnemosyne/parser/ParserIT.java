package ap.mnemosyne.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ap.mnemosyne.parser.pResources.Marker;
import ap.mnemosyne.parser.pResources.TextualAction;
import ap.mnemosyne.parser.pResources.TextualConstraint;
import ap.mnemosyne.parser.pResources.TextualTask;
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
				new Marker("entro", new String[]{"S"}),
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
				"(" + String.join(".+|", CONSTRAINT_TOKENS) +".+)\\sdevo\\s(.+)",
				"devo\\s(.+)\\s("+ String.join(".+$|", CONSTRAINT_TOKENS) + ".+$)",
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
			if (m.groupCount() == 2)
				toReturn = new SentenceStrips(m.group(0), m.group(1), m.group(2), this); //Supporting only one action and one constraint max
			else if (m.groupCount() == 1)
				toReturn = new SentenceStrips(m.group(0), m.group(1), "", this);
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
		Annotation constraintProcessed = null;
		if(sp.getConstraint() != null) constraintProcessed = pipeline.runRaw(sp.getConstraint());

		//Action Processing

		List<CoreMap> sentences = actionProcessed.get(CoreAnnotations.SentencesAnnotation.class);
		CoreMap sentence = sentences.get(0); //I only want a single sentence
		//System.out.println(sentence.toShorterString());
		List<String> verbList = new ArrayList<>();
		String subject = "";
		for(CoreLabel e: sentence.get(CoreAnnotations.TokensAnnotation.class))
		{
			System.out.println("\t" + e.toString() + " is " + e.get(CoreAnnotations.PartOfSpeechAnnotation.class));
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
		System.out.println(textAct.toString());

		//Constraint Processing
		TextualConstraint textConstr = null;
		if(constraintProcessed != null)
		{
			sentences = constraintProcessed.get(CoreAnnotations.SentencesAnnotation.class);
			sentence = sentences.get(0); //I only want a single sentence
			//System.out.println(sentence.toShorterString());

			for(Marker m: CONSTRAINT_MARKERS)
			{
				if(sp.getConstraint().contains(m.getMarker()))
				{
					String check = "";
					for (CoreLabel e : sentence.get(CoreAnnotations.TokensAnnotation.class))
					{
						System.out.println("\t" + e.toString() + " is " + e.get(CoreAnnotations.PartOfSpeechAnnotation.class));
						if(e.get(CoreAnnotations.PartOfSpeechAnnotation.class).matches("N|V|S")) check += e.get(CoreAnnotations.PartOfSpeechAnnotation.class) + " ";
					}
					check = check.trim();
					boolean found = false;
					String snPattern = "";
					for(String e: m.getSyntacticNeeds())
						if(e.equals(check))
						{
							found = true;
							snPattern = e;
							break;
						}

					if(!found) throw new MalformedNLStringException("No matching pattern found in constraint");

					String regexp = "(" + String.join("|", CONSTRAINT_TOKENS) + ")\\s(.+$)";

					Pattern reg = Pattern.compile(regexp);
					Matcher matcher = reg.matcher(sp.getConstraint());
					if(matcher.find() && matcher.groupCount() == 2)
					{
						int sn = 0;
						String res="";
						String[] snArray = snPattern.split(" ");
						for (CoreLabel e : sentence.get(CoreAnnotations.TokensAnnotation.class))
						{
							if(e.get(CoreAnnotations.PartOfSpeechAnnotation.class).equals(snArray[sn]))
							{
								res += e.toString().split("-")[0] + "_";
								sn++;
							}
						}
						textConstr = new TextualConstraint(matcher.group(1), res.substring(0,res.length()-1));
						System.out.println(textConstr);
					}
					else
					{
						throw new MalformedNLStringException("Malformed constraint");
					}
					break;
				}
			}
		}

		return new TextualTask(textAct, textConstr);
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
