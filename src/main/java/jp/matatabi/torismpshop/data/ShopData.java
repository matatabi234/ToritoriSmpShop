package jp.matatabi.torismpshop.data;

import org.bukkit.configuration.ConfigurationSection;

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

        // 💡 既存のリストを Map に変換するループをシンプルにする
        map.put("pay", payItems);       // ConfigurationSerializable が自動でリストをMap化する
        map.put("receive", receiveItems);

        return map;
    }

    /**
     * /**
     * YAMLから読み込み用（後で /torishop reload で使う）
     */
    public static ShopData fromSection(ConfigurationSection section) {
        String id = section.getString("id");
        String ownerName = section.getString("owner");
        UUID ownerUuid = UUID.fromString(section.getString("owner_uuid"));
        long createdAt = section.getLong("created_at");

        // 💡 リストとして取得するだけで、TradeItem型として自動復元されます
        List<TradeItem> payItems = (List<TradeItem>) section.getList("pay", new ArrayList<>());
        List<TradeItem> receiveItems = (List<TradeItem>) section.getList("receive", new ArrayList<>());

        return new ShopData(id, ownerName, ownerUuid, createdAt, payItems, receiveItems);
    }

    // ===================================================
    // 🌅 TradeItem ⇔ Map 変換ヘルパー
    // ===================================================

    /**
     * Trade（作成中データ）から ShopData（保存用データ）に変換
     *
     * @param trade     変換元の Trade
     * @param ownerName 保存するプレイヤー名
     * @param ownerUuid 保存するプレイヤーUUID
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

    public void save(ConfigurationSection section) {
        // 基本情報はそのまま
        section.set("id", id);
        section.set("owner", ownerName);
        section.set("owner_uuid", ownerUuid.toString());
        section.set("created_at", createdAt);

        // 💡 TradeItemのリストをそのまま保存（Bukkitが自動的にシリアライズします）
        section.set("pay", payItems);
        section.set("receive", receiveItems);
    }

    /**
     * Map から ShopData を復元する（リロード処理用）
     */
    public static ShopData fromMap(Map<String, Object> map) {
        String id = (String) map.get("id");
        String ownerName = (String) map.get("owner");
        UUID ownerUuid = UUID.fromString((String) map.get("owner_uuid"));
        long createdAt = (long) map.get("created_at");

        // リストを取得（ConfigurationSerializable が自動的に TradeItem に復元してくれます）
        List<TradeItem> payItems = (List<TradeItem>) map.get("pay");
        List<TradeItem> receiveItems = (List<TradeItem>) map.get("receive");

        // もし null なら空のリストにする
        if (payItems == null) payItems = new ArrayList<>();
        if (receiveItems == null) receiveItems = new ArrayList<>();

        return new ShopData(id, ownerName, ownerUuid, createdAt, payItems, receiveItems);
    }
}