package ru.itis.inform;

import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;


public class PageDao {
    private JdbcTemplate jdbcTemplate;
    private static final String SQL_INSERT = "INSERT INTO articles " +
            "(title, keywords, content, url, student_id) " +
            "VALUES (?, ?, ?, ?, ?)";


    public PageDao() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/search");

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(PageInfo pageInfo) {
        jdbcTemplate.update(SQL_INSERT,
                pageInfo.getTitle(),
                pageInfo.getTags(),
                pageInfo.getText(),
                pageInfo.getUrl(),
                107);
    }
}
