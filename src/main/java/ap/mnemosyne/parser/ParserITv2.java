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
import eu.fbk.dh.tint.runner.TintRunner;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserITv2
{
	private TintPipeline pipeline;

	public ParserITv2()
	{
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

	public TextualTask parseString(String text)
	{
		//Phase 1
		String pre = this.preProcessString(text);

		//Phase 2
		TextualTask tt = this.retrieveTextualTask(pre);
		System.out.println(tt);

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
		System.out.println(sg.toCompactString(true));
		TextualAction tact = null;
		List<TextualConstraint> tconstr = new ArrayList<>();

		String marker = null;
		String word = null;
		for(SemanticGraphEdge sge : sg.outgoingEdgeList(root))
		{
			switch(sge.getRelation().toString())
			{
				case "dobj":
					tact = new TextualAction(rootValue,sge.getTarget().value());
					break;

				case "nmod":
					marker = null;
					word = sge.getTarget().value();
					for(SemanticGraphEdge sge2 : sg.outgoingEdgeList(sge.getTarget()))
					{
						switch(sge2.getRelation().toString())
						{
							case "case":
								marker = sge2.getTarget().value();
								break;
						}
					}
					tconstr.add(new TextualConstraint(marker,word, "verb")); //TODO: resolve placeholder
					break;

				case "advcl":
					marker = null;
					word = null;
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
					tconstr.add(new TextualConstraint(marker, word, "verb")); //TODO resolve placeholder
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
