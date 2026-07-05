package jp.matatabi.torismpshop;

import jp.matatabi.torismpshop.data.TradeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ToriSmpShop extends JavaPlugin {

    private static ToriSmpShop instance;
    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        instance = this;

        // TradeManager を初期化
        this.tradeManager = new TradeManager();

        getLogger().info("ToriSmpShop が起動したよ！🌅");
    }

    @Override
    public void onDisable() {
        getLogger().info("ToriSmpShop を停止したよ〜");
    }

    /**
     * プラグインのインスタンスを取得（他クラスから使う用）
     */
    public static ToriSmpShop getInstance() {
        return instance;
    }

    /**
     * TradeManager を取得
     */
    public TradeManager getTradeManager() {
        return tradeManager;
    }
}