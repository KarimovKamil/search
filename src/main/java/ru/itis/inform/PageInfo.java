package ru.itis.inform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
    private String id;
    private String url;
    private String title;
    private String keywords;
    private String content;
    private int studentId;

    public PageInfo(String url, String title, String tags, String text) {
        this.url = url;
        this.title = title;
        this.keywords = tags;
        this.content = text;
    }
}
