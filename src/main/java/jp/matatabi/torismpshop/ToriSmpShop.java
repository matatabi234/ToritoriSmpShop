package jp.matatabi.torismpshop;

import jp.matatabi.torismpshop.data.*;
import jp.matatabi.torismpshop.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ToriSmpShop extends JavaPlugin {

    private static ToriSmpShop instance;
    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        instance = this;
        this.tradeManager = new TradeManager();
        ConfigurationSerialization.registerClass(TradeItem.class);
        ConfigurationSerialization.registerClass(PlayerConfig.class);

        // 🌅 起動時に必ず呼ぶ！
        ItemSelectGui.initialize(this);
        ShopStorage.initialize(this);
        PlayerSettingsManager.init(this);
        ShopStorage.loadAll();
        getServer().getPluginManager().registerEvents(new MainMenuListener(), this);
        getServer().getPluginManager().registerEvents(new ItemSelectListener(), this);
        getServer().getPluginManager().registerEvents(new NewItemListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
        getServer().getPluginManager().registerEvents(new TradeItemListListener(), this);
        getServer().getPluginManager().registerEvents(new ItemAddListener(), this);
        getServer().getPluginManager().registerEvents(new ShopListListener(), this);
        getServer().getPluginManager().registerEvents(new TradeListener(), this);
        getServer().getPluginManager().registerEvents(new SignBindListener(), this);
        getServer().getPluginManager().registerEvents(new BindGuiListener(), this);
        getServer().getPluginManager().registerEvents(new CustomItemSelectGuiListener(), this);

        getLogger().info("ToriSmpShop が起動したよ！");

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public void onDisable() {
        try {
            CommandMap commandMap = Bukkit.getCommandMap();

            // 登録したメインコマンド、エイリアス、ショートカットコマンドをすべて指定
            String[] myCommands = {
                    "torishop", "tshop",   // メインコマンドと大元の登録時エイリアス
                    "tgui", "tbind", "tunbind",  // 各種ショートカットコマンド
                    "tsettings", "toffline",
                    "treload"
            };

            for (String cmdLabel : myCommands) {
                org.bukkit.command.Command command = commandMap.getCommand(cmdLabel);
                if (command != null) {
                    // コマンドマップから完全に登録解除する
                    command.unregister(commandMap);
                }
            }
            getLogger().info("ToriSmpShop のコマンドを正常に登録解除したよ！");
        } catch (Exception e) {
            getLogger().warning("コマンドの登録解除中にエラーが発生したよ（リロード時）");
            e.printStackTrace();
        }
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