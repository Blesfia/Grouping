/*
    This code was taken from https://www.mkyong.com/java/how-to-export-data-to-csv-file-java/
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ';';
    private static final char DEFAULT_DECIMAL_SEPARATOR = ',';

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }
    
    public static List<String[]> readCSV(Reader r) throws IOException {
		return readCSV(r, 1);
    }
    
    public static HashMap<String, double[]> readPersons(List<String[]> lines, int c, int n) {
		HashMap<String, double[]> persons = new HashMap<String, double[]>();
	    for (String[] line : lines) {
	    		double[] characteristics = new double[c];
	    		for (int index = 0; index < c; index ++) {
	    			characteristics[index] = Double.parseDouble(line[index+1]);
	    		}
	    		if (persons.size() >= n) {
    				break;
    			}
    			persons.put(line[0], characteristics);
	    }
	    return persons;
	}

    public static List<String[]> readCSV(Reader r, int skip) throws IOException {
    		List<String[]> lines = new ArrayList<String[]>();

        try (BufferedReader br = new BufferedReader(r)) {
        		String line = "";
            while ((line = br.readLine()) != null) {
                // use comma as separator
	            	if (skip > 0) {
	            		skip --;
	            		continue;
	            	}
            		lines.add(line.replace(DEFAULT_DECIMAL_SEPARATOR, '.').split(DEFAULT_SEPARATOR+""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        if (result.contains(".")) {
            result = result.replace(".", DEFAULT_DECIMAL_SEPARATOR+"");
        }
        return result;

    }

    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }

}