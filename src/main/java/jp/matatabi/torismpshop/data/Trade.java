package jp.matatabi.torismpshop.data;

import org.bukkit.inventory.ItemStack;

public class Trade {

    private final String id;           // 取引ID（"iron_deal" とか）
    private String displayName;         // 表示名
    private ItemStack inputItem;        // プレイヤーが差し出すアイテム
    private ItemStack outputItem;       // プレイヤーがもらえるアイテム

    public Trade(String id) {
        this.id = id;
        this.displayName = id;  // 初期値はIDと同じ
        this.inputItem = null;
        this.outputItem = null;
    }

    // === Getter ===
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getInputItem() {
        return inputItem;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    // === Setter ===
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setInputItem(ItemStack inputItem) {
        this.inputItem = inputItem;
    }

    public void setOutputItem(ItemStack outputItem) {
        this.outputItem = outputItem;
    }

    // === 便利メソッド ===
    public boolean isReady() {
        // 入出力両方セットされてたら「使える状態」
        return inputItem != null && outputItem != null;
    }
}