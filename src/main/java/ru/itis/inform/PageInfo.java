package ru.itis.inform;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageInfo {
    private String url;
    private String title;
    private String tags;
    private String text;
}
