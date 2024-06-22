package de.samuelgesang.backend.crawls;

public class CrawlDiffItem {

    private String action;
    private String url;
    private boolean checked;

    // Getter und Setter
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
