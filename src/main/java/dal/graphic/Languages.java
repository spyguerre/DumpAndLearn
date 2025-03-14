package dal.graphic;

public enum Languages {
    ENGLISH,
    FRENCH,
    GERMAN,
    JAPANESE;

    public static String getStdCode(Languages lang) {
        return switch (lang) {
            case ENGLISH -> "en";
            case FRENCH -> "fr";
            case GERMAN -> "de";
            case JAPANESE -> "ja";
        };
    }

    public static String getTessCode(Languages lang) {
        return switch (lang) {
            case ENGLISH -> "eng";
            case FRENCH -> "fra";
            case GERMAN -> "deu";
            case JAPANESE -> "jpn";
        };
    }
}
