package cefriel.semanticfuel.utils.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for a list of parameters, one for each column of the file. All
 * parameters are stored as strings
 *
 */
public class CSVItem {
	private Map<String, String> params;
	private List<String> attributes;

	public CSVItem(CSVItem item) {
		this();

		for (String att : item.getTemplate())
			addParam(att, item.getParam(att));
	}

	public CSVItem() {
		params = new HashMap<>();
		attributes = new ArrayList<>();
	}

	/**
	 * @param name  name of the parameter (the name of the column)
	 * @param value the associated value
	 */
	public void addParam(String name, String value) {
		params.put(name, value);
		attributes.add(name);
	}

	/**
	 * @param paramName name of the parameter (the name of the column)
	 * @return the value associated (cannot be null)
	 */
	public String getParam(String paramName) {
		return params.get(paramName);
	}

	/**
	 * @param paramName name of the parameter (the name of the column)
	 * @return the value removes, null otherwise
	 */
	public String removeParam(String paramName) {
		return params.remove(paramName);
	}

	/**
	 * @return the template associated to this item
	 */
	public List<String> getTemplate() {
		return attributes;
	}

	/**
	 * @return the number of columns of this item
	 */
	public int size() {
		return attributes.size();
	}

	@Override
	public String toString() {
		String res = "item:\n";
		for (String s : params.keySet())
			res += s + " : " + params.get(s) + "\n";
		return res;
	}
}
