package cs5344.jiangkan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class AprioriTriangleMatrix {

	public static void main(String[] args) {
		int minSupport = 800;
		ArrayList<int[]> baskets = 
				AprioriHashTable.loadBaskets("D:\\Projects\\CS5344\\baskets.txt");
//		ArrayList<int[]> baskets = 
//				AprioriHashTable.loadBasketsRandom1("D:\\Projects\\CS5344\\baskets.txt", 0.2);

		int K=0;
		int[] itemArray = loadFromBaskets(baskets);
		Arrays.sort(itemArray);
		HashMap<ItemSet, Integer> candidates = null;
		HashMap<ItemSet, Integer> frequentItems = null;
		int[] candidate1 = null;
		int[] candidate1ReNum = null;
		TriangularMatrix candidate2 = null;
		do{
			K++;
			if (K==1){
				candidate1 = new int[itemArray.length];
				count1(candidate1, baskets, itemArray);
				System.out.println("K=" + K);
				output1(candidate1, minSupport, itemArray);
				K++;
				int num = 0;
				for (int i=0; i<candidate1.length; i++){
					if (candidate1[i] >= minSupport) { num++; }
				}
				candidate1ReNum = new int[num];
				num = 0;
				for (int i=0; i<candidate1.length; i++){
					if (candidate1[i] >= minSupport) { 
						candidate1ReNum[num++] = i;  
					}
				}
				candidate2 = new TriangularMatrix(num);
				count2(candidate2, baskets, itemArray, candidate1, 
						minSupport, candidate1ReNum);
				frequentItems = toHashTable(candidate2, minSupport, itemArray, 
						candidate1, candidate1ReNum, num);
			} else {
				HashSet<Integer> items = loadFromArray(itemArray);
				candidates = 
						AprioriHashTable.generateCandidates(K, frequentItems, items);
				AprioriHashTable.count(K, candidates, baskets);
				frequentItems = AprioriHashTable.eliminate(candidates, minSupport);
			}
			System.out.println("K=" + K + ", " + frequentItems.size());
			AprioriHashTable.output(frequentItems);			
		} while (!frequentItems.isEmpty());
		
	}

	private static HashMap<ItemSet, Integer> toHashTable(TriangularMatrix candidate2, int minSupport,
			int[] itemArray, int[] candidate1, int[] candidate1ReNum, int num) {
		HashMap<ItemSet, Integer> result = new HashMap<>();
		for(int i=0; i<num; i++){
			for(int j=i+1; j<num; j++){
				if (candidate2.get(i, j)>=minSupport){
					int[] items = new int[2];
					items[0] = itemArray[candidate1ReNum[i]];
					items[1] = itemArray[candidate1ReNum[j]];
					result.put(new ItemSet(items), candidate2.get(i, j));
				}
			}
		}
		return result;
	}

	private static void count2(TriangularMatrix candidate2,
			ArrayList<int[]> baskets, int[] itemArray, 
			int[] candidate1, int minSupport, int[] candidate1ReNum) {
		for (int[] b: baskets){
			if (b.length >= 2){
				HashSet<ItemSet> s = AprioriHashTable.subsets(b, 2);
				for(ItemSet i: s){
					int[] ia = i.getItems();
					int i1 = Arrays.binarySearch(itemArray, ia[0]);
					int i2 = Arrays.binarySearch(itemArray, ia[1]);
					int i3 = -1;
					int i4 = -1;
					if (candidate1[i1]>=minSupport){
						i3 = Arrays.binarySearch(candidate1ReNum, i1);
					}
					if (candidate1[i2]>=minSupport){
						i4 = Arrays.binarySearch(candidate1ReNum, i2);
					}
					if (i3!=-1 && i4!=-1){
						int count = candidate2.get(i3, i4);
						candidate2.set(i3, i4, ++count);
					}
				}
			}
		}
	}

	public static void output1(int[] candidate1, int minSupport, int[] itemArray) {
		for(int i=0; i<candidate1.length; i++){
			if (candidate1[i]>=minSupport){ 
				System.out.println("["+itemArray[i]+"], " + candidate1[i]);
			}
		}
	}

	private static void count1(int[] candidate1, ArrayList<int[]> baskets, int[] itemArray) {
		for (int[] b: baskets){
			for(int i: b){
				candidate1[Arrays.binarySearch(itemArray, i)]++;
			}
		}
	}

	public static HashSet<Integer> loadFromArray(int[] itemArray) {
		HashSet<Integer> item = new HashSet<>();
		for(int i: itemArray){	item.add(i); }
		return item;
	}

	public static int[] loadFromBaskets(ArrayList<int[]> baskets) {
		HashSet<Integer> item = new HashSet<>();
		for(int[] ia: baskets){
			for(int i: ia){	item.add(i); }
		}
		int[] result = new int[item.size()];
		int i = 0;
		for(int x: item){ result[i++] = x; }
		return result;
	}
}

class TriangularMatrix{
	private int n;
	private int[] raw;
	public TriangularMatrix(int size){
		n = size;
		raw = new int[n * (n-1) / 2];
	}

	// formula is taken from http://stackoverflow.com/questions/27086195/linear-index-upper-triangular-matrix
	public int get(int i, int j){ 
		if (i>j) { int t=j; j=i; i=t; }
		return raw[(n*(n-1)/2) - (n-i)*((n-i)-1)/2 + j - i - 1];
	}
	
	// formula is taken from http://stackoverflow.com/questions/27086195/linear-index-upper-triangular-matrix
	public void set(int i, int j, int v){
		if (i>j) { int t=j; j=i; i=t; }
		raw[(n*(n-1)/2) - (n-i)*((n-i)-1)/2 + j - i - 1] = v;
	}
	
	public String toString(){
		return "n=" + n + ", " + Arrays.toString(raw);
	}
}
