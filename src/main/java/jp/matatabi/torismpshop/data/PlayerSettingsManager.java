package jp.matatabi.torismpshop.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettingsManager {
    private static final Map<UUID, PlayerConfig> settings = new HashMap<>();
    private static File file;

    public static void init(File dataFolder) {
        file = new File(dataFolder, "player_settings.yml");
        load();
    }

    public static PlayerConfig get(UUID uuid) {
        return settings.computeIfAbsent(uuid, k -> new PlayerConfig(false));
    }

    public static void save() throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerConfig> entry : settings.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        config.save(file);
    }

    public static void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            settings.put(UUID.fromString(key), (PlayerConfig) config.get(key));
        }
    }

    // PlayerSettingsManager.java にメソッドを追加
    public static void setAllowSelfTrade(String playerName, boolean value) {
        // プレイヤー名からUUIDを引く処理（必要ならオフラインプレイヤーから取得）
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        PlayerConfig config = get(player.getUniqueId());
        config.setAllowSelfTrade(value);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}