package dal.data.word;

public class Review {
    private Long id;
    private long wordId;
    private long reviewTimestamp;
    private boolean success;
    private int hintUsed;
    private boolean isReviewedInForeign;

    public Review(Long id, long wordId, long reviewTimestamp, boolean success, int hintUsed, boolean isReviewedInForeign) {
        this(wordId, reviewTimestamp, success, hintUsed, isReviewedInForeign);
        this.id = id;
    }

    public Review(long wordId, long reviewTimestamp, boolean success, int hintUsed, boolean isReviewedInForeign) {
        this.id = null;
        this.wordId = wordId;
        this.reviewTimestamp = reviewTimestamp;
        this.success = success;
        this.hintUsed = hintUsed;
        this.isReviewedInForeign = isReviewedInForeign;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public long getReviewTimestamp() {
        return reviewTimestamp;
    }

    public void setReviewTimestamp(long reviewTimestamp) {
        this.reviewTimestamp = reviewTimestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getHintUsed() {
        return hintUsed;
    }

    public void setHintUsed(int hintUsed) {
        this.hintUsed = hintUsed;
    }

    public boolean isReviewedInForeign() {
        return isReviewedInForeign;
    }

    public void setReviewedInForeign(boolean reviewedInForeign) {
        this.isReviewedInForeign = reviewedInForeign;
    }
}
