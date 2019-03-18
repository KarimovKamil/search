package ru.itis.inform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import ru.itis.inform.dao.config.DaoConfig;

public class WordsDao {
    private JdbcTemplate jdbcTemplate;
    private static final String GET_WORD_IDF_SQL = "SELECT (log(2, ((SELECT count(article_id) FROM article_term))" +
            "  - log(2, (SELECT count(article_id) FROM article_term" +
            "            WHERE EXISTS(SELECT 1" +
            "                         FROM article_term a1" +
            "                         WHERE a1.term_id = (SELECT a1.term_id FROM terms_list WHERE term_text = ?))))))";
    private static final String GET_WORD_TF_IDF_SQL = "SELECT tf_idf FROM article_term " +
            "WHERE term_id = (SELECT term_id FROM terms_list WHERE term_text = ?) AND article_id = ?::UUID";

    public WordsDao() {
        this.jdbcTemplate = new JdbcTemplate(DaoConfig.getDataSource());
    }

    public void saveMyStemWords(Map<String, Set<String>> words) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
            for (String s : entry.getValue()) {
                stringBuilder.append("INSERT INTO words_mystem (term, articles_id) VALUES ('")
                        .append(entry.getKey())
                        .append("', '")
                        .append(s)
                        .append("');");
            }
        }
        jdbcTemplate.update(stringBuilder.toString());
    }

    public void savePorterWords(Map<String, Set<String>> words) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
            for (String s : entry.getValue()) {
                stringBuilder.append("INSERT INTO words_porter (term, articles_id) VALUES ('")
                        .append(entry.getKey())
                        .append("', '")
                        .append(s)
                        .append("');");
            }
        }
        jdbcTemplate.update(stringBuilder.toString());
    }

    public void saveWordsCount(Map<String, HashMap<String, Integer>> words) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, HashMap<String, Integer>> entry : words.entrySet()) {
            for (Map.Entry<String, Integer> stringIntegerEntry : entry.getValue().entrySet()) {
                stringBuilder.append("UPDATE article_term SET word_count = ")
                        .append(stringIntegerEntry.getValue())
                        .append(" WHERE term_id = (SELECT term_id FROM terms_list WHERE term_text = '")
                        .append(entry.getKey()).append("') AND article_id = '")
                        .append(stringIntegerEntry.getKey())
                        .append("'::UUID;");
            }
        }
        jdbcTemplate.update(stringBuilder.toString());
    }

    public double getWordIdf(String word) {
        return jdbcTemplate.queryForObject(GET_WORD_IDF_SQL, new Object[]{word}, double.class);
    }

    public double getWordTfIdf(String word, String articleId) {
        double result;
        try {
            result = jdbcTemplate.queryForObject(GET_WORD_TF_IDF_SQL, new Object[]{word, articleId}, double.class);
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }
}
