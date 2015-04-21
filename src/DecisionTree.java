/* An implementation of a decision tree */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class DecisionTree {
	private static List<List<String> > dataTable;					
	private static List<List<String> > tableInfo;					
	
	public static void main(String[] args){
		
		BufferedReader input;
		dataTable = new ArrayList<List<String> >();
		tableInfo = new ArrayList<List<String> >();
		try {
			/* Training phase */
			input = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			String singleInstance;
			
			while((singleInstance = input.readLine())!=null){
				parse(singleInstance);
			}
			input.close();
			//printAttributeTable(dataTable);
			generateTableInfo();
			Node root = new Node();
			buildDecisionTree(dataTable, root);
			String defaultClass = returnMostFrequentClass();
			System.out.println("Most common class is " + defaultClass);
			System.out.println("\n\n-----------------Printing Decision Tree----------------");
			printDecisionTree(root);
			System.out.println("\n\n");
			
		} catch (IOException e) {
			System.out.println("Could not read training data");
		}
	}
	
	private static String returnMostFrequentClass() {
		Map<String, Integer> trainingClasses = new HashMap<String, Integer>();           													 // Stores the classlabels and their frequency
		for (int i = 1; i < dataTable.size(); i++) {
			String key = dataTable.get(i).get(dataTable.get(0).size()-1);
			if (!trainingClasses.containsKey(key)) {
				trainingClasses.put(key, 1);
			} else {
				trainingClasses.put(key, trainingClasses.get(key)+1);
			}
		}   
		int highestClassCount = 0;
		String mostFrequentClass = null;
		//for (mapIter = trainingClasses.begin(); mapIter != trainingClasses.end(); mapIter++) {
		for(String k : trainingClasses.keySet()){
			Integer val = trainingClasses.get(k);
			if (val >= highestClassCount) {
				highestClassCount = val;
				mostFrequentClass = k;
			}   
		}
		return mostFrequentClass;
	}

	// parses an input line
	private static void parse(String someString){
		String[] tokens = someString.split(",");
		String[] data = Arrays.copyOfRange(tokens, 1, tokens.length);
		List<String> exampleVector = new ArrayList<String>(Arrays.asList(data));
		dataTable.add(exampleVector);
	}
	
	private static void printAttributeTable(List<List<String>> table){
		ListIterator<List<String> > it = table.listIterator();
		while(it.hasNext()){
			List<String> example = it.next();
			ListIterator<String> fit = example.listIterator();
			while(fit.hasNext()){
				String word = fit.next();
				System.out.print(word + ",");
			}
			System.out.print("\n");
		}
	}
	
	// table info contains the possible values for each column heading
	private static void generateTableInfo(){
		for (int i = 0; i < dataTable.get(0).size(); i++) {		/* for every column heading */		
			List<String> tempInfo = new ArrayList<String>();
			Map<String, Integer> tempMap = new HashMap<String, Integer>();
			for (int j = 0; j < dataTable.size(); j++) {
				String key = dataTable.get(j).get(i);
				if (!tempMap.containsKey(key)) {
					tempMap.put(key, 1);
					tempInfo.add(key);
				} else	{
					tempMap.put(key, tempMap.get(key) + 1);
				}
			}
			tableInfo.add(tempInfo);
		}
	}
	
	// recursive algorithm to build a decision tree
	private static void buildDecisionTree(List<List<String> > table, Node node){
		if (table.size() == 0) {
			return;
		}
		if (isHomogeneous(table)) {
			node.isLeaf = true;
			if(table.size() > 1)
				node.label = table.get(1).get(table.get(1).size()-1);	/* assign label of first example to node*/
			else
				node.label = "NONE";
			//System.out.println("Homogeneous node observed");
		} else {
			String splittingCol = decideSplittingColumn(table);
			//System.out.println("splitting col was decided to be " + splittingCol);
			node.splitOn = splittingCol;
			int colIndex = returnColumnIndex(splittingCol);
			for (int i = 1; i < tableInfo.get(colIndex).size(); i++) {
				Node newNode = new Node();
				String label = tableInfo.get(colIndex).get(i);
				newNode.label = label;
				//System.out.println("new node label is " + newNode.label);
				node.childrenValues.add(label);
				newNode.isLeaf = false;
				newNode.splitOn = splittingCol;
				List<List<String> > auxTable = pruneTable(table, splittingCol, label);
				//printAttributeTable(auxTable);
				buildDecisionTree(auxTable, newNode);
				node.children.add(newNode);
			}
		}
	}

	// given a column and label (value), extract all rows that contain that label(value) for the column 
	private static List<List<String>> pruneTable(List<List<String>> attributeTable,
			String colName, String value) {
		List<List<String> > prunedTable = new ArrayList<List<String> >();
		int column = -1;
		
		List<String> headerRow = new ArrayList<String>();
		for (int i = 0; i < attributeTable.get(0).size(); i++) {
			if (attributeTable.get(0).get(i).equals(colName)) {
				column = i;
				break;
			}
		}
		for (int i = 0; i < attributeTable.get(0).size(); i++) {
			 if (i != column) {
			 	headerRow.add(attributeTable.get(0).get(i));
			 }
		}
		prunedTable.add(headerRow);
		
		// TODO: Verify the change below
		for (int i = 1; i < attributeTable.size(); i++) {
			List<String> auxRow = new ArrayList<String>();
			if (attributeTable.get(i).get(column).equals(value)) {
				for (int j = 0; j < attributeTable.get(i).size(); j++) {
					if(j != column) {
						auxRow.add(attributeTable.get(i).get(j));
					}
				}
				prunedTable.add(auxRow);
			}
		}
		return prunedTable;
	}

	private static int returnColumnIndex(String columnName) {
		for (int i = 0; i < tableInfo.size(); i++) {
			if (tableInfo.get(i).get(0) == columnName) {
				return i;
			}
		}
		return -1;
	}

	private static String decideSplittingColumn(List<List<String>> table) {
		double minEntropy = Double.MAX_VALUE;
		int splittingColumn = 0;
		for (int column = 0; column < table.get(0).size() - 1; column++) {	/* all candidate features */
			String colName = table.get(0).get(column);
			Map<String, Integer> tempMap = new HashMap<String, Integer>();
			Map<String, Integer> counts = countDistinct(table, column);
			Map<String, Double> attributeEntropy = new HashMap<String, Double>();	/* entropy values for all label values of a column*/
			double columnEntropy = 0.0;
			
			double entropy = 0.0;
			for (int i = 1; i < table.size(); i++) {
				String label = table.get(i).get(column);
				if (tempMap.containsKey(label)) { 	// IF ATTRIBUTE VALUE IS ALREADY FOUND IN A COLUMN, UPDATE IT'S FREQUENCY
					tempMap.put(label, tempMap.get(label)+1);
				} else { 							// IF ATTRIBUTE VALUE IS FOUND FOR THE FIRST TIME IN A COLUMN, THEN PROCESS IT AND CALCULATE IT'S ENTROPY
					tempMap.put(label, 1);		/* new label value detected */
					List<List<String> > tempTable = pruneTable(table, colName, label);	/* extract subset of nodes with this label value */
					Map<String, Integer> classCounts = countDistinct(tempTable, tempTable.get(0).size()-1);	/* get all class counts for this subset*/
					int total = 0;
					for(Integer val : classCounts.values()){
						total += val;
					}
					for (String l : classCounts.keySet()) {
						double temp = (double) classCounts.get(l);
						entropy -= (temp/total)*(Math.log(temp/total) / Math.log(2));
					}
					attributeEntropy.put(label, entropy);
					entropy = 0.0;
				}	
			}
			
			/* compute total entropy for this attribute */
			int sum = 0;
			for (String label : counts.keySet()) {
				sum += counts.get(label);
				columnEntropy += ((double) counts.get(label) * (double) attributeEntropy.get(label));
			}
			columnEntropy = columnEntropy / ((double) sum);
			if (columnEntropy <= minEntropy) {
				minEntropy = columnEntropy;
				splittingColumn = column;
			}
		}
		return table.get(0).get(splittingColumn);
	}

	// returns count of all distinct values for a given column, in any order
	// last element contains the sum
	private static Map<String, Integer> countDistinct(List<List<String>> table,
			int column) {
		Map<String, Integer> vectorOfStrings = new HashMap<String, Integer>();
		List<Integer> counts = new ArrayList<Integer>();
		boolean found = false;
		//int foundIndex = 0;
		for (int i = 1; i < table.size(); i++) {
			String label = table.get(i).get(column);
			found = vectorOfStrings.containsKey(label);
			if (!found) {
				vectorOfStrings.put(label, 1);
			} else {
				vectorOfStrings.put(label, vectorOfStrings.get(label)+1);
			}
		}
		return vectorOfStrings;
	}

	private static boolean isHomogeneous(List<List<String>> table) {
		int lastCol = table.get(0).size() - 1;
		if(table.size() == 1)	return true;			/* empty node is homogeneous */
		String firstValue = table.get(1).get(lastCol);
		for (int i = 1; i < table.size(); i++) {
			if (firstValue != table.get(i).get(lastCol)) {
				return false;
			}
		}
		return true;
	}
	
	private static void printDecisionTree(Node nodePtr)
	{
		if(nodePtr == null) {
			return;
		}
		if (nodePtr.children.size()!=0) {
			System.out.println(" Value: " + nodePtr.label);
			System.out.print("Split on: " + nodePtr.splitOn);
			for (int i = 0; i < nodePtr.children.size(); i++) {   
				System.out.print("\t");
				printDecisionTree(nodePtr.children.get(i));
			}
	    } else {
			System.out.println("Predicted class = " + nodePtr.label);
		}
		return;
	}
	
}
