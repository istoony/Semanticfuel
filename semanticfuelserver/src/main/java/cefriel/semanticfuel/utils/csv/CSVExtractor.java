package cefriel.semanticfuel.utils.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVExtractor {
	private CSVParser parser;
	private BufferedReader reader;
	private Map<String, BiFunction<String, String, Map<String, String>>> parsers;
	private List<CSVItem> items;
	private char delimiter;
	private int lineToSkip;

	private static final char DEFAULT_DELIMITER = ';';

	public CSVExtractor() {
		parsers = new HashMap<>();
		items = new ArrayList<>();
	}

	/**
	 * Reset parsers
	 */
	public void clearParsers() {
		parsers.clear();
	}

	/**
	 * @param delimiter the CSV cell delimiter
	 */
	public CSVExtractor setDelimiter(char delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	public CSVExtractor skipFirstNLines(int nlines) {
		this.lineToSkip = nlines;
		return this;
	}

	/**
	 * 
	 * Parse the specified target into a list of items retrievable through
	 * getItems(). Only columns specified by one parser (added through
	 * addParamParser() method) are considered
	 * 
	 * @param file path to the target
	 * @return true if the parsing run without errors
	 * @throws IOException
	 */
	public boolean parse(String file) throws IOException {
		return parse(file, false);
	}

	public boolean parse(Reader file) throws IOException {
		return parse(file, false);
	}

	public boolean parse(String file, boolean parseAll) throws IOException {
		return parse(new FileReader(file), parseAll);
	}

	/**
	 * Parse the specified target into a list of items retrievable through
	 * getItems()
	 * 
	 * @param file     path to the target
	 * @param parseAll if false, extract just the columns specified through parsers,
	 *                 otherwise extract all the columns
	 * @return true if the parsing run without errors
	 * @throws IOException
	 */
	public boolean parse(Reader file, boolean parseAll) throws IOException {
		onStart(file);

		items.clear();

		Iterator<CSVRecord> iterator = parser.iterator();
		while (iterator.hasNext()) {
			CSVRecord record = iterator.next();

			CSVItem item = new CSVItem();

			int cellCounter = 0;
			for (String value : record) {
				String columnName = getHeader(cellCounter++);
				if (columnName == null)
					continue;

				if (parseAll || parsers.containsKey(columnName)) {
					BiFunction<String, String, Map<String, String>> f = parsers.get(columnName);
					if (f == null)
						item.addParam(columnName, parseParam(value));
					else {
						Map<String, String> params = f.apply(columnName, parseParam(value));
						for (Entry<String, String> e : params.entrySet())
							item.addParam(e.getKey(), e.getValue());
					}
				}
			}

			items.add(item);
		}

		return onEnd();
	}

	/**
	 * Register a new parser for the column named as "param". Only one parser per
	 * column can be registered
	 * 
	 * @param param  the name of the column
	 * @param parser a function which gets the content of the cell and the name of
	 *               the column, and returns a new Map string pairs each
	 *               representing a new attribute-value cell relation
	 */
	public void addParamParser(String param, BiFunction<String, String, Map<String, String>> parser) {
		parsers.put(param, parser);
	}

	/**
	 * Register a new parser for the column named as "param". Only one parser per
	 * column can be registered
	 * 
	 * @param param  the name of the column
	 * @param parser a function which gets the content of the cell and returns a
	 *               string function of this value
	 */
	public void addParamParser(String param, Function<String, String> parser) {
		addParamParser(param, (attribute, value) -> {
			Map<String, String> defaultEntry = new HashMap<>();
			defaultEntry.put(attribute, parser.apply(value));
			return defaultEntry;
		});
	}

	/**
	 * Register a new parser for the column named as "param"
	 * 
	 * @param param the name of the column
	 */
	public void addParamParser(String param) {
		addParamParser(param, value -> value);
	}

	/**
	 * @return the last list of items since the last call to parse
	 */
	public List<CSVItem> getItems() {
		return new ArrayList<>(items);
	}

	private void onStart(Reader file) throws IOException {
		reader = new BufferedReader(file);
		for (int i = 0; i < lineToSkip; i++)
			reader.readLine();

		parser = CSVFormat.DEFAULT.withDelimiter(delimiter == '\u0000' ? DEFAULT_DELIMITER : delimiter)
				.withFirstRecordAsHeader().withQuote(null).parse(reader);
	}

	private boolean onEnd() {
		try {
			reader.close();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private String parseParam(String cell) {
		return cell == null ? "" : cell;
	}

	private String getHeader(int cell) {
		return parser.getHeaderNames().get(cell);
	}
}
