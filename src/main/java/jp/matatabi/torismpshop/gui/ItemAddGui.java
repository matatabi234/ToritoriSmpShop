package jp.matatabi.torismpshop.gui;

import net.kyori.adventure.text.Component;
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
 * 🌅 アイテム1個を追加するGUI（アイテム / 個数 / 決定）
 * TradeItemListGui の ➕追加ボタンから開かれる
 */
public class ItemAddGui {

    public static final String TITLE_ADD = "§b➕ アイテム追加";
    public static final String TITLE_EDIT = "§b✏️ アイテム編集";


    // スロット定数
    public static final int SLOT_ITEM = 11;      // 🎁 アイテム設定
    public static final int SLOT_AMOUNT = 13;    // 🔢 個数設定
    public static final int SLOT_CONFIRM = 15;   // ✅ 決定
    public static final int SLOT_CANCEL = 22;    // ❌ キャンセル

    /**
     * GUI を開く
     */
    public static void open(Player player) {
        // 🌅 編集モードかどうかでタイトル切り替え
        boolean isEditing = NewItemSession.getEditingIndex(player) >= 0;
        String title = isEditing ? TITLE_EDIT : TITLE_ADD;

        Inventory gui = Bukkit.createInventory(null, 27, Component.text(title));

        // ===== 🎁 アイテム設定 =====
        gui.setItem(SLOT_ITEM, createItemButton(player));

        // ===== 🔢 個数設定 =====
        gui.setItem(SLOT_AMOUNT, createAmountButton(player));

        // ===== ✅ 決定 =====
        gui.setItem(SLOT_CONFIRM, createConfirmButton(player));

        // ===== ❌ キャンセル =====
        gui.setItem(SLOT_CANCEL, createCancelButton());

        player.openInventory(gui);
    }

    /**
     * 🎁 アイテム設定ボタン
     */
    private static ItemStack createItemButton(Player player) {
        Material tempMat = NewItemSession.getTempMaterial(player);

        // まだ選んでない → バリア表示
        Material displayMat = (tempMat != null) ? tempMat : Material.BARRIER;

        ItemStack item = new ItemStack(displayMat);
        ItemMeta meta = item.getItemMeta();

        if (tempMat != null) {
            meta.displayName(Component.text("§a§l🎁 アイテム: " + tempMat.name())
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            meta.displayName(Component.text("§c§l🎁 アイテム: 未設定")
                    .decoration(TextDecoration.ITALIC, false));
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7取引に使うアイテムを選ぶよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで選択").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 🔢 個数設定ボタン
     */
    private static ItemStack createAmountButton(Player player) {
        int amount = NewItemSession.getTempAmount(player);

        ItemStack item = new ItemStack(Material.PAPER, Math.min(amount, 64));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§e§l🔢 個数: " + amount + " 個")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7取引で使う個数を決めるよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックでチャット入力").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * ✅ 決定ボタン（追加 or 更新）
     */
    private static ItemStack createConfirmButton(Player player) {
        boolean isEditing = NewItemSession.getEditingIndex(player) >= 0;

        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (isEditing) {
            meta.displayName(Component.text("§a§l✅ 更新")
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            meta.displayName(Component.text("§a§l✅ 決定")
                    .decoration(TextDecoration.ITALIC, false));
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        if (isEditing) {
            lore.add(Component.text("§7このアイテムの内容を更新するよ").decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("§7このアイテムをリストに追加するよ").decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで確定").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * ❌ キャンセルボタン
     */
    private static ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§c§l❌ キャンセル")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7追加せずに戻るよ").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§e▶ クリックで戻る").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}