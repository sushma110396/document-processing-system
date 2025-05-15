package io.documentprocessing.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.documentprocessing.model.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

@Service
public class LuceneService {

    private final Path indexPath = Paths.get("lucene-index");

    // Custom analyzer with NGramTokenizer (2 to 15 characters)
    private final Analyzer analyzer = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer tokenizer = new NGramTokenizer(2, 15);
            TokenStream tokenStream = new LowerCaseFilter(tokenizer);
            return new TokenStreamComponents(tokenizer, tokenStream);
        }
    };

    // Index document
    public void indexDocument(String id, String name, String type, String extractedText, String userId) throws IOException {
        try (Directory directory = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
        	
            Document doc = new Document();
            doc.add(new StringField("id", id, Field.Store.YES));
            doc.add(new StringField("userId", userId, Field.Store.YES));
            doc.add(new StringField("type", type, Field.Store.YES));
            String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            System.out.println("Indexing: name=" + baseName.toLowerCase() + ", userId=" + userId); 
            doc.add(new TextField("name", baseName.toLowerCase(), Field.Store.YES));
            doc.add(new TextField("extractedText", extractedText.toLowerCase(), Field.Store.YES));

            writer.addDocument(doc);
        }
    }

    public List<SearchResult> searchByName(String name, String userId) throws IOException {
        List<SearchResult> results = new ArrayList<>();
        Directory directory = FSDirectory.open(indexPath);

        if (!DirectoryReader.indexExists(directory)) {
            System.out.println("Lucene index not found yet — returning empty result.");
            return results;
        }

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            String queryName = baseName.toLowerCase();

            Query userQuery = new TermQuery(new Term("userId", userId));
            Query exactNameQuery = new TermQuery(new Term("name", queryName));

            BooleanQuery exactQuery = new BooleanQuery.Builder()
                    .add(userQuery, BooleanClause.Occur.MUST)
                    .add(exactNameQuery, BooleanClause.Occur.MUST)
                    .build();

            System.out.println("Trying exact match for: " + queryName);

            TopDocs topDocs = searcher.search(exactQuery, 10);

            // If no exact results, fall back to fuzzy
            if (topDocs.totalHits.value == 0) {
                System.out.println("No exact match found, falling back to fuzzy search...");
                Query fuzzyNameQuery = new FuzzyQuery(new Term("name", queryName), 2); // edit distance = 2

                BooleanQuery fuzzyQuery = new BooleanQuery.Builder()
                        .add(userQuery, BooleanClause.Occur.MUST)
                        .add(fuzzyNameQuery, BooleanClause.Occur.MUST)
                        .build();

                topDocs = searcher.search(fuzzyQuery, 10);
            }

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                results.add(new SearchResult(
                        doc.get("id"),
                        doc.get("name"),
                        doc.get("type"),
                        doc.get("extractedText")
                ));
            }
        }

        return results;
    }
    
    public List<SearchResult> fuzzySearch(String queryStr, String userId) throws IOException {
        List<SearchResult> results = new ArrayList<>();
        Directory directory = FSDirectory.open(indexPath);

        // Prevent crash if index doesn't exist
        if (!DirectoryReader.indexExists(directory)) {
            System.out.println("Lucene index not found yet — returning empty result.");
            return results;
        }

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            Query userQuery = new TermQuery(new Term("userId", userId));

            List<String> ngrams = generateNGrams(queryStr.toLowerCase(), 2, 15);
            BooleanQuery.Builder contentQueryBuilder = new BooleanQuery.Builder();

            for (String gram : ngrams) {
                contentQueryBuilder.add(new TermQuery(new Term("name", gram)), BooleanClause.Occur.SHOULD);
                contentQueryBuilder.add(new TermQuery(new Term("extractedText", gram)), BooleanClause.Occur.SHOULD);
            }

            BooleanQuery finalQuery = new BooleanQuery.Builder()
                    .add(userQuery, BooleanClause.Occur.MUST)
                    .add(contentQueryBuilder.build(), BooleanClause.Occur.MUST)
                    .build();

            TopDocs topDocs = searcher.search(finalQuery, 10);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                results.add(new SearchResult(
                        doc.get("id"),
                        doc.get("name"),
                        doc.get("type"),
                        doc.get("extractedText")
                ));
            }
        }

        return results;
    }


    private List<String> generateNGrams(String input, int minGram, int maxGram) {
        List<String> ngrams = new ArrayList<>();
        int length = input.length();

        for (int n = minGram; n <= maxGram; n++) {
            for (int i = 0; i <= length - n; i++) {
                ngrams.add(input.substring(i, i + n));
            }
        }
        return ngrams;
    }
}
