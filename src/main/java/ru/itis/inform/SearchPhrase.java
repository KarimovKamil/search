package ru.itis.inform;

import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

import java.util.*;

public class SearchPhrase {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String[] words = input.split(" ");
        SearchPhraseDao dao = new SearchPhraseDao();
        MyStem mystemAnalyzer =
                new Factory("-igd --eng-gr --format json --weight")
                        .newMyStem("3.0", Option.empty()).get();

        TreeMap<String, Integer> wordsAndCount = new TreeMap<>();
        for (int i = 0; i < words.length; i++) {
            try {
                String word = mystemAnalyzer.analyze(Request.apply(words[i]))
                        .info().head().lex().get();
                int count = dao.getWordCount(word);
                if (count > 0) {
                    wordsAndCount.put(word, dao.getWordCount(word));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(wordsAndCount.entrySet());
        list.sort(Comparator.comparingInt(Map.Entry::getValue));
        String[] wordsForSearch = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            wordsForSearch[i] = list.get(0).getKey();
        }
        List<String> links = dao.getLinksByPhrase(wordsForSearch);
        System.out.println("\nСсылки:");
        for (String link : links) {
            System.out.println(link);
        }
    }
}
