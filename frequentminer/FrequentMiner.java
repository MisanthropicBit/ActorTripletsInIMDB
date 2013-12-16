package frequentminer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class FrequentMiner {
	public static String filename = "src/imdb_stream.txt";
	public static Integer[] items;
	public static int k, s, n, id1, id2, treshold, tooLong, itemsetsInBatch, actualCount;
	public static long itemCount = 0;
	public static int transactionCount = 0;
	public static Map<Itemset, Integer> storage;
	
	public static ArrayList<Integer[]> batch;
	public static Set<Integer> transactionsWithItemset;
	public static Map<Itemset, Set<Integer>> intersectionItemsets;
	public static ArrayList<Integer> intersectionItems;

	public static void main(String[] args) {
		initializeValues(args);
		tooLong = 33;
		treshold = (int) (s * 3.6);
		int longCount = 0;
		batch = new ArrayList<Integer[]>();
		storage = new HashMap<Itemset, Integer>(s);
		
		long t0 = System.currentTimeMillis();
		try {
			Scanner SC = new Scanner(new File(filename));
			// For each transaction in the input:
			while(SC.hasNextLine()) {
				System.out.println("Transaction: " + transactionCount);
				// Get list of items in the transaction
				items = getItems(SC.nextLine());
				n = items.length;
				
				// If transaction is small, process normally:
				if(n <= tooLong) {
					if(n >= k) {
						for(int i = 0; i < n-(k-1); i++) {
							recursPermAndUpdate(new int[]{items[i]}, k, i);
						}
					}
				} else {
					// Add to batch: 
					batch.add(items);
					itemsetsInBatch += getNumberOfSets(items.length);
					
					// If batch contains too many itemsets, calculate sets and insert into storage
					if(itemsetsInBatch >= treshold) {
						System.out.println("Calculating Batch...");
						// From intersections on all transactions in batch, create hashmap of itemsets and their supportCount
						Map<Itemset, Integer> frequencyOfItemsets = intersectionOnTransactions();

						// Misra-Gries for resulting pairs.
						for(Itemset itemset : frequencyOfItemsets.keySet()) {
							int support = frequencyOfItemsets.get(itemset);
							update(itemset, support);
						}
						
						// Clear batch
						batch = new ArrayList<Integer[]>();
						itemsetsInBatch = 0;
					}
					longCount++;
				}
				transactionCount++;
			}
			SC.close();
			
			long t1 = System.currentTimeMillis();
			System.out.println("\r\n" + "\r\n" + "Time used: " + (t1-t0)/1000 + "s");
			System.out.println("Total item count: " + itemCount);
			System.out.println("Queried frequency: " + itemCount / s);
			//double e = (1.0 / s) * 100;
			//System.out.println("Epsilon (1/s): " + e + "%");
			//System.out.println("Long transaction = " + tooLong);
			//System.out.println("Total number of long transactions: " + longCount);
			//System.out.println("Total transaction count: " + transactionCount);
			//System.out.println("Allowed size of storage: " + s);
			//System.out.println("\r\nSize of storage at end: " + storage.size());
			int highestFrequency = 0;
			Itemset mostFrequent = null;
			for(Itemset itemset : storage.keySet()) {
				if(storage.get(itemset) > highestFrequency) {
					highestFrequency = storage.get(itemset);
					mostFrequent = itemset;
				}
			}
			System.out.println("Most frequent triplet: " + mostFrequent + ". Estimated frequency: " + highestFrequency);
			
			// For counting the actual frequency of the itemset found
			actualFrequency(mostFrequent);
			System.out.println("Actual frequency: " + actualCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Recursive Permutations of itemsets for normal processing of small transactions.
 	public static void recursPermAndUpdate(int[] set, int k_i, int i) {
		if(k_i == 1) {
			//System.out.println(new Itemset(set));
			update(new Itemset(set), 1);
			itemCount++;
		} else {
			for(int j = i+1; j < n-(k_i-2); j++) {
				int[] largerSet = new int[set.length+1];
				System.arraycopy(set, 0, largerSet, 0, set.length);
				largerSet[set.length] = items[j];
				recursPermAndUpdate(largerSet, k_i-1, j);
			}
		}
	}
	
	// Do intersections on all transactions in batch, and save the found itemsets along with the ID's of the transactions.
	public static Map<Itemset, Integer> intersectionOnTransactions() {
		try {
			intersectionItemsets = new HashMap<Itemset, Set<Integer>>();
			for(int i = 0; i < batch.size(); i++) {
				id1 = i;
				for(int j = i+1; j < batch.size(); j++) {
					id2 = j;
					intersectionItems = intersect(batch.get(i), batch.get(j));
					n = intersectionItems.size();
					// Generate all itemsets, and add to hashmap
					if(intersectionItems.size() >= k) {
						for(int h = 0; h < n-(k-1); h++) {
							int[] set = new int[]{intersectionItems.get(h)};
							recursPermFromBatch(set, k, h);
						}
					}
					
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		// Create and return hashmap of itemsets and their support count (i.e. how many transcationIds are related to them)
		Map<Itemset, Integer> supportCount = new HashMap<Itemset, Integer>();
		for(Itemset itemset : intersectionItemsets.keySet()) {
			// Insert itemset and the size of the set of transactionIds related to it
			supportCount.put(itemset, intersectionItemsets.get(itemset).size());
		}
		return supportCount;
	}
	
	// Revursive permutations of itemsets from the batch of long transactions
		public static void recursPermFromBatch(int[] set, int k_i, int i) {
			if(k_i == 1) {
				//System.out.println(new Itemset(set));
				itemCount++;
				Itemset itemset = new Itemset(set);
				Set<Integer> transactionIds;
				if(intersectionItemsets.containsKey(itemset)) {
					transactionIds = intersectionItemsets.get(itemset);
					transactionIds.add(id1); transactionIds.add(id2);
				} else {
					transactionIds = new HashSet<Integer>();
					transactionIds.add(id1); transactionIds.add(id2);
				}
				intersectionItemsets.put(itemset, transactionIds);
			} else {
				for(int j = i+1; j < n-(k_i-2); j++) {
					int[] largerSet = new int[set.length+1];
					System.arraycopy(set, 0, largerSet, 0, set.length);
					largerSet[set.length] = intersectionItems.get(j);
					recursPermFromBatch(largerSet, k_i-1, j);
				}
			}
		}
	
	// Binomial Coefficient (n choose k)
	public static int getNumberOfSets(int numItems) {
		if(k < 2) {
			return numItems;
		} else if(k > numItems || k < 0 || numItems < 0) {
			return 0;
		} else {
			// Multiply length (n * (n-1) * (n-2) * ... * (n-k))
			int numSets = numItems;
			for(int i = 1; i < k; i++) {
				numSets = numSets * (numItems-i);
			}
			// Divide by possible duplicates (2,6,24,120,720,5040,...)
			int divide = 2;
			for(int i = 2; i < k; i++) {
				divide = divide + divide*i;
			}
			return (int) numSets / divide;
		}
	}
	
	// Updates the support count of an itemset in the storage, by the Misra-Gries logic
	public static void update(Itemset itemset, int support) {
		if(storage.containsKey(itemset)) {
			// If item is in storage, increase counter
			storage.put(itemset, storage.get(itemset)+support);
		} else {
			storage.put(itemset, support);
			
			// If item is new and there is no room, decrease all counter by 1, and remove those with count = 0
			if(storage.size() > s) {
				Iterator<Itemset> iter = storage.keySet().iterator();
				while(iter.hasNext()) {
					Itemset nextSet = iter.next();
					storage.put(nextSet, storage.get(nextSet)-1);
					if(storage.get(nextSet) == 0) {
						iter.remove();
						storage.remove(nextSet);
					}
				}
			}
		}
	}
	
	// Creates an intersection on two transactions, returning a list of items that are in both
	public static ArrayList<Integer> intersect(Integer[] t1, Integer[] t2) {
		Set<Integer> checkForMatch = new HashSet<Integer>();
		for(Integer i : t1) {
			checkForMatch.add(i);
		}
		// List of all items in both transactions
		ArrayList<Integer> intersection = new ArrayList<Integer>();
		for(Integer i : t2) {
			if(checkForMatch.contains(i)) {
				intersection.add(i);
			}
		}
		return intersection;
	}
	
	// From a line from the input-file, retrieve the items from the transaction and return a sorted array of Integers
	public static Integer[] getItems(String line) throws FileNotFoundException {
		line = (String) line.subSequence(line.lastIndexOf(":")+1, line.length());
		String[] strArray = line.split(",");
		Integer[] items = new Integer[strArray.length];
		for(int i = 0; i < strArray.length; i++) {
		    items[i] = Integer.parseInt(strArray[i]);
		}
		// Sort (n log n)
		Arrays.sort(items);
		return items;
	}
	
	// For finding the actual frequency of a specific Itemset, by just looking for each actor ID
	public static void actualFrequency(Itemset itemset) throws FileNotFoundException {
		Scanner SC = new Scanner(new File(filename));
		int[] itemsetIds = itemset.getItems();
		while(SC.hasNextLine()) {
			items = getItems(SC.nextLine());
			n = items.length;
			boolean itemsetIsPresent = true;
			if(n >= k) {
				for(int i = 0; i < itemsetIds.length; i++) {
					if(!Arrays.asList(items).contains(itemsetIds[i])) {
						// One of the Itemset ID's was not in transaction = Itemset cannot be here
						itemsetIsPresent = false;
					}
				}
			} else {
				// Transaction is not long enough to hold itemset
				itemsetIsPresent = false;
			}
			if(itemsetIsPresent) {
				actualCount++;
			}
		}
		SC.close();
	}
	
	public static void initializeValues(String[] args) {
		if(args.length > 0) {
			k = Integer.valueOf(args[0]);
		} else {
			k = 3;
		}
		if(args.length > 1) {
			s = Integer.valueOf(args[1]);
		} else {
			s = 4000000;
		}
		if(args.length > 2) {
			filename = args[2];
		} else {
			filename = "imdb_stream.txt";
		}
	}
}