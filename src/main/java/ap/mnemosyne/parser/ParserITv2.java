package ap.mnemosyne.parser;

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

	public String parseString(String text)
	{
		String pre = this.preProcessString(text);
		Annotation a = pipeline.runRaw(pre);
		List<CoreMap> sentences = a.get(CoreAnnotations.SentencesAnnotation.class);
		CoreMap sentence = sentences.get(0);
		SemanticGraph sg = sentence.get(BasicDependenciesAnnotation.class);

		this.retrieveTextualTask(sg);

		return null;
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


	private TextualTask retrieveTextualTask(SemanticGraph sg)
	{
		IndexedWord root = sg.getFirstRoot();
		String rootValue = root.value();
		System.out.println(sg.toCompactString(true));
		for(SemanticGraphEdge sge : sg.outgoingEdgeList(root))
		{
			switch(sge.getRelation().toString())
			{
				case "dobj":
					break;

				case "":
					break;
			}
		}

		return null;
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
