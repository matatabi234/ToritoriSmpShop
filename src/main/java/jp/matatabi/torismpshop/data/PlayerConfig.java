package jp.matatabi.torismpshop.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class PlayerConfig implements ConfigurationSerializable {
    private boolean allowSelfTrade = false; // デフォルトは false
    private boolean showAllTrades = false; // 💡 追加

    public PlayerConfig(boolean allowSelfTrade, boolean showAllTrades) { // コンストラクタ修正
        this.allowSelfTrade = allowSelfTrade;
        this.showAllTrades = showAllTrades;
    }

    public boolean isAllowSelfTrade() {
        return allowSelfTrade;
    }

    public void setAllowSelfTrade(boolean allowSelfTrade) {
        this.allowSelfTrade = allowSelfTrade;
    }

    public boolean isShowAllTrades() { return showAllTrades; }
    public void setShowAllTrades(boolean showAllTrades) { this.showAllTrades = showAllTrades; }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("allowSelfTrade", allowSelfTrade);
        map.put("showAllTrades", showAllTrades); // 保存用に追加
        return map;
    }

    public static PlayerConfig deserialize(Map<String, Object> map) {
        return new PlayerConfig(
                (Boolean) map.getOrDefault("allowSelfTrade", false),
                (Boolean) map.getOrDefault("showAllTrades", false) // 読み込み用に追加
        );
    }
}