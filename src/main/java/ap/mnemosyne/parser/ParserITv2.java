package ap.mnemosyne.parser;

import ap.mnemosyne.parser.resources.TextualAction;
import ap.mnemosyne.parser.resources.TextualConstraint;
import ap.mnemosyne.parser.resources.TextualTask;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserITv2
{
	private TintPipeline pipeline;
	private final Logger LOGGER = Logger.getLogger(ParserITv2.class.getName());
	private Map<String, String> textToTime = new HashMap<>();

	public ParserITv2()
	{
		LOGGER.info("Loading pipeline.. ");
		pipeline = new TintPipeline();
		try
		{
			pipeline.loadDefaultProperties();
			pipeline.setProperty("annotators", "ita_toksent,pos,ita_morpho,ita_lemma,depparse");

		}
		catch(IOException ioe)
		{
			LOGGER.severe(ioe.getMessage());
			return;
		}
		pipeline.load();
		initializeTTTMap();
		LOGGER.info("Loaded.");
	}

	public TextualTask parseString(String text)
	{
		//Phase 1
		String pre = this.preProcessString(text);

		//Phase 2
		TextualTask tt = this.retrieveTextualTask(pre);
		LOGGER.info(tt.toString());

		return tt;
	}

	private String preProcessString(String string)
	{
		String toRet = string.toLowerCase();
		toRet = toRet.replaceAll("mezzo giorno", "mezzogiorno");
		List<String> toExclude = new ArrayList<>(Arrays.asList("ricordami che (.+$)", "ricordami di (.+$)", "devo (.+$)", "ricordami che devo (.+$)"));
		int numMatch = matchesList(toRet, toExclude);

		if(numMatch >= 0)
		{
			Pattern reg = Pattern.compile(toExclude.get(numMatch));
			Matcher m = reg.matcher(toRet);
			if(m.find())
			{
				toRet = m.group(1);
			}

		}

		return toRet;
	}


	private TextualTask retrieveTextualTask(String text)
	{
		Annotation a = pipeline.runRaw(text);
		List<CoreMap> sentences = a.get(CoreAnnotations.SentencesAnnotation.class);
		CoreMap sentence = sentences.get(0);
		SemanticGraph sg = sentence.get(BasicDependenciesAnnotation.class);

		IndexedWord root = sg.getFirstRoot();
		String rootValue = root.getString(CoreAnnotations.LemmaAnnotation.class);
		LOGGER.info(sg.toCompactString(true));
		TextualAction tact = null;
		List<TextualConstraint> tconstr = new ArrayList<>();

		String marker = null;
		String word = null;
		String verb = "null";
		String actionVerb = null;
		String actionSubject = null;
		boolean isFuture = false;
		for(SemanticGraphEdge sge : sg.outgoingEdgeList(root))
		{
			switch(sge.getRelation().toString())
			{
				case "dobj":
					tact = new TextualAction(rootValue,sge.getTarget().value());
					break;

				case "advmod":
					if(sge.getTarget().value().equals("domani"))
					{
						word = sge.getTarget().value();
						for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
						{
							switch(sge2.getRelation().toString())
							{
								case "nmod":
									isFuture = true;
									word = textToTime.get(sge2.getTarget().value())==null ? sge2.getTarget().value() : textToTime.get(sge2.getTarget().value());
									break;

								case "case":
									marker = sge2.getTarget().value();
									break;

								case "mark":
									marker = sge2.getTarget().value();
									break;

								case "nummod":
									isFuture = true;
									try
									{
										//Gotta do this little hack, because docs on how to extract timestamps is scarce
										word = text.substring(sge2.getTarget().beginPosition(), sge2.getTarget().endPosition() + 3);
										if(word.length() == 4) word = "0" + word;
										LocalTime.parse(word);
									}
									catch (StringIndexOutOfBoundsException | DateTimeParseException e)
									{
										word = sge2.getTarget().value();
									}
							}
						}
					}
					else
					{
						word = sge.getTarget().value();
						for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
						{
							switch(sge2.getRelation().toString())
							{
								case "case":
									marker = sge2.getTarget().value();
									break;

								case "mark":
									marker = sge2.getTarget().value();
									break;
							}
						}
					}
					tconstr.add(new TextualConstraint(marker,word, "null", isFuture));
					break;

				case "nmod":
					if(sge.getTarget().value().equals("domani"))
					{
						word = sge.getTarget().value();
						for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
						{
							switch(sge2.getRelation().toString())
							{
								case "case":
									marker = sge2.getTarget().value();
									break;

								case "nmod":
									isFuture = true;
									word = sge2.getTarget().value();
									for(SemanticGraphEdge sge3 : sg.outgoingEdgeList(sge2.getTarget()))
									{
										switch(sge3.getRelation().toString())
										{
											case "nmod":
												word += "_" + sge3.getTarget().value();
										}
									}
									break;

								case "nummod":
									isFuture = true;
									word = sge2.getTarget().value();
									for(SemanticGraphEdge sge3 : sg.outgoingEdgeList(sge2.getTarget()))
									{
										switch(sge3.getRelation().toString())
										{
											case "nmod":
												word += "_" + sge3.getTarget().value();
										}
									}
									break;
							}
						}
						word = textToTime.get(word) == null ? word : textToTime.get(word);
					}
					else
					{
						try
						{
							//Gotta do this little hack, because docs on how to extract timestamps is scarce
							word = text.substring(sge.getTarget().beginPosition(), sge.getTarget().endPosition() + 3);
							if(word.length() == 4) word = "0" + word;
							LocalTime.parse(word);
						}
						catch (StringIndexOutOfBoundsException | DateTimeParseException e)
						{
							word = sge.getTarget().value();
						}
						for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
						{
							switch(sge2.getRelation().toString())
							{
								case "case":
									marker = sge2.getTarget().value();
									break;
							}
						}
					}
					tconstr.add(new TextualConstraint(marker,word, "null", isFuture));
					break;

				case "nummod":
					try
					{
						//Gotta do this little hack, because docs on how to extract timestamps is scarce
						word = text.substring(sge.getTarget().beginPosition(), sge.getTarget().endPosition() + 3);
						if(word.length() == 4) word = "0" + word;
						LocalTime.parse(word);
					}
					catch (StringIndexOutOfBoundsException | DateTimeParseException e)
					{
						word = sge.getTarget().value();
					}
					for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
					{
						switch(sge2.getRelation().toString())
						{
							case "case":
								marker = sge2.getTarget().value();
								break;
						}
					}
					tconstr.add(new TextualConstraint(marker,word, "null", isFuture));
					break;

				case "ccomp":
					actionVerb = rootValue + "_" + sge.getTarget().value();
					for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
					{
						switch(sge2.getRelation().toString())
						{
							case "nmod":
								actionSubject = sge2.getTarget().value();
								break;

							case "advcl":
								marker = null;
								word = null;
								verb = sge2.getTarget().get(CoreAnnotations.LemmaAnnotation.class);
								for(SemanticGraphEdge sge3 : sg.outgoingEdgeList(sge2.getTarget()))
								{
									switch(sge3.getRelation().toString())
									{
										case "mark":
											marker = sge3.getTarget().value();
											break;

										case "nmod":
											word = sge3.getTarget().getString(CoreAnnotations.LemmaAnnotation.class);
											break;
									}
								}
								tconstr.add(new TextualConstraint(marker, word,  verb, isFuture));
								break;
						}
					}
					tact = new TextualAction(actionVerb, actionSubject);
					break;


				case "advcl":
					verb = sge.getTarget().get(CoreAnnotations.LemmaAnnotation.class);
					for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
					{
						switch(sge2.getRelation().toString())
						{
							case "mark":
								marker = sge2.getTarget().value();
								break;

							case "nmod":
								word = sge2.getTarget().getString(CoreAnnotations.LemmaAnnotation.class);
								break;

							case "advmod":
								isFuture = sge2.getTarget().value().equals("domani");
								break;
						}
					}
					tconstr.add(new TextualConstraint(marker, word,  verb, isFuture));
					break;

			}
		}

		return new TextualTask(tact, tconstr, text);
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

	private void initializeTTTMap()
	{
		textToTime.put("mezzogiorno", "12:00");
		textToTime.put("una", "13:00");
		textToTime.put("una_notte", "1:00");
		textToTime.put("due", "14:00");
		textToTime.put("due_pomeriggio", "14:00");
		textToTime.put("due_notte", "2:00");
		textToTime.put("due_mattina", "2:00");
		textToTime.put("tre", "15:00");
		textToTime.put("tre_pomeriggio", "15:00");
		textToTime.put("tre_mattina", "3:00");
		textToTime.put("tre_notte", "3:00");
		textToTime.put("quattro", "16:00");
		textToTime.put("quattro_pomeriggio", "16:00");
		textToTime.put("quattro_notte", "4:00");
		textToTime.put("quattro_mattina", "4:00");
		textToTime.put("cinque", "17:00");
		textToTime.put("cinque_pomeriggio", "17:00");
		textToTime.put("cinque_mattina", "5:00");
		textToTime.put("cinque_notte", "5:00");
		textToTime.put("sei", "18:00");
		textToTime.put("sei_sera", "18:00");
		textToTime.put("sei_mattina", "6:00");
		textToTime.put("sei_notte", "6:00");
		textToTime.put("sette", "19:00");
		textToTime.put("sette_sera", "19:00");
		textToTime.put("sette_mattina", "7:00");
		textToTime.put("sette_notte", "7:00");
		textToTime.put("otto", "20:00");
		textToTime.put("otto_sera", "20:00");
		textToTime.put("otto_mattina", "8:00");
		textToTime.put("otto_notte", "8:00");
		textToTime.put("nove", "21:00");
		textToTime.put("nove_sera", "21:00");
		textToTime.put("nove_mattina", "9:00");
		textToTime.put("nove_notte", "9:00");
		textToTime.put("dieci", "22:00");
		textToTime.put("dieci_mattina", "10:00");
		textToTime.put("dieci_sera", "22:00");
		textToTime.put("undici", "23:00");
		textToTime.put("undici_mattina", "11:00");
		textToTime.put("undici_sera", "23:00");
	}

	/*
		System.out.println("ChildRelns: " + sg.childRelns(root));
		System.out.println("ChildPairs: " + sg.childPairs(root));
		System.out.println("Childs: " + sg.getChildren(root));
		System.out.println("Descendants: " + sg.descendants(root));
		System.out.println(sg.toCompactString(true));
		System.out.println("relns: " + sg.relns(root));
		System.out.println("Comments: " + sg.getComments());
		System.out.println("After: " + root.after());
		System.out.println("Before: " + root.before());
		System.out.println("Value: " + root.value());
		System.out.println("Lemma: " + root.lemma());
		System.out.println("Tag: " + root.tag());
		System.out.println("Backing Label: " + root.backingLabel());
		System.out.println("Ner: " + root.ner());
	 */
}
