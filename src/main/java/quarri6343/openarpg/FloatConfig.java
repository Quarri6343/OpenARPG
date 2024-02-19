package quarri6343.openarpg;

public enum FloatConfig {
    MOVEMENTSPEED(0.5f, "移動速度", "プレイヤーの移動速度", 0.1f, 3f),
    ZOOM(12f, "カメラのズーム", "3人称カメラがどれだけプレイヤーから引くか", 1f, 100f),
    DROPPEDITEMSCALE(0.7f, "ドロップアイテムのサイズ", "ドロップアイテムのテキストのフォントサイズ", 0.01f, 10f),
    OBSTACLETRIMMEDANGLE(30f, "切り取り角度", "3人称視点で、プレイヤーとカメラの間の壁をどれくらいの角度まで切り落とすか", 0f, 100f),
    OBSTACLETRIMMEDDISTANCE(2f, "切り取り距離", "3人称視点で、プレイヤーとカメラの間の壁をどれくらいの距離まで切り落とすか(逆数、小さくすればするほど長い距離の壁が切り落とされる)", 1f, 10f),
    MAXMOVEHEIGHT(3f, "最大反応高度", "3人称視点でプレイヤーより高い位置がクリックされた時、プレイヤーがどこまで反応するか", 1f, 10f);
    
    private float value;
    private final String displayName;
    private final String description;
    private final float minValue;
    private final float maxValue;

    FloatConfig(float value, String displayName, String description, float minValue, float maxValue) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public float getValue(){
        return value;
    }
    
    public void setValue(float value){
        if(value < minValue || value > maxValue)
            return;
        
        this.value = value;
    }
    
    public String getDisplayName(){
        return displayName;
    }
    
    public String getDescription(){
        return description;
    }
    
    public float getMinValue(){
        return minValue;
    }
    
    public float getMaxValue(){
        return maxValue;
    }
}
