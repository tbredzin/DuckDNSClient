package com.github.tbredzin.duckdns;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class DuckDnsClient {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2";

    public DuckDnsClient() {
    }

    public Optional<Document> httpGet(String url) {
        try {
            return Optional.ofNullable(Jsoup.connect(url)
                    .header("Cache-Control", "no-cache")
                    .userAgent(USER_AGENT)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .timeout(10 * 1000)
                    .get());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public Optional<String> getMyIp() {
        return httpGet("http://checkip.amazonaws.com")
                .map(Element::text)
                .filter(text -> text.length() > 7);
    }

    public String getDomainIP(String domain) throws UnknownHostException {
        return InetAddress.getByName(domain + ".duckdns.org").toString()
                .replace(domain + ".duckdns.org/", "");
    }

    public String updateDuckDns(String domain, String token, String ip) {
        if (domain.length() <= 0 && token.length() <= 0) {
            return "KO";
        }
        final String url = String.format("http://www.duckdns.org/update?domains=%s&token=%s&ip=%s", domain, token, ip);
        return httpGet(url)
                .map(Element::text)
                .orElse("Error");
    }
}
