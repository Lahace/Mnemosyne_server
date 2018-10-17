package ap.mnemosyne.ontology;

import org.junit.Test;

public class OntologyEngineTest
{
	@Test
	public void test()
	{
		OntologyEngine oe = new OntologyEngine();
		System.out.println(new GetVerbObjectParameterOntology(oe, "prendere", "pane").getVerbObjectParameter());
	}
}