package ru.itis.inform;

import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.Map;
import java.util.Set;

public class WordsDao {
    private JdbcTemplate jdbcTemplate;

    public WordsDao() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/search");

        this.jdbcTemplate = new JdbcTemplate(dataSource);
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
}
