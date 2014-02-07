package com.veikonkala.stringTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class customAnalyserMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		long startTime = System.currentTimeMillis();
		String inFile="";
		String outFile="";
		String outHashFile="duplicateHashmap.txt";
		String outUniqueFile="uniqueWords";
		String resolvedDuplicateFile="";
		double similarPercent=0;
		int minimumTags=0;
		int compareAlgorithm=1;
		int wordSwappingDepth=0;
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
		if (resolvedDuplicateFile==""){
			System.out.println("StringTool Usage");
			System.out.println(" Usage:");
			System.out.println(" HARDCODED LOGIC, ONLY SOME PARAMETERS IN USE!");
			System.out.println(" -d                 :The file where you have decided which is the correct key.");
			System.out.println("                    In initial run just with input and output file. Edit the output file");
			System.out.println("                    and leave only the keys you want to keep. This will recheck the duplicate output.");
			System.out.println(" -similarity        :How similar words should be, double between 0-1.0. Default is 0.82. Try different variations.");
			System.out.println(" -minWords          :The tool will generate file that has by default at least 3 instances. Integer.");
			System.out.println(" --wordSwapping     :The tool will try to swap the order of words in string (default 4 words), using space as separator. SLOW!");
			System.out.println(" -wordSwappingDepth :Depth for word swapping, integer value");
			System.out.println(" --humanLikeCompare :Alternative compare algorithm. Lightweight, human like, more sensitive");

			System.exit(0);
		}
	
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

		
		duplicateStringAnalyzer analyzer=new duplicateStringAnalyzer("viihde_meta.tsv","potential_duplicates_2013_2014");
		duplicateStringAnalyzer analyzer2=new duplicateStringAnalyzer("all_tags.txt","all_tags_temp_output.txt");
		duplicateStringAnalyzer analyzer3=new duplicateStringAnalyzer("unique_tags_All_Tags_MIN_13_INSTANCES.txt","potential_duplicates_for_all_at_least_13.txt");
		analyzer3.setInputFileColumn(1);
		
		if(!resolvedDuplicateFile.equals("")){
			analyzer.setInputResolvedDuplicateFile(resolvedDuplicateFile);
			analyzer2.setInputResolvedDuplicateFile(resolvedDuplicateFile);
			analyzer3.setInputResolvedDuplicateFile(resolvedDuplicateFile);
		}
		if(similarPercent!=0){
			analyzer.setSimilarity(similarPercent);
			analyzer2.setSimilarity(similarPercent);
			analyzer3.setSimilarity(similarPercent);
		}
		
		if(wordSwapping){
			analyzer.setWordSwapping(wordSwapping);
			analyzer2.setWordSwapping(wordSwapping);
			analyzer3.setWordSwapping(wordSwapping);
			if(wordSwappingDepth!=0){
				analyzer.setWordSwappingDepth(wordSwappingDepth);
				analyzer2.setWordSwappingDepth(wordSwappingDepth);
				analyzer3.setWordSwappingDepth(wordSwappingDepth);
			}
		}
		if(compareAlgorithm>1){
			analyzer.setCompareAlgorithm(compareAlgorithm);
			analyzer2.setCompareAlgorithm(compareAlgorithm);
			analyzer3.setCompareAlgorithm(compareAlgorithm);
		}
			
		
		//analyzer1
		analyzer.setMinimumWords(3);
		analyzer.setCustomInputFormat(true);
		analyzer.setOutPutUniqueWordsFile("unique_tags_2013_2014");
		analyzer.setOutputResolvedHashFile("MigrationDuplicateHashMap.txt");

		
		analyzer2.setMinimumWords(13);
		analyzer2.setOutPutUniqueWordsFile("unique_tags_All_Tags");
		analyzer2.setOutputResolvedHashFile("MigrationDuplicateHashMap.txt");
		Map<String,Integer> allUniqKeys=new TreeMap<String,Integer>(customComparator);
		analyzer2.readResolvedDuplicateMapFromFile();
		analyzer2.readUniqueKeysMapFromFile();	
		allUniqKeys=analyzer2.getOrderedUniqueWordsMap();


		analyzer3.setOutPutUniqueWordsFile("unique_tags_All_Tags_Limited");
		analyzer3.setOutputResolvedHashFile("MigrationDuplicateHashMap.txt");

		
		System.out.println("ANALYSING 2013 & 2014 AT LEAST 3 TAGS");
		analyzer.doAll();

		System.out.println("ANALYSING All TAGS FOR AT LEAST 13 TAGS");
		analyzer2.analyzeUniqueKeysForPotentialDuplicates();
		analyzer2.writeAllResultsToFiles();

		System.out.println("ANALYSING FOR DUPLICATES FOR ALL TAGS WHERE THERE IS AT LEAST 13 TAGS");
		analyzer3.doAll();

		
		
		Set<String> uniqueKeysWhiteList=new TreeSet<String>(customComparator);
		Set<String> uniqueKeysBlackList=new TreeSet<String>(customComparator);
		
		try {
			BufferedReader inDupl=new BufferedReader(new FileReader("unique_tags_All_Tags_Limited.txt"));
			String line;
			int counter=0;
			while ((line = inDupl.readLine()) !=null){
				counter+=1;
				String splitArray[] = line.split("\t");
				uniqueKeysWhiteList.add(splitArray[0]);	
				

			}
			
			inDupl.close();
			System.out.println(counter+" tag read from all tags at least 13 tags");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader inDupl=new BufferedReader(new FileReader("unique_tags_2013_2014_MIN_3_INSTANCES.txt"));
			String line;
			int counter=0;
			while ((line = inDupl.readLine()) !=null){
				counter+=1;
				String splitArray[] = line.split("\t");
				uniqueKeysWhiteList.add(splitArray[0]);	
				

			}
			
			inDupl.close();
			System.out.println(counter+" tag read from 2013 & 2014 at least 3 tags");
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		String whitelist="migration_whitelist_tags.txt";

		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(whitelist));
			for (String name: uniqueKeysWhiteList){
				String value =name.toString();
				writer.write(value);
				writer.newLine();	

			}

			writer.close( );
			System.out.println("Whitelist ("+uniqueKeysWhiteList.size()+")tags for migration generated to : " +whitelist);		

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(String s: allUniqKeys.keySet()){
			if(!uniqueKeysWhiteList.contains(s)){
				uniqueKeysBlackList.add(s);
			}
		}

		String blacklist="migration_blacklist_tags.txt";
		writer=null;
		try {
			writer = new BufferedWriter( new FileWriter(blacklist));
			for (String name: uniqueKeysBlackList){
				String value =name.toString();
				writer.write(value);
				writer.newLine();	

			}

			writer.close( );
			System.out.println("Blacklist ("+uniqueKeysBlackList.size()+") tags for migration generated to : " +blacklist);		

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		long endTime = System.currentTimeMillis();
		long spentTime = endTime - startTime;
		System.out.println("Execution time: "+(spentTime/1000)+" seconds");
		
		System.out.println("###### MIGRATION ##########");
		System.out.println("Hashmap - MigrationDuplicateHashMap.txt");
		System.out.println("BlackList - migration_blacklist_tags.txt");
		System.out.println("WhiteList - migration_whitelist_tags.txt");
		System.out.println("");
		System.out.println("###### POTENTIAL DUPLICATES  ##########");
		System.out.println("2013 & 2014: potential_duplicates_2013_2014.txt");
		System.out.println("2013 & 2014: potential_duplicates_for_all_at_least_13.txt");

	}

}
