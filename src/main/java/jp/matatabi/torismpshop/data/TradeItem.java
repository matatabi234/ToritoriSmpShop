package jp.matatabi.torismpshop.data;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🌅 取引アイテム1個分のデータ
 * 素材＋数量＋各種マッチ条件（エンチャ／名前／タグ）
 */
public class TradeItem implements ConfigurationSerializable {

    // ===== 基本フィールド =====
    private Material material;
    private int amount;

    // ===== エンチャント判定 =====
    private boolean checkEnchant = false;
    private Map<Enchantment, Integer> requiredEnchants = new HashMap<>();
    // 例: {SHARPNESS: 5} → シャープネスLv5「以上」で通す

    // ===== カスタム名判定 =====
    private boolean checkCustomName = false;
    private String requiredCustomName;
    // 例: "伝説の剣"

    // ===== PDCタグ判定（将来用）=====
    private boolean checkTag = false;
    private String tagKey;
    private String tagValue;

    // 追加フィールド
    private String itemName;        // 識別用などの名称
    private String customName;      // 表示名（DisplayName）
    private List<String> lore;      // 説明（Lore）

    // ... コンストラクタや既存メソッド ...

    // ===== 新規追加分の Getter / Setter =====
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }


    // ===== コンストラクタ =====

    /**
     * 基本（素材＋数量だけ）
     */
    // 変更: Material を ItemStack に
    private ItemStack itemStack;

    // コンストラクタを ItemStack 対応に
    public TradeItem(Material mat, int amount) {
        this(new ItemStack(mat), amount); // MaterialからItemStackを作成して転送
    }

    // ===== Getter =====
    public Material getMaterial() {
        return itemStack.getType();
    } // 互換性のため残す

    public boolean isCheckEnchant() {
        return checkEnchant;
    }

    public Map<Enchantment, Integer> getRequiredEnchants() {
        return requiredEnchants;
    }

    public boolean isCheckCustomName() {
        return checkCustomName;
    }

    public String getRequiredCustomName() {
        return requiredCustomName;
    }

    public boolean isCheckTag() {
        return checkTag;
    }

    public String getTagKey() {
        return tagKey;
    }

    public String getTagValue() {
        return tagValue;
    }

    // ===== Setter =====
    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    // ===== エンチャ関連 Setter =====
    public void setCheckEnchant(boolean checkEnchant) {
        this.checkEnchant = checkEnchant;
    }

    public void setRequiredEnchants(Map<Enchantment, Integer> requiredEnchants) {
        this.requiredEnchants = requiredEnchants;
    }

    /**
     * 🌌 便利メソッド：エンチャを1個追加
     */
    public void addRequiredEnchant(Enchantment ench, int level) {
        this.checkEnchant = true;
        this.requiredEnchants.put(ench, level);
    }

    // ===== カスタム名関連 Setter =====
    public void setCheckCustomName(boolean checkCustomName) {
        this.checkCustomName = checkCustomName;
    }

    public void setRequiredCustomName(String requiredCustomName) {
        this.requiredCustomName = requiredCustomName;
        this.checkCustomName = (requiredCustomName != null);
    }

    // ===== タグ関連 Setter（将来用）=====
    public void setCheckTag(boolean checkTag) {
        this.checkTag = checkTag;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    public enum MatchMode {
        MATERIAL_ONLY,
        HAS_TAG,
        EXACT
    }

    private MatchMode matchMode = MatchMode.MATERIAL_ONLY;

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    /**
     * プレイヤーのアイテムが条件に一致するか判定
     */
    // isMatch判定をItemStackベースにする
    public boolean isMatch(ItemStack playerItem) {
        // 1. ベースのItemStack（エンチャントや名前を含む）が一致しているか
        // isSimilar は、エンチャントや名前、Loreなどを比較してくれます
        // ※部分一致が必要な場合は、以前提案したループ判定ロジックを使ってください
        if (!playerItem.isSimilar(this.itemStack)) return false;

        // 2. 個数の判定
        if (playerItem.getAmount() < this.amount) return false;

        return true;
    }

    public TradeItem(ItemStack itemStack, int amount) {
        this.itemStack = itemStack.clone();
        this.amount = amount;
    }

    // YAML読み込み用コンストラクタ
    public TradeItem(Map<String, Object> map) {
        this.itemStack = (ItemStack) map.get("itemStack");
        this.amount = (int) map.get("amount");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemStack", itemStack); // ItemStackをそのまま保存
        map.put("amount", amount);
        return map;
    }

    // Getter
    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getAmount() {
        return amount;
    }

    public Map<String, Object> toLegacyMap() {
        // 💡 新方式である serialize() の結果を返すようにする
        return this.serialize();
    }
}