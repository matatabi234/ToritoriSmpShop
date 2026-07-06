package jp.matatabi.torismpshop;

import jp.matatabi.torismpshop.data.TradeManager;
import jp.matatabi.torismpshop.gui.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class ToriSmpShop extends JavaPlugin {

    private static ToriSmpShop instance;
    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        instance = this;
        this.tradeManager = new TradeManager();

        // 🌅 起動時に必ず呼ぶ！
        ItemSelectGui.initialize(this);

        getServer().getPluginManager().registerEvents(new MainMenuListener(), this);
        getServer().getPluginManager().registerEvents(new ItemSelectListener(), this);
        getServer().getPluginManager().registerEvents(new NewItemListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new TradeItemListListener(), this);
        getServer().getPluginManager().registerEvents(new ItemAddListener(), this);

        getLogger().info("ToriSmpShop が起動したよ！");
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