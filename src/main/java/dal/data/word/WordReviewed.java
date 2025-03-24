package dal.data.word;

import dal.graphic.word.startReview.WriteIn;

import java.util.Random;

public class WordReviewed extends Word {
    private boolean isWrittenInForeign;
    private String userAnswer = "";
    private int hintRevealed = 0;

    public WordReviewed(Word word, Boolean isWrittenInForeign) {
        super(word.getId(), word.getNative_(), word.getForeign(), word.getDescription(), word.getTimeStamp(), word.getReviewsCount(), word.getFailedReviews(), word.getLastReviewsTimestamp());
        this.isWrittenInForeign = isWrittenInForeign;
    }

    public WordReviewed(Word word, WriteIn writeIn) {
        super(word.getId(), word.getNative_(), word.getForeign(), word.getDescription(), word.getTimeStamp(), word.getReviewsCount(), word.getFailedReviews(), word.getLastReviewsTimestamp());
        if (writeIn == WriteIn.NATIVE) {
            this.isWrittenInForeign = false;
        } else if (writeIn == WriteIn.FOREIGN) {
            this.isWrittenInForeign = true;
        } else {
            this.isWrittenInForeign = new Random().nextBoolean();
        }
    }

    public boolean isWrittenInForeign() {
        return isWrittenInForeign;
    }

    public void setWrittenInForeign(boolean isWrittenInForeign) {
        this.isWrittenInForeign = isWrittenInForeign;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public int getHintRevealed() {
        return hintRevealed;
    }

    public void incrHintRevealed() {
        this.hintRevealed += 1;
        // Roll back to max size of the answer if hintRevealed exceeds the answer length
        int ansLength = isWrittenInForeign ? getForeign().length() : getNative_().length();
        if (this.hintRevealed > ansLength) {
            this.hintRevealed = ansLength;
        }
    }
}
