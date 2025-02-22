package dal.graphic.word.startReview;

public enum WriteIn {
    BOTH,
    NATIVE,
    FOREIGN;

    public static WriteIn getWriteIn(String writeIn) {
        return switch (writeIn) {
            case "Both" -> BOTH;
            case "Native Language" -> NATIVE;
            case "Foreign Language" -> FOREIGN;
            default -> throw new IllegalArgumentException();
        };
    }
}
