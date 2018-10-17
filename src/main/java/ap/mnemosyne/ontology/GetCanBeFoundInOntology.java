package ap.mnemosyne.ontology;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class GetCanBeFoundInOntology
{
	private OntologyEngine oe;
	private String item;
	private String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
			"PREFIX : <@=@>\n"+
			"SELECT ?y " +
			"WHERE " +
			"{\n" +
			":@=@ :canBeFoundIn ?y\n" +
			"}";

	public GetCanBeFoundInOntology(OntologyEngine oe, String verb) //TODO: test
	{
		this.oe = oe;
		this.item = verb;
	}

	public String getCanBeFoundInOntology()
	{
		oe.prepareQuery(query);
		oe.setParam(oe.ONTOLOGY_URI);
		oe.setParam(item);

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
