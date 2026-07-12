package jp.matatabi.torismpshop.data;

import org.bukkit.Bukkit;
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
     * 🌅 ShopData を保存する（同じIDなら上書き、無ければ追加）
     * ⚠️ ファイル書き込みは非同期でやる（サーバーを止めない）
     */
    public static void save(ShopData shop) {
        // ===== メモリ側：同じIDあれば置換、なければ追加 =====
        boolean replaced = false;
        for (int i = 0; i < shops.size(); i++) {
            if (shops.get(i).getId().equals(shop.getId())) {
                shops.set(i, shop);   // 🌅 上書き！
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            shops.add(shop);           // 新規追加
        }
        final boolean isUpdate = replaced;

        // ===== 非同期でファイル保存 =====
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long start = System.currentTimeMillis();

                YamlConfiguration config = YamlConfiguration.loadConfiguration(shopsFile);

                // 既存のリストを取得
                List<Map<?, ?>> existing = config.getMapList("shops");
                List<Map<?, ?>> updated = new ArrayList<>();

                // 🌅 同じIDのやつは飛ばして、あとから新しいのを追加する
                boolean fileReplaced = false;
                for (Map<?, ?> m : existing) {
                    Object idObj = m.get("id");
                    if (idObj != null && idObj.toString().equals(shop.getId())) {
                        updated.add(shop.toMap());   // 🌅 上書き！
                        fileReplaced = true;
                    } else {
                        updated.add(m);
                    }
                }
                // ファイル側にも無かったら末尾に追加
                if (!fileReplaced) {
                    updated.add(shop.toMap());
                }

                config.set("shops", updated);
                config.save(shopsFile);

                long elapsed = System.currentTimeMillis() - start;
                plugin.getLogger().info(
                        (isUpdate ? "shops.yml の商品を更新したよ！" : "shops.yml に商品を保存したよ！")
                                + " (id=" + shop.getId() + ", " + elapsed + "ms)"
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

    /**
     * ID指定でショップを1件取得
     */
    public static ShopData getById(String id) {
        for (ShopData shop : shops) {
            if (shop.getId().equals(id)) {
                return shop;
            }
        }
        return null;
    }

    /**
     * ID指定でショップを削除
     * メモリ・ファイル両方から消す
     */
    public static void delete(String id) {
        // メモリから削除
        boolean removed = shops.removeIf(s -> s.getId().equals(id));
        if (!removed) {
            plugin.getLogger().warning("削除対象が見つからない: " + id);
            return;
        }

        // 非同期でファイル書き換え
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long start = System.currentTimeMillis();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(shopsFile);
                List<Map<?, ?>> existing = config.getMapList("shops");

                // 該当ID以外を残す
                List<Map<?, ?>> updated = new ArrayList<>();
                for (Map<?, ?> map : existing) {
                    Object mapId = map.get("id");
                    if (mapId == null || !mapId.toString().equals(id)) {
                        updated.add(map);
                    }
                }

                config.set("shops", updated);
                config.save(shopsFile);

                long elapsed = System.currentTimeMillis() - start;
                plugin.getLogger().info(
                        "shops.yml から商品を削除したよ！ (id=" + id
                                + ", " + elapsed + "ms)"
                );
            } catch (IOException e) {
                plugin.getLogger().severe("shops.yml の削除保存に失敗: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // ShopStorage.java 内
    public static void reload() {
        // 1. 一旦クリア
        shops.clear();

        // 2. 再読み込み
        // YamlConfiguration を読み込み、ShopData.fromMap を使って shops に追加する処理
        YamlConfiguration config = YamlConfiguration.loadConfiguration(shopsFile);
        List<Map<?, ?>> list = config.getMapList("shops");

        for (Map<?, ?> map : list) {
            // Map<Object, Object> から Map<String, Object> への安全な変換が必要な場合があります
            Map<String, Object> castedMap = (Map<String, Object>) map;
            shops.add(ShopData.fromMap(castedMap));
        }
        plugin.getLogger().info("shops.yml をリロードしたよ！");
    }
}