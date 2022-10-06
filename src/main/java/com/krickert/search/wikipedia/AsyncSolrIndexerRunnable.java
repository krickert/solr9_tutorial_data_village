package com.krickert.search.wikipedia;

import com.google.common.collect.Lists;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSolrIndexerRunnable implements Runnable {

    private final String collection;
    private final Logger logger = LoggerFactory.getLogger(AsyncSolrIndexerRunnable.class);
    private final BlockingQueue<SolrInputDocument> documents;
    private final AtomicBoolean keepListeningBoolean = new AtomicBoolean(true);

    public AsyncSolrIndexerRunnable(BlockingQueue<SolrInputDocument> documents, String collection) {
        this.documents = documents;
        this.collection = collection;
    }

    public void stopListening() {
        keepListeningBoolean.set(false);
    }

    @Override
    public void run() {
        insertSolrDocs();
    }

    public void insertSolrDocs() {
        Collection<SolrInputDocument> docs = Lists.newArrayListWithExpectedSize(22000);
        while (keepListeningBoolean.get()) {
            try {
                SolrInputDocument doc = documents.poll(5, TimeUnit.SECONDS);
                if (doc != null) {
                    docs.add(doc);
                    if (docs.size() >= 20000) {
                        addDocsToSolr(docs);
                    }
                } else {
                    if (docs.size() >= 1) {
                        addDocsToSolr(docs);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("this shouldn't happen", e);
                break;
            }
        }
        //add the rest
        logger.info("completed document queue.  finishing last docs");
        addDocsToSolr(documents);
    }

    private void addDocsToSolr(Collection<SolrInputDocument> docs) {
        if (CollectionUtils.isEmpty(docs)) {
            logger.info("no additional docs to add");
            return;
        }

        try (SolrClient client = (new Http2SolrClient.Builder("http://localhost:8983/solr/")).build()) {
            client.add(collection, docs);
            client.commit(collection);
            docs.clear();
        } catch (SolrServerException | IOException e) {
            logger.error("solr add and commit threw exception", e);
        }
    }
}
