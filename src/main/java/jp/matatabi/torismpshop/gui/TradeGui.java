package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.ShopData;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🌅 取引画面GUI
 * プレイヤーが他のプレイヤーの取引をクリックしたときに開く画面
 */
public class TradeGui {

    public static final String TITLE_PREFIX = "§6取引: ";

    // ===== スロット定数 =====
    // 💰 支払うアイテム表示エリア
    public static final int PAY_START = 0;
    public static final int PAY_END = 17;      // 0〜17 (18スロット)

    // 🔄 装飾ライン
    public static final int DECO_START = 18;
    public static final int DECO_END = 26;     // 18〜26

    // 🎁 受け取るアイテム表示エリア
    public static final int RECEIVE_START = 27;
    public static final int RECEIVE_END = 44;  // 27〜44 (18スロット)

    // ボタン
    public static final int SLOT_BACK = 45;    // ⬅ 戻る
    public static final int SLOT_CONFIRM = 49; // ✅ 取引実行
    public static final int SLOT_CANCEL = 53;  // ❌ キャンセル

    // 🌅 表示中の ShopData を記憶（クリック時に使う）
    private static final Map<UUID, String> viewingShopId = new HashMap<>();

    /**
     * 取引GUI を開く
     */
    public static void open(Player player, ShopData shop) {
        // 表示中のショップIDを記憶
        viewingShopId.put(player.getUniqueId(), shop.getId());

        String title = TITLE_PREFIX + shop.getOwnerName();
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(title));

        // ===== 💰 支払うアイテム表示 =====
        placeItems(gui, shop.getPayItems(), PAY_START, PAY_END);

        // ===== 🔄 装飾ライン（交換マーク） =====
        for (int i = DECO_START; i <= DECO_END; i++) {
            if (i == 22) {
                // 中央に矢印
                gui.setItem(i, createArrow());
            } else {
                gui.setItem(i, createGlassPane());
            }
        }

        // ===== 🎁 受け取るアイテム表示 =====
        placeItems(gui, shop.getReceiveItems(), RECEIVE_START, RECEIVE_END);

        // ===== ⬅ 戻る =====
        gui.setItem(SLOT_BACK, createBackButton());

        // ===== ✅ 取引実行 =====
        gui.setItem(SLOT_CONFIRM, createConfirmButton());

        // ===== ❌ キャンセル =====
        gui.setItem(SLOT_CANCEL, createCancelButton());

        player.openInventory(gui);
    }

    /**
     * 🌅 アイテムを指定範囲に配置する
     * 64個以下 → アイコンをスタック表示
     * 64個超え → アイコン x1 + Lore に個数
     */
    private static void placeItems(Inventory gui, List<TradeItem> items, int startSlot, int endSlot) {
        int slot = startSlot;
        for (TradeItem shopItem : items) {
            if (slot > endSlot) break;  // スロット足りない場合は打ち切り

            Material mat = shopItem.getMaterial();
            int amount = shopItem.getAmount();

            ItemStack display;
            if (amount <= 64) {
                // 🌅 64個以下：スタック数で表示
                display = new ItemStack(mat, amount);
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text("§f" + mat.name() + " §7x " + amount)
                            .decoration(TextDecoration.ITALIC, false));
                    display.setItemMeta(meta);
                }
            } else {
                // 🌅 64個超え：アイコン x1 + Lore で個数明記
                display = new ItemStack(mat, 1);
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text("§f" + mat.name())
                            .decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("§7━━━━━━━━━━━━━")
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("§e§l個数: §f§l" + amount + " 個")
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("§7━━━━━━━━━━━━━")
                            .decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                    display.setItemMeta(meta);
                }
            }

            gui.setItem(slot, display);
            slot++;
        }
    }

    /**
     * 🔄 交換矢印（中央）
     */
    private static ItemStack createArrow() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§6§l🔄 交換")
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7上のアイテムを払うと").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§7下のアイテムが手に入るよ").decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 装飾ガラスパネル
     */
    private static ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * ⬅ 戻るボタン
     */
    private static ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§7§l⬅ 戻る")
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7一覧に戻るよ").decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * ✅ 取引実行ボタン
     */
    private static ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§l✅ 取引実行")
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§7支払うアイテムを持っていれば").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§7取引が成立するよ").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§e▶ クリックで実行").decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * ❌ キャンセルボタン
     */
    private static ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§l❌ キャンセル")
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7取引せずに閉じるよ").decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 🌅 表示中の ShopID を取得（Listener 用）
     */
    public static String getViewingShopId(Player player) {
        return viewingShopId.get(player.getUniqueId());
    }

    /**
     * 🌅 表示中の ShopID をクリア
     */
    public static void clearViewingShopId(Player player) {
        viewingShopId.remove(player.getUniqueId());
    }
}