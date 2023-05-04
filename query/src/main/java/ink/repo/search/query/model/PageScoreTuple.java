package ink.repo.search.query.model;

public class PageScoreTuple {
    public String pageId;
    public double score;

    public PageScoreTuple(String pageId, double score) {
        this.pageId = pageId;
        this.score = score;
    }
}
