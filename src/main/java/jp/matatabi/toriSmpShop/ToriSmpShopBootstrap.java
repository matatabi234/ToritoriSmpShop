package jp.matatabi.torismpshop;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import jp.matatabi.torismpshop.command.AdminShopCommand;
import org.jetbrains.annotations.NotNull;

public class ToriSmpShopBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // ライフサイクルイベントマネージャーを取得してコマンドを登録
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> {
                    event.registrar().register(
                            AdminShopCommand.build(),
                            "取引ショップの管理コマンド"
                    );
                }
        );
    }
}