package data.word;

public class Word {
    private long id;
    private String native_;
    private String foreign;
    private String description;
    private long timeStamp;
    private int reviewsCount;
    private int failedReviews;
    private long lastReviewsTimestamp;

    public Word(long id, String native_, String foreign, String description, long timeStamp, int reviewsCount, int failedReviews, long lastReviewsTimestamp) {
        this.id = id;
        this.native_ = native_;
        this.foreign = foreign;
        this.description = description;
        this.timeStamp = timeStamp;
        this.reviewsCount = reviewsCount;
        this.failedReviews = failedReviews;
        this.lastReviewsTimestamp = lastReviewsTimestamp;
    }

    public String toString() {
        return getNative_() + " = " + getForeign();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public long getLastReviewsTimestamp() {
        return lastReviewsTimestamp;
    }

    public void setLastReviewsTimestamp(int lastReviewsTimestamp) {
        this.lastReviewsTimestamp = lastReviewsTimestamp;
    }
}
