package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.TradeItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 支払い/受取アイテムリスト表示GUI
 * EditTarget によって支払い or 受取 を切り替える
 */
public class TradeItemListGui {

    public static final String TITLE_PAY = "§6💰 支払いアイテム設定";
    public static final String TITLE_RECEIVE = "§a🎁 受取アイテム設定";

    // スロット定数
    public static final int SLOT_TITLE = 0;
    public static final int SLOT_TOTAL = 4;
    public static final int SLOT_ADD = 45;
    public static final int SLOT_BACK = 49;
    public static final int SLOT_DELETE_MODE = 53;

    // アイテム表示エリア（9〜35 = 27スロット）
    public static final int ITEM_AREA_START = 9;
    public static final int ITEM_AREA_END = 35;
    public static final int MAX_ITEMS = 27;

    // 最大合計個数（インベントリ制限）
    public static final int MAX_TOTAL_AMOUNT = 3456;


    /**
     * GUI を開く
     * @param player プレイヤー
     * @param target 支払い or 受取
     */
    public static void open(Player player, NewItemSession.EditTarget target) {
        // 編集対象を保存
        NewItemSession.setEditTarget(player, target);

        // タイトル決定
        String title = (target == NewItemSession.EditTarget.PAY)
                ? TITLE_PAY
                : TITLE_RECEIVE;

        Inventory gui = Bukkit.createInventory(null, 54, Component.text(title));

        // ===== アイテムリスト取得 =====
        List<TradeItem> items = (target == NewItemSession.EditTarget.PAY)
                ? NewItemSession.getPayItems(player)
                : NewItemSession.getReceiveItems(player);

        int totalAmount = (target == NewItemSession.EditTarget.PAY)
                ? NewItemSession.getPayTotalAmount(player)
                : NewItemSession.getReceiveTotalAmount(player);

        // ===== 🏷️ タイトル表示 =====
        gui.setItem(SLOT_TITLE, createTitleItem(target));

        // ===== 📊 合計個数表示 =====
        gui.setItem(SLOT_TOTAL, createTotalItem(totalAmount));

        // ===== 📦 アイテムリスト表示 =====
        for (int i = 0; i < items.size() && i < MAX_ITEMS; i++) {
            TradeItem item = items.get(i);
            gui.setItem(ITEM_AREA_START + i, createDisplayItem(item, i));
        }

        // ===== ➕ 追加ボタン =====
        gui.setItem(SLOT_ADD, createAddButton());

        // ===== ← 戻るボタン =====
        gui.setItem(SLOT_BACK, createBackButton());

        // ===== 🗑️ 削除モードボタン =====
        gui.setItem(SLOT_DELETE_MODE, createDeleteButton());
        player.openInventory(gui);
    }


    // ===================================
    // 🎨 各種アイテム作成メソッド
    // ===================================

    /**
     * 🏷️ タイトル表示アイテム
     */
    private static ItemStack createTitleItem(NewItemSession.EditTarget target) {
        Material mat = (target == NewItemSession.EditTarget.PAY)
                ? Material.GOLD_INGOT
                : Material.EMERALD;

        String name = (target == NewItemSession.EditTarget.PAY)
                ? "§6§l💰 支払いアイテム"
                : "§a§l🎁 受取アイテム";

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7ここに追加したアイテムを").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7取引に使用するよ！").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }


    /**
     * 📊 合計個数表示アイテム
     */
    private static ItemStack createTotalItem(int totalAmount) {
        // 上限に近づくと色を変える🌅
        Material mat;
        String color;
        if (totalAmount >= MAX_TOTAL_AMOUNT) {
            mat = Material.RED_STAINED_GLASS_PANE;
            color = "§c";
        } else if (totalAmount >= MAX_TOTAL_AMOUNT * 0.8) {
            mat = Material.YELLOW_STAINED_GLASS_PANE;
            color = "§e";
        } else {
            mat = Material.LIME_STAINED_GLASS_PANE;
            color = "§a";
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§f§l📊 合計個数").decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(color + totalAmount + " §7/ §f" + MAX_TOTAL_AMOUNT + " §7個")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7※ インベントリの上限だよ").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 📦 アイテムリストの各アイテム表示
     */
    private static ItemStack createDisplayItem(TradeItem tradeItem, int index) {
        ItemStack item = new ItemStack(tradeItem.getMaterial(), tradeItem.getAmount());
        ItemMeta meta = item.getItemMeta();

        // 表示名（アイテム名 + 個数）
        meta.displayName(Component.text("§f§l" + tradeItem.getMaterial().name() + " §7x " + tradeItem.getAmount())
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで編集").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  アイテムや個数を変えられるよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        // 🌅 インデックスをloreに埋め込んでおく（後でクリック時に参照）
        lore.add(Component.text("§8index: " + index).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }


    /**
     * ➕ 追加ボタン
     */
    private static ItemStack createAddButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§a§l➕ アイテム追加")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7新しいアイテムを追加するよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで追加").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }


    /**
     * ← 戻るボタン
     */
    private static ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§f§l← 戻る")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7新規作成メニューに戻るよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで戻る").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }


    /**
     * 🗑️ 削除モードボタン
     */
    private static ItemStack createDeleteButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§c§l🗑️ 削除モード")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7アイテムを削除するモードに").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7切り替えるよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで削除モード").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7  （確認画面が出るよ）").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}