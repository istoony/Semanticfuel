package cefriel.semanticfuel.service.fetcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

@Service
@EnableScheduling
public class FetcherService {
	private final static String MISE_URL = "https://www.mise.gov.it/images/exportCSV/";
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
				// call the RML mapper routines to update the ontology

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
				Files.write(Paths.get(savePath + resource), response.getBody());
			} catch (IOException e) {
				e.printStackTrace();
				return new AsyncResult<>(false);
			}
			return new AsyncResult<>(true);
		}
		return new AsyncResult<>(false);
	}
}
