package article_analyzer;

public class BasicArticle {
	private String articleTitle;
	private String articleAuthor;
	private String articleURL;
	private String articlePolarity;
	
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
	
	public BasicArticle (String articleTitle, String articleAuthor, String articleURL) {
		this.setArticleTitle(articleTitle);
		this.setArticleAuthor(articleAuthor);
		this.setArticleURL(articleURL);
	}
}
