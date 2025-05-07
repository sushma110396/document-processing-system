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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

@Service
public class LuceneService {

    private final Path indexPath = Paths.get("lucene-index");

    // Custom analyzer with NGramTokenizer (3 to 5 chars)
    private final Analyzer analyzer = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer tokenizer = new NGramTokenizer(3, 5);
            TokenStream tokenStream = new LowerCaseFilter(tokenizer);
            return new TokenStreamComponents(tokenizer, tokenStream);
        }
    };

    public void indexDocument(String id, String name, String type, String extractedText, String userId) throws IOException {
        try (Directory directory = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {

            Document doc = new Document();
            doc.add(new StringField("id", id, Field.Store.YES));
            doc.add(new StringField("userId", userId, Field.Store.YES));
            doc.add(new StringField("type", type, Field.Store.YES));
            doc.add(new TextField("name", name, Field.Store.YES));
            doc.add(new TextField("extractedText", extractedText, Field.Store.YES));

            writer.addDocument(doc);
            System.out.println("Document indexed with ID: " + id);
        }
    }

    public List<SearchResult> fuzzySearch(String queryStr, String userId) throws IOException {
        List<SearchResult> results = new ArrayList<>();

        try (Directory directory = FSDirectory.open(indexPath);
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            // Build query for user filter
            Query userQuery = new TermQuery(new Term("userId", userId));

            // Generate 3- to 5-grams from the query string
            List<String> ngrams = generateNGrams(queryStr.toLowerCase(), 3, 5);

            // Build SHOULD clause for matching any of the n-grams
            BooleanQuery.Builder contentQueryBuilder = new BooleanQuery.Builder();
            for (String gram : ngrams) {
                contentQueryBuilder.add(new TermQuery(new Term("name", gram)), BooleanClause.Occur.SHOULD);
                contentQueryBuilder.add(new TermQuery(new Term("extractedText", gram)), BooleanClause.Occur.SHOULD);
            }

            // Combine with user filter
            BooleanQuery finalQuery = new BooleanQuery.Builder()
                    .add(userQuery, BooleanClause.Occur.MUST)
                    .add(contentQueryBuilder.build(), BooleanClause.Occur.MUST)
                    .build();

            System.out.println("Executing Lucene query: " + finalQuery);

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
