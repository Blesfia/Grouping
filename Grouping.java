import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Grouping {
	private static int c = -1; // Number of characteristics by person
	private static int k = 2; // Number of persons by team
	private static int n = 20; // Number of persons available
	private static int groups; // Number of groups
	private static HashMap<Integer, double[]> normalizedPersons = new HashMap<Integer, double[]>(); // Normalized persons 
	private static double[] cAverage; // Average of each characteristic
	public static double[][] resultGroup;
	private static HashMap<Integer, String> associationMap = new HashMap<Integer, String>();

	public static void printResult(Writer writer) throws IOException {
		for (double[] group : resultGroup) {
			List<String> team = new ArrayList<String>();
			for (int index = 0; index < group.length; index ++) {
				double personId = group[index];
				if (index == group.length - 1) {
					break;
				}
				team.add(associationMap.get((int)personId)+ "");
			}
			CSVUtils.writeLine(writer, team);
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("You should send (C, input.csv, output, N, K)");
		c = Integer.parseInt(args[0]);
        String csvFile = args[1];
		String outputFile = args[2];
		n = Integer.parseInt(args[3]);
		k = Integer.parseInt(args[4]);

		FileReader reader = new FileReader(csvFile);
		FileWriter writer = new FileWriter(outputFile);
		List<String[]> lines = CSVUtils.readCSV(reader);
		HashMap<String, double[]> persons = CSVUtils.readPersons(lines, c, n);
		
		System.out.println("G:" + n / k + " K:" + k + " N:" + n + " C:"+ c);
		System.out.format("| %-11s | %-11s | %-11s | %-11s |%n", "Seconds", "Improvement", "Final value", "Iterations");
		
		double[] result = grouping(n, c, k,  shufflePersons(persons));

		System.out.format("| %1.5e | %1.5e | %1.5e | %1.5e |%n", result[0], result[1], result[2], result[3]);
		printResult(writer);
		writer.close();
		System.out.println("Group saved in " + outputFile);
	}

	public static HashMap<Integer, double[]> shufflePersons(HashMap<String, double[]> persons) {
		HashMap<Integer, double[]> newPersons = new HashMap<Integer, double[]>();
		List<String> ids = new ArrayList<String>();
		ids.addAll(persons.keySet());
		Collections.shuffle(ids);
		for(String id: ids) {
			associationMap.put(associationMap.size(), id);
			newPersons.put(newPersons.size(), persons.get(id));
		}
		return newPersons;
	}

	private static HashMap<Integer, double[]> normalizeCharacteristics(HashMap<Integer, double[]> persons){
		HashMap<Integer, double[]> newPersons = new HashMap<Integer, double[]>();
		double[] minValue = new double[c];
		double[] maxValue = new double[c];
		// Get min and max value to each characteristic
		for(double[] person : persons.values()){
			for(int j = 0; j< c; j++){
				if(person[j] < minValue[j]) {
					minValue[j] = person[j];
				}
                if(person[j] > maxValue[j]) {
                		maxValue[j] = person[j];
                }
			}
		}
		//
		for(int i = 0; i< n; i++){
			double[] person = new double[c];
			for(int j = 0; j < c; j++){
				person[j] = (persons.get(i)[j] - minValue[j]) / (maxValue[j]-minValue[j]);
			}
			newPersons.put(i, person);
		}		
		return newPersons;
	}

	private static double[] calculateCharacteristicsAverage(){
        double [] characteristics= new double[c];
        for(double[] person : normalizedPersons.values()){
        	for(int i=0;i<characteristics.length;i++){
        		characteristics[i] += person[i];
            }
        }
        for(int i=0;i<characteristics.length;i++){
        	characteristics[i] /= normalizedPersons.size();
        }
        return characteristics;
	}

	private static double getGroupGrade(double[] group){
	    double[] result = new double[c];
	    for(int i = 0; i< group.length-1;i++) {
	    		for(int j = 0;j < c;j++) {
	    			result[j] += normalizedPersons.get((int)group[i])[j];
	    		}
	    }
	    double deviation = 0;
	    	for(int i=0;i<c;i++) {
	    		deviation += Math.pow((result[i] / (group.length-1))-cAverage[i], 2);
	    	}
	    	return Math.sqrt(deviation) / c;
	}

	private static double[][] getInitialTeams(){
		double[][] result = new double[groups][k + 1];
		for(int indexGroup = 0; indexGroup < groups; indexGroup++){
        		result[indexGroup] = new double[k + 1];
            for(int indexPerson = 0; indexPerson < k; indexPerson++)
            		result[indexGroup][indexPerson] = k * indexGroup + indexPerson;
            		result[indexGroup][k] = getGroupGrade(result[indexGroup]);
        }
        return result;
	}

	private static double gradeOfTeams(double[][] teams){
	    double grade = 0;
	    for(double[] team : teams) {
	    		grade += team[team.length-1];
	    }
	    return grade;
	}

	private static int selectMethod(double[][] groups, double actualGrade){
        float aleatorio = new Random().nextFloat()*(float)actualGrade;
        double acumulado = 0;
        for(int i = 0;i<groups.length;i++){
        	if(groups[i] != null)
    		{
        		acumulado += (double)groups[i][groups[i].length-1]; 
        		if(aleatorio <= acumulado)
        		    return i;
    		}
        }
        for(int i = 0;i<groups.length;i++)
        	if(groups[i] != null)
        		return i;
        System.out.println("ERROR: selectMethod fail.");
        return -1;
	}
	
	private static <T> List<List<T>> combinate(Collection<T> values, int size) {

	    if (0 == size) {
	        return Collections.singletonList(Collections.<T> emptyList());
	    }

	    if (values.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<List<T>> combination = new LinkedList<List<T>>();

	    T actual = values.iterator().next();

	    List<T> subSet = new LinkedList<T>(values);
	    subSet.remove(actual);

	    List<List<T>> subSetCombination = combinate(subSet, size - 1);

	    for (List<T> set : subSetCombination) {
	        List<T> newSet = new LinkedList<T>(set);
	        newSet.add(0, actual);
	        combination.add(newSet);
	    }

	    combination.addAll(combinate(subSet, size));

	    return combination;
	}

	/**
	 * This functions should be used just when a combination was made
	 * @param grupo
	 * @return
	 */
	private static double getGroupGrade(Double[] group){
	    double[] result = new double[c];
	    for(int i = 0; i< group.length;i++) {
	    		for(int j = 0;j < c;j++) {
	    			result[j] += normalizedPersons.get(group[i].intValue())[j];
	    		}
	    }
	    double deviation = 0;
	    	for(int i=0;i<c;i++) {
	    		deviation += Math.pow((result[i] / (group.length)) - cAverage[i], 2);
	    	}
	    	return Math.sqrt(deviation) / c;
	}
	
	private static double[][] betterCombination(double[] group1, double[] group2){
	    Collection<Double> persons = new ArrayList<Double>();

	    for(int index = 0;index<group1.length-1;index++) {
	    		persons.add(group1[index]);
	    		persons.add(group2[index]);
	    }
	    
	    List<List<Double>> mixedGroups = combinate(persons, group1.length-1);
	    Iterator<List<Double>> iterador = mixedGroups.iterator();
	    List<List<Double>> complementGroups = new  ArrayList<List<Double>>();
	    
	    double minGrade = Double.MAX_VALUE, groupGrade = 0, complementGrade = 0;
	    List<Double> minGroup = new ArrayList<Double>(), minComplementGroup = new ArrayList<Double>();

	    while(iterador.hasNext()) {
		    	List<Double> group = iterador.next();
		    	
		    	List<Double> complementGroup = new ArrayList<Double>(persons);
		    	complementGroup.removeAll(group);
		    	complementGroups.add(complementGroup);
		    	
		    	double _groupGrade = getGroupGrade(group.toArray(new Double[0])); 
		    	double _complementGrade = getGroupGrade(complementGroup.toArray(new Double[0]));
		    	double grade = _groupGrade + _complementGrade;
		    	if(grade < minGrade){
		    		minGrade = grade;
		    		minGroup = group;
		    		minComplementGroup = complementGroup;
		    		groupGrade = _groupGrade;
		    		complementGrade = _complementGrade;
		    	}
	    }
	    minGroup.add(groupGrade);
	    minComplementGroup.add(complementGrade);
	    double[] res1 = new double[minGroup.size()], res2 = new double[minGroup.size()];
	    for(int i = 0; i< res1.length;i++){
		    	res1[i] = minGroup.get(i);
		    	res2[i] = minComplementGroup.get(i);
	    }
	    return new double[][]{res1, res2};
	}

	private static double[][] runIteration(double actualGrade, double[][] groups){
		double[][] newAgrupation = new double[groups.length][groups[0].length];
		for(int i = 0; i < groups.length ;i+=2){
			int a, b;
			double[] grupo1, grupo2;

			a = selectMethod(groups, actualGrade);
			actualGrade -= (double)groups[a][groups[a].length-1];
			grupo1 = groups[a].clone();
			groups[a] = null;
			
			b = selectMethod(groups, actualGrade);
			actualGrade -= (double)groups[b][groups[b].length-1];
			grupo2 = groups[b].clone();
			groups[b] = null;

			double[][] newGroups = betterCombination(grupo1, grupo2);
			newAgrupation[i] = newGroups[0];
    			newAgrupation[i+1] = newGroups[1];
		}
		return newAgrupation;
	}
	
	/**
	 * 
	 * @param n
	 * @param c
	 * @param k
	 * @param persons
	 * @return Double array with values [ duration (ms), improvement (%), finalValue, iterations ]
	 */
	public static double[] grouping(int n, int c, int k, HashMap<Integer, double[]> persons) {
			Grouping.n = n;
			Grouping.c = c;
			Grouping.k = k;
			Grouping.groups = n / k;
			int iterations = (int)(2*groups); // Number of iterations
		    // Start time
			long startTime = System.currentTimeMillis();
			normalizedPersons = normalizeCharacteristics(persons);
			cAverage = calculateCharacteristicsAverage();
		    
		    double[][] groups = getInitialTeams();
		    double actualGrade = gradeOfTeams(groups);
		    double initialGrade = actualGrade;
		    int iteration = 0;
		    for(iteration = 0; iteration < iterations; iteration++){
		    		groups = runIteration(actualGrade, groups);
		    		actualGrade = gradeOfTeams(groups);
			    	if(actualGrade == 0){
			    		break;
			    	}
		    }
		    long endTime = System.currentTimeMillis();
		    actualGrade /= groups.length;
		    initialGrade /= groups.length;
            long duration = (endTime - startTime);
            resultGroup = groups;
            return new double[]{
            		(double)duration / 1000,
            		1 - actualGrade / initialGrade,
            		actualGrade, 
            		iteration};
		}
}
