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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

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

public class QueryEngineService extends AbstractService {
	public static void main(String[] args) throws IOException, SpatialIndexException {
		new QueryEngineService().updateOntology();
	}

	public void updateOntology() throws IOException, SpatialIndexException {
		boolean skip = true;

		long start = 0;

		if (!skip) {
			System.out.println("parso");
			start = System.currentTimeMillis();

			Pair<QuadStore, RDF4JStore> result = runRMLParser();
			QuadStore tripleList = result.getLeft();
			RDF4JStore rmlStore = result.getRight();

			List<Quad> triples = tripleList.getQuads(null, null, null);

			System.out.println("finito " + ((System.currentTimeMillis() - start) / 1000));

			// triples.forEach(a -> System.out.println(
			// a.getSubject().toString() + " " + a.getPredicate().toString() + " " +
			// a.getObject().toString()));

			System.out.println("scrivo");
			start = System.currentTimeMillis();

			tripleList.write(new FileWriter("prova.ttl"), "turtle");

			System.out.println("finito " + ((System.currentTimeMillis() - start) / 1000));
		}

		System.out.println("leggo");
		start = System.currentTimeMillis();

		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileInputStream("prova.ttl"), null, "TTL");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("finito " + ((System.currentTimeMillis() - start) / 1000));

		// Create a new query
		String queryString = "PREFIX gso:  <http://gas_station.example.com/data#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?station " + "WHERE {" + "      ?station rdf:type gso:gas_station}";

		String coneQuery = "PREFIX gso:  <http://gas_station.example.com/data#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?station " + "WHERE {"
				+ "      ?station gso:has_address ?address . ?address gso:city 'CONEGLIANO'}";

		Query query = QueryFactory.create(coneQuery);

		// Execute the query and obtain results
		try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qe.execSelect();

			System.out.println("result");
			// Output query results
			ResultSetFormatter.out(System.out, results, query);
		}

		boolean stop = false;
		if (stop)
			return;

		System.out.println("yeye");

		GeoSPARQLConfig.setupMemoryIndex();
		Dataset spatialDataset = SpatialIndex.wrapModel(model);

		queryString = "PREFIX gso:  <http://gas_station.example.com/data#> "
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " PREFIX geo: <http://www.opengis.net/ont/geosparql#> "
				+ " PREFIX geof: <http://www.opengis.net/def/function/geosparql/> " + " SELECT ?station ?name"
				+ " WHERE{ ?station gso:has_pump ?pump . ?pump gso:fuel 'Benzina' . ?station gso:name ?name }";

		queryString = "PREFIX gso:  <http://gas_station.example.com/data#> "
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " PREFIX geo: <http://www.opengis.net/ont/geosparql#> "
				+ " PREFIX geof: <http://www.opengis.net/def/function/geosparql/> " + " SELECT ?station ?name ?wkt"
				+ " WHERE{?station geo:hasGeometry ?geom . " + " ?geom geo:asWKT ?wkt . "
				+ " ?station gso:has_pump ?pump . ?pump gso:fuel 'Benzina' . ?station gso:name ?name }";

		queryString = "PREFIX gso:  <http://gas_station.example.com/data#> "
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " PREFIX geo: <http://www.opengis.net/ont/geosparql#> "
				+ " PREFIX geof: <http://www.opengis.net/def/function/geosparql/> " + " SELECT ?station ?name"
				+ " WHERE{?station geo:hasGeometry ?geom . " + " ?geom geo:asWKT ?wkt . "
				+ " ?station gso:has_pump ?pump . ?pump gso:fuel 'Benzina' . ?station gso:name ?name . "
				+ " ?station gso:has_address ?address . ?address gso:city 'FIRENZE' . FILTER(geof:sfWithin(?wkt, '''<http://www.opengis.net/def/crs/OGC/1.3/CRS84>"
				+ "            Polygon ((47 5, 47 16, 35 23, 36 7, 47 5))" + "        '''^^geo:wktLiteral))" + "}";

		queryString = "PREFIX gso:  <http://gas_station.example.com/data#> "
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " PREFIX geo: <http://www.opengis.net/ont/geosparql#> "
				+ " PREFIX geof: <http://www.opengis.net/def/function/geosparql/> " + " SELECT ?station ?name ?city"
				+ " WHERE{?station geo:hasGeometry ?geom . " + " ?geom geo:asWKT ?wkt . "
				+ " ?station gso:has_pump ?pump . ?pump gso:fuel 'Benzina' . ?station gso:name ?name . "
				+ " ?station gso:has_address ?address . ?address gso:city ?city . FILTER(geof:sfWithin(?wkt, '''<http://www.opengis.net/def/crs/OGC/1.3/CRS84>"
				+ "            Polygon ((45.381781 8.941661, 45.590982 8.990870, 45.549157 9.355141, 45.367502 9.382289, 45.381781 8.941661))"
				+ "        '''^^geo:wktLiteral))" + "}";

		System.out.println("yoyo");

		try (QueryExecution qe1 = QueryExecutionFactory.create(queryString, spatialDataset)) {
			ResultSet rs = qe1.execSelect();
			ResultSetFormatter.out(System.out, rs, qe1.getQuery());
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
