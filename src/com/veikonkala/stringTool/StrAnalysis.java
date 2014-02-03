package com.veikonkala.stringTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class StrAnalysis {
	///Kilpaileva

	/** @return an array of adjacent letter pairs contained in the input string */
	private static String[] letterPairs(String str) {
		int numPairs = str.length()-1;
		//System.out.println("Stringi:"+str+"Loppu");
		//System.out.println(numPairs);
		String[] pairs = new String[numPairs];
		for (int i=0; i<numPairs; i++) {
			pairs[i] = str.substring(i,i+2);
		}
		return pairs;
	}
	/** @return an ArrayList of 2-character Strings. */
	private static ArrayList<String> wordLetterPairs(String str) {
		ArrayList<String> allPairs = new ArrayList<String>();
		// Tokenize the string and put the tokens/words into an array
		String[] words = str.split("\\s");
		// For each word
		for (int w=0; w < words.length; w++) {
			// Find the pairs of characters
			if(words[w].length()>0){
				String[] pairsInWord = letterPairs(words[w]);
				for (int p=0; p < pairsInWord.length; p++) {
					allPairs.add(pairsInWord[p]);
				}
			}
		}
		return allPairs;
	}
	/** @return lexical similarity value in the range [0,1] */
	public static double compareStrings(String str1, String str2) {
		//System.out.println(str1.toUpperCase()+ " "+str2.toUpperCase());
		ArrayList<String> pairs1 = wordLetterPairs(str1.toUpperCase());
		ArrayList<String> pairs2 = wordLetterPairs(str2.toUpperCase());
		int intersection = 0;
		int union = pairs1.size() + pairs2.size();
		for (int i=0; i<pairs1.size(); i++) {
			Object pair1=pairs1.get(i);
			for(int j=0; j<pairs2.size(); j++) {
				Object pair2=pairs2.get(j);
				if (pair1.equals(pair2)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}
		return (2.0*intersection)/union;
	}
	//Kilpaileva loppuu
	public static double similarityWithWordSwapping(CharSequence s1, CharSequence s2, double threshold, int depth) {
		double result=similarity(s1,s2);
		String s1s=s1.toString();
		String s2s=s2.toString();
		String[] splitted1 = s1s.split("\\s+");
		String[] splitted2 = s2s.split("\\s+");

		if(result>=threshold || splitted1.length>depth ||splitted2.length>depth){
			return result;
		}	


		Permutations<String> perm = new Permutations<String>(splitted1);
		Permutations<String> perm2 = new Permutations<String>(splitted2);


		while(perm.hasNext()){
			StringBuilder builder = new StringBuilder();
			for(String s : perm.next()) {
				builder.append(s+" ");
			}
			String str1=builder.toString();
			String str1f=str1.substring(0,str1.length()-1);
			while(perm2.hasNext()){
				StringBuilder builder2 = new StringBuilder();
				for(String s : perm2.next()) {
					builder2.append(s+" ");
				}
				String str2=builder2.toString();
				String str2f=str2.substring(0,str2.length()-1);

				double sim=similarity(str2f,str1f);
				if(sim>result)
					result=sim-0.01;
				if(result>(threshold))
					return result;


			}	

		}


		return result;
	}
	public static double similarityWithWordSwapping2(CharSequence s1, CharSequence s2, double threshold, int depth) {
		double result=similarity(s1,s2);
		String s1s=s1.toString();
		String s2s=s2.toString();
		String[] splitted1 = s1s.split("\\s+");
		String[] splitted2 = s2s.split("\\s+");
		int max=Math.max(s1s.length(), s2s.length())-Math.max(splitted1.length, splitted2.length);
		if(result>=threshold || splitted1.length>depth ||splitted2.length>depth){
			return result;
		}	
		

		Map<String,Double> resultMap=new HashMap<String,Double>();
		for(String s: splitted1){
			for(String k: splitted2){
				if(!s.equals(k)){
					double sim=similarity(s,k);
					if(resultMap.containsKey(s)){
						double simMap=resultMap.get(s);
						if(sim>simMap){
							resultMap.put(s, sim);
						}
					}
					else{
						resultMap.put(s, sim);
					}
				}
			}
			
			result=0;
			for(String x: resultMap.keySet()){
				int strLength=x.length();
				result=result+(1.0*strLength/max*resultMap.get(x));

			}
		}
			//System.out.println(result);

			return result;
		}

		public static double similarity(CharSequence s1, CharSequence s2) {

			return (1 - (StrAnalysis.getLevenshteinDistance(s2,s1)*1.0 / Math.max(s1.length(),s2.length()) ) );
		}

		public static int getLevenshteinDistance(CharSequence s, CharSequence t) {
			if (s == null || t == null) {
				throw new IllegalArgumentException("Strings must not be null");
			}

			/*
           The difference between this impl. and the previous is that, rather
           than creating and retaining a matrix of size s.length() + 1 by t.length() + 1,
           we maintain two single-dimensional arrays of length s.length() + 1.  The first, d,
           is the 'current working' distance array that maintains the newest distance cost
           counts as we iterate through the characters of String s.  Each time we increment
           the index of String t we are comparing, d is copied to p, the second int[].  Doing so
           allows us to retain the previous cost counts as required by the algorithm (taking
           the minimum of the cost count to the left, up one, and diagonally up and to the left
           of the current cost count being calculated).  (Note that the arrays aren't really
           copied anymore, just switched...this is clearly much better than cloning an array
           or doing a System.arraycopy() each time  through the outer loop.)

           Effectively, the difference between the two implementations is this one does not
           cause an out of memory condition when calculating the LD over two very large strings.
			 */

			int n = s.length(); // length of s
			int m = t.length(); // length of t

			if (n == 0) {
				return m;
			} else if (m == 0) {
				return n;
			}

			if (n > m) {
				// swap the input strings to consume less memory
				final CharSequence tmp = s;
				s = t;
				t = tmp;
				n = m;
				m = t.length();
			}

			int p[] = new int[n + 1]; //'previous' cost array, horizontally
			int d[] = new int[n + 1]; // cost array, horizontally
			int _d[]; //placeholder to assist in swapping p and d

			// indexes into strings s and t
			int i; // iterates through s
			int j; // iterates through t

			char t_j; // jth character of t

			int cost; // cost

			for (i = 0; i <= n; i++) {
				p[i] = i;
			}

			for (j = 1; j <= m; j++) {
				t_j = t.charAt(j - 1);
				d[0] = j;

				for (i = 1; i <= n; i++) {
					cost = s.charAt(i - 1) == t_j ? 0 : 1;
					// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
					d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
				}

				// copy current distance counts to 'previous row' distance counts
				_d = p;
				p = d;
				d = _d;
			}

			// our last action in the above loop was to switch d and p, so p now
			// actually has the most recent cost counts
			return p[n];
		}

		public static int distance(String s1, String s2) {
			//s1 = s1.toLowerCase();
			//s2 = s2.toLowerCase();

			int[] costs = new int[s2.length() + 1];
			for (int i = 0; i <= s1.length(); i++) {
				int lastValue = i;
				for (int j = 0; j <= s2.length(); j++) {
					if (i == 0)
						costs[j] = j;
					else {
						if (j > 0) {
							int newValue = costs[j - 1];
							if (s1.charAt(i - 1) != s2.charAt(j - 1))
								newValue = Math.min(Math.min(newValue, lastValue),
										costs[j]) + 1;
							costs[j - 1] = lastValue;
							lastValue = newValue;
						}
					}
				}
				if (i > 0)
					costs[s2.length()] = lastValue;
			}
			return costs[s2.length()];
		}

	}
