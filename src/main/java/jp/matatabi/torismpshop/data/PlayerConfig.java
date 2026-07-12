package jp.matatabi.torismpshop.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class PlayerConfig implements ConfigurationSerializable {
    private boolean allowSelfTrade = false; // デフォルトは false

    public PlayerConfig(boolean allowSelfTrade) {
        this.allowSelfTrade = allowSelfTrade;
    }

    public boolean isAllowSelfTrade() {
        return allowSelfTrade;
    }

    public void setAllowSelfTrade(boolean allowSelfTrade) {
        this.allowSelfTrade = allowSelfTrade;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("allowSelfTrade", allowSelfTrade);
        return map;
    }

    public static PlayerConfig deserialize(Map<String, Object> map) {
        return new PlayerConfig((Boolean) map.getOrDefault("allowSelfTrade", false));
    }
}