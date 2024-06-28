package de.samuelgesang.backend.crawls;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CrawlDiffItem {
    
    private String action;
    private String url;
    private boolean checked;

}
