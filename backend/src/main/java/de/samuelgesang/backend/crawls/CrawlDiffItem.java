package de.samuelgesang.backend.crawls;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CrawlDiffItem {

    private String action;
    private String url;
    private boolean checked;

}
