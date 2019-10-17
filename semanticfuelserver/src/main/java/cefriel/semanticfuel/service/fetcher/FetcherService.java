package cefriel.semanticfuel.service.fetcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

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

import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.utils.csv.CSVCreator;
import cefriel.semanticfuel.utils.csv.CSVExtractor;
import cefriel.semanticfuel.utils.csv.CSVItem;

@Service
@EnableScheduling
public class FetcherService extends AbstractService {
	private final static String SOURCE_PRICE = "https://www.mise.gov.it/images/exportCSV/prezzo_alle_8.csv";
	private final static String SOURCE_LIST = "https://www.mise.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv";

	private final static String PATH_TO_SOURCES = "src" + File.separator + "main" + File.separator + "sources";
	private final static String TARGET_PRICE = "preprocessed_prices.csv";
	private final static String TARGET_LIST = "preprocessed_list.csv";

	/**
	 * Fetch data from the two MISE's sources every day at 8:30 AM.
	 */
	// @Scheduled(cron = "0 30 8 * * *")
	@Scheduled(fixedRate = 60000)
	public void fetch() {
		System.out.println("saving");
		// async calls to download the two sources from MISE
		Future<byte[]> stationList = fetchFile(SOURCE_LIST);
		Future<byte[]> stationPrices = fetchFile(SOURCE_PRICE);

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

		new File(PATH_TO_SOURCES).mkdirs();

		// async calls to preprocess and save the two sources from MISE
		Future<Boolean> listSaved = saveFile(PATH_TO_SOURCES + File.separator + TARGET_LIST, stationListResp,
				this::preprocessListSource);
		Future<Boolean> pricesSaved = saveFile(PATH_TO_SOURCES + File.separator + TARGET_PRICE, stationPricesResp,
				this::preprocessListPrices);

		try {
			// blocking calls
			if (!listSaved.get() || !pricesSaved.get())
				// if an error occurred saving the two sources, abort
				return;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("done");
	}

	/**
	 * Async method to download a given resource.
	 * 
	 * @param resourcePath the url of the resource
	 * @return the buffer of byte representing the content of the resource
	 */
	@Async
	private Future<byte[]> fetchFile(String resourcePath) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		ResponseEntity<byte[]> response = restTemplate.exchange(resourcePath, HttpMethod.GET, entity, byte[].class,
				"1");

		if (response.getStatusCode() == HttpStatus.OK)
			return new AsyncResult<>(response.getBody());
		return new AsyncResult<>(null);
	}

	@Async
	private Future<Boolean> saveFile(String destinationFile, byte[] content,
			Function<byte[], List<CSVItem>> preProcessingFunction) {
		if (preProcessingFunction == null) {
			try (FileOutputStream fos = new FileOutputStream(Paths.get(destinationFile).toString())) {
				fos.write(content);
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

	private List<CSVItem> preprocessListSource(byte[] source) {
		CSVExtractor reader = new CSVExtractor();

		reader.setDelimiter(';').skipFirstNLines(1);
		reader.addParamParser(Ontology.SourceList.STATION_NAME, this::parseCommas);
		reader.addParamParser(Ontology.SourceList.STATION_OWNER, this::parseCommas);
		reader.addParamParser(Ontology.SourceList.STATION_TYPE, this::parseCommas);
		reader.addParamParser(Ontology.SourceList.StationAddress.STATION_ADDRESS, this::parseCommas);

		try {
			reader.parse(new InputStreamReader(new ByteArrayInputStream(source)), true);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return reader.getItems();
	}

	private List<CSVItem> preprocessListPrices(byte[] source) {
		CSVExtractor reader = new CSVExtractor();

		reader.setDelimiter(';').skipFirstNLines(1);
		reader.addParamParser(Ontology.SourcePrices.StationPump.PUMP_UPDATE, this::parseDateTime);

		try {
			reader.parse(new InputStreamReader(new ByteArrayInputStream(source)), true);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return reader.getItems();
	}

	private String parseCommas(String source) {
		return source.replaceAll(",", " - ");
	}

	private String parseDateTime(String source) {
		DateFormat currentDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		DateFormat targetDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		try {
			return targetDF.format(currentDF.parse(source));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
