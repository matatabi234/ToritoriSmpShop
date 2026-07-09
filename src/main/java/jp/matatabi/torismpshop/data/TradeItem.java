package jp.matatabi.torismpshop.data;

import org.bukkit.Material;

/**
 * 取引アイテム1個分のデータ
 * （素材 + 個数 のセット）
 */
public class TradeItem {

    private Material material;
    private int amount;

    public TradeItem(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    // ===== Getter =====
    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    // ===== Setter =====
    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public enum MatchMode {
        MATERIAL_ONLY,   // 現状：Materialだけ一致でOK（デフォ）
        HAS_TAG,         // ②：指定PDCタグが付いてればOK
        EXACT            // ①：ItemStack完全一致（将来用）
    }

    private MatchMode matchMode = MatchMode.MATERIAL_ONLY;  // ← 追加
    private String tagKey;  // ← 追加（HAS_TAGの時に使う）
    private String tagValue;// ← 追加（HAS_TAGの時に使う。null なら「タグが存在するだけでOK」）
}