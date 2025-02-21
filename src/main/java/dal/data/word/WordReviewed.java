package dal.data.word;

import dal.graphic.startReview.WriteIn;

import java.util.Random;

public class WordReviewed extends Word {
    private boolean isWrittenInForeign;
    private String userAnswer = "";

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
}
