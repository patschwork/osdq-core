package org.arrah.framework.dataquality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.arrah.framework.ndtable.ReportTableModel;
import org.simmetrics.StringMetric;
import org.simmetrics.StringMetricBuilder;


/***********************************************
 *     Copyright to Vivek Kumar Singh          *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for creating utility 
 * functions will be used by string similarity
 * based on open source simmetric function
 */


public class SimmetricsUtil {

	
	public SimmetricsUtil() {
		
	} // Constructor
	
	public static class MongeElkan {
		// Default Constructor
		private org.simmetrics.metrics.MongeElkan modMongeElkan;
		
		public MongeElkan() {
			modMongeElkan =  new org.simmetrics.metrics.MongeElkan(new org.simmetrics.metrics.JaroWinkler());
		}
		
		public float compare(List<String> a, List<String> b) {
			return modMongeElkan.compare(a, b);
		}
		
	}
	
	public static class Soundex {
		private StringMetric soundex;
		public Soundex() {
			soundex = org.simmetrics.StringMetrics.soundex();
		}
		
		public float compare(String a, String b) {
			return soundex.compare(a, b);
		}
	}
	
	public static class qGramDistance {
		private StringMetric qGram;
		public qGramDistance() {
			qGram = org.simmetrics.StringMetrics.qGramsDistance();
		}
		
		public float compare(String a, String b) {
			return qGram.compare(a, b);
		}
	}
	
	public static class DoubleMetaPhone {
		private StringMetric dmetaPhone;
		public DoubleMetaPhone() {
			dmetaPhone = StringMetricBuilder.with(org.simmetrics.StringMetrics.jaroWinkler()).
					simplify(new org.simmetrics.simplifiers.DoubleMetaphone()).build();
		}
		
		public float compare(String a, String b) {
			return dmetaPhone.compare(a, b);
		}
	}
	
	
	public static class CustomNames {
		private org.simmetrics.metrics.MongeElkan customName;
		// Skip Salution for name
		String [] salutation = new String[] {"Shri","Shree","Mr.","Mrs.","Sri","Thiru"};
	    Hashtable<String, String> bwH = new Hashtable<String, String>();
	    
	    // Add words replacement for Indian names
	    private  Hashtable<String, String> replaceWords() {

	    	bwH.put("kr", "kumar");
	    	bwH.put("kr.", "kumar");
	    	bwH.put("jp", "jai Prakash");
	    	return bwH;
	    	
	    }
		
		public CustomNames() {
			customName =  new org.simmetrics.metrics.MongeElkan(new org.simmetrics.metrics.JaroWinkler());
			replaceWords();
		}
		
		public float compare(String a, String b) {
			String[] as = a.split("\\s+"); String[] bs = b.split("\\s+");
			List<String>al = Arrays.asList(as);List<String>bl = Arrays.asList(bs);
			List<String> sal = Arrays.asList(salutation);
			
			for(int i=0; i < al.size(); i++) {
				String s= al.get(i);
				String rep = bwH.get(s);
				if (rep != null )
					al.set(i, rep);
				
				int index = sal.indexOf(s);
				if (index != -1) // matched salutation
					al.remove(i);
			}
			
			for(int i=0; i < bl.size(); i++) {
				String s= bl.get(i);
				String rep = bwH.get(s);
				if (rep != null )
					bl.set(i, rep);
				
				int index = sal.indexOf(s);
				if (index != -1) // matched salutation
					bl.remove(i);
			}
			
			
			return customName.compare(al, bl);
		}
		
	}
	public static ReportTableModel runSimForAll(Object[] leftA, Object[] rightA) {
		List<String> leftV = new ArrayList<String>();
		List<String> rightV = new ArrayList<String>();
		for (int i=0; i < leftA.length; i++) {
			try {
				Object a = leftA[i];
				Object b = rightA[i];
				if (a == null || b == null ) continue;
				leftV.add(a.toString()); rightV.add(b.toString());
			} catch (Exception e) {
				continue;
				// my be array out of bound if left and right are not matching
			}
		}
		return runSimForAll(leftV,rightV);
		
	}
	public static ReportTableModel runSimForAll(List<String> leftV, List<String> rightV) {
		
		String[] algoList = new String[]{"Levenshtein","JaroWinkler","Jaro",
				"NeedlemanWunch","SmithWaterman","SmithWatermanGotoh","CosineSimilarity",
				"DiceSimilarity","JaccardSimilarity","OverlapCoefficient","BlockDistance",
				"EuclideanDistance","MatchingCoefficient","SimonWhite","MongeElkan","Soundex","qGramDistance","DoubleMetaPhone","CustomNames"};
		
		RecordMatch diff = new RecordMatch();
		RecordMatch.operator doDiff = diff.new operator(); // Just to intantiate 
		
		String[] colName = new String[algoList.length + 2] ;
		colName[0] = "Value";colName[1] = "MatchedValue";
		for (int i=2; i < colName.length; i++)
			colName[i] = algoList[i-2];
		
		ReportTableModel rtm = new ReportTableModel(colName,true,true);

		for ( int i=0; i < leftV.size(); i++) {
			Object[] row = new Object[colName.length];
			row[0] = leftV.get(i);
			row[1] = rightV.get(i);
			int j = 2;	
			for (String algo:algoList) {
				RecordMatch.fuzzyCompareStrings cmpStr = diff.new fuzzyCompareStrings(algo.toUpperCase());
				float mvalue = cmpStr.compare(leftV.get(i), rightV.get(i));
				//System.out.println(algo + ":" + mvalue + ":" +leftV.get(i) + ":"+ rightV.get(i));
				row[j++] = mvalue;
			}
			rtm.addFillRow(row);
		}
		
		return rtm;
		
	}
	
	public static void main(String[] argv) {
		//MongeElkan mg = new MongeElkan();
		List <String> ls = new ArrayList<String>();
		List <String> rs = new ArrayList<String>();
		
		ls.add("k.");ls.add("vivek");ls.add("singh");ls.add("shree");
		rs.add("singh");rs.add("vivek"); rs.add("kumar");rs.add("shree");
		
		//System.out.println(mg.compare(ls,rs));
		ReportTableModel rtm = runSimForAll(ls,rs);
		rtm.toPrint();
	}
	
	
	
} //SimmetricsUtil
