package jp.matatabi.torismpshop.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品（ShopData）の保存・読み込み処理
 * shops.yml を管理する
 */
public class ShopStorage {

    private static Plugin plugin;
    private static File shopsFile;

    // メモリ上のショップリスト（reload で読み込む用・今は保存のみ使用）
    private static final List<ShopData> shops = new ArrayList<>();

    /**
     * プラグイン起動時に呼ぶ（メインクラスの onEnable から）
     */
    public static void initialize(Plugin pl) {
        plugin = pl;
        shopsFile = new File(plugin.getDataFolder(), "shops.yml");

        // フォルダが無ければ作る
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // ファイルが無ければ空で作る
        if (!shopsFile.exists()) {
            try {
                shopsFile.createNewFile();
                plugin.getLogger().info("shops.yml を新規作成したよ🌅");
            } catch (IOException e) {
                plugin.getLogger().severe("shops.yml の作成に失敗: " + e.getMessage());
            }
        }
    }

    /**
     * ShopData を保存する（追記）
     * ⚠️ ファイル書き込みは非同期でやる（サーバーを止めない）
     */
    public static void save(ShopData shop) {
        // メモリにも追加
        shops.add(shop);

        // 非同期でファイル保存
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long start = System.currentTimeMillis();

                YamlConfiguration config = YamlConfiguration.loadConfiguration(shopsFile);

                // 既存のリストを取得
                List<Map<?, ?>> existing = config.getMapList("shops");
                List<Map<?, ?>> updated = new ArrayList<>(existing);

                // 新しいショップを追加
                updated.add(shop.toMap());

                config.set("shops", updated);
                config.save(shopsFile);

                long elapsed = System.currentTimeMillis() - start;
                plugin.getLogger().info(
                        "shops.yml に商品を保存したよ！ (id=" + shop.getId()
                                + ", " + elapsed + "ms)"
                );

            } catch (IOException e) {
                plugin.getLogger().severe("shops.yml の保存に失敗: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * shops.yml から全ショップを読み込む
     * ⚠️ /torishop reload で使う予定（今は呼ばれない）
     */
    public static void loadAll() {
        shops.clear();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(shopsFile);
        List<Map<?, ?>> list = config.getMapList("shops");

        for (Map<?, ?> map : list) {
            try {
                // Map → ConfigurationSection 経由で読み込むのが安全
                YamlConfiguration temp = new YamlConfiguration();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    temp.set(entry.getKey().toString(), entry.getValue());
                }
                ShopData shop = ShopData.fromSection(temp);
                shops.add(shop);
            } catch (Exception e) {
                plugin.getLogger().warning("ショップの読み込みに失敗（スキップ）: " + e.getMessage());
            }
        }

        plugin.getLogger().info(shops.size() + " 件のショップを読み込んだよ🌅");
    }

    /**
     * メモリ上の全ショップを取得
     */
    public static List<ShopData> getAll() {
        return new ArrayList<>(shops);
    }

    /**
     * メモリ上のショップ数
     */
    public static int size() {
        return shops.size();
    }
}