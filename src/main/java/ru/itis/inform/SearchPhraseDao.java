package ru.itis.inform;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.itis.inform.dao.config.DaoConfig;

import java.util.LinkedList;
import java.util.List;

public class SearchPhraseDao {
    private JdbcTemplate jdbcTemplate;
    private static final String GET_WORD_COUNT_SQL = "SELECT count(a.article_id) " +
            "FROM article_term a INNER JOIN terms_list t ON a.term_id = t.term_id " +
            "WHERE t.term_text = ?;";
    private static final String GET_WORD_ARTICLES_ID_SQL = "SELECT a.article_id " +
            "FROM article_term a INNER JOIN terms_list t ON a.term_id = t.term_id " +
            "WHERE t.term_text = ?;";
    private static final String GET_LINKS_BY_PHRASE_SQL_BEGIN = "SELECT url " +
            "FROM articles ar INNER JOIN (";
    private static final String GET_LINKS_BY_PHRASE_SQL_WORD = "(SELECT article_id " +
            " FROM article_term a INNER JOIN (SELECT term_id " +
            " FROM terms_list " +
            " WHERE term_text = ?) t ON t.term_id = a.term_id)";
    private static final String GET_LINKS_BY_PHRASE_SQL_END = ") s ON ar.id = s.article_id;";
    private static final String GET_ARTICLES_IDS_SQL = "SELECT DISTINCT article_id from article_term;";
    private static final String GET_ARTICLE_LINK_BY_ID_SQL = "SELECT url from articles WHERE id = ?::UUID;";

    public SearchPhraseDao() {
        jdbcTemplate = new JdbcTemplate(DaoConfig.getDataSource());
    }

    public List<String> getLinksByPhrase(String[] words) {
        if (words == null || words.length < 1) {
            return new LinkedList<>();
        }
        StringBuilder sb = new StringBuilder(GET_LINKS_BY_PHRASE_SQL_BEGIN);
        sb.append(GET_LINKS_BY_PHRASE_SQL_WORD);
        for (int i = 1; i < words.length; i++) {
            sb.append(" INTERSECT ")
                    .append(GET_LINKS_BY_PHRASE_SQL_WORD);
        }
        sb.append(GET_LINKS_BY_PHRASE_SQL_END);
        List<String> links = jdbcTemplate.queryForList(sb.toString(), words, String.class);
        return links;
    }

    public int getWordCount(String word) {
        return jdbcTemplate.queryForObject(GET_WORD_COUNT_SQL, new Object[]{word}, Integer.class);
    }
    
    public List<String> getArticlesId() {
        return jdbcTemplate.queryForList(GET_ARTICLES_IDS_SQL, String.class);
    }
    
    public String getLinkById(String articleId) {
        return jdbcTemplate.queryForObject(GET_ARTICLE_LINK_BY_ID_SQL, new Object[]{articleId}, String.class);
    }
}
