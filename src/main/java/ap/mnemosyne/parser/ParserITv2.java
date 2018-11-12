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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserITv2
{
	private TintPipeline pipeline;
	private final Logger LOGGER = Logger.getLogger(ParserITv2.class.getName());

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
		List<String> toExclude = new ArrayList<>(Arrays.asList("ricordami che (.+$)", "ricordami di (.+$)", "devo (.+$)", "ricordami che devo (.+$)"));
		int numMatch = matchesList(string, toExclude);

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
		for(SemanticGraphEdge sge : sg.outgoingEdgeList(root))
		{
			switch(sge.getRelation().toString())
			{
				case "dobj":
					tact = new TextualAction(rootValue,sge.getTarget().value());
					break;

				case "advmod":
					marker = null;
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
					tconstr.add(new TextualConstraint(marker,word, "null"));
					break;

				case "nmod":
					marker = null;
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
					tconstr.add(new TextualConstraint(marker,word, "null"));
					break;

				case "nummod":
					marker = null;
					word = null;
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
					tconstr.add(new TextualConstraint(marker,word, "null"));
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
								tconstr.add(new TextualConstraint(marker, word,  verb));
								break;
						}
					}
					tact = new TextualAction(actionVerb, actionSubject);
					break;


				case "advcl":
					marker = null;
					word = null;
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
						}
					}
					tconstr.add(new TextualConstraint(marker, word,  verb));
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
