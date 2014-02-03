package com.veikonkala.stringTool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class StrCompareCallable implements Callable<Map<String,ArrayList<String>>> {

	private int algorithm;
	private double similarity;
	private int swappingDepth;
	private HashMap<String,Integer> compareMap;
	private HashSet<String> keySet;
	//private Map<String,ArrayList<String>> duplicateMap;


	public StrCompareCallable(HashSet<String> keySet, HashMap<String, Integer> compareMap, int algorithm, double similarity, int swappingDepth){
		this.compareMap=compareMap;
		this.keySet=keySet;
		this.algorithm=algorithm;
		this.similarity=similarity;
		this.swappingDepth=swappingDepth;
		//this.duplicateMap=new HashMap<String,ArrayList<String>>();
	}

	@Override
	public Map<String,ArrayList<String>> call() throws Exception {

		Map<String,ArrayList<String>> duplicateMap=new HashMap<String,ArrayList<String>>();
		//duplicateMap.put("hahah", value)

		for(String keyStr: this.keySet){
			ArrayList<String> doubles=new ArrayList<String>();
			
			for (String compareStr: this.compareMap.keySet()){
				double sim=0;
				if(!keyStr.equals(compareStr)){
					
					if(this.algorithm==1)
						sim=StrAnalysis.similarity(keyStr,compareStr);

					if(this.algorithm==2){
						sim=StrAnalysis.compareStrings(keyStr,compareStr);

					}	
					if(sim<this.similarity && this.swappingDepth>0){
							sim=StrAnalysis.similarityWithWordSwapping(keyStr,compareStr, this.similarity,this.swappingDepth);

					}
					if(sim>=this.similarity){
						doubles.add(compareStr+" |"+this.compareMap.get(compareStr).toString()+"|");
					}
					 


				}
			}
			
			if(!doubles.isEmpty()){
				duplicateMap.put(keyStr+" |"+this.compareMap.get(keyStr).toString()+"|", doubles);
				//System.out.println(keyStr+" "+doubles);

			}	  


		}
		return (Map<String,ArrayList<String>>) duplicateMap;


	} 
}	