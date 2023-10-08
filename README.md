# search_indexer


## Overview
This project is made to be an OOTB enterprise data pipeline for processing data from multiple 
sources and to be placed into a datastream for processing.  Processors can be written in most popular languages.

The data crawled is wikipeda right now but will expand to be multiple open data sources with reliable formats.  Additonally, a web and database crawler are possible.

The data processing steps from multiple sources which perform the following:
* Retrieval - grab the data from a data source and save it to a shared storage device.  As of now it only processes wikipedia data, but additional data will be added.
* Parsing - After the data is retrieved, the data will go into a parsing step which will convert it to a plain text document for text processing.
* Enrichment - Now that the document is plain text, it will be applied to a pipeline which would execute multiple services to enrich the document.
* Sink - Once the data is fully enriched, it will go into a data sink to output to the desired service such as a search engine, vector store, or a data scientist experiementing with data.

As a PoC, we are first processing the entire set of wikipedia documents.

## Technical architecture
### Components used
* Protocol buffers - the data format of all data in the stream
* Kafka - the messaging queue 
* gRPC - the service layer for document enrichment.  Can be any gRPC service in any supported language.
* Consul - gRPC and REST service registration
* OpenJDK 17  - Base layer for project
* Micronaut - Dependency injection layer 
* There's many more, we'll add them here later

### Data flows
#### Overall data flow
![cartoon for managers](/docs/arch_diagrams/search_indexer-StreamFlow.drawio.svg)

The above demonstrates the following flow:
* Document Retrieval
* Document Parsing
* Document Enrichment 

##### Document Retrieval 
The data is sent to a repository for raw data processing.  
##### Document parsing
Take the document and parse it into plain text.
##### Document enrichment
Take the document and add some feature.

#### Wikipedia Document Retrieval
![document retrieval](/docs/arch_diagrams/search_indexer-WikiRetrieval.drawio.svg)

The document retrieval does the following steps to ensure:
* The latest version of the documents are retrieved
* if the latest version is not intended, the user can configure it to a specific dump date
* user configures how many downloads would happen simultaneously in the settings.  Wikimedia allows up to 3 at a time.
* all downloads are validated by their md5 sum
* downloads are going to be stored onto disk.  In the future s3 buckets would be supported.

##### Next document retrievals planned
* Web crawler integration such as [heritix](https://github.com/internetarchive/heritrix3) or [scrapy](https://scrapy.org/) integration
* JDBC crawler
* 311 data from various open APIs and data dumps
* IRS data
* Weather data
* Map data

#### Wiki Document parsing
The parsing only removes wiki data and adds the metadata returned from the dumps.  Current types of wiki documents from the Wikipedia dumps that are supported go as follows:
* ARTICLE 
* CATEGORY
* LIST
* DRAFT
* WIKIPEDIA
* TEMPLATE
* FILE 
* REDIRECT

#### Pipeline Processing

![Pipeline Processor Flow](/docs/arch_diagrams/search_indexer-PipelineProcessorFlow.drawio.svg)

Once the parsed document is parsed, it is cleaned up, the pipeline processing step can enhance the document by applying a pipeline step to the document.

This is a set of services all with the same gRPC interface which simply inputs a Pipeline Document and outputs the same.  The service can enhance, read, or manipulate the document through the series of pipelines.

As of now the project has two pipeline steps: a vectorizer and an NLP named entity rocognition service.  They're both a reference point as to how to create a gRPC service to enhance the document.  Further implementations of gRPC services in multiple languages are planned.

## install directions

The docker directory has the latest confluent platform. It also contains a script that automatically creates all the
needed topics for this application.

(in progress)

Search Indexer is an open source search indexer meant to create a document indexer that scales out-of-the-box. It's a
work-in-progress aimed at the following milestones:

1. *End-to-end searching of wikipedia* - complete. Automatically download and install solr search engine. Then it'll
   download all of wikipedia into solr. Finally, it'll categorize each wiki article using OpenNLP category tagger.
2. *Scale wikipedia indexing with kafka* - completed. Create the above steps using Spring cloud flow through 100% avro
   serialization/deserialization. Create a generic document interface.
3. *Create multiple search engine indices* - not started. Support for open search and an interface for other search
   engines.
4. *Allow for multiple document types to be indexed* - not started. Move away from wikipedia and allow for more generic
   input of documents.
5. *Integrate pipeline steps for search to allow for dense vector calculations* - not started.

# Changes

1. Moved to protocol buffers instead of avro. The IDL for Protocol buffers is more mature, and the OOTB nature to
   integrate it with gRPC makes this a solid choice.
2. The above chart does work
3. Protocol buffers are used as the value for all the kafka topics.
4. Taking advantage of the automatic dockerized kafka during unit testing. Added multiple tests that test the
   serialization.

# things to do

* learn more about kafka topics. About 1/2 way through some of the books, not jumping into any of the super fancy stuff
  yet.
* figure out if we will use key/value storage with KTable? I feel odd moving away from the avro binary to json,
* get a pipeline arch going which will allow for registering multiple pipeline services using gRPC. The idea is to
  create a templated request service and the end user can create the service which is "registerd" in the application yml
  of the pipeline project.

# things to cache/store outside of the pipeline

* NLP cache by revision ID key to reduce the cost of processing
* create a feature that doesn't parse the articles outside of the raw stage if it's already in the db and marked
  processed
* find a pluggable way to add enhancements to the document. probably just by tagging and a revision ID along with some
  more metadata. trying to avoid doing a SQL database and if I do, consider using the sql dumps from wikimedia. But I
  want to keep this search-centric and not use that model.
* start a document store once the document is cleaned. can just use a PipeDocument topic for now?
* once we get this into solr, create https://vespa.ai/ search engine
* also consider using weaviate

# categories listed

a ton of documents cause redirects. start seeing how we can get that data because the wikiparser doesn't seem to give
the article id when redirecting. It'll be cool to save that data because it's an amazing source of similarity stuff to
do with the dense vectors.

# What is the purpose project?

This project is made to test a large practical set of data on a search engine to practice on improving a search
experience. As of now we're only dealing with wikipedia data.

This is a maven project that's a self contained executiable made to install, hydrate, and create a full search engine
for wikipedia. The goal is to target java developers who are interested in learning more about advanced search features
in solr.

I saw that solr now has dense vector indices - which is a new way to store and rank documents but has a far deeper
learning curve than the keyword matching via BM25 algorithm that solr defaults to. I'm creating this project to quickly
index wikipedia so I can quickkly test multiple dense vector configurations.

Once I get the above completed, which should take a few months, I'd like to introduce other datasets:

* weather data
* city 311 data
* income tax data
* transportation data from major cities
* census data

Open for any suggestions.

