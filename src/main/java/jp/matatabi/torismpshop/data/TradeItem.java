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
}