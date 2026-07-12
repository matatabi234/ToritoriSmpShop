package jp.matatabi.torismpshop.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomItemSelectGuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // ===== タイトルチェック =====
        Component title = event.getView().title();
        if (title == null) return;

        // 2. プレイヤーのインベントリがクリックされた場合
        if (event.getClickedInventory() == event.getView().getBottomInventory()) {
            // ここで true をセットしなければ、プレイヤーは自由にインベントリ操作が可能
            // つまり、ここは何も書かなくてOK（またはイベントをキャンセルしない）
            return;
        }

        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        // 🌅 追加モード / 編集モード どっちも受け付ける
        if (!titleStr.contains("➕ カスタムアイテム追加")) return;

        // ===== 操作キャンセル =====
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        if (slot == CustomItemSelectGui.SLOT_ITEM) {
            event.setCancelled(false);
        }

//        // ===== 🎁 アイテム設定ボタン =====
//        if (slot == ItemAddGui.SLOT_ITEM) {
//            // アイテム選択モードON
//            NewItemSession.startSelecting(player);
//            // アイテム選択GUIを開く
//            ItemSelectGui.open(player, 0);
//            return;
//        }

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
            // 1. GUIのアイテム設定スロットからItemStackを直接取得する
            ItemStack itemInSlot = event.getInventory().getItem(CustomItemSelectGui.SLOT_ITEM);

            // 2. アイテムが置かれているか確認し、安全なクローンを保存する
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) {
                ItemStack itemToSave = itemInSlot.clone(); // NBTなど全て含むItemStackを保存
                NewItemSession.setTempItem(player, itemToSave);
            } else {
                player.sendMessage("§c❌ アイテムスロットにアイテムを置いてね！");
                return;
            }

            int amount = NewItemSession.getTempAmount(player);

            int editIndex = NewItemSession.getEditingIndex(player);
            boolean isEditing = (editIndex >= 0);

            ItemStack confirmItem = event.getInventory().getItem(ItemAddGui.SLOT_CONFIRM);
            if (confirmItem == null || confirmItem.getType() == Material.AIR) {
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
                player.sendMessage("§7現在: " + currentTotal + " / " + (isEditing ? "変更後" : "追加") + ": " + amount);
                return;
            }

            // 1. まず、処理に必要な変数を準備
            ItemStack item = NewItemSession.getTempItem(player); // 選択済みのアイテムを取得
            if (item == null) {
                player.sendMessage("§c❌ アイテムが選択されていないよ！");
                return;
            }

            // 2. どちらのモードでも同じ ItemStack と amount を渡せるようにする
            if (isEditing) {
                // 🌅 更新モード
                NewItemSession.updateItem(player, target, editIndex, item, amount);
                player.sendMessage("§a✅ アイテムを " + amount + " 個に更新したよ！🌅");
            } else {
                // 🌅 新規追加モード
                NewItemSession.addItem(player, target, item, amount);
                player.sendMessage("§a✅ アイテムを " + amount + " 個追加したよ！🌅");
            }

            // ===== 一時保存クリア =====
            NewItemSession.clearTempMaterial(player);
            NewItemSession.clearTempAmount(player);
            NewItemSession.clearEditingIndex(player);  // 🌅 編集モードも解除！
            NewItemSession.clearTempItem(player);

            // ===== リストGUIに戻る =====
            TradeItemListGui.open(player, target);
            return;
        }

        // ===== ❌ キャンセルボタン =====
        if (slot == ItemAddGui.SLOT_CANCEL) {
            // 一時保存クリア
            NewItemSession.clearTempMaterial(player);
            NewItemSession.clearTempAmount(player);
            NewItemSession.clearEditingIndex(player);
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

    private static final Map<UUID, Boolean> CustomMode = new HashMap<>();

    public static boolean isCustomMode(Player player) {
        return CustomMode.getOrDefault(player.getUniqueId(), false);
    }
}
