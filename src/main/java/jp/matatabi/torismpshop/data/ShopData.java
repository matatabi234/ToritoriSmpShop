
package jp.matatabi.torismpshop.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     */
    private static Map<String, Object> tradeItemToMap(TradeItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("material", item.getMaterial().name());
        map.put("amount", item.getAmount());
        return map;
    }

    /**
     * Map → TradeItem（YAML読み込み用）
     */
    private static TradeItem mapToTradeItem(Map<?, ?> map) {
        String matName = (String) map.get("material");
        int amount = (int) map.get("amount");
        Material mat = Material.valueOf(matName);
        return new TradeItem(mat, amount);
    }
}