package article_analyzer;

public class Keyword implements Comparable{
	private String keyword = "";
	private int count = 0;
	private String type = "";
	
	public void incCount () {
		this.count++;
	}
	
	public int getCount () {
		return this.count;
	}
	
	public String getKeyword () {
		return this.keyword;
	}
	
	public String getType () {
		return this.type;
	}
	
	public int compareTo(Object o) {
		int retval = 0;
		
		if (o instanceof Keyword) {
			Keyword toCompare = (Keyword) o;
			
			retval = (this.getCount() - toCompare.getCount()) * -1; //Orders items in descending order
		}
		
		return retval;
	}
	
	public Keyword (String keyword, String type) {
		this.keyword = keyword;
		this.type = type;
		this.count = 1;
	}
}
