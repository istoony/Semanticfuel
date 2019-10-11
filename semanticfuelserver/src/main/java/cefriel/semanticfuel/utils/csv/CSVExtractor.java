package cefriel.semanticfuel.utils.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

/**
 * @author MTODERO00
 *
 */
public class CSVExtractor {
	private CSVParser parser;
	private BufferedReader reader;
	private Map<String, BiFunction<String, String, Map<String, String>>> parsers;
	private List<CSVItem> items;
	private char delimiter;

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
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * 
	 * Parse the specified target into a list of items retrievable through
	 * getItems(). Only columns specified by one parser (added through
	 * addParamParser() method) are considered
	 * 
	 * @param file path to the target
	 * @return true if the parsing run without errors
	 */
	public boolean parse(String file) {
		return parse(file, false);
	}

	/**
	 * Parse the specified target into a list of items retrievable through
	 * getItems()
	 * 
	 * @param file     path to the target
	 * @param parseAll if false, extract just the columns specified through parsers,
	 *                 otherwise extract all the columns
	 * @return true if the parsing run without errors
	 */
	public boolean parse(String file, boolean parseAll) {
		if (!onStart(file))
			return false;

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

				if (parseAll || parsers.containsKey(columnName.toLowerCase())) {
					BiFunction<String, String, Map<String, String>> f = parsers.get(columnName.toLowerCase());
					if (f == null)
						item.addParam(columnName.toLowerCase(), parseParam(value));
					else {
						Map<String, String> params = f.apply(columnName.toLowerCase(), parseParam(value));
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
		parsers.put(param.toLowerCase(), parser);
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

	private boolean onStart(String file) {
		try {
			reader = new BufferedReader(new FileReader(file));
			parser = CSVFormat.DEFAULT.withDelimiter(delimiter == '\u0000' ? DEFAULT_DELIMITER : delimiter)
					.withFirstRecordAsHeader().parse(reader);
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}

		return true;
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

	public static void main(String[] args) {
		CSVExtractor csv = new CSVExtractor();

		BiFunction<String, String, Map<String, String>> parser = ((attribute, input) -> {
			Map<String, String> params = new HashMap<>();

			final String medium = "Medium";
			final String high = "High";
			final String low = "Low";

			switch (input) {
			case medium:
				params.put(attribute, "3");
				break;
			case high:
				params.put(attribute, "4");
				break;
			case low:
				params.put(attribute, "2");
				break;
			default:
				params.put(attribute, "0");
			}

			return params;
		});
		csv.addParamParser("Availability", parser);
		csv.addParamParser("Privacy", parser);
		csv.addParamParser("integrity", parser);
		csv.addParamParser("Name");
		csv.addParamParser("Id");

		System.out.println(csv.parse("Input_Example.csv", true));

		System.out.println(csv.getItems().size());

		for (CSVItem i : csv.getItems())
			System.out.println(i.toString());
	}
}
