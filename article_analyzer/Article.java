package article_analyzer;

import java.io.File;
import java.util.Vector;

public class Article extends BasicArticle {
	private static int HEADLINE = 0;
	private static int POLITICS = 1;
	private static int BUSINESS = 2;
	private static int TECHNOLOGY = 3;
	private static int HEALTH = 4;
	
	private File articleFile;
	private String articleHook;
	private int articleType;
	private String published;
	private Vector<Keyword> keywords;
	
	//Accessors & Modifiers
	public String getArticleHook() {
		return articleHook;
	}
	
	public void setArticleHook(String articleHook) {
		this.articleHook = articleHook;
	}
	
	public String getPublished() {
		return published;
	}
	
	public void setPublished(String published) {
		this.published = published;
	}
	
	public int getArticleType() {
		return articleType;
	}
	
	public void setArticleFile(File articleFile) {
		this.articleFile = articleFile;
	}
	
	public File getArticleFile() {
		return this.articleFile;
	}
	
	public void addKeyword(Keyword keyword) {
		this.keywords.add(keyword);
	}
	
	public Vector<Keyword> getKeywords() {
		return this.keywords;
	}
	
	public void setArticleType(String typeString) {
		if (typeString != null) {
			String[] tokens = typeString.split("_");
			
			if (tokens.length == 3) {
				String type = tokens[1];
				
				if (type.equals("HEADLINE")) {
					this.articleType = HEADLINE;
				} else if (type.equals("BUSINESS")) {
					this.articleType = BUSINESS;
				} else if (type.equals("TECHNOLOGY")) {
					this.articleType = TECHNOLOGY;
				} else if (type.equals("HEALTH")) {
					this.articleType = HEALTH;
				}
			}
		}
	}
	
	public String printArticleDetails () {
		String retval = "";
		Keyword currentKeyword = null;
		
		retval += String.format("Title:    %s\n", this.getArticleTitle());
		retval += String.format("Author:   %s\n", this.getArticleAuthor());
		retval += String.format("Hook:     %s\n", this.getArticleHook());
		retval += String.format("Date:     %s\n", this.getPublished());
		retval += String.format("URL:      %s\n", this.getArticleURL());
		retval += String.format("Type:     %d\n", this.getArticleType());
		retval += String.format("Polarity: %s\n", this.getArticlePolarity());
		retval += "Keywords:\n";
		
		for (int i = 0; i < this.getKeywords().size(); i++) {
			currentKeyword = this.getKeywords().get(i);
			retval += String.format("Keyword: %s Type: %s\n", currentKeyword.getKeyword(), currentKeyword.getType());
		}
		
		return retval;
	}
	
	//Constructor
	public Article (String articleTitle, String articleAuthor, String articleURL, String hook, String date, String typeString, File articleFile) {
		super(articleTitle, articleAuthor, articleURL);
		this.setArticleHook(hook);
		this.setPublished(date);
		this.setArticleType(typeString);
		this.setArticleFile(articleFile);
		this.keywords = new Vector<Keyword>();
	}
}
