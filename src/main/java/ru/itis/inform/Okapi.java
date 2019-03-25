package ru.itis.inform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

public class Okapi {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String[] words = input.split(" ");
        SearchPhraseDao searchPhraseDao = new SearchPhraseDao();
        WordsDao wordsDao = new WordsDao();
        MyStem mystemAnalyzer =
                new Factory("-igd --eng-gr --format json --weight")
                        .newMyStem("3.0", Option.empty()).get();

        Set<String> wordsInQ = new HashSet<>();
        for (String word : words) {
            try {
                wordsInQ.add(mystemAnalyzer.analyze(Request.apply(word))
                        .info().head().lex().get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> articles = searchPhraseDao.getArticlesId();
        HashMap<String, Double> okapiScore = new HashMap<>();

        for (String article : articles) {
            double articleScore = 0;
            for (String s : wordsInQ) {
                double score = wordsDao.getWordScore(s, article);
                if (score > 0) {
                    articleScore += score;
                }
            }
            okapiScore.put(article, articleScore);
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(okapiScore.entrySet());
        list.sort(Comparator.comparingDouble(Map.Entry::getValue));
        for (Map.Entry<String, Double> aList : list) {
            if (aList.getValue().isNaN() || aList.getValue().isInfinite()) {
                aList.setValue(0.0);
            }
        }

        int count = 0;
        List<String> sout = new ArrayList<>();
        while (list.get(count).getValue() > 0.0 && count < 10) {
            sout.add(searchPhraseDao.getLinkById(list.get(count).getKey()) + " - " + list.get(count).getValue());
            count++;
        }

        for (String s : sout) {
            System.out.println(s);
        }
    }
}
