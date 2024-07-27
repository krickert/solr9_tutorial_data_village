package com.krickert.search.indexer.enhancers;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import java.util.*;

public class CustomChunker implements Chunker {
    StanfordCoreNLP pipeline;
    int chunkSize;

    public CustomChunker(int chunkSize) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit");
        this.pipeline = new StanfordCoreNLP(props);
        this.chunkSize = chunkSize;
    }

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            String chunk = "";
            for (String word: sentence.toString().split("\\s+")) {
                if (chunk.length() + word.length() > chunkSize) {
                    chunks.add(chunk.trim());
                    chunk = "";
                }
                chunk = chunk + " " + word;
            }
            chunks.add(chunk.trim());
        }

        return chunks;
    }

}