package jp.matatabi.torismpshop.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * 支払い/受取アイテムリストGUIのクリック処理
 */
public class TradeItemListListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // ===== タイトルチェック =====
        Component title = event.getView().title();
        if (title == null) return;

        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        // "💰 支払いアイテム設定" または "🎁 受取アイテム設定" で始まるかチェック
        boolean isPayGui = titleStr.contains("支払いアイテム設定");
        boolean isReceiveGui = titleStr.contains("受取アイテム設定");
        if (!isPayGui && !isReceiveGui) return;

        // ===== 操作キャンセル =====
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // ===== ← 戻るボタン =====
        if (slot == TradeItemListGui.SLOT_BACK) {
            NewItemGui.open(player);
            return;
        }

        // ===== ➕ 追加ボタン =====
        if (slot == TradeItemListGui.SLOT_ADD) {
            // 一時保存をクリア（前回の残骸を消す）
            NewItemSession.clearTempMaterial(player);
            NewItemSession.clearTempAmount(player);
            // アイテム追加GUIを開く
            ItemAddGui.open(player);
            return;
        }

        // ===== 🗑️ 削除モードボタン =====
        if (slot == TradeItemListGui.SLOT_DELETE_MODE) {
            player.sendMessage("§e🚧 削除モードは次のステップで実装するよ〜🌅");
            return;
        }

        // ===== 📦 アイテムクリック（編集） =====
        if (slot >= TradeItemListGui.ITEM_AREA_START
                && slot <= TradeItemListGui.ITEM_AREA_END) {
            player.sendMessage("§e🚧 アイテム編集機能は次のステップで実装するよ〜🌅");
            return;
        }

        // ===== 🏷️ タイトル / 📊 合計 =====
        // 何もしない（クリックしても無視）
    }
}