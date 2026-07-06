package jp.matatabi.torismpshop.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * 新規作成メニューGUI
 * 「支払い設定」「受取設定」「決定」の3ボタン
 */
public class NewItemGui {

    // スロット定数
    public static final int SLOT_PAY = 11;      // 💰 支払い設定
    public static final int SLOT_RECEIVE = 13;  // 🎁 受取設定
    public static final int SLOT_CONFIRM = 15;  // ✅ 決定

    /**
     * 新規作成GUIを開く
     */
    public static void open(Player player) {
        Component title = Component.text("新規作成", NamedTextColor.GOLD);
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // ===== セッションからリスト取得 =====
        int payCount = NewItemSession.getPayItems(player).size();
        int payTotal = NewItemSession.getPayTotalAmount(player);

        int receiveCount = NewItemSession.getReceiveItems(player).size();
        int receiveTotal = NewItemSession.getReceiveTotalAmount(player);

        // ===== 💰 支払い設定ボタン =====
        if (payCount > 0) {
            // 設定済み → 金インゴットで表示
            inv.setItem(SLOT_PAY, createButton(
                    Material.GOLD_INGOT,
                    Component.text("💰 支払い設定済み", NamedTextColor.GREEN),
                    Component.text("種類: " + payCount + " 個", NamedTextColor.GRAY),
                    Component.text("合計: " + payTotal + " 個", NamedTextColor.GRAY),
                    Component.text("クリックで編集", NamedTextColor.YELLOW)
            ));
        } else {
            // 未設定 → チェスト表示
            inv.setItem(SLOT_PAY, createButton(
                    Material.CHEST,
                    Component.text("💰 支払い設定", NamedTextColor.YELLOW),
                    Component.text("お客が払うアイテムを設定", NamedTextColor.GRAY),
                    Component.text("クリックで設定", NamedTextColor.GRAY)
            ));
        }

        // ===== 🎁 受取設定ボタン =====
        if (receiveCount > 0) {
            // 設定済み → エメラルドで表示
            inv.setItem(SLOT_RECEIVE, createButton(
                    Material.EMERALD,
                    Component.text("🎁 受取設定済み", NamedTextColor.GREEN),
                    Component.text("種類: " + receiveCount + " 個", NamedTextColor.GRAY),
                    Component.text("合計: " + receiveTotal + " 個", NamedTextColor.GRAY),
                    Component.text("クリックで編集", NamedTextColor.YELLOW)
            ));
        } else {
            // 未設定 → チェスト表示
            inv.setItem(SLOT_RECEIVE, createButton(
                    Material.CHEST,
                    Component.text("🎁 受取設定", NamedTextColor.YELLOW),
                    Component.text("お客が受け取るアイテムを設定", NamedTextColor.GRAY),
                    Component.text("クリックで設定", NamedTextColor.GRAY)
            ));
        }

        // ===== ✅ 決定ボタン =====
        // 支払い・受取どっちも1個以上あれば緑
        boolean isReady = (payCount > 0 && receiveCount > 0);

        if (isReady) {
            inv.setItem(SLOT_CONFIRM, createButton(
                    Material.LIME_WOOL,
                    Component.text("✅ 決定", NamedTextColor.GREEN),
                    Component.text("クリックで登録するよ", NamedTextColor.GRAY)
            ));
        } else {
            inv.setItem(SLOT_CONFIRM, createButton(
                    Material.RED_WOOL,
                    Component.text("❌ 未設定", NamedTextColor.RED),
                    Component.text("支払いと受取を", NamedTextColor.GRAY),
                    Component.text("両方設定してね", NamedTextColor.GRAY)
            ));
        }

        player.openInventory(inv);
    }

    /**
     * ボタン作成のヘルパーメソッド
     */
    private static ItemStack createButton(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore.length > 0) {
                meta.lore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}