package jp.matatabi.torismpshop.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettingsManager {
    private static final Map<UUID, PlayerConfig> settings = new HashMap<>();
    private static File file;
    private static Plugin plugin;

    public static void init(Plugin pl) {
        plugin = pl; // ここでプラグイン本体を保持する

        // フォルダのパスを取得
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        file = new File(dataFolder, "player_settings.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                plugin.getLogger().info("player_settings.yml を新規作成したよ🌅");
            } catch (IOException e) {
                plugin.getLogger().severe("player_settings.yml の作成に失敗: " + e.getMessage());
            }
        }

        // 最後にロード
        load();
    }

    public static PlayerConfig get(UUID uuid) {
        return settings.computeIfAbsent(uuid, k -> new PlayerConfig(false,false));
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
    public static void reload() {
        // 1. メモリ上の現在の設定をすべてクリアする
        settings.clear();

        // 2. ファイルから読み込み直す
        load();
        plugin.getLogger().info("player_settings.yml をリロードしたよ！");
    }
}