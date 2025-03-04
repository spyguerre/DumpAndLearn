package dal.graphic;

public enum Languages {
    ENGLISH,
    FRENCH,
    GERMAN,
    JAPANESE;

    public static String getCode(Languages lang) {
        return switch (lang) {
            case ENGLISH -> "en";
            case FRENCH -> "fr";
            case GERMAN -> "de";
            case JAPANESE -> "jp";
        };
    }
}
