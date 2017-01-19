package tejasvib_project2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class tejasvib_project2 {
	static int counterDAATOr=0;
	static int counterDAATAnd=0;

	private static String inputfile;	//path of input file	
	private static PrintWriter outputfile;	//path of output file
	
	private static void PrintingPostings(String query, HashMap<String, TreeSet<Integer>> Map)
	{
		String querySplit[] = query.split(" ");
		for (int queryterm = 0; queryterm < querySplit.length; queryterm++) 	//loop to retrieve the postiongs list of the query terms
		{
			TreeSet<Integer> termtree = Map.get(querySplit[queryterm]);
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(termtree);

			String formattedlist = termpostings.toString().replace("[", "").replace(",", "").replace("]", "").trim();		//formatting to match required pattern of output as per project description 
			outputfile.write("GetPostings\n" + querySplit[queryterm] + "\nPostings list: " + formattedlist + "\n");
		}
	}

	public static void PrintingTaatOR(String query, ArrayList<Integer> termTAATOr, int size, int count) {
		String formattedlist;
		if (size == 0)							//check if result is empty
			formattedlist = "empty";
		else
			formattedlist = termTAATOr.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("TaatOr\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ termTAATOr.size() + "\nNumber of comparisons: " + count);
	}

	public static void PrintingTaatAND(String query, ArrayList<Integer> termTAATAnd, int size, int count) {
		String formattedlist;
		if (size == 0)					//check if result is empty
			formattedlist = "empty";
		else
			formattedlist = termTAATAnd.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("TaatAnd\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ termTAATAnd.size() + "\nNumber of comparisons: " + count);
	}

	public static void PrintingDaatOr(String query, ArrayList<Integer> dAATOr, int size, int count) {
		String formattedlist;
		if (size == 0)			//check if result is empty
			formattedlist = "empty";
		else
			formattedlist = dAATOr.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("DaatOr\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ size + "\nNumber of comparisons: " + count);

	}

	private static void PrintingDaatAnd(String query, ArrayList<Integer> dAATAnd, int size, int count) {
		String formattedlist;
		if (size == 0)			//check if result is empty
			formattedlist = "empty";
		else
			formattedlist = dAATAnd.toString().replace("[", "").replace(",", "").replace("]", "").trim();
		outputfile.println("DaatAnd\n" + query + "\nResults: " + formattedlist + "\nNumber of documents in results: "
				+ size + "\nNumber of comparisons: " + count);

	}

	public static void main(String args[]) throws Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(args[0]))); 
	
		inputfile = args[2]; // gets input query file from args[2]
		BufferedReader br = new BufferedReader(new FileReader(tejasvib_project2.inputfile));
		String inputline = null;

		String outputpath = args[1]; // gets outputfile from args[1]
		outputfile = new PrintWriter(outputpath, "UTF-8");

		HashMap<String, TreeSet<Integer>> invertedIndex = new HashMap<String, TreeSet<Integer>>();
		ArrayList<String> languages = new ArrayList<String>();				//fields in the index to iterate for terms
		languages.add("text_nl");
		languages.add("text_fr");
		languages.add("text_de");
		languages.add("text_ja");
		languages.add("text_ru");
		languages.add("text_pt");
		languages.add("text_es");
		languages.add("text_it");
		languages.add("text_da");
		languages.add("text_no");
		languages.add("text_sv");

		for (String lang : languages) {
			Terms terms = MultiFields.getTerms(reader, lang); // get all terms of this field
			TermsEnum termsenum = terms.iterator();
			BytesRef term = null;
			while ((term = termsenum.next()) != null) 
			{
				PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, lang, term);			//gets the document for the particular term
				TreeSet<Integer> temp = new TreeSet<>();

				while (postingsEnum.nextDoc() != PostingsEnum.NO_MORE_DOCS) 
				{
					if (invertedIndex.containsKey(term.utf8ToString())) 			//check if term already present in dictionary
					{
						temp = invertedIndex.get(term.utf8ToString());
						temp.add(postingsEnum.docID());

					} else 
					{
						temp.add(postingsEnum.docID());
					}
					invertedIndex.put(term.utf8ToString(), temp);
				}
			}
		}

		while ((inputline = br.readLine()) != null) 						//while there are queries present
		{
			PrintingPostings(inputline, invertedIndex);						//function to print the postings of the terms
			// System.out.println(inputline);
			TAATAnd(inputline, invertedIndex);
			TAATOr(inputline, invertedIndex);
			DAATAnd(inputline, invertedIndex);
			DAATOr(inputline, invertedIndex);
			// for(String s: invertedIndex.keySet())
			// System.out.println(s+" "+invertedIndex.get(s));

			
		}
		br.close();														//closing the IO resources
		outputfile.close();

	}

	public static ArrayList<ArrayList> sortPostingList(String query, HashMap<String, TreeSet<Integer>> Map) 			//function to sort the terms as per the size of their postings list 
	{
		ArrayList<ArrayList> postingArrayList = new ArrayList<ArrayList>();
		String querySplit[] = query.split(" ");
		for (int queryterm = 0; queryterm < querySplit.length; queryterm++) {

			TreeSet<Integer> termtree = Map.get(querySplit[queryterm]);
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(termtree);
			postingArrayList.add(termpostings);
		}

		Collections.sort(postingArrayList, new Comparator<ArrayList>() 			//override comparator to sort ArrayList of ArrayList
		{
			public int compare(ArrayList a1, ArrayList a2) {
				return a1.size() - a2.size(); 
			}
		});
		return postingArrayList;
	}

	public static int findMin(ArrayList<ArrayList> postingArraylist) 		//finds min docId of the given arraylist's 1st elements
	{
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		int min = -1;														//assuming docId's in given index are greater than 0
		ArrayList<Integer> currentPosting = new ArrayList<Integer>();
		for (int i = 0; i < postingArraylist.size(); i++) 
		{
			if (postingArraylist.get(i).size() == 0) 
			{
				postingArraylist.remove(i);
			}
		}

		for (int i = 0; i < postingArraylist.size(); i++) 
		{
			if (postingArraylist.get(i).size() > 0) {
				currentPosting = postingArraylist.get(i);
				postingPointers.add(currentPosting.get(0));
			}
		}

		if (postingPointers.size()>1) 
		{
			min = postingPointers.get(0);
			for (int i = 1; i < postingPointers.size(); i++) 
			{
				counterDAATOr++;
				if ((postingPointers.get(i)) < min) 
				{
					min = postingPointers.get(i);
				}
			}

		}
		else if(postingPointers.size()==1)
		{
			min = postingPointers.get(0);
		}

		return min;

	}

	public static ArrayList<Integer> TAATAnd(String query, HashMap<String, TreeSet<Integer>> Map) 		//function to find TAAT And
	{
		ArrayList<ArrayList> postingArraylist = sortPostingList(query, Map);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(postingArraylist.get(0));								//intermediate list to store intermediate results

		ArrayList<Integer> termTAATAnd = new ArrayList<Integer>();
		int count = 0;
		for (int termNum = 1; termNum < postingArraylist.size(); termNum++) 
		{
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(postingArraylist.get(termNum));
		
			int i = 0, j = 0;
			while (i < temp.size() && j < termpostings.size())   		
			{
				count++;														//increementing counter as a comaparision is made
				// System.out.println(temp.get(i).equals(termpostings.get(j)));
				//System.out.println(temp.get(i)==termpostings.get(j));
				if (temp.get(i).equals(termpostings.get(j))) 
				{
					termTAATAnd.add(temp.get(i));
					i++;
					j++;
				} else if (temp.get(i) < termpostings.get(j)) 
				{
					i++;
				}
				else if (i == (temp.size() - 1) && !temp.get(i).equals(termpostings.get(j))) 
				{
					j++;
				} else if (i < temp.size() && temp.get(i) > termpostings.get(j))
				{
					j++;
				}
			}
			temp.removeAll(temp);
			temp.addAll(termTAATAnd);
			termTAATAnd.removeAll(termTAATAnd);
		}
		termTAATAnd.addAll(temp);

		PrintingTaatAND(query, termTAATAnd, termTAATAnd.size(), count);
					
		return termTAATAnd;

	}

	public static ArrayList<Integer> TAATOr(String query, HashMap<String, TreeSet<Integer>> Map) 
	{
		ArrayList<ArrayList> postingArraylist = sortPostingList(query, Map);		//sorting as per size of postings list
		ArrayList<Integer> temp = new ArrayList<Integer>();
		temp.addAll(postingArraylist.get(0));										//intermediate list to store intermediate results
		ArrayList<Integer> termTAATOr = new ArrayList<Integer>();
		int count = 0;
		for (int termNum = 1; termNum < postingArraylist.size(); termNum++) 
		{
			ArrayList<Integer> termpostings = new ArrayList<Integer>();
			termpostings.addAll(postingArraylist.get(termNum));					//postings list for other terms
			// System.out.println(termpostings.size());
			int i = 0, j = 0;
			while (i < temp.size() && j < termpostings.size()) 
			{
				if (temp.get(i).equals(termpostings.get(j))) 
				{
					count++;
					if (!(i == (temp.size() - 1)))
					{
						i++;
						j++;
					} 
					else
					{
						j++;
					} 

				}
				else if (temp.get(i) < termpostings.get(j) && !(i == (temp.size() - 1))) 
				{
					i++;
					count++;
				} else if (temp.get(i) > termpostings.get(j)) 
				{
					termTAATOr.add(termpostings.get(j));
					j++;
					count++;
				} else if (i == (temp.size() - 1) && !temp.get(i).equals(termpostings.get(j)) && temp.get(i)<termpostings.get(j)) 
				{
					termTAATOr.add(termpostings.get(j));
					j++;
				}
			}
			temp.addAll(termTAATOr);									//adding new elements to intermediate list and sorting them
			Collections.sort(temp);
			termTAATOr.removeAll(termTAATOr);
		}
		Collections.sort(temp);
		termTAATOr = temp;
		
		/*System.out.println("TaatOr\n" + query);
		if (termTAATOr.size() > 0)
			System.out.println("Results: " + termTAATOr);
		else
			System.out.println("Results: empty");
		System.out.println("Number of documents in results: " + termTAATOr.size());
		System.out.println("Number of comparisons: " + count); */
		
		PrintingTaatOR(query, termTAATOr, termTAATOr.size(), count);				//printing to output file
		return termTAATOr;
	}

	public static ArrayList<Integer> DAATAnd(String query, HashMap<String, TreeSet<Integer>> Map) 		//funtion to find DAAT And of terms
	{
		ArrayList<ArrayList> postingArraylist = new ArrayList<ArrayList>();
		postingArraylist = sortPostingList(query, Map);
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		ArrayList<Integer> DAATAnd = new ArrayList<Integer>();

		for (int queryterm = 0; queryterm < postingArraylist.size(); queryterm++) {
			ArrayList<Integer> termPostings = new ArrayList<Integer>();
			termPostings.addAll(postingArraylist.get(queryterm));
			postingPointers.add(termPostings.get(0));
		}
		int max=0;
		counterDAATAnd = 0;
		while (postingArraylist.size() > 0) {
			
			max = findMax(postingPointers);																//finds max docId

			outerloop: for (int i = 0; i < postingArraylist.size(); i++) 
			{
				ArrayList<Integer> currentPosting = new ArrayList<Integer>();
				currentPosting = postingArraylist.get(i);

				if (currentPosting.size() > 0 && currentPosting.get(0) == max) 
				{
					if (findEqual(postingPointers) == 1) 
					{
						counterDAATAnd++;
						DAATAnd.add(postingPointers.get(0));									//add element to result set if all docIds are same
						for (int p = 0; p < postingArraylist.size(); p++) 
						{
							ArrayList<Integer> samePosting = new ArrayList<Integer>();			//add same elements to a list
							samePosting = postingArraylist.get(p);
							
						if (samePosting.size() > 1)
						{
							postingPointers.remove(p);											//remove same elements from the postings list after they have been added
							postingPointers.add(p, samePosting.get(1));							
						}
						samePosting.remove(0);
						}
						break outerloop;														//break outer for loop to consider next elements
					}					
				}
				while (currentPosting.size() > 0 && currentPosting.get(0) < max) 				//discard all elements that are less than max docId
				{
					counterDAATAnd++;
					if (postingPointers.get(i) != 0)
						postingPointers.remove(i);
					if (currentPosting.size() > 1)
						postingPointers.add(i, currentPosting.get(1));
					currentPosting.remove(0);

				}

				if (currentPosting.size() == 0) {
					postingArraylist.remove(i);
					postingArraylist.removeAll(postingArraylist);
				}
			}
		}	
		
		PrintingDaatAnd(query, DAATAnd, DAATAnd.size(), counterDAATAnd);
		return DAATAnd;
	}

	public static int findMax(ArrayList<Integer> postingPointers) 						//finds max docId of given elements
	{
		int max = postingPointers.get(0);
		if (findEqual(postingPointers) == 1){
			return max;
		}
		else {
			
			for (int i = 1; i < postingPointers.size(); i++) {
				counterDAATAnd++;
				if ((postingPointers.get(i)) > max) {
					max = postingPointers.get(i);
				}
			}
			return max;
		}
		
	}

	public static int findEqual(ArrayList<Integer> postingPointers) 				//compares if all pointers have the same docIds
	{
		int equality = 0;
		int temp = postingPointers.get(0);
		for (int i = 1; i < postingPointers.size(); i++) {
			if ((postingPointers.get(i)) == temp)
				equality = equality + 1;
		}
		if (equality == postingPointers.size() - 1)
			return 1;															//returns 1 only when all elements are equal 
		else
			return 0;
	}

	
	public static ArrayList<Integer> DAATOr(String query, HashMap<String, TreeSet<Integer>> Map) 			//function to find DAAT Or
	{
		ArrayList<ArrayList> postingArraylist = new ArrayList<ArrayList>();
		postingArraylist = sortPostingList(query, Map);
		ArrayList<Integer> postingPointers = new ArrayList<Integer>();
		ArrayList<Integer> DAATOr = new ArrayList<Integer>();

		for (int termNum = 0; termNum < postingArraylist.size(); termNum++) 						//add 1st element of all postings list
		{
			ArrayList<Integer> termPostings = new ArrayList<Integer>();
			termPostings.addAll(postingArraylist.get(termNum));
			postingPointers.add(termPostings.get(0));
		}
		counterDAATOr=0;

		while (postingArraylist.size() > 1) {
		
			int min = findMin(postingArraylist);
			if (min == -1) 																	//returns -1 when all the lists are empty
			{
				break;
			}
			DAATOr.add(min);
			counterDAATOr++;
			for (int i = 0; i < postingArraylist.size(); i++) {
				ArrayList<Integer> currentPosting = new ArrayList<Integer>();
				currentPosting = postingArraylist.get(i);

				if (currentPosting.size() > 0 && currentPosting.get(0) == min) {
					currentPosting.remove(0);
				}

			}
		}
		if (postingArraylist.size() ==1) {
			ArrayList<Integer> currentPosting = new ArrayList<Integer>();
			currentPosting = postingArraylist.get(0);
			if (currentPosting.size() > 0 ) 
			{
				DAATOr.addAll(currentPosting);										//add all remaining elements if only one list is left

			}

		}
		
		PrintingDaatOr(query, DAATOr, DAATOr.size(), counterDAATOr);
		return DAATOr;

	}

}