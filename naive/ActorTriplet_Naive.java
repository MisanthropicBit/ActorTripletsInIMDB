package naive;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class ActorTriplet_Naive {
	private static Map<Triplet, Integer> storage;
	private static Triplet mostFrequentTriplet = null;
	private static int n, frequency;
	
	public static void main(String[] args) {
		initializeValues(args);
		storage = new HashMap<Triplet, Integer>();
		try {
			Scanner SC = new Scanner(new File(filename));
			int rowCount = 0;
			while(SC.hasNextLine()) {
				System.out.println(rowCount);
				int[] actors = getActors(SC.nextLine());
				n = actors.length;
				if(n > 2) {
					findTriplets(actors);
				}
				rowCount++;
			}
			SC.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
			
			for(Triplet t : storage.keySet()) {
				System.out.println("Triplet: " + t + " = " + storage.get(t));
				if(mostFrequentTriplet == null) {
					mostFrequentTriplet = t;
				} else if(storage.get(t) > storage.get(mostFrequentTriplet)) {
					mostFrequentTriplet = t;
				}
			}
			frequency = storage.get(mostFrequentTriplet);
			System.out.println("Number of unique Triplets: " + storage.size());
			System.out.println("Most frequent triplet " + mostFrequentTriplet + ": " + frequency);
		
	}
	
	// Goes through every list (movie) and counts up the number of times an actor triplet is found.
	public static void findTriplets(int[] actorList) {
		for(int i = 0; i < n-2; i++) {
			for(int j = i+1; j<n-1; j++) {
				for(int k = j+1; k < n; k++) {
					Triplet t = sortedTriplet(actorList[i],actorList[j],actorList[k]);
					if(storage.containsKey(t)) {
						storage.put(t, storage.get(t)+1);
					} else {
						storage.put(t, 1);
					}
				}
			}	
		}
	}
	
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
			filename = args[0];
		} else {
			filename = "imdb_stream.txt";
		}
	}
}
