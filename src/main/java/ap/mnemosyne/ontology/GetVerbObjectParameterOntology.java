package ap.mnemosyne.ontology;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.List;

public class GetVerbObjectParameterOntology
{
	private OntologyEngine oe;
	private String verb, obj;
	private String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
	"PREFIX : <@=@>\n"+
	"SELECT ?y " +
			"WHERE " +
			"{\n" +
				"?x :vword \"@=@\"^^xsd:string.\n" +
				"?x :relatedTo :@=@.\n" +
				"?x :requires ?y\n" +
			"}";

	public GetVerbObjectParameterOntology(OntologyEngine oe, String verb, String obj)
	{
		this.oe = oe;
		this.verb = verb;
		this.obj = obj;
	}

	public String getVerbObjectParameter()
	{
		oe.prepareQuery(query);
		oe.setParam(oe.ONTOLOGY_URI);
		oe.setParam(verb);
		oe.setParam(obj);

		ResultSet result = oe.execute();

		List<String> vars = result.getResultVars();
		String toReturn=null;
		while(result.hasNext())
		{
			QuerySolution qs = result.nextSolution();
			for(String v: vars)
				toReturn = qs.get("y").asResource().getLocalName();
		}
		return toReturn;
	}
}
