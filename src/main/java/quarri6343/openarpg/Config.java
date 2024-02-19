package quarri6343.openarpg;

//TODO:CameraMixinのズームとか色々こちらに持ってくる
public class Config {
    
    private static float movementSpeedModifier = 0.5f;

    public static float getMovementSpeedModifier() {
        return movementSpeedModifier;
    }

    public static void setMovementSpeedModifier(float movementSpeedModifier) {
        Config.movementSpeedModifier = movementSpeedModifier;
    }
}
