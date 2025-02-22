package dal.graphic.word.startReview;

public enum ReviewPreference {
    ANY,
    RECENT,
    OLD,
    OFTEN_FAILED;

    public static ReviewPreference getReviewPreference(String preference) {
        return switch (preference) {
            case "Any" -> ANY;
            case "Learn Recent Words" -> RECENT;
            case "Review Old Words" -> OLD;
            case "Review Often Failed Words" -> OFTEN_FAILED;
            default -> throw new IllegalArgumentException();
        };
    }
}
