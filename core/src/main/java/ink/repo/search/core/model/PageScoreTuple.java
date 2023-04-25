package ink.repo.search.core.model;

public class PageScoreTuple {
    public String pageId;
    public double score;

    public PageScoreTuple(String pageId, double score) {
        this.pageId = pageId;
        this.score = score;
    }
}
