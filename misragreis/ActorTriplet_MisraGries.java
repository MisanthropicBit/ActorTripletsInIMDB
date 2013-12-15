package misragreis;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class ActorTriplet_MisraGries {
	private static int s, m;
	private static int highestFrequency;
	private static String filename;
	
	public static void main(String[] args) {
		initializeValues(args);
		
		long startTime = System.currentTimeMillis();
		frequentTriplet();
		long endTime = System.currentTimeMillis();
		System.out.println("Total running time: " + (double) (endTime-startTime)/1000 + " seconds");			
	}
	
	public static void frequentTriplet() {
		m = 0;
		highestFrequency = 0;
		// First pass
		System.out.println("Starting first pass");
		Map<Triplet, Integer> storage = firstPass(s);
		
		// Reset counters
		for(Triplet t : storage.keySet()) {
			storage.put(t, 0);
		}
		
		// Second pass
		System.out.println("Starting second pass");
		storage = secondPass(storage);
		
		// Verify frequency (f_j = m/k)
		int frequentCount = 0;
		Triplet mostFrequentTriplet = null;
		for(Triplet t : storage.keySet()) {
			if(storage.get(t) > m/s) {
				//System.out.println("  Frequent Triplet: " + t + " = " + storage.get(t));
				if(storage.get(t) > highestFrequency) {
					highestFrequency = storage.get(t);
					mostFrequentTriplet = t;
				}
				frequentCount++;
			}
		}
		System.out.println("Second Pass Done.");
		System.out.println("Number of triplets: " + m);
		System.out.println("Number of frequent triplets: " + frequentCount);
		System.out.println("Frequency required (m/k): " + m + "/" + s + " = " + (double) m/s);
		if(mostFrequentTriplet != null) {
			System.out.println("(One of the) Most frequent Triplet: " + mostFrequentTriplet.toString() + " = " + highestFrequency);
		} else {
			System.out.println("No triplet with required frequency.");
		}
		System.out.println("Optimal k (m/f): " + (double) m/highestFrequency);
	}
	
	// First Pass, Misra-Gries for each triplet
	public static Map<Triplet,Integer> firstPass(int k) {
		Map<Triplet, Integer> storage = new HashMap<Triplet, Integer>();
		try {
			int counter = 1;
			Scanner SC = new Scanner(new File(filename));
			while(SC.hasNextLine()) {
				int[] actorList = getActors(SC.nextLine());
				int r = actorList.length;
				System.out.println(counter);
				// Construct Triplets
				if(r > 2) {
					for(int i = 0; i < r-2; i++) {
						for(int j = i+1; j < r-1; j++) {
							for(int h = j+1; h < r; h++) {
								Triplet t = sortedTriplet(actorList[i],actorList[j],actorList[h]);
								m++;
								// Misra Gries:
								if(storage.containsKey(t)) {
									storage.put(t, storage.get(t)+1);
								} else if(storage.size() < k-1) {
									storage.put(t, 1);
								} else {
									//System.out.println("Overflow");
									Iterator<Triplet> it = storage.keySet().iterator();
									while(it.hasNext()) {
										Triplet tt = it.next();
										storage.put(tt, storage.get(tt)-1);
										if(storage.get(tt) == 0) {
											it.remove();
											storage.remove(tt);
										}
									}
								}
								
							}
						}
					}
				}
				counter++;
			}
			SC.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return storage;
	}
	
	// 2nd Pass, count occurrences of triplets in storage
	public static Map<Triplet, Integer> secondPass(Map<Triplet, Integer> storage) {
		try {
			Scanner SC = new Scanner(new File(filename));
			int counter = 1;
			while(SC.hasNextLine()) {
				int[] actorList = getActors(SC.nextLine());
				int r = actorList.length;
				System.out.println(counter);
				for(int i = 0; i < r-2; i++) {
					for(int j = i+1; j < r-1; j++) {
						for(int h = j+1; h < r; h++) {
							Triplet t = sortedTriplet(actorList[i],actorList[j],actorList[h]);
							if(storage.containsKey(t)) {
								if(storage.containsKey(t)) {
									storage.put(t, storage.get(t) + 1);
								} else {
									storage.put(t, 1);
								}
							}
						}
					}	
				}
				counter++;
			}
			SC.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return storage;
	}
	
	// Sorts the actors IDs for a triplet
	public static Triplet sortedTriplet(int x, int y, int z) {
		if(x < y && y < z) {
			return new Triplet(x,y,z);
		} else {
			int a, b, c;
			if(x < y) {
				a = x;
				b = y;
			} else {
				a = y;
				b = x;
			}
			if(a < z) {
				if(b < z) {
					c = z;
				} else {
					c = b;
					b = z;
				}
			} else {
				c = b;
				b = a;
				a = z;
			}
			return new Triplet(a,b,c);
		}
	}
	
	// Retrieves list of actors from a movie
	public static int[] getActors(String line) throws FileNotFoundException {
		line = (String) line.subSequence(line.lastIndexOf(":")+1, line.length());
		String[] strArray = line.split(",");
		int[] actors = new int[strArray.length];
		for(int i = 0; i < strArray.length; i++) {
		    actors[i] = Integer.parseInt(strArray[i]);
		}
		return actors;
	}
	
	public static void initializeValues(String[] args) {
		if(args.length > 0) {
			s = Integer.valueOf(args[0]);
		} else {
			s = 4000000;
		}
		if(args.length > 0) {
			filename = args[1];
		} else {
			filename = "imdb_stream.txt";
		}
	}
}
