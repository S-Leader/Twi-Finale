package show.twilight.finale.entity.combat;

public interface ColoredAdherent {
    int BLUE = 0, YELLOW = 1, GREEN = 2, RED = 3, PURPLE = 4;

    int finale$getColor();

    void finale$setColor(int color);

    static int tint(int color) {
        return switch (color) {
            case YELLOW -> 0xFFFFD34C;
            case GREEN -> 0xFF70DF69;
            case RED -> 0xFFFF6262;
            case PURPLE -> 0xFFBF77FF;
            default -> 0xFF69ACFF;
        };
    }
}
