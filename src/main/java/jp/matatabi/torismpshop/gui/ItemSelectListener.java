package jp.matatabi.torismpshop.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSelectListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // タイトルで判定（「アイテム選択」で始まるやつ）
        Component title = event.getView().title();
        if (title == null) return;
        String titleStr = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.startsWith("アイテム選択")) return;

        // アイテム移動禁止
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        int currentPage = ItemSelectGui.getCurrentPage(player);

        // 下段の操作ボタン
        if (slot == 45) {
            // 前のページ
            ItemSelectGui.open(player, currentPage - 1);
            return;
        }
        if (slot == 53) {
            // 次のページ
            player.sendMessage("§e[Debug] 現在ページ: " + currentPage + " → " + (currentPage + 1) + " へ");
            ItemSelectGui.open(player, currentPage + 1);
            return;
        }
        if (slot == 49) {
            // 中央のページ情報表示（何もしない）
            return;
        }

// 上段45スロット = アイテム選択エリア
        if (slot >= 0 && slot < 45) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            // 選択したアイテムを取得！
            org.bukkit.Material selectedMaterial = clicked.getType();

            // 🌅 アイテム追加モード中なら → tempMaterial に保存して ItemAddGui に戻る
            if (NewItemSession.isSelecting(player)) {
                // 一時保存（まだリストには入れない！）
                NewItemSession.setTempMaterial(player, selectedMaterial);

                // モード解除
                NewItemSession.stopSelecting(player);

                // メッセージ
                player.sendMessage(Component.text(
                        "🎁 " + selectedMaterial.name() + " を選んだよ！個数も設定してね🌅",
                        NamedTextColor.GREEN
                ));

                // ItemAddGui に戻る（アイテムが入った状態で表示される）
                ItemAddGui.open(player);
                return;
            }

            // 通常モード（今まで通りの処理）
            player.closeInventory();
            player.sendMessage(Component.text(
                    "選択したよ: " + selectedMaterial.name(),
                    NamedTextColor.GREEN
            ));
        }
    }

    /**
     * GUIが閉じられたらページ情報をクリア
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Component title = event.getView().title();
        if (title == null) return;

        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.startsWith("アイテム選択")) return;

        if (event.getPlayer() instanceof Player player) {
            // 🌅 ページ送り中なら clearPage しない！
            if (ItemSelectGui.isSwitching(player)) return;
            ItemSelectGui.clearPage(player);
        }
    }
}