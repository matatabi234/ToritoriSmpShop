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

        boolean isPayGui = titleStr.contains("支払いアイテム設定");
        boolean isReceiveGui = titleStr.contains("受取アイテム設定");
        boolean isDeleteGui = titleStr.contains("🗑️ 削除モード中");
        if (!isPayGui && !isReceiveGui && !isDeleteGui) return;

        // ===== 操作キャンセル =====
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        // 🌅 target判定（先に済ませる！）
        NewItemSession.EditTarget target;
        if (isDeleteGui) {
            target = NewItemSession.getEditTarget(player);
            if (target == null) {
                player.closeInventory();
                return;
            }
        } else {
            target = isPayGui
                    ? NewItemSession.EditTarget.PAY
                    : NewItemSession.EditTarget.RECEIVE;
        }

        // ===== ← 戻るボタン =====
        if (slot == TradeItemListGui.SLOT_BACK) {
            // 🌅 削除モード解除しとく
            NewItemSession.disableDeleteMode(player);
            NewItemGui.open(player);
            return;
        }

        // ===== ➕ 追加ボタン =====
        if (slot == TradeItemListGui.SLOT_ADD) {
            // 削除モード中はボタン無効
            if (NewItemSession.isDeleteMode(player)) {
                player.sendMessage("§c❌ 削除モード中は追加できないよ！");
                return;
            }
            NewItemSession.clearTempMaterial(player);
            NewItemSession.clearTempAmount(player);
            ItemAddGui.open(player);
            return;
        }

        // ===== 🗑️ 削除モードボタン（トグル） =====
        if (slot == TradeItemListGui.SLOT_DELETE_MODE) {
            if (NewItemSession.isDeleteMode(player)) {
                NewItemSession.disableDeleteMode(player);
                player.sendMessage("§a✅ 通常モードに戻ったよ〜🌅");
            } else {
                NewItemSession.enableDeleteMode(player);
                player.sendMessage("§c🗑️ 削除モードON！アイテムをクリックで削除できるよ");
            }
            // GUI再描画
            TradeItemListGui.open(player, target);
            return;
        }

        // ===== 📦 アイテムクリック（編集 or 削除） =====
        if (slot >= TradeItemListGui.ITEM_AREA_START
                && slot <= TradeItemListGui.ITEM_AREA_END) {

            // スロット → インデックス変換
            int index = slot - TradeItemListGui.ITEM_AREA_START;

            // リスト取得
            var list = (target == NewItemSession.EditTarget.PAY)
                    ? NewItemSession.getPayItems(player)
                    : NewItemSession.getReceiveItems(player);

            // 範囲外チェック（空スロットクリック）
            if (index < 0 || index >= list.size()) {
                return;
            }

            var tradeItem = list.get(index);

// 🌅 削除モードか編集モードかで分岐！
            if (NewItemSession.isDeleteMode(player)) {
                // ===== 🗑️ 削除 =====
                NewItemSession.removeItem(player, target, index);

                // 💡 表示名を取得する補助メソッドを使い、カスタム名があればそれを表示
                String displayName = tradeItem.getItemStack().hasItemMeta() && tradeItem.getItemStack().getItemMeta().hasDisplayName()
                        ? PlainTextComponentSerializer.plainText().serialize(tradeItem.getItemStack().getItemMeta().displayName())
                        : tradeItem.getItemStack().getType().name();

                player.sendMessage("§c🗑️ " + displayName + " x " + tradeItem.getAmount() + " を削除したよ！");
                TradeItemListGui.open(player, target);
            } else {
                // ===== ✏️ 編集 =====
                // 💡 startEditing に Material ではなく ItemStack を渡すように変更
                NewItemSession.startEditing(player, index, tradeItem.getItemStack(), tradeItem.getAmount());

                String displayName = tradeItem.getItemStack().hasItemMeta() && tradeItem.getItemStack().getItemMeta().hasDisplayName()
                        ? PlainTextComponentSerializer.plainText().serialize(tradeItem.getItemStack().getItemMeta().displayName())
                        : tradeItem.getItemStack().getType().name();

                player.sendMessage("§b✏️ " + displayName + " x " + tradeItem.getAmount() + " を編集するよ〜🌅");

                // 💡 ここでカスタムアイテム判定をして、開くGUIを出し分ける！
                if (NewItemSession.isCustomItem(tradeItem.getItemStack())) {
                    CustomItemSelectGui.open(player);
                } else {
                    ItemAddGui.open(player);
                }
            }
            return;
        }

        // ===== 🏷️ タイトル / 📊 合計 =====
        // 何もしない
    }
}