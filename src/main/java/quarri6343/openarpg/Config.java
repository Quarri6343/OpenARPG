package quarri6343.openarpg;

public class Config {

    /**
     * プレイヤーの移動速度
     */
    private static float movementSpeedModifier = 0.5f;

    /**
     * 3人称カメラがどれだけプレイヤーから引くか
     */
    private static float zoom = 12f;

    /**
     * ドロップアイテムのテキストのフォントサイズ
     */
    private static float droppedItemScale = 0.7f;

    /**
     * 3人称視点で、プレイヤーとカメラの間の壁をどれくらいの角度まで切り落とすか
     */
    private static float obstacleTrimmedAngle = 30f;

    /**
     * 3人称視点で、プレイヤーとカメラの間の壁をどれくらいの距離まで切り落とすか(逆数、小さくすればするほど長い距離の壁が切り落とされる)
     */
    private static float obstacleTrimmedDistance = 2f;

    /**
     * 3人称視点でプレイヤーより高い位置がクリックされた時、プレイヤーがどこまで反応するか
     */
    private static float maxMoveHeight = 3f;

    public static float getMovementSpeedModifier() {
        return movementSpeedModifier;
    }

    public static void setMovementSpeedModifier(float movementSpeedModifier) {
        Config.movementSpeedModifier = movementSpeedModifier;
    }

    public static float getZoom() {
        return zoom;
    }

    public static void setZoom(float zoom) {
        Config.zoom = zoom;
    }

    public static float getDroppedItemScale() {
        return droppedItemScale;
    }

    public static void setDroppedItemScale(float droppedItemScale) {
        Config.droppedItemScale = droppedItemScale;
    }

    public static float getObstacleTrimmedAngle() {
        return obstacleTrimmedAngle;
    }

    public static void setObstacleTrimmedAngle(float obstacleTrimmedAngle) {
        Config.obstacleTrimmedAngle = obstacleTrimmedAngle;
    }

    public static float getObstacleTrimmedDistance() {
        return obstacleTrimmedDistance;
    }

    public static void setObstacleTrimmedDistance(float obstacleTrimmedDistance) {
        Config.obstacleTrimmedDistance = obstacleTrimmedDistance;
    }

    public static float getMaxMoveHeight() {
        return maxMoveHeight;
    }

    public static void setMaxMoveHeight(float maxMoveHeight) {
        Config.maxMoveHeight = maxMoveHeight;
    }
}
