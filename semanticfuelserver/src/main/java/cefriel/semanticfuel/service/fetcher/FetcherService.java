package cefriel.semanticfuel.service.fetcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
@EnableScheduling
public class FetcherService extends AbstractService {
	private final static String MISE_URL = "https://www.mise.gov.it/images/exportCSV";
	private final static String SOURCE_PRICE = "prezzo_alle_8.csv";
	private final static String SOURCE_LIST = "anagrafica_impianti_attivi.csv";

	/**
	 * Fetch data from the two MISE's sources every day at 8:30 AM.
	 */
	@Scheduled(cron = "0 30 8 * * *")
	public void fetch() {
		// async calls to download the two source from MISE
		Future<Boolean> listFetched = fetchFile(MISE_URL, SOURCE_LIST, "");
		Future<Boolean> priceFetched = fetchFile(MISE_URL, SOURCE_PRICE, "");

		try {
			// blocking calls
			if (listFetched.get() && priceFetched.get()) {
				// the two sources are saved at the given save path
				// notify the query engine to update the ontology
				QuadStore ontology = createOntology();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Async method to download a given resource to the given local path.
	 * 
	 * @param resourcePath the url to the resource
	 * @param resource     the name of the resource to download
	 * @param savePath     the path to the directory where to save the file
	 * @return true, if the file is correctly downloaded
	 * @throws IOException
	 */
	@Async
	private Future<Boolean> fetchFile(String resourcePath, String resource, String savePath) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		ResponseEntity<byte[]> response = restTemplate.exchange(resourcePath + resource, HttpMethod.GET, entity,
				byte[].class, "1");

		if (response.getStatusCode() == HttpStatus.OK) {
			try {
				Files.write(Paths.get(savePath + "/" + resource), response.getBody());
			} catch (IOException e) {
				e.printStackTrace();
				return new AsyncResult<>(false);
			}
			return new AsyncResult<>(true);
		}
		return new AsyncResult<>(false);
	}

	private QuadStore createOntology() {
		String mOptionValue = "";

		RDF4JStore rmlStore = Utils.readTurtle(ClassLoader.class.getResourceAsStream(mOptionValue), RDFFormat.TURTLE);
		RecordsFactory factory = new RecordsFactory(System.getProperty("user.dir"));

		String outputFormat = "turtle";
		QuadStore outputStore = new RDF4JStore();

		Map<String, Class> libraryMap = new HashMap<>();
		libraryMap.put("GrelFunctions", GrelProcessor.class);
		libraryMap.put("IDLabFunctions", IDLabFunctions.class);
		FunctionLoader functionLoader = new FunctionLoader(null, null, libraryMap);

		// We have to get the InputStreams of the RML documents again,
		// because we can only use an InputStream once.
		Executor executor;
		try {
			executor = new Executor(rmlStore, factory, functionLoader, outputStore,
					Utils.getBaseDirectiveTurtle(ClassLoader.class.getResourceAsStream(mOptionValue)));
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
}
