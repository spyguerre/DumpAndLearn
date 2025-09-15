package dal.data.word;

public class Review {
    private Long id;
    private long wordId;
    private long reviewTimestamp;
    private boolean success;
    private int hintUsed;

    public Review(Long id, long wordId, long reviewTimestamp, boolean success, int hintUsed) {
        this(wordId, reviewTimestamp, success, hintUsed);
        this.id = id;
    }

    public Review(long wordId, long reviewTimestamp, boolean success, int hintUsed) {
        this.id = null;
        this.wordId = wordId;
        this.reviewTimestamp = reviewTimestamp;
        this.success = success;
        this.hintUsed = hintUsed;
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
}
