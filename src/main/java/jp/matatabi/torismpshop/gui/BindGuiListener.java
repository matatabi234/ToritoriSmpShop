package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.BindModeManager;
import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * 🔗 BindGui のクリック処理
 */
public class BindGuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // タイトルで判定
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());
        if (!title.contains("Bind対象を選ぼう")) return;

        // クリックキャンセル（アイテムを動かせないように）
        event.setCancelled(true);

        int slot = event.getRawSlot();
        Inventory topInv = event.getView().getTopInventory();

        // GUI外クリックは無視
        if (slot < 0 || slot >= topInv.getSize()) return;

        // ===== ⬅ 前ページ =====
        if (slot == BindGui.SLOT_PREV_PAGE) {
            int page = BindGui.getCurrentPage(player);
            if (page > 0) {
                BindGui.openPage(player, page - 1);
            }
            return;
        }

        // ===== ➡ 次ページ =====
        if (slot == BindGui.SLOT_NEXT_PAGE) {
            int page = BindGui.getCurrentPage(player);
            BindGui.openPage(player, page + 1);
            return;
        }

        // ===== ❌ キャンセル =====
        if (slot == BindGui.SLOT_CANCEL) {
            BindGui.clearSession(player);
            player.closeInventory();
            player.sendMessage(Component.text("§7Bind をキャンセルしたよ〜"));
            return;
        }

        // ===== 🎁 取引アイコンクリック → Bindモード開始 =====
        if (slot >= BindGui.SLOT_ITEM_START && slot <= BindGui.SLOT_ITEM_END) {
            String shopId = BindGui.getShopIdBySlot(player, slot);
            if (shopId == null) return;

            // ShopData 取得
            ShopData shop = ShopStorage.getById(shopId);
            if (shop == null) {
                player.sendMessage(Component.text("§cその取引はもう存在しないみたい💦"));
                BindGui.openPage(player, BindGui.getCurrentPage(player));
                return;
            }

            // 念のため所有者チェック（他人のはbindできない）
            if (!shop.getOwnerUuid().equals(player.getUniqueId())) {
                player.sendMessage(Component.text("§c自分の取引しか bind できないよ〜🙈"));
                return;
            }

            // ===== 表示名の決定 =====
            // GUI経由の場合は表示名指定できないので、デフォルトで "取引 #xxxxxxxx" を使う
            String defaultDisplayName = "取引 #" + shopId.substring(0, 8);

            // ===== Bindモード開始 =====
            BindModeManager.setNewBind(player, shopId, defaultDisplayName);
            BindGui.clearSession(player);
            player.closeInventory();

            player.sendMessage(Component.text("§a✅ Bind モード開始！"));
            player.sendMessage(Component.text(
                    "§7次にクリックした看板に §f[" + defaultDisplayName + "] §7を紐付けるよ🔗"
            ));
            player.sendMessage(Component.text(
                    "§7キャンセルは §e/torishop bind cancel §7でどうぞ〜"
            ));

            return;
        }
    }
}