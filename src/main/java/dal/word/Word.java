package dal.word;

public class Word {
    private long id;
    private String native_;
    private String foreign;
    private long timeStamp;
    private int reviewsCount;
    private int failedReviews;
    private int lastReviewsTimestamp;

    public Word(long id, String native_, String foreign, long timeStamp, int reviewsCount, int failedReviews, int lastReviewsTimestamp) {
        this.id = id;
        this.native_ = native_;
        this.foreign = foreign;
        this.timeStamp = timeStamp;
        this.reviewsCount = reviewsCount;
        this.failedReviews = failedReviews;
        this.lastReviewsTimestamp = lastReviewsTimestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNative_() {
        return native_;
    }

    public void setNative_(String native_) {
        this.native_ = native_;
    }

    public String getForeign() {
        return foreign;
    }

    public void setForeign(String foreign) {
        this.foreign = foreign;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public int getFailedReviews() {
        return failedReviews;
    }

    public void setFailedReviews(int failedReviews) {
        this.failedReviews = failedReviews;
    }

    public int getLastReviewsTimestamp() {
        return lastReviewsTimestamp;
    }

    public void setLastReviewsTimestamp(int lastReviewsTimestamp) {
        this.lastReviewsTimestamp = lastReviewsTimestamp;
    }
}
