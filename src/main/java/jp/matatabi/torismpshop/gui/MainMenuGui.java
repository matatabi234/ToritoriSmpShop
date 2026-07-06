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

public class MainMenuGui {

    // GUIのタイトル（クリック判定にも使う）
    public static final Component TITLE = Component.text("ToriSmpShop 管理画面", NamedTextColor.GOLD);

    /**
     * メインメニューを開く
     */
    public static void open(Player player) {
        // 27スロット（3行）のインベントリを作成
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // 11番スロット：新規作成ボタン
        inv.setItem(11, createButton(
                Material.WRITABLE_BOOK,
                Component.text("新規作成", NamedTextColor.GREEN),
                List.of(
                        Component.text("新しい取引を作成するよ", NamedTextColor.GRAY),
                        Component.text("クリックで開始", NamedTextColor.YELLOW)
                )
        ));

        // 13番スロット：取引一覧ボタン
        inv.setItem(13, createButton(
                Material.CHEST,
                Component.text("取引一覧", NamedTextColor.AQUA),
                List.of(
                        Component.text("登録済みの取引を見る", NamedTextColor.GRAY),
                        Component.text("クリックで一覧表示", NamedTextColor.YELLOW)
                )
        ));

        // 15番スロット：閉じるボタン
        inv.setItem(15, createButton(
                Material.BARRIER,
                Component.text("閉じる", NamedTextColor.RED),
                List.of(
                        Component.text("メニューを閉じる", NamedTextColor.GRAY)
                )
        ));
        player.openInventory(inv);
    }

    /**
     * ボタン用のアイテムを作るヘルパー
     */
    private static ItemStack createButton(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}