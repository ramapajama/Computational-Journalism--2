package article_analyzer;

import com.aliasi.*;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.classify.*;
import com.aliasi.lm.*;
import com.aliasi.util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Processor {
	private Vector<Article> articles = null;
	private File articlesDir = null;
	private static int ARTICLE_TITLE = 2;
	private static int ARTICLE_AUTHOR = 3;
	private static int ARTICLE_HOOK = 5;
	private static int ARTICLE_DATE = 4;
	private static int ARTICLE_URL = 10;
	
	//Polarity vars
	private File trainingFiles = null;
	private String[] polarityCategories = null;
	private DynamicLMClassifier<NGramProcessLM> polarityClassifier = null;
	
	private String fileReader (File fileToRead) {
		String retval = "";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileToRead));
			String readLine = null;
			
			while ((readLine = br.readLine()) != null) {
				retval += readLine;
				retval += "\n";
			}
			
			br.close();
		} catch (FileNotFoundException fnfe) {
			retval = null;
		} catch (IOException ioe) {
			retval = null;
		}
			
		return retval;
	}
	
	private void initalizeArticles (String articlesDirName) {
		System.out.println("Extracting article information");
		
		articlesDir = new File(articlesDirName);
		File[] articleDirs = articlesDir.listFiles();
		
		if (articleDirs != null) {
			for (int dirIndex = 0; dirIndex < articleDirs.length; dirIndex++) {
				File[] articleFiles = articleDirs[dirIndex].listFiles();
				
				if (articleFiles != null) {
					Article mainArticle = null;
					Vector<BasicArticle> blogs = new Vector<BasicArticle>();
					
					for (int i = 0; i < articleFiles.length; i++) {
						String articleText = this.fileReader(articleFiles[i]);
						
						if (articleText != null) {
							String[] sentences = articleText.split("\n");
						
							if (sentences.length > 5) {
								if (articleFiles[i].getName().contains("HEADLINE")) {
									mainArticle = new Article(sentences[ARTICLE_TITLE], sentences[ARTICLE_AUTHOR], sentences[ARTICLE_URL], sentences[ARTICLE_HOOK], sentences[ARTICLE_DATE], articleFiles[i].getName(), articleFiles[i]);
									articles.add(mainArticle);
								} else {
									blogs.add(new BasicArticle(sentences[ARTICLE_TITLE], sentences[ARTICLE_AUTHOR], sentences[ARTICLE_URL], articleFiles[i]));
								}
							}
						}
					}
					
					if (mainArticle != null) {
						for (int j = 0; j < blogs.size(); j++) {
							mainArticle.addBlog(blogs.get(j));
						}
					}
				}
			}
		} else {
			System.err.println("Exiting - no article data to read");
			System.exit(1);
		}
	}//end initializeArticles (String)
	
	private void printArticles () {
		for (int i = 0; i < articles.size(); i++) {
			System.out.print(articles.get(i).printArticleDetails());
		}
	}
	
	private String generateHTML () {
		String retval = "";
		
		//Header code
		retval += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";
		retval += "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n";
		retval += "<head><title>At a Glance</title>\n";
		retval += "<style type=\"text/css\" media=\"all\">\n";
		retval += "@import \"main.css\";\n";
		retval += "@import \"home.css\";\n";
		retval += "</style>\n";
		retval += "</head>\n";
		retval += "<body>\n";
		retval += "<div class=\"container\">\n";
		retval += "<div class=\"header\">\n";
		retval += "<h1>New York Times</h1>\n";
		retval += "<h2>Headlines RSS Feed</h2>\n";
		retval += "</div>\n";
		retval += "<div class=\"content\">\n";
		retval += "<h1>Headlines</h1>\n";
		
		//Article code
		for (int i = 0; i < this.articles.size(); i++) {
			retval += this.articles.get(i).printArticleHTML();
			retval += "\n";
		}
		
		//Footer code
		retval += "</div><!--content-->\n";
		retval += "<div class=\"footer\"/>\n";
		retval += " </div><!--container-->\n";
		retval += "</body>\n";
		
		return retval;
	}
	
	private void printArticlesToHTML (String htmlText) {
		System.out.println("Generating index.html");
		
		File resultingHTML = new File("index.html");
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(resultingHTML));
			bw.write(htmlText);
			bw.close();
		} catch (IOException ioe) {
			System.err.println("Cannot write to output file");
		}
	}
	
	//Polarity methods
	private void trainPolarityClassifier (String trainingDir) {
		trainingFiles = new File(trainingDir, "txt_sentoken");
		polarityCategories = trainingFiles.list();
		int nGram = 8;
		polarityClassifier = DynamicLMClassifier.createNGramProcess(polarityCategories, nGram);
		
		System.out.println("Training polarity classifier");
		
		for (int i = 0; i < polarityCategories.length; i++) {
			String category = polarityCategories[i];
			File categoryDir = new File(trainingFiles, category);
			File[] filesToTrain = categoryDir.listFiles();
			
			for (int j = 0; j < filesToTrain.length; j++) {
				File fileToTrain = filesToTrain[j];
				String review = this.fileReader(fileToTrain);
				
				if (review != null) {
					polarityClassifier.train(category, review);
				}
			}
		}
	}
	
	private String calculatePolarity (BasicArticle articleToProcess) {
		String articleText = this.fileReader(articleToProcess.getArticleFile());
		Classification classification = this.polarityClassifier.classify(articleText);
		
		return classification.bestCategory();
	}
	
	private void evaluateArticlePolarity () {
		System.out.println("Evaluating article polarity");
		
		Article currentArticle = null;
		BasicArticle currentBlog = null;
		
		for (int i = 0; i < articles.size(); i++) {
			currentArticle = articles.get(i);
			
			currentArticle.setArticlePolarity(this.calculatePolarity(currentArticle));
			
			for (int j = 0; j < currentArticle.getBlogs().size(); j++) {
				currentBlog = currentArticle.getBlogs().get(j);
				
				currentBlog.setArticlePolarity(this.calculatePolarity(currentBlog));
			}
		}
	}
	
	//Extract keywords
	private void addTopTenKeywords (TreeSet<Keyword> orderedKeywords, Article currentArticle) {
		Iterator<Keyword> keywords = orderedKeywords.iterator();
		int i = 0;
		
		while ((keywords.hasNext()) && (i < 10)) {
			currentArticle.addKeyword(keywords.next());
			i++;
		}
	}
	
	private void extractArticleEntities (String serializedObject) {
		System.out.println("Extracting article keywords");
		
		Article currentArticle = null;
		Chunker entityChunker = null;
		File serializedFile = new File(serializedObject);
		
		try {
			entityChunker = (Chunker) AbstractExternalizable.readObject(serializedFile);
		} catch (IOException ioe) {
			System.err.printf("Error opening serialized file: %s\n", serializedFile.getName());
		} catch (ClassNotFoundException cnfe) {
			System.err.printf("Class not found for file: %s\n", serializedFile.getName());
		}
		
		for (int i = 0; i < articles.size(); i++) {
			currentArticle = articles.get(i);
			String articleText = this.fileReader(currentArticle.getArticleFile());
			Chunking currentChunking = entityChunker.chunk(articleText);
			Set<Chunk> chunks = currentChunking.chunkSet();
			HashMap<String, Keyword> entities = new HashMap<String, Keyword>();
			Iterator<Chunk> chunksIterator = chunks.iterator();
			
			while (chunksIterator.hasNext()) {
				Chunk currentChunk = chunksIterator.next();
				int entityStart = currentChunk.start();
				int entityEnd = currentChunk.end();
				String entity = articleText.substring(entityStart, entityEnd);
				
				if (entities.containsKey(entity)) {
					entities.get(entity).incCount();
				} else {
					entities.put(entity, new Keyword(entity, currentChunk.type()));
				}
			}
			
			Collection<Keyword> byCount = entities.values();
			this.addTopTenKeywords(new TreeSet<Keyword>(byCount), currentArticle);
		}
	}
	
	public Processor () {
		articles = new Vector<Article>();
	}
	
	/**
	 * Main method
	 * 
	 * @param args arg[0] is the polarity training directory, arg[1] is the entity extractor, arg[2] is the article directory
	 */
	public static void main (String[] args) {
		Processor p = new Processor();
		
		p.trainPolarityClassifier(args[0]);
		p.initalizeArticles(args[2]);
		p.evaluateArticlePolarity();
		p.extractArticleEntities(args[1]);
		p.printArticlesToHTML(p.generateHTML());
		
		System.out.println("Finished");
	}
}
