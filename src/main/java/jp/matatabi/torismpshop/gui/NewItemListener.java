package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 新規作成メニューGUIのクリック処理
 */
public class NewItemListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // タイトルチェック
        Component title = event.getView().title();
        if (title == null) return;

        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.equals("新規作成")) return;

        // GUIの操作をキャンセル（アイテム取れないように）
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // ===== 💰 支払い設定 =====
        if (slot == NewItemGui.SLOT_PAY) {
            TradeItemListGui.open(player, NewItemSession.EditTarget.PAY);
            return;
        }

        // ===== 🎁 受取設定 =====
        if (slot == NewItemGui.SLOT_RECEIVE) {
            TradeItemListGui.open(player, NewItemSession.EditTarget.RECEIVE);
            return;
        }

// ===== ✅ 決定 =====
        if (slot == NewItemGui.SLOT_CONFIRM) {
            // 未設定チェック（支払い・受取どっちも1個以上）
            int payCount = NewItemSession.getPayItems(player).size();
            int receiveCount = NewItemSession.getReceiveItems(player).size();

            if (payCount == 0 || receiveCount == 0) {
                player.sendMessage("§c支払いと受取を両方設定してね！");
                return;
            }

            // 🌅 ===== ShopData 組み立て（超シンプル！） =====
            String shopId = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();

            ShopData shop = new ShopData(
                    shopId,
                    player.getName(),
                    player.getUniqueId(),
                    now,
                    NewItemSession.getPayItems(player),      // そのまま渡せる！🌅
                    NewItemSession.getReceiveItems(player)   // そのまま渡せる！🌅
            );

            // 🌅 ===== 保存実行！ =====
            ShopStorage.save(shop);

            // ===== 完了メッセージ =====
            player.sendMessage("§a✅ 商品を登録したよ〜🌅");
            player.sendMessage("§7ID: §f" + shopId.substring(0, 8) + "...");
            player.sendMessage("§7支払い: " + payCount + " 種類 / 合計 "
                    + NewItemSession.getPayTotalAmount(player) + " 個");
            player.sendMessage("§7受取: " + receiveCount + " 種類 / 合計 "
                    + NewItemSession.getReceiveTotalAmount(player) + " 個");

            // セッションクリア
            NewItemSession.clear(player);

            // GUIを閉じる
            player.closeInventory();
            return;
        }
    }
}