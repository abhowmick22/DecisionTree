import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


/* An implementation of a decision tree */

public class DecisionTree {

	private static List<List<String> > dataTable;					// vvs
	private static List<List<String> > tableInfo;					// vvs
	
	public static void main(String[] args){
		
		BufferedReader input;
		dataTable = new ArrayList<List<String> >();
		tableInfo = new ArrayList<List<String> >();
		try {
			input = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			String singleInstance;
			
			while((singleInstance = input.readLine())!=null){
				parse(singleInstance);
			}
		input.close();
		printAttributeTable();
		generateTableInfo();
			
			
		} catch (IOException e) {
			System.out.println("Could not read training data");
		}
	}
	
	// parses an input line
	private static void parse(String someString){
		String[] tokens = someString.split(",");
		List<String> featureVector = new ArrayList<String>(Arrays.asList(tokens));
		dataTable.add(featureVector);
	}
	
	private static void printAttributeTable(){
		ListIterator<List<String> > it = dataTable.listIterator();
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
	
	private static void generateTableInfo(){
		for (int i = 0; i < dataTable.get(0).size(); i++) {
			List<String> tempInfo = new ArrayList<String>();
			Map<String, Integer> tempMap = new HashMap<String, Integer>();
			for (int j = 0; j < dataTable.size(); j++) {
				if (!tempMap.containsKey(dataTable.get(j).get(i))) {
					tempMap.put(dataTable.get(j).get(i), 1);
					tempInfo.add(dataTable.get(j).get(i));
				} else	{
					String key = dataTable.get(j).get(i);
					tempMap.put(key, tempMap.get(key) + 1);
				}
			}
			tableInfo.add(tempInfo);
		}
	}
	
	
}
