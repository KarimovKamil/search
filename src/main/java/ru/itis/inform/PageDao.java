package ru.itis.inform;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.itis.inform.dao.config.DaoConfig;

import java.util.List;

public class PageDao {
    private JdbcTemplate jdbcTemplate;
    private static final String SQL_INSERT = "INSERT INTO articles " +
            "(title, keywords, content, url, student_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL = "SELECT * FROM articles;";
    private static final String SQL_GET_URL_BY_ID = "SELECT url from articles " +
            "WHERE id = ?::UUID;";

    public PageDao() {
        this.jdbcTemplate = new JdbcTemplate(DaoConfig.getDataSource());
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

    public String getPageUrlById(String id) {
        return jdbcTemplate.queryForObject(SQL_GET_URL_BY_ID, new Object[]{id}, String.class);
    }
}
