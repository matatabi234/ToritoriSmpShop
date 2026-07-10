package jp.matatabi.torismpshop.data;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * 🌅 取引アイテム1個分のデータ
 * 素材＋数量＋各種マッチ条件（エンチャ／名前／タグ）
 */
public class TradeItem {

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

    // ===== コンストラクタ =====

    /** 基本（素材＋数量だけ）*/
    public TradeItem(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    // ===== Getter =====
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }

    public boolean isCheckEnchant() { return checkEnchant; }
    public Map<Enchantment, Integer> getRequiredEnchants() { return requiredEnchants; }

    public boolean isCheckCustomName() { return checkCustomName; }
    public String getRequiredCustomName() { return requiredCustomName; }

    public boolean isCheckTag() { return checkTag; }
    public String getTagKey() { return tagKey; }
    public String getTagValue() { return tagValue; }

    // ===== Setter =====
    public void setMaterial(Material material) { this.material = material; }
    public void setAmount(int amount) { this.amount = amount; }

    // ===== エンチャ関連 Setter =====
    public void setCheckEnchant(boolean checkEnchant) { this.checkEnchant = checkEnchant; }
    public void setRequiredEnchants(Map<Enchantment, Integer> requiredEnchants) {
        this.requiredEnchants = requiredEnchants;
    }
    /** 🌌 便利メソッド：エンチャを1個追加 */
    public void addRequiredEnchant(Enchantment ench, int level) {
        this.checkEnchant = true;
        this.requiredEnchants.put(ench, level);
    }

    // ===== カスタム名関連 Setter =====
    public void setCheckCustomName(boolean checkCustomName) { this.checkCustomName = checkCustomName; }
    public void setRequiredCustomName(String requiredCustomName) {
        this.requiredCustomName = requiredCustomName;
        this.checkCustomName = (requiredCustomName != null);
    }

    // ===== タグ関連 Setter（将来用）=====
    public void setCheckTag(boolean checkTag) { this.checkTag = checkTag; }
    public void setTagKey(String tagKey) { this.tagKey = tagKey; }
    public void setTagValue(String tagValue) { this.tagValue = tagValue; }
}