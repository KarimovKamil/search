package ru.itis.inform;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.springframework.util.StringUtils;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {
    public static void main(String[] args) throws FileNotFoundException, MyStemApplicationException {
        PageDao pageDao = new PageDao();
        List<PageInfo> pages = pageDao.getAll();
        Map<String, Set<String>> wordsInTexts = new HashMap<>();

        Pattern pattern = Pattern.compile("[А-Яа-яЁё]+[[-][А-Яа-яЁё]]*");
        for (PageInfo page : pages) {
            Matcher matcher = pattern.matcher(page.getContent());
            String word;
            while (matcher.find()) {
                word = matcher.group().toLowerCase();
                if (wordsInTexts.containsKey(word)) {
                    wordsInTexts.get(word).add(page.getId());
                } else {
                    wordsInTexts.put(word, new HashSet<>(Collections.singletonList(page.getId())));
                }
            }
        }

        File fileSW = new File("src/main/resources/stopwords-ru.txt");
        Scanner scanner = new Scanner(fileSW);
        while (scanner.hasNext()) {
            wordsInTexts.remove(scanner.next());
        }

        WordsDao wordsDao = new WordsDao();
        mystem(wordsInTexts, wordsDao);
        porter(wordsInTexts, wordsDao);
    }

    private static void mystem(Map<String, Set<String>> wordsInTexts, WordsDao wordsDao) {
        MyStem mystemAnalyzer =
                new Factory("-igd --eng-gr --format json --weight")
                        .newMyStem("3.0", Option.empty()).get();

        Map<String, Set<String>> words = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : wordsInTexts.entrySet()) {
            try {
                String word = mystemAnalyzer.analyze(Request.apply(entry.getKey()))
                        .info().head().lex().get();

                if (!StringUtils.isEmpty(word)) {
                    if (words.containsKey(word)) {
                        words.get(word).addAll(entry.getValue());
                    } else {
                        words.put(word, entry.getValue());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        wordsDao.saveMyStemWords(words);
    }

    private static void porter(Map<String, Set<String>> wordsInTexts, WordsDao wordsDao) {
        Map<String, Set<String>> words = new HashMap<>();
        SnowballStemmer portersStem = new SnowballStemmer(SnowballStemmer.ALGORITHM.RUSSIAN);

        for (Map.Entry<String, Set<String>> entry : wordsInTexts.entrySet()) {
            String word = portersStem.stem(entry.getKey()).toString();
            if (!StringUtils.isEmpty(word)) {
                if (words.containsKey(word)) {
                    words.get(word).addAll(entry.getValue());
                } else {
                    words.put(word, entry.getValue());
                }
            }
        }
        wordsDao.savePorterWords(words);
    }
}
