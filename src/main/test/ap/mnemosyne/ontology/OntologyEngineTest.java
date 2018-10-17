package ap.mnemosyne.ontology;

import org.junit.Test;

public class OntologyEngineTest
{
	@Test
	public void test()
	{
		OntologyEngine oe = new OntologyEngine();
		System.out.println(new GetMarkerWordParameterOntology(oe, "quando", "torno_casa").getMarkerWordParameter());
	}
}