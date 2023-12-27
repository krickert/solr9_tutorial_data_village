package com.krickert.search.crawler;

import com.google.common.net.InternetDomainName;

import java.net.MalformedURLException;
import java.net.URL;

public class CrawlEntry {
    public final URL url;
    public final InternetDomainName internetDomainName;
    public final int depth;


    public CrawlEntry(String link) throws MalformedURLException {
        this(link, 0);
    }

    public CrawlEntry(String link, int depth) throws MalformedURLException {
        this.url = new URL(link);
        this.internetDomainName = InternetDomainName.from(url.getHost());
        this.depth = depth;
    }

    public URL getUrl() {
        return url;
    }

    public InternetDomainName getInternetDomainName() {
        return internetDomainName;
    }

    public int getDepth() {
        return depth;
    }
}
