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

            // 🌅 スロット → リストのインデックスに変換
            int index = slot - TradeItemListGui.ITEM_AREA_START;

            // 現在の編集対象取得
            NewItemSession.EditTarget target = NewItemSession.getEditTarget(player);
            if (target == null) {
                player.sendMessage("§c❌ 編集対象が不明だよ！最初からやり直してね💦");
                return;
            }

            // リスト取得
            var list = (target == NewItemSession.EditTarget.PAY)
                    ? NewItemSession.getPayItems(player)
                    : NewItemSession.getReceiveItems(player);

            // 範囲外チェック（空スロットをクリックした場合）
            if (index < 0 || index >= list.size()) {
                return;  // 何もしない
            }

            // 編集対象のアイテムを取得
            var tradeItem = list.get(index);

            // 🌅 編集モード開始！（インデックス保存 + 一時保存に既存値セット）
            NewItemSession.startEditing(player, index, tradeItem.getMaterial(), tradeItem.getAmount());

            // メッセージ
            player.sendMessage("§b✏️ " + tradeItem.getMaterial().name()
                    + " x " + tradeItem.getAmount() + " を編集するよ〜🌅");

            // ItemAddGui を編集モードで開く（tempMaterial / tempAmount に既存値が入ってる状態）
            ItemAddGui.open(player);
            return;
        }

        // ===== 🏷️ タイトル / 📊 合計 =====
        // 何もしない（クリックしても無視）
    }
}