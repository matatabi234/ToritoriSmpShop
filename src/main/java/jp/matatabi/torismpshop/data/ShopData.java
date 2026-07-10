
package jp.matatabi.torismpshop.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

/**
 * 商品（トレード）1個を表すクラス
 */
public class ShopData {

    private final String id;
    private final String ownerName;
    private final UUID ownerUuid;
    private final long createdAt;
    private final List<TradeItem> payItems;
    private final List<TradeItem> receiveItems;

    public ShopData(String id, String ownerName, UUID ownerUuid, long createdAt,
                    List<TradeItem> payItems, List<TradeItem> receiveItems) {
        this.id = id;
        this.ownerName = ownerName;
        this.ownerUuid = ownerUuid;
        this.createdAt = createdAt;
        this.payItems = payItems;
        this.receiveItems = receiveItems;
    }

    // ===== Getter =====
    public String getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public List<TradeItem> getPayItems() {
        return payItems;
    }

    public List<TradeItem> getReceiveItems() {
        return receiveItems;
    }
    /**
     * YAML保存用に Map に変換
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("owner", ownerName);
        map.put("owner_uuid", ownerUuid.toString());
        map.put("created_at", createdAt);

        List<Map<String, Object>> payList = new ArrayList<>();
        for (TradeItem t : payItems) payList.add(tradeItemToMap(t));
        map.put("pay", payList);

        List<Map<String, Object>> receiveList = new ArrayList<>();
        for (TradeItem t : receiveItems) receiveList.add(tradeItemToMap(t));
        map.put("receive", receiveList);

        return map;
    }

    /**

     /**
     * YAMLから読み込み用（後で /torishop reload で使う）
     */
    public static ShopData fromSection(ConfigurationSection section) {
        String id = section.getString("id");
        String ownerName = section.getString("owner");
        UUID ownerUuid = UUID.fromString(section.getString("owner_uuid"));
        long createdAt = section.getLong("created_at");

        List<TradeItem> payItems = new ArrayList<>();
        List<Map<?, ?>> payList = section.getMapList("pay");
        for (Map<?, ?> m : payList) {
            payItems.add(mapToTradeItem(m));
        }

        List<TradeItem> receiveItems = new ArrayList<>();
        List<Map<?, ?>> receiveList = section.getMapList("receive");
        for (Map<?, ?> m : receiveList) {
            receiveItems.add(mapToTradeItem(m));
        }

        return new ShopData(id, ownerName, ownerUuid, createdAt, payItems, receiveItems);
    }

    // ===================================================
    // 🌅 TradeItem ⇔ Map 変換ヘルパー
    // ===================================================

    /**
     * TradeItem → Map（YAML書き出し用）
     * 🌌 matchMode / tagKey / tagValue も保存
     */
    private static Map<String, Object> tradeItemToMap(TradeItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("material", item.getMaterial().name());
        map.put("amount", item.getAmount());

// 🌌 デフォ状態（何もチェックしない）なら書き出さない → yml スッキリ✨

        // ===== エンチャ判定 =====
        if (item.isCheckEnchant() && !item.getRequiredEnchants().isEmpty()) {
            map.put("checkEnchant", true);
            Map<String, Integer> enchMap = new HashMap<>();
            for (Map.Entry<Enchantment, Integer> e : item.getRequiredEnchants().entrySet()) {
                // Enchantment を "minecraft:sharpness" みたいな文字列に変換
                enchMap.put(e.getKey().getKey().toString(), e.getValue());
            }
            map.put("requiredEnchants", enchMap);
        }

        // ===== カスタム名判定 =====
        if (item.isCheckCustomName() && item.getRequiredCustomName() != null) {
            map.put("checkCustomName", true);
            map.put("requiredCustomName", item.getRequiredCustomName());
        }

        // ===== タグ判定（将来用）=====
        if (item.isCheckTag()) {
            map.put("checkTag", true);
            if (item.getTagKey() != null)   map.put("tagKey", item.getTagKey());
            if (item.getTagValue() != null) map.put("tagValue", item.getTagValue());
        }
        return map;
    }

    /**
     * Map → TradeItem（YAML読み込み用）
     * 🌌 後方互換：matchMode 無ければ MATERIAL_ONLY 扱い
     */
    private static TradeItem mapToTradeItem(Map<?, ?> map) {
        String matName = (String) map.get("material");
        int amount = ((Number) map.get("amount")).intValue();
        Material mat = Material.valueOf(matName);

        // 🌅 まず基本コンストラクタで作る（既存互換）
        TradeItem item = new TradeItem(mat, amount);

        // 🌌 matchMode が保存されてれば読み込む
        Object modeObj = map.get("matchMode");
        if (modeObj != null) {
            try {
                item.setMatchMode(TradeItem.MatchMode.valueOf(modeObj.toString()));
            } catch (IllegalArgumentException e) {
                // 不正な値ならデフォ扱い（何もしない）
            }
        }
        // 🌌 tagKey / tagValue
        Object keyObj = map.get("tagKey");
        if (keyObj != null) {
            item.setTagKey(keyObj.toString());
        }

        Object valObj = map.get("tagValue");
        if (valObj != null) {
            item.setTagValue(valObj.toString());
        }

        return item;
    }

    /**
     * Trade（作成中データ）から ShopData（保存用データ）に変換
     *
     * @param trade      変換元の Trade
     * @param ownerName  保存するプレイヤー名
     * @param ownerUuid  保存するプレイヤーUUID
     * @return 保存用の ShopData
     */
    public static ShopData fromTrade(Trade trade, String ownerName, UUID ownerUuid) {
        // Trade の inputItem/outputItem を List<TradeItem> に変換
        List<TradeItem> payItems = new ArrayList<>();
        if (trade.getInputItem() != null) {
            payItems.add(new TradeItem(
                    trade.getInputItem().getType(),
                    trade.getInputItem().getAmount()
            ));
        }

        List<TradeItem> receiveItems = new ArrayList<>();
        if (trade.getOutputItem() != null) {
            receiveItems.add(new TradeItem(
                    trade.getOutputItem().getType(),
                    trade.getOutputItem().getAmount()
            ));
        }

        return new ShopData(
                trade.getId(),
                ownerName,
                ownerUuid,
                System.currentTimeMillis(),
                payItems,
                receiveItems
        );
    }
}