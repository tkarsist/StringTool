package com.veikonkala.stringTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

/*
Most important variables:
	resolvedDuplicateMap			hashmap<String,String>					Resolved duplicates from file (format: wrong_key, correctkey)
	uniqueMap 							hashmap<String,String>					Unique values, resolved duplicates accounted with the correct key (format:key,frequency)
	duplicateMap 					treemap<String,Arraylist<String>>		Analysis of potetial duplicates left (the resolved are left out). Format: WORD {SYNONYM_1,SYNONUM_N}
	uniqueWordsMap					treemap<String,String>					Same as map, but a treemap. This is only for the sorting
	uniqueWordsMapLimited			treemap<String,String>					Same as uniqueWorsMap but this list is filtered to have only at least minWords occurences of the tags


*/



public class duplicateStringAnalyzer {
	private static final int NTHREDS = Runtime.getRuntime().availableProcessors();
	private String inFile;
	private String outFile;
	private String outHashFile;
	private String outUniqueFile;
	private String resolvedDuplicateFile;
	private double similarPercent;
	private int minimumTags;
	private int compareAlgorithm;
	private int wordSwappingDepth;
	private boolean wordSwapping;
	private boolean customTSV;
	private String separator;
	private boolean inputWithNumbers;
	private int inputFileColumn;
	private Map <String, String> resolvedDuplicateMap; 
	private Map <String, ArrayList<String>> duplicateMap;
	private Map <String, Integer> uniqueMap;
	private Map <String, Integer> uniqueWordsMap;
	private Map <String, Integer> uniqueWordsMapLimited;

	
	public duplicateStringAnalyzer(){
		this("","");
	}
	
	public duplicateStringAnalyzer(String inFile, String outFile){
		this.inFile=inFile;
		this.outFile=outFile;
		this.outHashFile="duplicateHashmap.txt";
		this.outUniqueFile="uniqueWords";
		this.resolvedDuplicateFile="";
		this.similarPercent=0.82;
		this.minimumTags=3;
		this.compareAlgorithm=1;
		this.wordSwappingDepth=4;
		this.wordSwapping=false;
		this.customTSV=false;
		this.separator="\t";
		this.inputWithNumbers=true;
		this.inputFileColumn=-1;
		this.resolvedDuplicateMap = new HashMap<String,String>();
		this.duplicateMap=new TreeMap<String,ArrayList<String>>(new customComparator()); //do not put case insensitive order, it will lose some keys
		this.uniqueMap=new HashMap<String,Integer>();
		this.uniqueWordsMap = new TreeMap<String,Integer>(new customComparator());
		this.uniqueWordsMapLimited = new TreeMap<String,Integer>(new customComparator());
	}

	public duplicateStringAnalyzer(String inFile, String outFile, String resolvedDuplicateFile){
		this(inFile,outFile);
		this.resolvedDuplicateFile=resolvedDuplicateFile;
	}

