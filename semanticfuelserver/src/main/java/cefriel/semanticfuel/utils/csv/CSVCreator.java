package cefriel.semanticfuel.utils.csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVCreator {
	private List<String> header;
	private List<CSVItem> rows;
	private char delimiter;
	private static final char DEFAULT_DELIMITER = ',';

	private boolean largeFileMode;
	private int nCheckpointRows;
	private static final int DEFAULT_CHECKPOINT_ROWS = 100;

	public CSVCreator() {
		header = new ArrayList<>();
		rows = new ArrayList<>();
	}

	public CSVCreator setHeader(List<String> header) {
		this.header = header;
		return this;
	}

	public CSVCreator setRows(List<CSVItem> items) {
		this.rows = items;
		return this;
	}

	public CSVCreator setDelimiter(char delimiter) {
		this.delimiter = delimiter;
		return this;
	}

	public CSVCreator setLargeFileMode(boolean largeFileMode) {
		return setLargeFileMode(largeFileMode, DEFAULT_CHECKPOINT_ROWS);
	}

	public CSVCreator setLargeFileMode(boolean largeFileMode, int nCheckpointRows) {
		this.largeFileMode = largeFileMode;
		this.nCheckpointRows = nCheckpointRows;
		return this;
	}

	public void buildCSV(String output) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(output));
				CSVPrinter csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withDelimiter(delimiter == '\u0000' ? DEFAULT_DELIMITER : delimiter)
								.withHeader(header.toArray(new String[0])));) {
			int rowCounterForFlush = 0;
			for (CSVItem it : rows) {
				List<String> record = new ArrayList<>();
				for (String h : header)
					record.add(it.getTemplate().contains(h) ? it.getParam(h) : "");

				csvPrinter.printRecord(record);

				if (largeFileMode) {
					rowCounterForFlush++;
					if (rowCounterForFlush > nCheckpointRows) {
						csvPrinter.flush();
						rowCounterForFlush = 0;
					}
				}
			}

			csvPrinter.flush();
		}
	}
}
