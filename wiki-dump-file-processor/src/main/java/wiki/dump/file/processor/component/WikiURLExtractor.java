package wiki.dump.file.processor.component;

import com.google.common.collect.Lists;
import com.krickert.search.model.wiki.Link;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class WikiURLExtractor {

    public List<Link> parseUrlEntries(String pageText) {
        String[] urlElements = StringUtils.substringsBetween(pageText, "[http", "]");
        if (urlElements == null) {
            return Collections.emptyList();
        }
        ArrayList<Link> urlEntries = Lists.newArrayListWithExpectedSize(urlElements.length);
        for (String urlElement : urlElements) {
            Link entry = generateUrlEntry(urlElement);
            if (entry != null) {
                urlEntries.add(entry);
            }
        }
        return urlEntries;

    }


    private static Link generateUrlEntry(String wikiCleanedText) {
        String[] entries = StringUtils.split(wikiCleanedText, " ", 2);
        if (ArrayUtils.isEmpty(entries)) {
            return null;
        }

        final String url;
        final String value;
        //just a URL
        url = "http" + entries[0];
        if (entries.length == 1) {
            value = StringUtils.EMPTY;
        } else {
            value = entries[1];
        }
        return Link.newBuilder()
                .setUrl(url)
                .setDescription(value)
                .build();

    }


}
