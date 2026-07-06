package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.ToriSmpShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemSelectGui {

    public static final Component TITLE_PREFIX = Component.text("アイテム選択", NamedTextColor.GOLD);

    // 1ページに表示するアイテム数
    public static final int ITEMS_PER_PAGE = 45;

    // プレイヤーごとの現在ページを記憶（Playerのuuid → page番号）
    private static final Map<java.util.UUID, Integer> currentPage = new HashMap<>();

    // 全アイテムリスト（初回だけ作って使い回す）
    private static List<Material> cachedItems = null;
    // 🌅 ページ送り中フラグ（GUIを開き直す時は clearPage を防ぐ）
    private static final java.util.Set<java.util.UUID> switching = new java.util.HashSet<>();

    public static boolean isSwitching(Player player) {
        return switching.contains(player.getUniqueId());
    }

    public static void markSwitching(Player player) {
        switching.add(player.getUniqueId());
    }

    public static void unmarkSwitching(Player player) {
        switching.remove(player.getUniqueId());
    }
    /**
     * 表示可能な全アイテムを取得（キャッシュあり）
     */
    public static List<Material> getAllItems() {
        // キャッシュ済み → それを返す
        if (cachedItems != null) return cachedItems;

        // 🚨 まだ準備できてない場合（起動直後にGUI開いた時など）
        // 空のリストを返して、プレイヤーには待ってもらう
        return new ArrayList<>();
    }

    /**
     * GUIを開く（初回は0ページ目）
     */
    public static void open(Player player) {
        open(player, 0);
    }

    /**
     * 指定ページを開く
     */
    public static void open(Player player, int page) {
        List<Material> allItems = getAllItems();

        // 🚨 まだアイテムリスト準備中
        if (allItems.isEmpty()) {
            player.sendMessage("§eアイテムリスト準備中だよ〜！ちょっと待ってから開いてね🌅");
            return;
        }

        int totalPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);

        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        currentPage.put(player.getUniqueId(), page);

        // 🌅 デバッグ追加
        player.sendMessage("§b[Debug/open] 保存したページ: " + page + " (map size: " + currentPage.size() + ")");

        // タイトルにページ番号を含める
        Component title = Component.text("アイテム選択 [" + (page + 1) + "/" + totalPages + "]", NamedTextColor.GOLD);
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // アイテムを配置
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            Material mat = allItems.get(i);
            ItemStack item = new ItemStack(mat);
            inv.setItem(i - startIndex, item);
        }

        // 下段の操作ボタン（45〜53スロット）
        // 45: 前のページ
        if (page > 0) {
            inv.setItem(45, createButton(
                    Material.ARROW,
                    Component.text("前のページ", NamedTextColor.YELLOW),
                    Component.text("ページ " + page + " へ", NamedTextColor.GRAY)
            ));
        }

        // 49: 中央にページ情報
        inv.setItem(49, createButton(
                Material.PAPER,
                Component.text("ページ " + (page + 1) + " / " + totalPages, NamedTextColor.AQUA),
                Component.text("全 " + allItems.size() + " アイテム", NamedTextColor.GRAY)
        ));

        // 53: 次のページ
        if (page < totalPages - 1) {
            inv.setItem(53, createButton(
                    Material.ARROW,
                    Component.text("次のページ", NamedTextColor.YELLOW),
                    Component.text("ページ " + (page + 2) + " へ", NamedTextColor.GRAY)
            ));
        }

        // 🌅 ページ送り中フラグを立てる（openInventory の直前）
        markSwitching(player);
        player.openInventory(inv);
        // 🌅 フラグを降ろす（次のtickで安全に）
        Bukkit.getScheduler().runTask(
                ToriSmpShop.getInstance(),
                () -> unmarkSwitching(player)
        );

        player.openInventory(inv);
    }

    /**
     * プレイヤーの現在ページを取得
     */
    public static int getCurrentPage(Player player) {
        int result = currentPage.getOrDefault(player.getUniqueId(), 0);
        player.sendMessage("§d[Debug/get] 取得したページ: " + result + " (map size: " + currentPage.size() + ")");
        return currentPage.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * プレイヤーの現在ページ情報を削除（GUI閉じた時などに）
     */
    public static void clearPage(Player player) {
        currentPage.remove(player.getUniqueId());
    }

    /**
     * ボタン用アイテムを作るヘルパー（説明1行版）
     */
    private static ItemStack createButton(Material material, Component name, Component lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }
    /**
     * サーバー起動時に一度だけ呼ぶ。
     * items.yml があれば読み込み、無ければ生成する。
     */
    public static void initialize(org.bukkit.plugin.Plugin plugin) {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");

        // ファイルがあれば → 読み込むだけ（超速い）
        if (itemsFile.exists()) {
            loadFromFile(plugin, itemsFile);
            return;
        }

        // ファイルが無ければ → 非同期で生成
        plugin.getLogger().info("items.yml が無いので生成するよ...（初回のみ）");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            generateAndSave(plugin, itemsFile);
        });
    }

    /**
     * items.yml から読み込む
     */
    private static void loadFromFile(org.bukkit.plugin.Plugin plugin, File file) {
        long start = System.currentTimeMillis();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> names = config.getStringList("items");

        List<Material> items = new ArrayList<>(names.size());
        for (String name : names) {
            try {
                Material mat = Material.valueOf(name);
                items.add(mat);
            } catch (IllegalArgumentException e) {
                // マイクラのバージョンアップで消えたアイテムはスキップ
                plugin.getLogger().warning("不明なアイテム: " + name);
            }
        }

        cachedItems = items;

        long elapsed = System.currentTimeMillis() - start;
        plugin.getLogger().info(
                "items.yml から " + items.size() + "個のアイテムを読み込んだよ (" + elapsed + "ms)"
        );
    }

    /**
     * 全アイテムを計算して items.yml に保存する（初回のみ・非同期）
     */
    private static void generateAndSave(org.bukkit.plugin.Plugin plugin, File file) {
        long start = System.currentTimeMillis();

        // 🌅 Material.values() は1回だけ！
        Material[] allMaterials = Material.values();

        List<Material> items = new ArrayList<>(allMaterials.length);
        List<String> names = new ArrayList<>(allMaterials.length);

        for (Material mat : allMaterials) {
            if (!mat.isItem()) continue;  // ← 重い処理はここだけ！（初回1回のみ）
            if (mat == Material.AIR) continue;
            if (mat.isLegacy()) continue;

            items.add(mat);
            names.add(mat.name());
        }

        // メモリにキャッシュ
        cachedItems = items;

        // ファイルに保存
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", names);
        config.set("version", "1.0");  // 将来の互換性用
        config.set("generated_at", System.currentTimeMillis());

        try {
            // フォルダが無ければ作る
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            config.save(file);

            long elapsed = System.currentTimeMillis() - start;
            plugin.getLogger().info(
                    "items.yml を生成したよ！ " + items.size() + "個 (" + elapsed + "ms)"
            );
        } catch (IOException e) {
            plugin.getLogger().severe("items.yml の保存に失敗: " + e.getMessage());
        }
    }
}