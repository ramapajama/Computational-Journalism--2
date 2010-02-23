package article_analyzer;

import com.aliasi.*;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.classify.*;
import com.aliasi.lm.*;
import com.aliasi.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Processor {
	private Vector<Article> articles = null;
	private File articleDir = null;
	private File[] articleFiles = null;
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
		} catch (FileNotFoundException fnfe) {
			retval = null;
		} catch (IOException ioe) {
			retval = null;
		}
			
		return retval;
	}
	
	private void initalizeArticles (String articleDirName) {
		System.out.println(articleDirName);
		articleDir = new File(articleDirName);
		System.err.println(articleDir.isDirectory());
		articleFiles = articleDir.listFiles();
		
		if (articleFiles != null) {
			for (int i = 0; i < articleFiles.length; i++) {
				String articleText = this.fileReader(articleFiles[i]);
			
				if (articleText != null) {
					String[] sentences = articleText.split("\n");
				
					if (sentences.length > 5) {
						Article articleToCreate = new Article(sentences[ARTICLE_TITLE], sentences[ARTICLE_AUTHOR], sentences[ARTICLE_URL], sentences[ARTICLE_HOOK], sentences[ARTICLE_DATE], articleFiles[i].getName(), articleFiles[i]);
						articles.add(articleToCreate);
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
	
	//Polarity methods
	private void trainPolarityClassifier (String trainingDir) {
		trainingFiles = new File(trainingDir, "txt_sentoken");
		polarityCategories = trainingFiles.list();
		int nGram = 8;
		polarityClassifier = DynamicLMClassifier.createNGramProcess(polarityCategories, nGram);
		
		for (int i = 0; i < polarityCategories.length; i++) {
			String category = polarityCategories[i];
			File categoryDir = new File(trainingFiles, category);
			File[] filesToTrain = categoryDir.listFiles();
				
			for (int j = 0; j < filesToTrain.length; j++) {
				File fileToTrain = filesToTrain[j];
				String review = this.fileReader(fileToTrain);
				System.out.printf("Training file: %s\n", fileToTrain.getName());
				if (review != null) {
					polarityClassifier.train(category, review);
				}
			}
		}
	}
	
	private void evaluateArticlePolarity () {
		Article currentArticle = null;
		
		for (int i = 0; i < articles.size(); i++) {
			currentArticle = articles.get(i);
			
			String review = this.fileReader(currentArticle.getArticleFile());
			Classification classification = polarityClassifier.classify(review);
			String resultCategory = classification.bestCategory();
			currentArticle.setArticlePolarity(resultCategory);
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
	 * @param args arg[0] is the polarity training directory, arg[1] is the article directory
	 */
	public static void main (String[] args) {
		Processor p = new Processor();
		
		p.trainPolarityClassifier(args[0]);
		p.initalizeArticles(args[2]);
		p.evaluateArticlePolarity();
		p.extractArticleEntities(args[1]);
		p.printArticles();
	}
}
