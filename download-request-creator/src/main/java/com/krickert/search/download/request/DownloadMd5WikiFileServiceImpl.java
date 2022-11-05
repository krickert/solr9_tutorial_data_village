package com.krickert.search.download.request;

import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static io.micronaut.http.HttpRequest.GET;

@Singleton
public class DownloadMd5WikiFileServiceImpl implements DownloadMd5WikiFileService {
    private static final Logger log = LoggerFactory.getLogger(DownloadMd5WikiFileServiceImpl.class);

    final String wikipediaMd5UrlString;
    final String wikipediaPrefixUrl;
    final String wikiDownloadName;
    final Rx3HttpClient client;
    final ResourceLoader resourceLoader;

    @Inject
    public DownloadMd5WikiFileServiceImpl(
            @Value("${wikipedia.md5-url}")
            String wikipediaMd5UrlString,
            @Value("${wikipedia.prefix-url}")
            String wikipediaPrefixUrl,
            @Value("${wikipedia.download-name}")
            String wikiDownloadName,
            @Client("${download.request-url}")
            Rx3HttpClient client,
            ResourceLoader resourceLoader) {
        this.wikipediaPrefixUrl = wikipediaPrefixUrl;
        this.wikipediaMd5UrlString = wikipediaMd5UrlString;
        this.wikiDownloadName = wikiDownloadName;
        this.client = client;
        this.resourceLoader = resourceLoader;
    }


    @Override
    public String downloadWikiMd5AsString(String fileList) {
        if (fileList != null) {
            try {
                ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
                Optional<URL> resource = loader.getResource("classpath:" + fileList);
                if (resource.isPresent()) {
                    return FileUtils.readFileToString(new File(resource.get().getFile()), Charset.defaultCharset());
                }
            } catch (NoSuchElementException | IOException e) {
                log.debug("element not found in classpath", e);
            }
            FileSystemResourceLoader loader = new ResourceResolver().getLoader(FileSystemResourceLoader.class).get();
            Optional<URL> resource = loader.getResource("file:" + fileList);
            log.debug("did we find the file? {}", resource.isPresent());
            try {
                return FileUtils.readFileToString(new File(resource.get().getFile()), Charset.defaultCharset());
            } catch (NoSuchElementException | IOException e) {
                log.error("File was specified: [{}] but does not exist in classpath or as a file.", fileList);
                throw new RuntimeException("File was not found: " + fileList, e);
            }
        }
        File theFile = new File(wikiDownloadName);
        if (FileUtils.isRegularFile(theFile)) {
            //check to see if the file exists
            try {
                return FileUtils.readFileToString(theFile, Charset.defaultCharset());
            } catch (IOException e) {
                log.warn("Md5 file is in config but not found.  Downloading file instead");
            }
        }
        if (StringUtils.isEmpty(wikiDownloadName)) {
            throw new RuntimeException("file name to download not specified.  Please use -f [FILE_NAME] or set the wikipedia.download-name configuration.");
        }
        String m = client.retrieve(
                GET("enwiki-latest-md5sums.txt"),
                String.class).blockingFirst();
        return m;
    }

    @Override
    public Collection<String[]> parseFileList(String m) {
        Collection<String[]> fileList = parseFileList(m, WIKI_FILE_TYPE.MULTISTREAM);
        if (CollectionUtils.isEmpty(fileList)) {
            fileList = parseFileList(m, WIKI_FILE_TYPE.ARTICLE);
        }
        if (CollectionUtils.isEmpty(fileList)) {
            log.error("no valid files found.  Exiting.");
            System.exit(-1);
        }
        return fileList;
    }

    @Override
    public Collection<String[]> parseFileList(String m, WIKI_FILE_TYPE type) {
        Collection<String[]> filesToDownload = new ArrayList<>();
        String[] lines = m.split("\n");
        String[] data;
        for (String line:lines) {
            data = line.split(" {2}");
            String fileName = data[1];
            if (isFileType(fileName, type)) {
                filesToDownload.add(data);
            }
        }
        return filesToDownload;
    }

    private boolean isFileType(String fileName, WIKI_FILE_TYPE fileType) {
        if (fileType == WIKI_FILE_TYPE.MULTISTREAM) {
            return isMultiStreamArticleFile(fileName);
        } else if (fileType == WIKI_FILE_TYPE.ARTICLE) {
            return isArticleFile(fileName);
        } else {
            return false;
        }
    }

    private static boolean isArticleFile(String fileName) {
        return fileName.contains("pages-article") &&
                !fileName.contains("pages-articles.xml.bz2");
    }


    public enum WIKI_FILE_TYPE {
        MULTISTREAM, ARTICLE
    }

    private static boolean isMultiStreamArticleFile(String fileName) {
        return fileName.contains("pages-articles-multistream") &&
                !fileName.contains("pages-articles-multistream.xml.bz2") &&
                !fileName.contains("index");
    }


    @Override
    public Collection<DownloadFileRequest> createDownloadRequests(Collection<String[]> fileList) {
        Collection<DownloadFileRequest> response = new ArrayList<>(fileList.size());
        for(String[] data : fileList) {
            if (ArrayUtils.isNotEmpty(data) && data.length == 2) {
                String dumpFileDateStr = parseWikiDateFromFile(data[1]);
                response.add(DownloadFileRequest.newBuilder()
                        .setErrorCheck(ErrorCheck.newBuilder()
                                .setErrorCheck(data[0])
                                .setErrorCheckType(ErrorCheckType.MD5))
                        .setUrl(wikipediaPrefixUrl + dumpFileDateStr + "/" + data[1])
                        .setFileName(data[1])
                        .setFileDumpDate(dumpFileDateStr)
                        .build());
            }
        }
        return response;
    }
    private String parseWikiDateFromFile(String wikiFileName) {
        return StringUtils.substringBetween(wikiFileName, "enwiki-", "-pages");
    }

}
