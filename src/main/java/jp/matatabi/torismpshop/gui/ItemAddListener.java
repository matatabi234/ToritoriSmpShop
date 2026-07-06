package jp.matatabi.torismpshop.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * 🌅 アイテム追加GUI（ItemAddGui）のクリック処理
 */
public class ItemAddListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // ===== タイトルチェック =====
        Component title = event.getView().title();
        if (title == null) return;

        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.contains("➕ アイテム追加")) return;

        // ===== 操作キャンセル =====
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // ===== 🎁 アイテム設定ボタン =====
        if (slot == ItemAddGui.SLOT_ITEM) {
            // アイテム選択モードON
            NewItemSession.startSelecting(player);
            // アイテム選択GUIを開く
            ItemSelectGui.open(player, 0);
            return;
        }

        // ===== 🔢 個数設定ボタン =====
        if (slot == ItemAddGui.SLOT_AMOUNT) {
            // 個数入力モードON
            NewItemSession.startAmountInput(player);
            player.closeInventory();
            player.sendMessage("§e🔢 個数をチャットで入力してね！（1〜3456）");
            player.sendMessage("§7キャンセルは §fcancel §7って入力してね");
            return;
        }

        // ===== ✅ 決定ボタン =====
        if (slot == ItemAddGui.SLOT_CONFIRM) {
            Material mat = NewItemSession.getTempMaterial(player);
            int amount = NewItemSession.getTempAmount(player);

            // 未設定チェック
            if (mat == null) {
                player.sendMessage("§c❌ アイテムを選んでね！");
                return;
            }
            if (amount <= 0) {
                player.sendMessage("§c❌ 個数を1個以上に設定してね！");
                return;
            }
            // 編集対象取得
            NewItemSession.EditTarget target = NewItemSession.getEditTarget(player);
            if (target == null) {
                player.sendMessage("§c❌ 編集対象が不明だよ！最初からやり直してね💦");
                player.closeInventory();
                return;
            }

            // ===== 合計個数チェック（3456制限） =====
            int currentTotal = (target == NewItemSession.EditTarget.PAY)
                    ? NewItemSession.getPayTotalAmount(player)
                    : NewItemSession.getReceiveTotalAmount(player);

            if (currentTotal + amount > TradeItemListGui.MAX_TOTAL_AMOUNT) {
                player.sendMessage("§c❌ 合計個数が上限（"
                        + TradeItemListGui.MAX_TOTAL_AMOUNT + "個）を超えちゃうよ！");
                player.sendMessage("§7現在: " + currentTotal + " / 追加しようとした: " + amount);
                return;
            }

            // ===== リストに追加！ =====
            NewItemSession.addItem(player, target, mat, amount);

            // ===== 一時保存クリア =====
            NewItemSession.clearTempMaterial(player);
            NewItemSession.clearTempAmount(player);

            // ===== 完了メッセージ =====
            player.sendMessage("§a✅ " + mat.name() + " x " + amount + " を追加したよ！🌅");

            // ===== リストGUIに戻る =====
            TradeItemListGui.open(player, target);
            return;
        }

        // ===== ❌ キャンセルボタン =====
        if (slot == ItemAddGui.SLOT_CANCEL) {
            // 一時保存クリア
            NewItemSession.clearTempMaterial(player);
            NewItemSession.clearTempAmount(player);

            // リストGUIに戻る
            NewItemSession.EditTarget target = NewItemSession.getEditTarget(player);
            if (target != null) {
                TradeItemListGui.open(player, target);
            } else {
                player.closeInventory();
            }
            return;
        }
    }
}