package ru.itis.inform;

import org.postgresql.Driver;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.List;

public class PageDao {
    private JdbcTemplate jdbcTemplate;
    private static final String SQL_INSERT = "INSERT INTO articles " +
            "(title, keywords, content, url, student_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL = "SELECT * FROM articles;";

    public PageDao() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/search");

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<PageInfo> getAll() {
        List<PageInfo> pages = jdbcTemplate.query(SQL_SELECT_ALL, new BeanPropertyRowMapper<>(PageInfo.class));
        return pages;
    }

    public void insert(PageInfo pageInfo) {
        jdbcTemplate.update(SQL_INSERT,
                pageInfo.getTitle(),
                pageInfo.getKeywords(),
                pageInfo.getContent(),
                pageInfo.getUrl(),
                107);
    }
}
