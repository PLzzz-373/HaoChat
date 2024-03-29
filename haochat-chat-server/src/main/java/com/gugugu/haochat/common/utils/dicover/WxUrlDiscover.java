package com.gugugu.haochat.common.utils.dicover;

import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;

public class WxUrlDiscover extends AbstractUrlDiscover {

    @Nullable
    @Override
    public String getTitle(Document document) {
        return document.getElementsByAttributeValue("property", "og:title").attr("content");
    }

    @Nullable
    @Override
    public String getDescription(Document document) {
        return document.getElementsByAttributeValue("property", "og:description").attr("content");
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        String href = document.getElementsByAttributeValue("property", "og:image").attr("content");
        return isConnect(href) ? href : null;
    }
}
