package cefriel.semanticfuel.service.fetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.Term;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class ModelKeeperService extends AbstractService {
	private static final String MODEL_FILE_TTL = "ontology.ttl";
	private final static String PATH_TO_MODEL = "src" + File.separator + "main" + File.separator + "model";

	private Dataset spatialDataset;

	@Autowired
	private FetcherService fetcher;

	@PostConstruct
	public void init() {
		new File(PATH_TO_MODEL).mkdirs();

		// search for a previously saved model, or start a fetching session (this will
		// require about 20 minutes)
		if (new File(PATH_TO_MODEL + File.separator + MODEL_FILE_TTL).exists()) {
			readModel();

			// notify services waiting for the availability of the dataset
			synchronized (spatialDataset) {
				spatialDataset.notifyAll();
			}
		} else
			fetcher.fetch();
	}

	public void updateOntology() {
		LOG.info("Starting updating ontology...");
		long updateStart = System.currentTimeMillis();
		long operationStart = updateStart;

		// run the RML parser
		QuadStore result = runRMLParser();

		LOG.debug("RML parsing finished in " + ((System.currentTimeMillis() - operationStart) / 1000) + " seconds");

		new File(PATH_TO_MODEL).mkdirs();

		operationStart = System.currentTimeMillis();

		// write the model into a file
		String ontologyPath = PATH_TO_MODEL + File.separator + MODEL_FILE_TTL;
		try {
			result.write(new FileWriter(ontologyPath), RDFFormat.TURTLE.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		LOG.debug("Ontology written to " + ontologyPath + " in "
				+ ((System.currentTimeMillis() - operationStart) / 1000) + " seconds");

		operationStart = System.currentTimeMillis();

		// read the model from the file
		// the passage is needed in order not to care about the manual insertion of all
		// triples contained in the QuadStore got from the parsing operation (the
		// implementation of that function is left as future work)
		readModel();

		LOG.debug("Ontology loaded from " + ontologyPath + " in "
				+ ((System.currentTimeMillis() - operationStart) / 1000) + " seconds");

		LOG.info("Ontology updated in " + ((System.currentTimeMillis() - updateStart) / 1000) + " seconds");

		// notify services waiting for the availability of the dataset
		synchronized (spatialDataset) {
			spatialDataset.notifyAll();
		}
	}

	private void readModel() {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileInputStream(PATH_TO_MODEL + File.separator + MODEL_FILE_TTL), null, "TTL");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// configure the dataset for spatial operations
		GeoSPARQLConfig.setupMemoryIndex();
		try {
			spatialDataset = SpatialIndex.wrapModel(model);
		} catch (SpatialIndexException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private QuadStore runRMLParser() {
		String mOptionValue = "GSOmapping.rml";

		RDF4JStore rmlStore = Utils.readTurtle(getClass().getClassLoader().getResourceAsStream(mOptionValue),
				RDFFormat.TURTLE);
		RecordsFactory factory = new RecordsFactory(System.getProperty("user.dir"));

		QuadStore outputStore = new RDF4JStore();

		Map<String, Class> libraryMap = new HashMap<>();
		libraryMap.put("GrelFunctions", GrelProcessor.class);
		libraryMap.put("IDLabFunctions", IDLabFunctions.class);
		FunctionLoader functionLoader = new FunctionLoader(null, null, libraryMap);

		Executor executor;
		try {
			executor = new Executor(rmlStore, factory, functionLoader, outputStore,
					Utils.getBaseDirectiveTurtle(getClass().getClassLoader().getResourceAsStream(mOptionValue)));
		} catch (Exception e) {
			e.printStackTrace();
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

		return result;
	}

	public Dataset getCurrentModel() {
		while (spatialDataset == null)
			synchronized (spatialDataset) {
				try {
					spatialDataset.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		return spatialDataset;
	}
}