	public void addFromFileToResolvedDuplicateMap(String file){
		/*
		Reads the resolved duplicates. There will be excluded from the results. You can check the results by this
		This will read the resolved file (format: CORRECT_WORD |FREQ| WRONG_WORD_1 |FREQ| ... WRONG_WORD_N |FREQ|
		It will create hashmap of format: WRONG_WORD (key), CORRECT_WORD (value)
		Example (resolvedDuplicateMap):
				atuomobile  Automobile
				automobile  Automobile
				autommobile Automobile

		 */

		try {
			BufferedReader inDupl=new BufferedReader(new FileReader(file));
			String line;
			int counter=0;
			while ((line = inDupl.readLine()) !=null){
				counter+=1;
				String splitArray[] = line.split(this.separator);
					
				if(splitArray.length>1){
					
					String value=splitInputWord(splitArray[0]);
					
					for(int i=1;i<(splitArray.length);i++){
						String key=splitInputWord(splitArray[i]);
						if(!key.equals("")&&!value.equals(""))
							this.resolvedDuplicateMap.put(key, value);
					}

				}
				

			}
			
			inDupl.close();
			System.out.println("Added "+counter+ " resolved duplicate lines to analysis. Total resolved map size: "+this.resolvedDuplicateMap.size());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	
	public void addFromFileToUniqueKeysMap(String file){
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
		
		int allArticleCounter=0;
		int articleInScopeCounter=0;

		try {
			BufferedReader in=new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) !=null){
				allArticleCounter+=1;

				if(!this.customTSV){ //in normal mode, no custom format
					String lineStr=line.toString();
					if(this.inputFileColumn>-1){
						String splitArray[] = line.split(this.separator);
						lineStr=splitArray[this.inputFileColumn-1];
					}
					if(!this.resolvedDuplicateMap.containsKey(lineStr)){
						Integer freq = this.uniqueMap.get(lineStr);
						if(!lineStr.equals(""))
							this.uniqueMap.put(lineStr, (freq == null) ? 1 : freq + 1);
					}
					else{
						Integer freq = uniqueMap.get(this.resolvedDuplicateMap.get(lineStr));
						this.uniqueMap.put(this.resolvedDuplicateMap.get(lineStr), (freq == null) ? 1 : freq + 1);
					}

				}
				else{
					String splitArray[] = line.split("\t");
					if(splitArray.length > 5 && splitArray[5] != null && (splitArray[5].contains("2014")||splitArray[5].contains("2013")||splitArray[3].contains("2013"))){
						articleInScopeCounter+=1;

						if(splitArray.length > 6 && splitArray[6] != null && splitArray[6] != ""){
							if(!this.resolvedDuplicateMap.containsKey(splitArray[6])){
								Integer freq = this.uniqueMap.get(splitArray[6]);
								if(!splitArray[6].equals(""))
									this.uniqueMap.put(splitArray[6], (freq == null) ? 1 : freq + 1);
							}
							else{
								Integer freq = this.uniqueMap.get(this.resolvedDuplicateMap.get(splitArray[6]));
								uniqueMap.put(this.resolvedDuplicateMap.get(splitArray[6]), (freq == null) ? 1 : freq + 1);
							}
						}

						if(splitArray.length > 7 && splitArray[7] != null && splitArray[7] != ""){
							if(!this.resolvedDuplicateMap.containsKey(splitArray[7])){
								Integer freq = this.uniqueMap.get(splitArray[7]);
								if(!splitArray[6].equals(""))
									this.uniqueMap.put(splitArray[7], (freq == null) ? 1 : freq + 1);
							}
							else{
								Integer freq = this.uniqueMap.get(this.resolvedDuplicateMap.get(splitArray[7]));
								this.uniqueMap.put(this.resolvedDuplicateMap.get(splitArray[7]), (freq == null) ? 1 : freq + 1);
							}


						}
					}
				}

			}
			in.close();
		} catch (IOException e) {

			e.printStackTrace();
		}


		System.out.println("Total number of lines in input: " +allArticleCounter);
		if(articleInScopeCounter!=0)
			System.out.println("Total number of lines to be evaluated: " +articleInScopeCounter);
		System.out.println("Distinct words in original file: "+this.uniqueMap.size());

	}
	public void analyzeUniqueKeysForPotentialDuplicates(){
		
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

		
		System.out.println("Starting duplicate analysis : ");

		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		List<Future<Map<String,ArrayList<String>>>> futureList = new ArrayList<Future<Map<String,ArrayList<String>>>>();
		int batchCounter=0;
		int jobsize=uniqueMap.size()/(NTHREDS)+1;
		int elementCounter=0;
		Set<String> jobWords=new HashSet<String>();
		for (String name: uniqueMap.keySet()){
			elementCounter+=1;
			jobWords.add(name);
			batchCounter+=1;

			if(batchCounter>=jobsize || elementCounter==uniqueMap.size()){
				int wordSwappingTmp=0;
				if(this.wordSwapping)
					wordSwappingTmp=this.wordSwappingDepth;
				Callable<Map<String,ArrayList<String>>> worker = new StrCompareCallable((HashSet)jobWords,(HashMap)this.uniqueMap, this.compareAlgorithm, this.similarPercent, wordSwappingTmp, this.inputWithNumbers);
				Future<Map<String,ArrayList<String>>> submit = executor.submit(worker);
				futureList.add(submit);
				batchCounter=0;
				jobWords=new HashSet<String>();

			}



		}

		System.out.println("Map size:" +this.uniqueMap.size()+" Batchjobsize: "+jobsize+ " Number of batches: "+futureList.size());	
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
					this.duplicateMap.put(key, futureDupl);
				}
				System.out.println(" ");

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();


	}
	
	public TreeSet<String> checkResolvedMapCollision(){
		
		Set<String> keyValueCollision=new TreeSet<String>();
		for (String name: this.resolvedDuplicateMap.keySet()){
			String key =name.toString();
			String value=this.resolvedDuplicateMap.get(key);
			if(this.resolvedDuplicateMap.containsKey(value))
				keyValueCollision.add(value);
		}

		if(keyValueCollision.size()>0){
			System.out.println("");
			System.out.println("WARNING: The resolved duplicate file uses following values as key and also as value. Resolve these in the file:");
			Iterator<String> iterator = keyValueCollision.iterator();
			while (iterator.hasNext()) {
				System.out.println(iterator.next());
			}
		}
		return (TreeSet<String>) keyValueCollision;

		
	}
	
	public void doAll(){
		this.readResolvedDuplicateMapFromFile();
		this.readUniqueKeysMapFromFile();
		this.analyzeUniqueKeysForPotentialDuplicates();
		this.writeAllResultsToFiles();
	}

	private void generateOrderedFrequencyLimitedUniqueWordsMap(){

		for (String name: this.uniqueMap.keySet()){

			String key =name.toString();
			int value = this.uniqueMap.get(name);  
			if(value>=this.minimumTags){
				this.uniqueWordsMapLimited.put(key, value);
			}

		}

	}

	private void generateOrderedUniqueWordsMap(){

		for (String name: this.uniqueMap.keySet()){
			String key =name.toString();
			int value = this.uniqueMap.get(name);  
			this.uniqueWordsMap.put(key, value);
		}

	}

	public TreeMap<String,Integer> getOrderedFrequencyLimitedUniqueWordsMap(){
		return (TreeMap<String, Integer>) this.uniqueWordsMapLimited;
	}

	public TreeMap<String,Integer> getOrderedUniqueWordsMap(){
		if(this.uniqueWordsMap.size()==0)
			this.generateOrderedUniqueWordsMap();
		return (TreeMap<String, Integer>) this.uniqueWordsMap;
	}
	public TreeMap<String,ArrayList<String>> getPotentialDuplicateMap(){
		return (TreeMap<String, ArrayList<String>>) this.duplicateMap;
	}
	public HashMap<String,String> getResolvedDuplicateMap(){
		return (HashMap<String, String>) this.resolvedDuplicateMap;
	}
	public HashMap<String,Integer> getUniqueKeysMap(){
		return (HashMap<String, Integer>) this.uniqueMap;
	}
	public void readResolvedDuplicateMapFromFile(){

		if(!this.resolvedDuplicateFile.equals("")){
			this.addFromFileToResolvedDuplicateMap(this.resolvedDuplicateFile);
			this.checkResolvedMapCollision();

		}

	}
	public void readUniqueKeysMapFromFile(){
		this.addFromFileToUniqueKeysMap(this.inFile);

	}

	public void setCompareAlgorithm(int algorithm){
		this.compareAlgorithm=algorithm;
	}
	
	public void setCustomInputFormat(boolean customFormat){
		this.customTSV=customFormat;
	}

	public void setInputFile(String file){
		this.inFile=file;
	}
	
	public void setInputFileColumn(int inputFileColumn){
		this.inputFileColumn=inputFileColumn;
	}
	
	public void setInputOutputWithNumbers(boolean inputWithNumbers){
		this.inputWithNumbers=inputWithNumbers;
	}
	

	public void setInputResolvedDuplicateFile(String file){
		this.resolvedDuplicateFile=file;
	}

	public void setMinimumWords(int words){
		this.minimumTags=words;
	}

	public void setOutputPotentialDuplicateFile(String file){
		this.outFile=file;
	}

	public void setOutputResolvedHashFile(String file){
		this.outHashFile=file;
	}

	public void setOutPutUniqueWordsFile(String file){
		this.outUniqueFile=file;
	}

	public void setPotentialDuplicateMap(TreeMap<String,ArrayList<String>> duplicateMap){
		this.duplicateMap=duplicateMap;
	}

	public void setResolvedDuplicateMap(HashMap<String,String> resolvedDuplicateMap ){
		this.resolvedDuplicateMap=resolvedDuplicateMap;
	}
	public void setSeparator(String separator){
		this.separator=separator;
	}
	public void setSimilarity(double similarity){
		this.similarPercent=similarity;
	}
	public void setUniqueKeysMap(HashMap<String,Integer> uniqueKeysMap){
		this.uniqueMap=uniqueKeysMap;
	}

	public void setWordSwapping(boolean wordSwapping){
		this.wordSwapping=wordSwapping;
	}

	public void setWordSwappingDepth(int wordSwappingDepth){
		this.wordSwappingDepth=wordSwappingDepth;
	}

	private String splitInputWord(String strValue){
		String value=strValue;
		if(this.inputWithNumbers){
		int indexChar=strValue.indexOf("|")-1;
		if(indexChar>0)
			value=strValue.substring(0, indexChar);
		else
			value=strValue;
		}
		return value;
	}

	public void writeAllResultsToFiles(){
		this.generateOrderedUniqueWordsMap();
		this.generateOrderedFrequencyLimitedUniqueWordsMap();
		this.writePotentialDuplicatesToFile();
		this.writeResolvedDuplicateMapToHashFile();
		this.writeUniqueWordsMapToFile();
		this.writeFrequencyLimitedUniqueWordsMapToFile();

	}

	public void writeFrequencyLimitedUniqueWordsMapToFile(){
		
		//generate Unique tags file with tag limit(resolved duplicates are not withing)
		String outUniqueFileLimited=outUniqueFile+"_MIN_"+minimumTags+"_INSTANCES.txt";
		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(outUniqueFileLimited));
			for (String name: this.uniqueWordsMapLimited.keySet()){
				String key =name.toString();
				int value= this.uniqueWordsMapLimited.get(key);
				writer.write(key+this.separator+value);
				writer.newLine();	

			}
			if ( writer != null)
				writer.close( );
			System.out.println("Unique tags file created (duplicates removed based on provided resolved duplicate file), with minimum "+minimumTags+" appearances: "+outUniqueFileLimited);		

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//generate Unique tags file with tag limit(resolved duplicates are not withing). This is Exclude List
		String outUniqueFileLimitedExclude=this.outUniqueFile+"_MAX_"+(minimumTags-1)+"_INSTANCES.txt";
		writer = null;
		try {
			writer = new BufferedWriter( new FileWriter(outUniqueFileLimitedExclude));
			for (String name: uniqueWordsMap.keySet()){
				String key =name.toString();
				if(!uniqueWordsMapLimited.containsKey(key)){
					int value= uniqueWordsMap.get(key);
					writer.write(key+this.separator+value);
					writer.newLine();	

				}

			}

			if ( writer != null)
				writer.close( );
			System.out.println("Unique tags file created (duplicates removed based on provided resolved duplicate file), with minimum "+(minimumTags-1)+" appearances: "+outUniqueFileLimitedExclude);	

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void writePotentialDuplicatesToFile(){
		//Puts duplicate results to the output file

		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(this.outFile));
			for (String name: this.duplicateMap.keySet()){
				String key =name.toString();
				ArrayList<String> value=duplicateMap.get(key);
				String toFile=key;
				for (String duplicateString : value) {
					toFile=toFile+this.separator+duplicateString;
				}
				writer.write(toFile);
				writer.newLine();	

			}
			writer.close( );
			System.out.println("Potential duplicates ("+this.duplicateMap.size()+") written to : " +this.outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
	public void writeResolvedDuplicateMapToHashFile(){

		if(!this.resolvedDuplicateFile.equals("")){
			//generate Duplicate hashmap file
			
			BufferedWriter writer;
			try {
				writer = new BufferedWriter( new FileWriter( outHashFile));
				for (String name: this.resolvedDuplicateMap.keySet()){
					String key =name.toString();
					String value=this.resolvedDuplicateMap.get(key);
					String toFile=key+this.separator+value;
					writer.write(toFile);
					writer.newLine();	
				}

				if ( writer != null)
					writer.close( );

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			System.out.println("Duplicate hasmap file created (based on -d resolved duplicate file): " +outHashFile);

		}
	}

	public void writeUniqueWordsMapToFile(){
		String outUniqueFileNormal=this.outUniqueFile+".txt";

		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(outUniqueFileNormal));
			for (String name: this.uniqueWordsMap.keySet()){
				String key =name.toString();
				int value= this.uniqueWordsMap.get(key);
				writer.write(key+this.separator+value);
				writer.newLine();	

			}

			writer.close( );
			System.out.println("Unique tags file created (duplicates removed based on provided resolved duplicate file) : " +outUniqueFileNormal);		

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
