package quarri6343.openarpg;

public class Config {
    
    private static float movementSpeedModifier = 0.5f;

    public static float getMovementSpeedModifier() {
        return movementSpeedModifier;
    }

    public static void setMovementSpeedModifier(float movementSpeedModifier) {
        Config.movementSpeedModifier = movementSpeedModifier;
    }
}
