package com.veikonkala.stringTool;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class StrCompareCallableOld implements Callable<ArrayList<String>> {

	private String strkey;
	private String strcompare;
	private int algorithm;
	private double similarity;
	private int swappingDepth;
	
  
  public StrCompareCallableOld(String strkey, String strcompare,int algorithm, double similarity, int swappingDepth){
	  this.strkey=strkey;
	  this.strcompare=strcompare;
	  this.algorithm=algorithm;
	  this.similarity=similarity;
	  this.swappingDepth=swappingDepth;
  }
  
  @Override
  public ArrayList<String> call() throws Exception {
	    ArrayList<String> result=new ArrayList<String>();
	  	double sim=0;
	  	//System.out.println(this.strkey+" "+strcompare);
	  	if(this.algorithm==1)
	  		sim=StrAnalysis.similarity(this.strkey,this.strcompare);
	  		
	  	if(this.algorithm==2)
			sim=StrAnalysis.compareStrings(this.strkey,this.strcompare);
	  	if(this.algorithm==3 && this.swappingDepth>0){
	  		if(sim<=this.similarity){
	  			sim=StrAnalysis.similarityWithWordSwapping(this.strkey,this.strcompare, this.similarity,this.swappingDepth);
	  			//System.out.println("hit");
	  		}
	  	}

	  	if(sim>=this.similarity){
	  		result.add(this.strkey);
	  		result.add(this.strcompare);

	  	}
	  	//System.out.println(sim);
    return result;
  }

} 