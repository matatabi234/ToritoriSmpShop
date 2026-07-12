package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
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

import java.util.*;

/**
 * 🔗 Bind用取引一覧GUI
 * 自分が作った取引だけを表示して、クリックで Bindモードに入る
 */
public class BindGui {

    // ===== スロット定数 =====
    public static final int SLOT_PREV_PAGE = 45;
    public static final int SLOT_CANCEL = 49;
    public static final int SLOT_NEXT_PAGE = 53;

    // 商品表示エリア（9〜35 = 27個）
    public static final int ITEMS_PER_PAGE = 27;
    public static final int SLOT_ITEM_START = 9;
    public static final int SLOT_ITEM_END = 35;

    // ===== プレイヤーの状態管理 =====
    private static final Map<UUID, Integer> currentPage = new HashMap<>();
    private static final Map<UUID, Map<Integer, String>> slotToShopId = new HashMap<>();

    /**
     * GUIを開く
     */
    public static void open(Player player) {
        openPage(player, currentPage.getOrDefault(player.getUniqueId(), 0));
    }

    /**
     * 指定ページを開く
     */
    public static void openPage(Player player, int page) {
        // 自分の取引だけ抽出
        List<ShopData> myShops = new ArrayList<>();
        for (ShopData shop : ShopStorage.getAll()) {
            if (shop.getOwnerUuid().equals(player.getUniqueId())) {
                myShops.add(shop);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(myShops.size() / (double) ITEMS_PER_PAGE));

        // ページ範囲チェック
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        currentPage.put(player.getUniqueId(), page);

        // ===== インベントリ作成 =====
        String title = "§d🔗 Bind対象を選ぼう §7[" + (page + 1) + "/" + totalPages + "]";
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(title));

        // ===== 区切りガラス（0-8 と 36-44） =====
        ItemStack glass = createGlass();
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = 36; i < 45; i++) inv.setItem(i, glass);

        // ===== 自分の取引が0件の場合 =====
        if (myShops.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.displayName(Component.text("§c取引がまだ無いよ〜💦")
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("§7まず /torishop で取引を作ってね〜🌅")
                            .decoration(TextDecoration.ITALIC, false)
            ));
            empty.setItemMeta(meta);
            inv.setItem(22, empty); // 中央に配置
        }

        // ===== 商品配置 =====
        Map<Integer, String> slotMap = new HashMap<>();
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, myShops.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopData shop = myShops.get(i);
            int slot = SLOT_ITEM_START + (i - startIndex);
            inv.setItem(slot, createShopIcon(shop));
            slotMap.put(slot, shop.getId());
        }
        slotToShopId.put(player.getUniqueId(), slotMap);

        // ===== 下段ボタン =====
        // ⬅ 前ページ
        if (page > 0) {
            inv.setItem(SLOT_PREV_PAGE, createButton(
                    Material.ARROW,
                    Component.text("⬅ 前のページ", NamedTextColor.YELLOW),
                    List.of(Component.text("ページ " + page + " へ", NamedTextColor.GRAY))
            ));
        } else {
            inv.setItem(SLOT_PREV_PAGE, glass);
        }

        // ❌ キャンセル
        inv.setItem(SLOT_CANCEL, createButton(
                Material.BARRIER,
                Component.text("❌ キャンセル", NamedTextColor.RED),
                List.of(
                        Component.text("クリックで閉じる", NamedTextColor.GRAY),
                        Component.text("Bindモードには入らないよ〜", NamedTextColor.DARK_GRAY)
                )
        ));

        // ➡ 次ページ
        if (page < totalPages - 1) {
            inv.setItem(SLOT_NEXT_PAGE, createButton(
                    Material.ARROW,
                    Component.text("➡ 次のページ", NamedTextColor.YELLOW),
                    List.of(Component.text("ページ " + (page + 2) + " へ", NamedTextColor.GRAY))
            ));
        } else {
            inv.setItem(SLOT_NEXT_PAGE, glass);
        }

        player.openInventory(inv);
    }

    // ===================================================
    // 🎁 商品アイコン作成
    // ===================================================
    private static ItemStack createShopIcon(ShopData shop) {
        // 代表アイコン = 受取リストの1個目のマテリアル
        List<TradeItem> receiveItems = shop.getReceiveItems();
        Material iconMat = (receiveItems.isEmpty())
                ? Material.BARRIER
                : receiveItems.get(0).getMaterial();

        ItemStack icon = new ItemStack(iconMat);
        ItemMeta meta = icon.getItemMeta();

        // タイトル
        String shortId = shop.getId().substring(0, 8);
        meta.displayName(Component.text("取引 #" + shortId, NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));

        // Lore組み立て
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));

        // 支払い
        lore.add(Component.text("💰 支払い:", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        for (TradeItem t : shop.getPayItems()) {
            lore.add(Component.text("  ・" + t.getMaterial().name() + " x" + t.getAmount(),
                    NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }

        // 受取
        lore.add(Component.text("🎁 受取:", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        for (TradeItem t : shop.getReceiveItems()) {
            lore.add(Component.text("  ・" + t.getMaterial().name() + " x" + t.getAmount(),
                    NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.text("━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));

        // ID表示
        lore.add(Component.text("ID: ", NamedTextColor.GRAY)
                .append(Component.text(shop.getId(), NamedTextColor.DARK_GRAY))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text(""));

        // 操作説明
        lore.add(Component.text("🔗 クリックで Bindモード開始！", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("→ 次にクリックした看板に紐付くよ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    // ===================================================
    // 🔧 ヘルパー
    // ===================================================
    private static ItemStack createGlass() {
        ItemStack glass = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.text(" "));
        glass.setItemMeta(meta);
        return glass;
    }

    private static ItemStack createButton(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        // Lore の斜体解除
        List<Component> cleanLore = new ArrayList<>();
        for (Component c : lore) {
            cleanLore.add(c.decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(cleanLore);

        item.setItemMeta(meta);
        return item;
    }

    // ===================================================
    // 🎯 外部から呼ぶ用（Listenerから）
    // ===================================================

    /**
     * スロット番号から ShopID を取得
     */
    public static String getShopIdBySlot(Player player, int slot) {
        Map<Integer, String> slotMap = slotToShopId.get(player.getUniqueId());
        if (slotMap == null) return null;
        return slotMap.get(slot);
    }

    /**
     * 現在のページ番号を取得
     */
    public static int getCurrentPage(Player player) {
        return currentPage.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * セッションクリア（GUIを閉じる時など）
     */
    public static void clearSession(Player player) {
        UUID uuid = player.getUniqueId();
        currentPage.remove(uuid);
        slotToShopId.remove(uuid);
    }
}