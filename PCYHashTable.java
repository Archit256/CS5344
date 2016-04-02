package cs5344.jiangkan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.Combinations;

public class PCYHashTable {

	public static void main(String[] args) {
		int minSupport = 800;
		int numBuckets = 10000000;
		List<HashFunction> hashFunctions = new ArrayList<>();
		hashFunctions.add(new HashFunction1(numBuckets));
		hashFunctions.add(new HashFunction2(numBuckets));
		List<int[]> hashResults = new ArrayList<>();
		List<BitSet> hashBitMap = new ArrayList<>();
		
		ArrayList<int[]> baskets = 
				AprioriHashTable.loadBaskets("D:\\Projects\\CS5344\\baskets.txt");

		int K=0;
		int[] itemArray = AprioriTriangleMatrix.loadFromBaskets(baskets);
		Arrays.sort(itemArray);
		HashMap<ItemSet, Integer> candidates = null;
		HashMap<ItemSet, Integer> frequentItems = null;
		int[] candidate1 = null;
		int[] candidate1ReNum = null;
		HashMap<ItemSet, Integer> candidate2 = null;
		do{
			K++;
			if (K==1){
				candidate1 = new int[itemArray.length];
				for(HashFunction f: hashFunctions){
					hashResults.add(new int[numBuckets]);
				}
				count1(candidate1, baskets, itemArray, hashFunctions, hashResults);
				System.out.println("K=" + K);
				AprioriTriangleMatrix.output1(candidate1, minSupport, itemArray);
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
				for(int[] ia: hashResults){
					BitSet b = new BitSet(ia.length);
					for(int i=0; i<ia.length; i++){
						if (ia[i] >= minSupport){
							b.set(i, true);
						} else {
							b.set(i, false);
						}
					}
					hashBitMap.add(b);
				}
				candidate2 = generateCandidates2(candidate1ReNum, hashFunctions, hashBitMap, itemArray);
				AprioriHashTable.count(2, candidate2, baskets);
				frequentItems = AprioriHashTable.eliminate(candidate2, minSupport);
			} else {
				HashSet<Integer> items = AprioriTriangleMatrix.loadFromArray(itemArray);
				candidates = 
						AprioriHashTable.generateCandidates(K, frequentItems, items);
				AprioriHashTable.count(K, candidates, baskets);
				frequentItems = AprioriHashTable.eliminate(candidates, minSupport);
			}
			System.out.println("K=" + K + ", " + frequentItems.size());
			AprioriHashTable.output(frequentItems);			
		} while (!frequentItems.isEmpty());
	}

	private static HashMap<ItemSet, Integer> generateCandidates2(
			int[] candidate1ReNum, List<HashFunction> hashFunctions, 
			List<BitSet> hashBitMap, int[] itemArray) {
		HashMap<ItemSet, Integer> candidate = new HashMap<>();
		HashSet<ItemSet> s = AprioriHashTable.subsets(candidate1ReNum, 2);
		for(ItemSet is: s){
			int[] ia = is.getItems();
			int[] ib = new int[2];
			ib[0] = itemArray[ia[0]];
			ib[1] = itemArray[ia[1]];
			ItemSet it = new ItemSet(ib);

			boolean allBitMapTrue = true;
			int j = 0;
			for(HashFunction f: hashFunctions){
				BitSet b = hashBitMap.get(j++);
				if (!b.get(f.calcHash(it))){
					allBitMapTrue = false;
					break;
				}
			}
			if (allBitMapTrue) {
				candidate.put(it, 0);
			}
		}
		return candidate;
	}

	private static void count1(int[] candidate1, ArrayList<int[]> baskets,
			int[] itemArray, List<HashFunction> hashFunctions,
			List<int[]> hashResults) {
		for (int[] b: baskets){
			for(int i: b){
				candidate1[Arrays.binarySearch(itemArray, i)]++;
			}
			if (b.length >= 2){
				HashSet<ItemSet> s = AprioriHashTable.subsets(b, 2);
				for(ItemSet i: s){
					int j = 0;
					for(HashFunction f: hashFunctions){
						int[] r = hashResults.get(j++);
						int h = f.calcHash(i);
						int count = r[h];
						r[h] = ++count;
					}
				}
			}
		}
	}
}

abstract class HashFunction{
	int numBuckets;
	abstract public int calcHash(ItemSet is);
	public HashFunction(int numBuckets){
		this.numBuckets = numBuckets;
	}
}

class HashFunction1 extends HashFunction{
	public HashFunction1(int numBuckets) {
		super(numBuckets);
	}

	public int calcHash(ItemSet is){
		int[] items = is.getItems();
		return (items[0] * 17 + items[1]) % numBuckets;
	}
}

class HashFunction2 extends HashFunction{
	public HashFunction2(int numBuckets) {
		super(numBuckets);
	}

	public int calcHash(ItemSet is){
		int[] items = is.getItems();
		return (items[0] * 19 + items[1]) % numBuckets;
	}
}