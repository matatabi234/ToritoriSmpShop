package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.PlayerSettingsManager;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
        if (!titleStr.contains("取引: ")) return;

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

        // 🌅 自分の取引は実行できないようにする（ただし、設定で許可されている場合は例外）
        if (shop.getOwnerUuid().equals(player.getUniqueId())) {
            // 💡 設定を取得（PlayerSettingsManager がなければ false を返すようにしておく）
            boolean canSelfTrade = PlayerSettingsManager.get(player.getUniqueId()).isAllowSelfTrade();

            if (!canSelfTrade) {
                player.sendMessage("§c自分の取引は実行できないよ〜🙈 (設定で許可すればOK)");
                return;
            }
        }

        PlayerInventory inv = player.getInventory();
        List<TradeItem> payItems = shop.getPayItems();
        List<TradeItem> receiveItems = shop.getReceiveItems();

        // ===== ① 支払い分を持っているかチェック（TradeItem単位）=====
        for (TradeItem ti : payItems) {
            int have = countSatisfyingItems(inv, ti);
            if (have < ti.getAmount()) {
                player.sendMessage("§c§l✖ 必要な条件を満たすアイテムが足りないよ！");

                // 🌌 表示メッセージも MatchMode で分岐
                String label = ti.getMaterial().name();
                if (ti.getMatchMode() == TradeItem.MatchMode.HAS_TAG) {
                    label += " [タグ: " + ti.getTagKey()
                            + (ti.getTagValue() != null ? "=" + ti.getTagValue() : "") + "]";
                }

                player.sendMessage("§7必要: §f" + label + " x " + ti.getAmount()
                        + " §7(§c不足 " + (ti.getAmount() - have) + "個§7)");
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

        // ===== ③ 支払い分を削除（修正版）=====
        for (TradeItem ti : payItems) {
            int remainingToRemove = ti.getAmount();
            ItemStack[] contents = inv.getContents();

            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item == null || item.getType() == Material.AIR) continue;

                // 条件を満たすアイテムなら削除対象にする
                if (TradeGui.isSatisfiedBy(item, ti.getItemStack())) {
                    int amount = item.getAmount();
                    if (amount <= remainingToRemove) {
                        remainingToRemove -= amount;
                        inv.setItem(i, null); // アイテムを消去
                    } else {
                        item.setAmount(amount - remainingToRemove);
                        remainingToRemove = 0;
                    }
                }
                if (remainingToRemove <= 0) break;
            }
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
     * 🌌 ItemStack が TradeItem の条件を満たすか判定
     * MatchMode に応じて分岐する
     */
    private boolean matchesTradeItem(ItemStack playerItem, TradeItem tradeItem) {
        if (playerItem == null || playerItem.getType() == Material.AIR) return false;

        // 💡 以前は Material 比較でしたが、ItemStack.isSimilar を使います
        // isSimilar は、エンチャント、カスタム名、Lore、NBT(PDC)まで完全に比較します！
        ItemStack target = tradeItem.getItemStack();

        // 素材が一致 かつ メタデータ(エンチャント等)が一致しているか
        return playerItem.isSimilar(target) && playerItem.getAmount() >= tradeItem.getAmount();
    }

    /**
     * 🌌 PDC タグを持ってるかチェック
     * tagValue が null なら「キーが存在するだけでOK」
     */
    private boolean hasTag(ItemStack item, String tagKey, String tagValue) {
        if (tagKey == null) return false;
        if (!item.hasItemMeta()) return false;

        org.bukkit.persistence.PersistentDataContainer pdc =
                item.getItemMeta().getPersistentDataContainer();

        // tagKey は "namespace:key" 形式を想定
        String[] parts = tagKey.split(":", 2);
        if (parts.length != 2) return false;

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(parts[0], parts[1]);

        // STRING型で判定（他の型は将来対応）
        if (!pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return false;
        }

        // tagValue が null → キー存在するだけでOK
        if (tagValue == null) return true;
        String actual = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        return tagValue.equals(actual);
    }

    /**
     * 🌅 インベントリ内の指定Materialの合計数を数える
     */
    private int countTradeItem(PlayerInventory inv, TradeItem ti) {
        int count = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (matchesTradeItem(item, ti)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * 🌅 インベントリから指定Materialを指定数だけ削除
     */
    private void removeTradeItem(PlayerInventory inv, TradeItem ti, int amount) {
        int remaining = amount;
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (remaining <= 0) break;
            ItemStack item = contents[i];
            if (matchesTradeItem(item, ti)) {
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

    private int countSatisfyingItems(PlayerInventory inv, TradeItem ti) {
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            // 💡 ここで以前作った「条件包含判定」を呼び出す
            if (TradeGui.isSatisfiedBy(item, ti.getItemStack())) {
                count += item.getAmount();
            }
        }
        return count;
    }
}