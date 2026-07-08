package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
import jp.matatabi.torismpshop.data.TradeItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🌅 取引画面GUI のクリック処理
 */
public class TradeListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // タイトルチェック
        Component title = event.getView().title();
        if (title == null) return;
        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.startsWith("🤝 取引: ")) return;

        // 🌅 全操作キャンセル（アイテム持ち出し防止）
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // ===== ⬅ 戻る =====
        if (slot == TradeGui.SLOT_BACK) {
            TradeGui.clearViewingShopId(player);
            player.closeInventory();
            // TODO: ShopListGui.open(player); を呼ぶならここ
            return;
        }

        // ===== ❌ キャンセル =====
        if (slot == TradeGui.SLOT_CANCEL) {
            TradeGui.clearViewingShopId(player);
            player.closeInventory();
            player.sendMessage("§7取引をキャンセルしたよ");
            return;
        }

        // ===== ✅ 取引実行 =====
        if (slot == TradeGui.SLOT_CONFIRM) {
            executeTrade(player);
            return;
        }
    }

    /**
     * 🌅 取引実行のメイン処理
     */
    private void executeTrade(Player player) {
        // ===== 表示中のShopIDを取得 =====
        String shopId = TradeGui.getViewingShopId(player);
        if (shopId == null) {
            player.sendMessage("§c取引情報が見つからないよ💦");
            player.closeInventory();
            return;
        }

        // ===== ShopData を取得 =====
        ShopData shop = ShopStorage.getById(shopId);
        if (shop == null) {
            player.sendMessage("§cこの取引はもう存在しないよ💦");
            TradeGui.clearViewingShopId(player);
            player.closeInventory();
            return;
        }

        // ===== 自分の取引は実行できないようにする（お好み）=====
        if (shop.getOwnerUuid().equals(player.getUniqueId().toString())) {
            player.sendMessage("§c自分の取引は実行できないよ〜🙈");
            return;
        }

        PlayerInventory inv = player.getInventory();
        List<TradeItem> payItems = shop.getPayItems();
        List<TradeItem> receiveItems = shop.getReceiveItems();

        // ===== ① 支払い分を持っているかチェック =====
        Map<Material, Integer> needed = new HashMap<>();
        for (TradeItem ti : payItems) {
            needed.merge(ti.getMaterial(), ti.getAmount(), Integer::sum);
        }

        for (Map.Entry<Material, Integer> e : needed.entrySet()) {
            int have = countMaterial(inv, e.getKey());
            if (have < e.getValue()) {
                player.sendMessage("§c§l✖ 支払うアイテムが足りないよ！");
                player.sendMessage("§7必要: §f" + e.getKey().name() + " x " + e.getValue()
                        + " §7(§c不足 " + (e.getValue() - have) + "個§7)");
                return;
            }
        }

        // ===== ② インベントリ空きチェック（簡易）=====
        int emptySlots = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        // 🌅 receive のアイテムが必要とするスロット数を概算
        int neededSlots = 0;
        for (TradeItem ti : receiveItems) {
            int max = ti.getMaterial().getMaxStackSize();
            neededSlots += (int) Math.ceil((double) ti.getAmount() / max);
        }

        // 🌅 pay を消費するので、pay分のスロットは空くと想定
        int payWillFreeSlots = 0;
        for (TradeItem ti : payItems) {
            int max = ti.getMaterial().getMaxStackSize();
            payWillFreeSlots += (int) Math.ceil((double) ti.getAmount() / max);
        }

        if (emptySlots + payWillFreeSlots < neededSlots) {
            player.sendMessage("§c§l✖ インベントリに空きがないよ！");
            player.sendMessage("§7いらないアイテムを片付けてから、もう一度どうぞ〜");
            return;
        }

        // ===== ③ 支払い分を削除 =====
        for (Map.Entry<Material, Integer> e : needed.entrySet()) {
            removeMaterial(inv, e.getKey(), e.getValue());
        }

        // ===== ④ 受け取り分を付与 =====
        for (TradeItem ti : receiveItems) {
            int remaining = ti.getAmount();
            int maxStack = ti.getMaterial().getMaxStackSize();

            while (remaining > 0) {
                int give = Math.min(remaining, maxStack);
                ItemStack stack = new ItemStack(ti.getMaterial(), give);
                Map<Integer, ItemStack> leftover = inv.addItem(stack);

                // 入りきらなかった分は足元にドロップ
                if (!leftover.isEmpty()) {
                    Location loc = player.getLocation();
                    for (ItemStack drop : leftover.values()) {
                        player.getWorld().dropItemNaturally(loc, drop);
                    }
                    player.sendMessage("§eインベに入りきらなかった分は足元にドロップしたよ〜🌅");
                }
                remaining -= give;
            }
        }

        // ===== ⑤ 成功メッセージ =====
        player.sendMessage("§a§l✅ 取引成立！");
        player.sendMessage("§7" + shop.getOwnerName() + " §7さんの取引を利用したよ🤝");
        player.playSound(player.getLocation(),
                org.bukkit.Sound.ENTITY_VILLAGER_YES, 1.0f, 1.2f);
    }

    /**
     * 🌅 インベントリ内の指定Materialの合計数を数える
     */
    private int countMaterial(PlayerInventory inv, Material mat) {
        int count = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item != null && item.getType() == mat) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * 🌅 インベントリから指定Materialを指定数だけ削除
     */
    private void removeMaterial(PlayerInventory inv, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (remaining <= 0) break;
            ItemStack item = contents[i];
            if (item != null && item.getType() == mat) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    inv.setItem(i, null);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }

    /**
     * 🌅 GUI閉じたら記憶をクリア
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Component title = event.getView().title();
        if (title == null) return;
        String titleStr = PlainTextComponentSerializer.plainText().serialize(title);
        if (!titleStr.startsWith("🤝 取引: ")) return;

        if (event.getPlayer() instanceof Player player) {
            TradeGui.clearViewingShopId(player);
        }
    }
}