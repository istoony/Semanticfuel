package cefriel.semanticfuel.service.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.Term;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class QueryEngineService extends AbstractService {

	public void updateOntology() throws IOException, SpatialIndexException {
		Pair<QuadStore, RDF4JStore> result = runRMLParser();
		QuadStore tripleList = result.getLeft();
		RDF4JStore rmlStore = result.getRight();

		List<Quad> triples = tripleList.getQuads(null, null, null);

//		triples.forEach(a -> System.out.println(
		// a.getSubject().toString() + " " + a.getPredicate().toString() + " " +
		// a.getObject().toString()));

		tripleList.write(new FileWriter("prova.ttl"), "turtle");
		/**
		 * Model model = ModelFactory.createDefaultModel(); model.set for (Quad q :
		 * triples) { Resource res = model.createResource(q.getSubject().getValue());
		 * res.addProperty(model.createProperty(q.getPredicate().getValue()),
		 * q.getObject().getValue()); }
		 **/

		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileInputStream("prova.ttl"), null, "TTL");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Namespace ns : rmlStore.getNamespaces()) {
			model.setNsPrefix(ns.getPrefix(), ns.getName());
			System.out.println(ns.getPrefix() + " " + ns.getName());
		}

		StmtIterator si = model.listStatements();
		while (si.hasNext()) {
			System.out.println(si.next());
		}

		// Create a new query
		String queryString = "PREFIX gso:  <http://gas_station.example.com/data#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?station " + "WHERE {" + "      ?station rdf:type gso:gas_station}";

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		System.out.println("result");
		// Output query results
		ResultSetFormatter.out(System.out, results, query);

		// Important ‑ free up resources used running the query
		qe.close();

		System.out.println("yeye");

		GeoSPARQLConfig.setupMemoryIndex();
		SpatialIndex.wrapModel(model);
		queryString = "PREFIX spatial: <http://jena.apache.org/spatial#> PREFIX gso:  <http://gas_station.example.com/data#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + "SELECT ?placeName" + " WHERE{"
				+ "    ?place spatial:nearby (13.60 3.71 1 'km') . ?place rdf:type gso:gas_station}";

		System.out.println("yoyo");

		try (QueryExecution qe1 = QueryExecutionFactory.create(queryString, model)) {
			ResultSet rs = qe1.execSelect();
			ResultSetFormatter.out(System.out, rs, query);
		}
	}

	private Pair<QuadStore, RDF4JStore> runRMLParser() {
		String mOptionValue = "GSOmapping.rml";

		RDF4JStore rmlStore = Utils.readTurtle(getClass().getClassLoader().getResourceAsStream(mOptionValue),
				RDFFormat.TURTLE);
		RecordsFactory factory = new RecordsFactory(System.getProperty("user.dir"));

		QuadStore outputStore = new RDF4JStore();

		Map<String, Class> libraryMap = new HashMap<>();
		libraryMap.put("GrelFunctions", GrelProcessor.class);
		libraryMap.put("IDLabFunctions", IDLabFunctions.class);
		FunctionLoader functionLoader = new FunctionLoader(null, null, libraryMap);

		// We have to get the InputStreams of the RML documents again,
		// because wecan only use an InputStream once.
		Executor executor;
		try {
			executor = new Executor(rmlStore, factory, functionLoader, outputStore,
					Utils.getBaseDirectiveTurtle(getClass().getClassLoader().getResourceAsStream(mOptionValue)));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("vai qui");
			return null;
		}

		List<Term> triplesMaps = new ArrayList<>();
		QuadStore result;
		try {
			result = executor.execute(triplesMaps, false, null);
			result.setNamespaces(rmlStore.getNamespaces());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return Pair.of(result, rmlStore);
	}
}
