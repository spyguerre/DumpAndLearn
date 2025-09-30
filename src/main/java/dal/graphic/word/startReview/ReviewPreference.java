package dal.graphic.word.startReview;

public enum ReviewPreference {
    ANY,
    RECENT,
    OLD,
    LEAST_REVIEWED,
    OFTEN_FAILED;

    public static ReviewPreference getReviewPreference(String preference) {
        return switch (preference) {
            case "Any" -> ANY;
            case "Learn Recent Words" -> RECENT;
            case "Review Old Words" -> OLD;
            case "Review Least Reviewed Words" -> LEAST_REVIEWED;
            case "Review Often Failed Words" -> OFTEN_FAILED;
            default -> throw new IllegalArgumentException();
        };
    }

    public static String getString(ReviewPreference preference) {
        return switch (preference) {
            case ANY -> "Any";
            case RECENT -> "Learn Recent Words";
            case OLD -> "Review Old Words";
            case LEAST_REVIEWED -> "Review Least Reviewed Words";
            case OFTEN_FAILED -> "Review Often Failed Words";
        };
    }
}
