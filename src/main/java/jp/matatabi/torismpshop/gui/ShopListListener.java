package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class ShopListListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // タイトルで判定（🏪 or ✏️ どっちも取引一覧GUI）
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());
        if (!title.contains("取引一覧")) return;

        // クリックキャンセル（アイテムを動かせないように）
        event.setCancelled(true);

        int slot = event.getRawSlot();
        Inventory topInv = event.getView().getTopInventory();

        // GUI外クリックは無視
        if (slot < 0 || slot >= topInv.getSize()) return;

        // ===== ⬅ 前ページ =====
        if (slot == ShopListGui.SLOT_PREV_PAGE) {
            int page = ShopListGui.getCurrentPage(player);
            if (page > 0) {
                ShopListGui.openPage(player, page - 1);
            }
            return;
        }

        // ===== ➡ 次ページ =====
        if (slot == ShopListGui.SLOT_NEXT_PAGE) {
            int page = ShopListGui.getCurrentPage(player);
            ShopListGui.openPage(player, page + 1);
            return;
        }

        // ===== ✏️ 編集モード切替 =====
        if (slot == ShopListGui.SLOT_EDIT_MODE) {
            // 🌅 明示的にトグル
            ShopListGui.toggleEditMode(player);

            // トグル後の状態でメッセージ
            boolean isNowEditMode = ShopListGui.isEditMode(player);
            player.sendMessage(isNowEditMode
                    ? "§c✏️ 編集モードを ON にしたよ〜"
                    : "§a✅ 編集モードを OFF にしたよ〜");

            // GUI再描画
            ShopListGui.openPage(player, ShopListGui.getCurrentPage(player));
            return;
        }

        // ===== 🏠 メインGUIへ戻る =====
        if (slot == ShopListGui.SLOT_HOME) {
            ShopListGui.clearSession(player);
            MainMenuGui.open(player);   // ⚠️ メインGUIのメソッド名は要確認！
            return;
        }

        // ===== 🎁 商品アイコンクリック =====
        if (slot >= ShopListGui.SLOT_ITEM_START && slot <= ShopListGui.SLOT_ITEM_END) {
            String shopId = ShopListGui.getShopIdBySlot(player, slot);
            if (shopId == null) return;

            ShopData shop = ShopStorage.getById(shopId);
            if (shop == null) {
                player.sendMessage("§cその商品はもう存在しないみたい💦");
                ShopListGui.openPage(player, ShopListGui.getCurrentPage(player));
                return;
            }

            // 編集モード判定
            if (ShopListGui.isEditMode(player)) {
                // 🔧 左クリック：編集
                if (event.getClick() == ClickType.LEFT) {
                    // ShopData をセッションにロード
                    NewItemSession.loadFromShopData(player, shop);
                    player.sendMessage("§b✏️ 編集モードで開くよ〜🌅");
                    // 編集画面（NewItemGui を再利用）
                    NewItemGui.open(player);
                }
                // 🗑 右クリック：削除
                else if (event.getClick() == ClickType.RIGHT) {
                    ShopStorage.delete(shopId);
                    player.sendMessage("§c🗑 商品を削除したよ！ (id=" + shopId.substring(0, 8) + "...)");
                    ShopListGui.openPage(player, ShopListGui.getCurrentPage(player));
                }
            } else {
                // 通常モード：詳細表示（読み取り専用モードで NewItemGui を再利用）
                NewItemSession.loadFromShopData(player, shop);
                player.sendMessage("§b🔍 商品詳細だよ〜🌅");
                NewItemGui.open(player);
            }
            return;
        }
    }

    /**
     * GUIを閉じたらセッションクリア
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());
        if (!title.contains("取引一覧")) return;

        // ⚠️ ページ移動やモード切替でも Close は発火するので、
        // 完全に閉じたかは判定できない → セッションクリアはやらない方が安全
        // （clearSession は SLOT_HOME クリック時にだけ呼ぶ）
    }
}