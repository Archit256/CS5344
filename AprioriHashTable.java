package cs5344.jiangkan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.util.Combinations;

public class AprioriHashTable {

	public static void main(String[] args) {
		int minSupport = 800;
		ArrayList<int[]> baskets = loadBaskets("D:\\Projects\\CS5344\\baskets.txt");

		int K=0;
		HashSet<Integer> items = loadFromBaskets(baskets);
		HashMap<ItemSet, Integer> candidates = null;
		HashMap<ItemSet, Integer> frequentItems = null;
		do{
			K++;
			candidates = generateCandidates(K, frequentItems, items);
			count(K, candidates, baskets);
			frequentItems = eliminate(candidates, minSupport);
			System.out.println("K=" + K + ", " + frequentItems.size());
			output(frequentItems);
		} while (!frequentItems.isEmpty());
		
	}

	static ArrayList<int[]> loadBaskets(String fileName) {
		ArrayList<int[]> baskets = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] sa = line.split("\\s");
		    	int[] ia = new int[sa.length];
		    	for(int i=0; i<sa.length; i++){
		    		ia[i] = Integer.parseInt(sa[i]);
		    	}
		    	baskets.add(ia);
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return baskets;
	}
	
	static ArrayList<int[]> loadBasketsRandom1(String fileName, double sampleRate) {
		ArrayList<int[]> baskets = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if (Math.random()<sampleRate){
			    	String[] sa = line.split("\\s");
			    	int[] ia = new int[sa.length];
			    	for(int i=0; i<sa.length; i++){
			    		ia[i] = Integer.parseInt(sa[i]);
			    	}
			    	baskets.add(ia);
		    	}
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return baskets;
	}

	private static HashSet<Integer> loadFromBaskets(ArrayList<int[]> baskets) {
		HashSet<Integer> item = new HashSet<>();
		for(int[] ia: baskets){
			for(int i: ia){	item.add(i); }
		}
		return item;
	}

	static void output(HashMap<ItemSet, Integer> itemSets) {
		for(Map.Entry<ItemSet, Integer> entry: itemSets.entrySet()){
			System.out.println(entry.getKey() + ", " + entry.getValue());
		}
	}

	static HashMap<ItemSet, Integer> eliminate(
			HashMap<ItemSet, Integer> candidates, int minSupport) {
		HashMap<ItemSet, Integer> result = new HashMap<>();
		for(ItemSet i: candidates.keySet()){
			int count = candidates.get(i);
			if (count >= minSupport) result.put(i, count);
		}
		return result;
	}

	static void count(int k, HashMap<ItemSet, Integer> candidate, 
			ArrayList<int[]> baskets) {
		for (int[] b: baskets){
			if (b.length >= k){
				HashSet<ItemSet> s = subsets(b, k);
				for(ItemSet i: s){
					if (candidate.containsKey(i)){
						int count = candidate.get(i);
						candidate.put(i, ++count);
					}
				}
			}
		}
	}

	static HashSet<ItemSet> subsets(int[] items, int k) {
		HashSet<ItemSet> s = new HashSet<>();
		for(Iterator<int[]> iter=new Combinations(items.length, k).iterator(); 
				iter.hasNext();){
			int[] x = new int[k];
			int[] y = iter.next();
			for(int i=0; i<k; i++){
				x[i] = items[y[i]];
			}
			s.add(new ItemSet(x));
		}
		return s;
	}

	static HashMap<ItemSet, Integer> generateCandidates(int k,
			HashMap<ItemSet, Integer> frequentItems, HashSet<Integer> item) {
		if (k==1){
			HashMap<ItemSet, Integer> candidate = new HashMap<>();
			for(Integer i:item){
				int[] ia = new int[1];
				ia[0] = i;
				candidate.put(new ItemSet(ia), 0);
			}
			return candidate;
		} else {
			HashMap<ItemSet, Integer> candidate = new HashMap<>();
			for(ItemSet x: frequentItems.keySet()){
				for(Integer y: item){
					int[] xItems = x.getItems();
					int[] zItems = new int[k];
					for(int i=0; i<k-1; i++) zItems[i] = xItems[i];
					zItems[k-1] = y;
					ItemSet z = new ItemSet(zItems);
					HashSet<ItemSet> s= subsets(zItems, k-1);
					boolean allFrequent = true;
					for(ItemSet i: s){
						if (!frequentItems.keySet().contains(i)){
							allFrequent = false;
							break;
						}
					}
					if (allFrequent) {
						candidate.put(z, 0);
					} 
				}
			}
			return candidate;
		}
	}
}

class ItemSet{
	private int[] items;
	
	public ItemSet(int[] items){ 
		int[] copy = new int[items.length];
		for(int i=0; i<copy.length; i++) copy[i] = items[i];
		Arrays.sort(copy);
		this.setItems(copy); 
	}

	public int[] getItems() {
		int[] copy = new int[items.length];
		for(int i=0; i<copy.length; i++) copy[i] = items[i];
		return copy;
	}

	private void setItems(int[] items) { this.items = items; }
	
	@Override
	public int hashCode() { return Arrays.hashCode(items); }
	
	@Override
	public boolean equals(Object obj) { 
		if (!(obj instanceof ItemSet))
            return false;
        if (obj == this)
            return true;
        
		return Arrays.equals(items, ((ItemSet)obj).items);
	}

	@Override
	public String toString(){ return Arrays.toString(items); }
}
