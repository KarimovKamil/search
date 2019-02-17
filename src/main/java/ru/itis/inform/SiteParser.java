package ru.itis.inform;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SiteParser {
    public static void main(String[] args) throws Exception {
        List<String> links = new LinkedList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        Document doc = getDocument("https://tproger.ru/");

        String expr = "//article/div/a/@href";
        NodeList linkNodes = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);

        for (int i = 10; i < 40; i++) {
            String linkNode = linkNodes.item(i).getNodeValue();
            links.add(linkNode);
        }

        Pattern pattern = Pattern.compile("[&].*[;]");

        for (String link : links) {
            Document document = getDocument(link);

            String title = ((String) xpath.evaluate("normalize-space(//h1)", document, XPathConstants.STRING)).trim();
            title = pattern.matcher(title).replaceAll("");


            String tags = ((String) xpath.evaluate("//footer[@class='entry-meta clearfix']", document, XPathConstants.STRING)).trim();
            tags = tags.replace(",", ";");

            String body = ((String) xpath.evaluate("//div[@class='entry-container']", document, XPathConstants.STRING)).trim();
            body = pattern.matcher(body).replaceAll("");

            PageInfo pageInfo = new PageInfo(link, title, tags, body);

            PageDao pageDao = new PageDao();
            pageDao.insert(pageInfo);
        }
    }

    private static Document getDocument(String link) throws IOException, ParserConfigurationException {
        URLConnection connection;
        String content;
        connection = new URL(link).openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 " +
                "(Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        content = scanner.next();
        scanner.close();

        TagNode tagNode = new HtmlCleaner().clean(content);
        return new DomSerializer(
                new CleanerProperties()).createDOM(tagNode);
    }
}
