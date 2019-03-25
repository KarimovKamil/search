package ru.itis.inform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import ru.itis.inform.dao.config.DaoConfig;

public class WordsDao {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    private static final String GET_WORD_IDF5_SQL = "SELECT (((SELECT count(id) FROM articles)::DECIMAL - (SELECT count(article_id) FROM article_term " +
            "WHERE EXISTS(SELECT 1 " +
            "             FROM article_term a1 " +
            "             WHERE a1.term_id = (SELECT a1.term_id FROM terms_list WHERE term_text = ?)))::DECIMAL + 0.5) / " +
            "((SELECT count(article_id) FROM article_term " +
            "WHERE EXISTS(SELECT 1 " +
            "             FROM article_term a1 " +
            "             WHERE a1.term_id = (SELECT a1.term_id FROM terms_list WHERE term_text = ?)))::DECIMAL + 0.5))";
    private static final String GET_WORD_SCORE_SQL = "SELECT (((SELECT word_count FROM article_term WHERE term_id = " +
            "   (SELECT article_id FROM terms_list WHERE term_text = :wordText) AND article_id = :articleId) / " +
            "(SELECT sum(word_count) FROM article_term WHERE article_id = :articleId))::DECIMAL * (:k1 + 1) / " +
            "   ((SELECT word_count FROM article_term WHERE term_id = " +
            "       (SELECT article_id FROM terms_list WHERE term_text = :wordText) AND article_id = :articleId) + :k1 * " +
            "(1 - :b + :b * (SELECT count(id) FROM articles) / " +
            "(SELECT avg(words_sum) FROM (SELECT sum(word_count) as words_sum FROM article_term GROUP BY article_id) inner_query))::DECIMAL))";
    private static final String GET_WORD_IDF_SQL = "SELECT (log(2, ((SELECT count(article_id) FROM article_term))" +
            "  - log(2, (SELECT count(article_id) FROM article_term" +
            "            WHERE EXISTS(SELECT 1" +
            "                         FROM article_term a1" +
            "                         WHERE a1.term_id = (SELECT a1.term_id FROM terms_list WHERE term_text = ?))))))";
    private static final String GET_WORD_TF_IDF_SQL = "SELECT tf_idf FROM article_term " +
            "WHERE term_id = (SELECT term_id FROM terms_list WHERE term_text = ?) AND article_id = ?::UUID";

    public WordsDao() {
        this.jdbcTemplate = new JdbcTemplate(DaoConfig.getDataSource());
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(DaoConfig.getDataSource());
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

    @Transactional
    public double getWordScore(String word, String articleId) {
        double result;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("wordText", word);
        params.addValue("articleId", articleId);
        params.addValue("k1", 1.2);
        params.addValue("b", 0.75);
        try {
            result = jdbcTemplate.queryForObject(GET_WORD_IDF5_SQL, new Object[]{word}, double.class) *
                    namedJdbcTemplate.queryForObject(GET_WORD_SCORE_SQL, params, double.class);
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }
}
