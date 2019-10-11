package cefriel.semanticfuel.service.fetcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

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

import com.google.common.io.Files;

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
import cefriel.semanticfuel.utils.csv.CSVCreator;
import cefriel.semanticfuel.utils.csv.CSVItem;

@Service
@EnableScheduling
public class FetcherService extends AbstractService {
	private final static String MISE_URL = "https://www.mise.gov.it/images/exportCSV";
	private final static String SOURCE_PRICE = "prezzo_alle_8.csv";
	private final static String SOURCE_LIST = "anagrafica_impianti_attivi.csv";

	private final static String PATH_TO_SOURCES = "src" + File.separator + "main" + File.separator + "sources";
	private final static String TARGET_PRICE = "preprocessed_prices.csv";
	private final static String TARGET_LIST = "preprocessed_list.csv";

	/**
	 * Fetch data from the two MISE's sources every day at 8:30 AM.
	 */
	@Scheduled(cron = "0 30 8 * * *")
	public void fetch() {
		// async calls to download the two sources from MISE
		Future<byte[]> stationList = fetchFile(MISE_URL, SOURCE_LIST);
		Future<byte[]> stationPrices = fetchFile(MISE_URL, SOURCE_PRICE);

		byte[] stationListResp, stationPricesResp;
		try {
			// blocking calls
			if ((stationListResp = stationList.get()) == null || (stationPricesResp = stationPrices.get()) == null)
				// if at least one of the two sources was not downloaded, abort
				return;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return;
		}

		// async calls to preprocess and save the two sources from MISE
		Future<Boolean> listSaved = saveFile(PATH_TO_SOURCES + File.separator + TARGET_LIST, stationListResp, null);
		Future<Boolean> pricesSaved = saveFile(PATH_TO_SOURCES + File.separator + TARGET_PRICE, stationPricesResp,
				null);

		try {
			// blocking calls
			if (!listSaved.get() || !pricesSaved.get())
				// if an error occurred saving the two sources, abort
				return;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return;
		}

	}

	/**
	 * Async method to download a given resource.
	 * 
	 * @param resourcePath the url to the resource
	 * @param resource     the name of the resource to download
	 * @return the buffer of byte rapresenting the content of the resource
	 */
	@Async
	private Future<byte[]> fetchFile(String resourcePath, String resource) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		ResponseEntity<byte[]> response = restTemplate.exchange(resourcePath + resource, HttpMethod.GET, entity,
				byte[].class, "1");

		if (response.getStatusCode() == HttpStatus.OK)
			return new AsyncResult<>(response.getBody());
		return new AsyncResult<>(null);
	}

	@Async
	private Future<Boolean> saveFile(String destinationFile, byte[] content,
			Function<byte[], List<CSVItem>> preProcessingFunction) {
		if (preProcessingFunction == null) {
			try {
				Files.write(content, Paths.get(destinationFile).toFile());
				return new AsyncResult<>(true);
			} catch (IOException e) {
				e.printStackTrace();
				return new AsyncResult<>(false);
			}
		}

		List<CSVItem> ppItems = preProcessingFunction.apply(content);

		try {
			CSVCreator writer = new CSVCreator().setDelimiter(',').setLargeFileMode(true).setRows(ppItems)
					.setHeader(ppItems.get(0).getTemplate());
			writer.buildCSV(destinationFile);
			return new AsyncResult<>(true);
		} catch (IOException e) {
			e.printStackTrace();
			return new AsyncResult<>(false);
		}
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
