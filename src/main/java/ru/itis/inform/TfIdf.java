package ru.itis.inform;

import org.springframework.util.StringUtils;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TfIdf {
    public static void main(String[] args) throws FileNotFoundException {
        PageDao pageDao = new PageDao();
        List<PageInfo> pages = pageDao.getAll();
        Map<String, HashMap<String, Integer>> wordsInTexts = new HashMap<>();

        Pattern pattern = Pattern.compile("[А-Яа-яЁё]+[[-][А-Яа-яЁё]]*");
        for (PageInfo page : pages) {
            Matcher matcher = pattern.matcher(page.getContent());
            String word;
            while (matcher.find()) {
                word = matcher.group().toLowerCase();
                if (wordsInTexts.containsKey(word)) {
                    if (wordsInTexts.get(word).containsKey(page.getId())) {
                        wordsInTexts.get(word).put(page.getId(), wordsInTexts.get(word).get(page.getId()) + 1);
                    } else {
                        wordsInTexts.get(word).put(page.getId(), 1);
                    }
                } else {
                    wordsInTexts.put(word, new HashMap<>());
                    wordsInTexts.get(word).put(page.getId(), 1);
                }
            }
        }

        File fileSW = new File("src/main/resources/stopwords-ru.txt");
        Scanner scanner = new Scanner(fileSW);
        while (scanner.hasNext()) {
            wordsInTexts.remove(scanner.next());
        }
        mystem(wordsInTexts);
    }

    private static void mystem(Map<String, HashMap<String, Integer>> wordsInTexts) {
        MyStem mystemAnalyzer =
                new Factory("-igd --eng-gr --format json --weight")
                        .newMyStem("3.0", Option.empty()).get();

        Map<String, HashMap<String, Integer>> words = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Integer>> entry : wordsInTexts.entrySet()) {
            try {
                String word = mystemAnalyzer.analyze(Request.apply(entry.getKey()))
                        .info().head().lex().get();

                if (!StringUtils.isEmpty(word)) {
                    words.put(word, entry.getValue());
                }
            } catch (Exception ignored) {
            }
        }
        WordsDao wordsDao = new WordsDao();
        wordsDao.saveWordsCount(words);
    }
}
