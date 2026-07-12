package jp.matatabi.torismpshop.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MainMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!MainMenuGui.TITLE.equals(event.getView().title())) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 11 -> {
                // 新規作成ボタン → 新規作成メニューを開く
                NewItemSession.clear(player);  // 🌅 前回のデータをクリア
                NewItemGui.open(player);
            }
            case 13 -> {
                ShopListGui.openPage(player, 0);
            }
            case 15 -> {
                player.closeInventory();
            }
        }
    }
}