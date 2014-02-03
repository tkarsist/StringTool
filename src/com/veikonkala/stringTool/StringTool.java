package com.veikonkala.stringTool;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//import org.apache.commons.lang3.StringUtils;

public class StringTool {
	private static final int NTHREDS = Runtime.getRuntime().availableProcessors();
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {



		long startTime = System.currentTimeMillis();
		String inFile="";
		String outFile="";
		String outHashFile="duplicateHashmap.txt";
		String outUniqueFile="uniqueWords";
		String resolvedDuplicateFile="";
		double similarPercent=0.82;
		int minimumTags=3;
		int compareAlgorithm=1;
		int wordSwappingDepth=4;
		boolean wordSwapping=false;
		boolean customTSV=false;



		for(int i=0;i<args.length;i++){
			if(args[i].equals("-i") && args[i+1]!=null){

				inFile=args[i+1];
				System.out.println("Infile: " +inFile);

			}
			if(args[i].equals("-o") && args[i+1]!=null){
				outFile=args[i+1];
				System.out.println("Outfile (list of potential duplicates): " +outFile);

			}
			if(args[i].equals("-d") && args[i+1]!=null){
				resolvedDuplicateFile=args[i+1];
				System.out.println("Resolved Duplicate File: " +resolvedDuplicateFile);


			}
			if(args[i].equals("-similarity") && args[i+1]!=null){
				similarPercent=Double.parseDouble(args[i+1]);
				System.out.println("Similarity set to: " +similarPercent);

			}
			if(args[i].equals("-minWords") && args[i+1]!=null){
				minimumTags=Integer.parseInt(args[i+1]);
				System.out.println("minWords set to: " +minimumTags);
			}
			if(args[i].equals("--wordSwapping")){
				wordSwapping=true;
				//compareAlgorithm=3;
				System.out.println("Wordswapping enabled ");
			}
			if(wordSwapping && args[i].equals("-wordSwappingDepth") && args[i+1]!=null){
				wordSwappingDepth=Integer.parseInt(args[i+1]);
				System.out.println("wordSwappingDepth set to: " +wordSwappingDepth);
			}
			if(args[i].equals("--humanLikeCompare")){
				compareAlgorithm=2;
				System.out.println("Human like lightweight compare in use");
			}
			if(args[i].equals("--customFormat")){
				customTSV=true;
				System.out.println("Custom TSV format defined ");
			}
		}
		if (inFile=="" || outFile==""){
			System.out.println("StringTool");
			System.out.println("The tool finds potential duplicate words based on custom Levenstein algorithm.");
			System.out.println("The duplicates can be resolved by taking the 'correct' lines from the output of");
			System.out.println("the potential duplicates and place in resolved duplicates file (-d option). ");
			System.out.println("The first word in the resolved duplicate file will define the 'correct' key for ");
			System.out.println("all of the entries.");
			System.out.println("");
			System.out.println("The tool takes tab separated infile (when -customFormat defined). It will check for string ");
			System.out.println("'2013' or '2014' from column 4 (hard coded). It will read the words from column 7 & 8. ");
			System.out.println("Without the -customFormat the tool expects just word per line. ");
			System.out.println("");
			System.out.println("The tool will output always the following files:");
			System.out.println("- file of potential duplicates (-o option). This file should be checked by hand");
			System.out.println("  and 'correct' lines moved to resolved duplicate file (defined with -d)");
			System.out.println("- file of unique tags (duplicates removed based on resolved");
			System.out.println("  duplicates file): uniqueWords.txt");
			System.out.println("- file of unique tags with at least X occurences of word: ");
			System.out.println("  uniqueWords_MIN_X_INTANCES.txt");
			System.out.println("- file of unique tags with at less than X-1 occurences of word (negation): ");
			System.out.println("  uniqueWords_MAX_X-1_INTANCES.txt");
			System.out.println("- hashMap file based on the resolved duplicate file (-d). The structure of");
			System.out.println("  the file is: wrongKey correctKey");
			System.out.println("");
			System.out.println(" Usage:");
			System.out.println(" -i                 :The input file (file to analyze)");
			System.out.println(" -o                 :The potential duplicates output file");
			System.out.println(" -d                 :The file where you have decided which is the correct key.");
			System.out.println("                    In initial run just with input and output file. Edit the output file");
			System.out.println("                    and leave only the keys you want to keep. This will recheck the duplicate output.");
			System.out.println(" -similarity        :How similar words should be, double between 0-1.0. Default is 0.82. Try different variations.");
			System.out.println(" -minWords          :The tool will generate file that has by default at least 3 instances. Integer.");
			System.out.println(" --wordSwapping     :The tool will try to swap the order of words in string (default 4 words), using space as separator. SLOW!");
			System.out.println(" -wordSwappingDepth :Depth for word swapping, integer value");
			System.out.println(" --customFormat     :Uses the custom TSV and 2013 & 2014. Otherwise just flat file with word per line");
			System.out.println(" --humanLikeCompare :Alternative compare algorithm. Lightweight, human like, more sensitive");

			System.exit(0);
		}

		/*
		 Most important variables:
			resolvedDuplicateMap			hashmap<String,String>					Resolved duplicates from file (format: wrong_key, correctkey)
			map 							hashmap<String,String>					Unique values, resolved duplicates accounted with the correct key (format:key,frequency)
			duplicateMap 					treemap<String,Arraylist<String>>		Analysis of potetial duplicates left (the resolved are left out). Format: WORD {SYNONYM_1,SYNONUM_N}
			uniqueWordsMap					treemap<String,String>					Same as map, but a treemap. This is only for the sorting
			uniqueWordsMapLimited			treemap<String,String>					Same as uniqueWorsMap but this list is filtered to have only at least minWords occurences of the tags


		 */



		/*
		Reads the resolved duplicates. There will be excluded from the results. You can check the results by this
		This will read the resolved file (format: CORRECT_WORD |FREQ| WRONG_WORD_1 |FREQ| ... WRONG_WORD_N |FREQ|
		It will create hashmap of format: WRONG_WORD (key), CORRECT_WORD (value)
		Example (resolvedDuplicateMap):
				atuomobile  Automobile
				automobile  Automobile
				autommobile Automobile

		 */
		Map <String, String> resolvedDuplicateMap = new HashMap<String,String>();
		if(!resolvedDuplicateFile.equals("")){
			try {
				BufferedReader inDupl=new BufferedReader(new FileReader(resolvedDuplicateFile));
				String line;
				while ((line = inDupl.readLine()) !=null){

					String splitArray[] = line.split("\t");

					if(splitArray.length>1){
						int indexChar=splitArray[0].indexOf("|")-1;
						String value=splitArray[0].substring(0, indexChar);

						for(int i=1;i<(splitArray.length);i++){
							int indexChar2=splitArray[i].indexOf("|")-1;
							String key=splitArray[i].substring(0, indexChar2);
							resolvedDuplicateMap.put(key, value);
						}

					}

				}
				inDupl.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		

		}

		/*
			Read all words/tags for file and calculate frequencies for unique entries. It will skip the entries in the duplicate file (already handled)
		 	The map generated is hashmap of key, frequency (the key is the tag and frequency is how many times it occurs)
		 	This block also evaluates the resolved duplicates (resolvedDuplicateMap). If it will find that the tag is in the duplicate map, then it will
		 	put the tag to the correct tag.
		 	Example (duplicate file): Automobile |4| atuomobile |2| automobile |1| (the numbers in this file does not matter, it will calculate the frequencies 
		 	from the actual input file

		 	Example (map):
		 	Cat 13
		 	Car 14
		 	Automobile 7 (note that it will not add the wrong tags, insted it will combile and add the frequencies to the "correct tag"

		 */

		Map <String, Integer> map = new HashMap<String,Integer>();
		int allArticleCounter=0;
		int articleInScopeCounter=0;

		try {
			BufferedReader in=new BufferedReader(new FileReader(inFile));
			String line;
			while ((line = in.readLine()) !=null){
				allArticleCounter+=1;

				if(!customTSV){ //in normal mode, no custom format
					String lineStr=line.toString();
					if(!resolvedDuplicateMap.containsKey(lineStr)){
						Integer freq = map.get(lineStr);
						map.put(lineStr, (freq == null) ? 1 : freq + 1);
					}
					else{
						Integer freq = map.get(resolvedDuplicateMap.get(lineStr));
						map.put(resolvedDuplicateMap.get(lineStr), (freq == null) ? 1 : freq + 1);
					}

				}
				else{
					String splitArray[] = line.split("\t");
					if(splitArray.length > 3 && splitArray[3] != null && (splitArray[3].contains("2014")||splitArray[3].contains("2013")||splitArray[3].contains("2013"))){
						articleInScopeCounter+=1;

						if(splitArray.length > 6 && splitArray[6] != null && splitArray[6] != ""){
							if(!resolvedDuplicateMap.containsKey(splitArray[6])){
								Integer freq = map.get(splitArray[6]);
								map.put(splitArray[6], (freq == null) ? 1 : freq + 1);
							}
							else{
								Integer freq = map.get(resolvedDuplicateMap.get(splitArray[6]));
								map.put(resolvedDuplicateMap.get(splitArray[6]), (freq == null) ? 1 : freq + 1);
							}
						}

						if(splitArray.length > 7 && splitArray[7] != null && splitArray[7] != ""){
							if(!resolvedDuplicateMap.containsKey(splitArray[7])){
								Integer freq = map.get(splitArray[7]);
								map.put(splitArray[7], (freq == null) ? 1 : freq + 1);
							}
							else{
								Integer freq = map.get(resolvedDuplicateMap.get(splitArray[7]));
								map.put(resolvedDuplicateMap.get(splitArray[7]), (freq == null) ? 1 : freq + 1);
							}


						}
					}
				}

			}
			in.close();
		} catch (IOException e) {

			e.printStackTrace();
		}


		System.out.println("Total number of articles: " +allArticleCounter);
		if(articleInScopeCounter!=0)
			System.out.println("Total number of articles to evaluate: " +articleInScopeCounter);
		System.out.println("Distinct words in original file: "+map.size());

		/*
		Custom comparator for output file readability. The native comparator does not put A and a after each other (it's easier to human to resolve
		independent of the case

		 */
		Comparator<String> customComparator= new Comparator<String>(){
			public int compare(String s1, String s2)
			{
				String s1n = s1.toLowerCase();
				String s2n = s2.toLowerCase();

				if(s1n.equals(s2n))
				{
					return s1.compareTo(s2);
				}
				return s1n.compareTo(s2n);
			}
		};

		/*
		  This will find similar words. It can use tree different algorithms:
		  - Modified Levenshtein: Return 0-1.0 value how close the words are
		  - Word order swapping (for strings that have spaces). E.g. "Jarkko Ruutu" is almost the same as "Ruutu Jarkko"
		  - Letterpair matching, sometimes works better that Levenshtein

		 This will output duplicateMap of format (it is a treemap, so it is in human readable alphabetical order):
		 WORD {SYNONYM_1,...,SYNONYM_N}

		 The duplicateMap is having the potential duplicates, user needs to take the correct duplicates and put in separate file. This map is
		 written in the output file.
		 Note: All possible word combinations are in the list (all keys with all synonyms)
		 */

		//Omat huomiot: Tässä on snadisti turhaa, looppi. Kun kerran pareja on verrattu, niin se vois olla jo tiedossa, niin ei vertais uudestaan
		//vois olla hasmap avain1avain2 similarity (eka ois konkatenoitu)
		
		System.out.println("Starting duplicate analysis : ");
		Map <String, ArrayList<String>> duplicateMap=new TreeMap<String,ArrayList<String>>(customComparator); //do not put case insensitive order, it will lose some keys
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		List<Future<Map<String,ArrayList<String>>>> futureList = new ArrayList<Future<Map<String,ArrayList<String>>>>();
		int batchCounter=0;
		int jobsize=map.size()/(NTHREDS)+1;
		int elementCounter=0;
		Set<String> jobWords=new HashSet<String>();
		for (String name: map.keySet()){
			elementCounter+=1;
			jobWords.add(name);
			batchCounter+=1;

			if(batchCounter>=jobsize || elementCounter==map.size()){
				int wordSwappingTmp=0;
				if(wordSwapping)
					wordSwappingTmp=wordSwappingDepth;
				Callable<Map<String,ArrayList<String>>> worker = new StrCompareCallable((HashSet)jobWords,(HashMap)map, compareAlgorithm, similarPercent, wordSwappingTmp);
				Future<Map<String,ArrayList<String>>> submit = executor.submit(worker);
				futureList.add(submit);
				batchCounter=0;
				jobWords=new HashSet<String>();

			}



		}
		
		System.out.println("Map size:" +map.size()+" Batchjobsize: "+jobsize+ " Number of batches: "+futureList.size());	
		int counter=0;
		for (Future<Map<String,ArrayList<String>>> future : futureList) {
			counter+=1;
			System.out.print(1.0*counter/futureList.size()*100+"% ");
			try {

				Map<String,ArrayList<String>> futureResult=new HashMap<String,ArrayList<String>>();
				futureResult=future.get();
				for(String key: futureResult.keySet()){
					ArrayList<String> futureDupl=new ArrayList<String>();
					futureDupl=futureResult.get(key);
					duplicateMap.put(key, futureDupl);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();

		System.out.println(" ");




		//Unique words, treemap only for order. This is printed in file
		Map <String, Integer> uniqueWordsMap = new TreeMap<String,Integer>(customComparator);
		for (String name: map.keySet()){
			String key =name.toString();
			int value = map.get(name);  
			uniqueWordsMap.put(key, value);
		}

		//Unique words with minimun frequency
		Map <String, Integer> uniqueWordsMapLimited = new TreeMap<String,Integer>(customComparator);
		for (String name: map.keySet()){

			String key =name.toString();
			int value = map.get(name);  
			if(value>=minimumTags){
				uniqueWordsMapLimited.put(key, value);
			}

		}
		System.out.println("Distinct words if tag used at least "+minimumTags+" times: "+uniqueWordsMapLimited.size());


		/// FILE PRINTING STARTS
		/// ********************

		//Puts duplicate results to the output file
		BufferedWriter writer = null;
		writer = new BufferedWriter( new FileWriter( outFile));
		for (String name: duplicateMap.keySet()){
			String key =name.toString();
			ArrayList<String> value=duplicateMap.get(key);
			String toFile=key;
			for (String duplicateString : value) {
				toFile=toFile+"\t"+duplicateString;
			}
			writer.write(toFile);
			writer.newLine();	

		}

		if ( writer != null)
			writer.close( );
		System.out.println("Potential duplicates written to : " +outFile);

		if(!resolvedDuplicateFile.equals("")){
			//generate Duplicate hashmap file
			Set<String> keyValueCollision=new TreeSet<String>();
			writer = null;
			writer = new BufferedWriter( new FileWriter( outHashFile));
			for (String name: resolvedDuplicateMap.keySet()){
				String key =name.toString();
				String value=resolvedDuplicateMap.get(key);
				String toFile=key+"\t"+value;
				writer.write(toFile);
				writer.newLine();	

				if(resolvedDuplicateMap.containsKey(value))
					keyValueCollision.add(value);
			}

			if ( writer != null)
				writer.close( );

			if(keyValueCollision.size()>0){
				System.out.println("");
				System.out.println("WARNING: The resolved duplicate file uses following values as key and also as value. Resolve these in the file:");
				Iterator<String> iterator = keyValueCollision.iterator();
				while (iterator.hasNext()) {
					System.out.println(iterator.next());
				}
			}

			System.out.println("Duplicate hasmap file created (based on -d resolved duplicate file): " +outHashFile);

		}

		//generate Unique tags file (resolved duplicates are not withing)
		String outUniqueFileNormal=outUniqueFile+".txt";
		writer = null;
		writer = new BufferedWriter( new FileWriter(outUniqueFileNormal));
		for (String name: uniqueWordsMap.keySet()){
			String key =name.toString();
			int value= uniqueWordsMap.get(key);
			writer.write(key+"\t"+value);
			writer.newLine();	

		}

		if ( writer != null)
			writer.close( );
		System.out.println("Unique tags file created (duplicates removed based on provided resolved duplicate file) : " +outUniqueFileNormal);		

		//generate Unique tags file with tag limit(resolved duplicates are not withing)
		String outUniqueFileLimited=outUniqueFile+"_MIN_"+minimumTags+"_INSTANCES.txt";
		writer = null;
		writer = new BufferedWriter( new FileWriter(outUniqueFileLimited));
		for (String name: uniqueWordsMapLimited.keySet()){
			String key =name.toString();
			int value= uniqueWordsMapLimited.get(key);
			writer.write(key+"\t"+value);
			writer.newLine();	

		}

		if ( writer != null)
			writer.close( );
		System.out.println("Unique tags file created (duplicates removed based on provided resolved duplicate file), with minimum "+minimumTags+" appearances: "+outUniqueFileLimited);		

		//generate Unique tags file with tag limit(resolved duplicates are not withing). This is Exclude List
		String outUniqueFileLimitedExclude=outUniqueFile+"_MAX_"+(minimumTags-1)+"_INSTANCES.txt";
		writer = null;
		writer = new BufferedWriter( new FileWriter(outUniqueFileLimitedExclude));
		for (String name: uniqueWordsMap.keySet()){
			String key =name.toString();
			if(!uniqueWordsMapLimited.containsKey(key)){
				int value= uniqueWordsMap.get(key);
				writer.write(key+"\t"+value);
				writer.newLine();	

			}

		}

		if ( writer != null)
			writer.close( );
		System.out.println("Unique tags file created (duplicates removed based on provided resolved duplicate file), with minimum "+(minimumTags-1)+" appearances: "+outUniqueFileLimitedExclude);	

		long endTime = System.currentTimeMillis();
		long spentTime = endTime - startTime;
		System.out.println("Execution time: "+(spentTime/1000)+" seconds");
	}


}
