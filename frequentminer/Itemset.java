package frequentminer;

import java.util.Arrays;

public class Itemset {
	public int k;
	public int[] item;
	
	public Itemset(int[] newItems) {
		this.k = newItems.length;
		this.item = newItems;
	}
	
	public int getItem(int i) {
		return item[i];
	}
	
	public String toString() {
		String str = k + "-itemset: (";
		for(int i = 0; i < k-1; i++) {
			str = str + item[i] + ", ";
		}
		str = str + item[k-1] + ")";
		return str;
	}
	
	public int[] getItems() {
		return item;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(item);
		result = prime * result + k;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Itemset other = (Itemset) obj;
		if (!Arrays.equals(item, other.item))
			return false;
		if (k != other.k)
			return false;
		return true;
	}
}