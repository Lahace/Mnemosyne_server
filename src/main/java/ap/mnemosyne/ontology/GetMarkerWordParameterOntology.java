package ap.mnemosyne.ontology;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

@Deprecated
public class GetMarkerWordParameterOntology
{
	private OntologyEngine oe;
	private String marker, word;
	private String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
			"PREFIX : <@=@>\n"+
			"SELECT ?y " +
			"WHERE " +
			"{\n" +
			"?x :wword \"@=@\"^^xsd:string.\n" +
			"?x :after :@=@.\n" +
			"?x :wants ?y\n" +
			"}";

	public GetMarkerWordParameterOntology(OntologyEngine oe, String marker, String word)
	{
		this.oe = oe;
		this.marker = marker;
		this.word = word;
	}

	public String getMarkerWordParameter()
	{
		oe.prepareQuery(query);
		oe.setParam(oe.ONTOLOGY_URI);
		oe.setParam(word);
		oe.setParam(marker);

		ResultSet result = oe.execute();

		String toReturn=null;
		while(result.hasNext())
		{
			QuerySolution qs = result.nextSolution();
			toReturn = qs.get("y").asResource().getLocalName();
		}
		return toReturn;
	}
}
