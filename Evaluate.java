import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Evaluate {

	public static void main(String[] args) throws IOException {
		System.out.println("You should send (C, inputFile.csv, runs, outputFile, n, k)");
		int c = Integer.parseInt(args[0]);
		String csvFile = args[1];
		int runs = Integer.parseInt(args[2]);
		String outputFile = args[3];
		int n = Integer.parseInt(args[4]);
		int k = Integer.parseInt(args[5]);
		
		FileReader reader = new FileReader(csvFile);
		List<String[]> lines = CSVUtils.readCSV(reader);

		System.out.println("G:" + n / k + " K:" + k + " N:" + n + " C:"+ c + " Runs:" + runs);
		System.out.format("| %-27s | %-27s | %-27s | %-27s |%n", "Seconds", "Improvement", "Final value", "Iterations");
		System.out.format("| %-7s | %-7s | %-7s | %-7s | %-7s | %-7s | %-7s | %-7s | %-7s | %-7s | %-7s | %-7s |%n", "min", "average", "max", "min", "average", "max", "min", "average", "max", "min", "average", "max");
		
		double[][] result = evaluateGrouping(n, k, c, runs, CSVUtils.readPersons(lines, c, n), outputFile);
		
		System.out.format(
			"| %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e | %1.1e |%n", result[0][0], result[0][1], result[0][2], result[0][3],
			result[1][0], result[1][1], result[1][2], result[1][3],
			result[2][0], result[2][1], result[2][2], result[2][3]);
		
		System.out.println("Better grouping saved in " + outputFile);
	}

	public static double[][] evaluateGrouping(int n, int k, int c, int runs, HashMap<String, double[]> persons, String outputFile) throws IOException  {
		double[] averages = new double[]{0, 0, 0, 0};
		double[] min = new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		double[] max = new double[]{0, 0, 0, 0};
		double[][] betterGrouping;
		
		for(int run = 0; run < runs; run ++) {
			double[] result = Grouping.grouping(n, c, k, Grouping.shufflePersons(persons));
			// Se suman resultados
			for (int index = 0; index< averages.length; index ++) {
				averages[index] += result[index];
				min[index] = Math.min(min[index], result[index]);
				max[index] = Math.max(max[index], result[index]);
				if (min[2] == result[2]) {
					FileWriter writer = new FileWriter(outputFile);
					Grouping.printResult(writer);
					writer.close();
				}
			}
        }
		// Se obtienen promedios
		for (int index = 0; index< averages.length; index ++) {
			averages[index] /= runs;
		}
		return new double[][]{averages, min, max};
	}
}
