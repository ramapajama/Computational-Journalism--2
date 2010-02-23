package article_analyzer;

import java.io.File;

public class BasicArticle {
	private String articleTitle = "";
	private String articleAuthor = "";
	private String articleURL = "";
	private String articlePolarity = "";
	private File articleFile = null;
	
	public String getArticleAuthor() {
		return articleAuthor;
	}
	public void setArticleAuthor(String articleAuthor) {
		this.articleAuthor = articleAuthor;
	}
	public String getArticlePolarity() {
		return articlePolarity;
	}
	public void setArticlePolarity(String articlePolarity) {
		this.articlePolarity = articlePolarity;
	}
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public String getArticleURL() {
		return articleURL;
	}
	public void setArticleURL(String articleURL) {
		this.articleURL = articleURL;
	}
	public File getArticleFile() {
		return articleFile;
	}
	public void setArticleFile(File articleFile) {
		this.articleFile = articleFile;
	}
	
	public String printArticleDetails () {
		String retval = "";
		
		retval += String.format("Title:    %s\n", this.getArticleTitle());
		retval += String.format("Author:   %s\n", this.getArticleAuthor());
		retval += String.format("URL:      %s\n", this.getArticleURL());
		retval += String.format("Polarity: %s\n", this.getArticlePolarity());
		
		return retval;
	}
	
	public String printArticleHTML () {
		String retval = "";
		String polarityStr = "";
		
		if (this.getArticlePolarity().equals("pos")) {
			polarityStr = "blogPositive";
		} else {
			polarityStr = "blogNegative";
		}
		
		retval += String.format("<div class=\"%s\">", polarityStr);
		retval += String.format("<h4>%s</h4>", this.getArticleAuthor());
		retval += String.format("<a href=\"%s\">", this.getArticleURL());
		retval += String.format("%s</a></div>\n", this.getArticleTitle());
		
		return retval;
	}
	
	public BasicArticle (String articleTitle, String articleAuthor, String articleURL, File articleFile) {
		this.setArticleTitle(articleTitle);
		this.setArticleAuthor(articleAuthor);
		this.setArticleURL(articleURL);
		this.setArticleFile(articleFile);
	}
}
