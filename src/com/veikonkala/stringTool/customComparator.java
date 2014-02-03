package com.veikonkala.stringTool;

import java.util.Comparator;

public class customComparator implements Comparator<String>{

	@Override
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
}


