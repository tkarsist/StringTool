package com.veikonkala.stringTool;



//import org.apache.commons.lang3.StringUtils;

public class StringTool {
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args){



		long startTime = System.currentTimeMillis();
		//String inFile="";
		//String outFile="";
		//String outHashFile="duplicateHashmap.txt";
		//String outUniqueFile="uniqueWords";
		//String resolvedDuplicateFile="";
		//double similarPercent=0;
		//int minimumTags=0;
		//int compareAlgorithm=1;
		//int wordSwappingDepth=0;
		//boolean wordSwapping=false;
		//boolean customTSV=false;

		duplicateStringAnalyzer analyzer=new duplicateStringAnalyzer();

		for(int i=0;i<args.length;i++){
			if(args[i].equals("-i") && args[i+1]!=null){
				
				analyzer.setInputFile(args[i+1]);
				System.out.println("Infile: " +args[i+1]);

			}
			if(args[i].equals("-o") && args[i+1]!=null){
				analyzer.setOutputPotentialDuplicateFile(args[i+1]);
				System.out.println("Outfile (list of potential duplicates): " +args[i+1]);

			}
			if(args[i].equals("-d") && args[i+1]!=null){
				analyzer.setInputResolvedDuplicateFile(args[i+1]);
				System.out.println("Resolved Duplicate File: " +args[i+1]);


			}
			if(args[i].equals("-similarity") && args[i+1]!=null){
				double similarity=Double.parseDouble(args[i+1]);
				analyzer.setSimilarity(similarity);
				System.out.println("Similarity set to: " +similarity);

			}
			
			if(args[i].equals("-separator") && args[i+1]!=null){
				analyzer.setSeparator(args[i+1]);
				System.out.println("Separator set to: word1" +args[i+1]+"word2");

			}
			
			if(args[i].equals("-minWords") && args[i+1]!=null){
				int minimumTags=Integer.parseInt(args[i+1]);
				analyzer.setMinimumWords(minimumTags);
				System.out.println("minWords set to: " +minimumTags);
			}
			if(args[i].equals("--wordSwapping")){
				boolean wordSwapping=true;
				analyzer.setWordSwapping(wordSwapping);
				//compareAlgorithm=3;
				System.out.println("Wordswapping enabled ");
			}
			if(args[i].equals("-wordSwappingDepth") && args[i+1]!=null){
				int wordSwappingDepth=Integer.parseInt(args[i+1]);
				analyzer.setWordSwappingDepth(wordSwappingDepth);
				System.out.println("wordSwappingDepth set to: " +wordSwappingDepth);
			}
			if(args[i].equals("-inputFileColumn") && args[i+1]!=null){
				int inputFileColumn=Integer.parseInt(args[i+1]);
				analyzer.setInputFileColumn(inputFileColumn);
				System.out.println("InputFileColumn set to: " +inputFileColumn);
			}

			if(args[i].equals("--humanLikeCompare")){
				int compareAlgorithm=2;
				analyzer.setCompareAlgorithm(compareAlgorithm);
				System.out.println("Human like lightweight compare in use");
			}
			if(args[i].equals("--customFormat")){
				boolean customTSV=true;
				analyzer.setCustomInputFormat(customTSV);
				System.out.println("Custom TSV format defined ");
			}
			if(args[i].equals("--noFrequency")){
				analyzer.setInputOutputWithNumbers(false);
				System.out.println("No frequency handling in use");
			}
		}
		if (args.length<1){
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
			System.out.println("- duplicateHashhMap file based on the resolved duplicate file (-d). The structure of");
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
			System.out.println(" --humanLikeCompare :Alternative compare algorithm. Lightweight, human like, more sensitive");
			System.out.println(" -separator         : The separator for input & output files, Default is tab");
			System.out.println(" -inputFileColumn   : The unique words will be read from column. Separator is the same as defined.");
			System.out.println("                      Define integer value to set the column, e.g. 8 would column 8.");
			System.out.println(" --noFrequency      : The output file is without frequencies. Note: there can be no frequencies");
			System.out.println("                      in the resolved duplicated file (-d option).");
			System.out.println(" --customFormat     :Uses the custom TSV and 2013 & 2014. Otherwise just flat file with word per line");
			System.out.println("");
			System.out.println("EXAMPLE USAGE:");
			System.out.println("java -jar StringTool.jar -i unitTest.txt -o output.txt -d unitTestResolved.txt --humanLikeCompare " +
					"-similarity 0.90 -separator ; -inputFileColumn 3 -wordSwappingDepth 3 --wordSwapping");
			
			System.exit(0);
		}
		analyzer.doAll();
/*		
		if(!resolvedDuplicateFile.equals("")){
			analyzer.setInputResolvedDuplicateFile(resolvedDuplicateFile);
		}
		if(similarPercent!=0){
			analyzer.setSimilarity(similarPercent);
		}
		if(minimumTags!=0){
			analyzer.setMinimumWords(minimumTags);
		}
		if(wordSwapping){
			analyzer.setWordSwapping(wordSwapping);
			if(wordSwappingDepth!=0){
				analyzer.setWordSwappingDepth(wordSwappingDepth);
			}
		}
		if(compareAlgorithm>1){
			analyzer.setCompareAlgorithm(compareAlgorithm);
		}
		if(customTSV){
			analyzer.setCustomInputFormat(customTSV);
		}
		analyzer.setSeparator("\t");
		analyzer.setInputOutputWithNumbers(true);
	*/	
		//altenative to these for is .doAll()
		
		/*
		analyzer.readResolvedDuplicateMapFromFile();
		analyzer.readUniqueKeysMapFromFile();
		analyzer.analyzeUniqueKeysForPotentialDuplicates();
		analyzer.writeAllResultsToFiles();
		*/
		
		long endTime = System.currentTimeMillis();
		long spentTime = endTime - startTime;
		System.out.println("Execution time: "+(spentTime/1000)+" seconds");
		
		




		/*
		Reads the resolved duplicates. There will be excluded from the results. You can check the results by this
		This will read the resolved file (format: CORRECT_WORD |FREQ| WRONG_WORD_1 |FREQ| ... WRONG_WORD_N |FREQ|
		It will create hashmap of format: WRONG_WORD (key), CORRECT_WORD (value)
		Example (resolvedDuplicateMap):
				atuomobile  Automobile
				automobile  Automobile
				autommobile Automobile

		 */
	

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
		
	
		
	}



}
