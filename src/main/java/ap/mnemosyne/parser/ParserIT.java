package ap.mnemosyne.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

public class ParserIT
{
	TintPipeline pipeline;

	public ParserIT()
	{
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
		InputStream stream = new ByteArrayInputStream(toParse.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		Annotation annotation;
		try
		{
			annotation = pipeline.run(stream, System.out, TintRunner.OutputFormat.JSON);
		}
		catch(IOException ioe)
		{
			System.out.print(ioe.getMessage());
			return null;
		}
		return annotation;
	}
}
