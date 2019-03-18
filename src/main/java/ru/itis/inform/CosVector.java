package ru.itis.inform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

public class CosVector {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String[] words = input.split(" ");
        SearchPhraseDao searchPhraseDao = new SearchPhraseDao();
        WordsDao wordsDao = new WordsDao();
        MyStem mystemAnalyzer =
                new Factory("-igd --eng-gr --format json --weight")
                        .newMyStem("3.0", Option.empty()).get();

        for (int i = 0; i < words.length; i++) {
            try {
                words[i] = mystemAnalyzer.analyze(Request.apply(words[i]))
                        .info().head().lex().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Double> wordsIdf = new HashMap<>();
        for (String word1 : words) {
            try {
                String word = mystemAnalyzer.analyze(Request.apply(word1))
                        .info().head().lex().get();
                int count = searchPhraseDao.getWordCount(word);
                if (count > 0) {
                    wordsIdf.put(word, wordsDao.getWordIdf(word));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> articles = searchPhraseDao.getArticlesId();
        HashMap<String, Double> cos = new HashMap<>();
        double idfs = 0;
        for (Double value : wordsIdf.values()) {
            idfs += value * value;
        }
        idfs = Math.sqrt(idfs);
        for (String article : articles) {
            double numerator = 0;
            double denominator = 0;
            for (Map.Entry<String, Double> entry : wordsIdf.entrySet()) {
                double tfidf = wordsDao.getWordTfIdf(entry.getKey(), article);
                numerator += entry.getValue() * tfidf;
                denominator = tfidf * tfidf;
            }
            denominator = Math.sqrt(denominator);
            denominator *= idfs;
            cos.put(article, numerator / denominator);
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(cos.entrySet());
        list.sort(Comparator.comparingDouble(Map.Entry::getValue));
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getValue().isNaN() || list.get(i).getValue().isInfinite()) {
                list.get(i).setValue(0.0);
            }
        }

        int count = 0;
        while (list.get(count).getValue() > 0.0 && count < 10) {
            System.out.println(searchPhraseDao.getLinkById(list.get(count).getKey()) + " - " + list.get(count).getValue());
            count++;
        }
    }
}
