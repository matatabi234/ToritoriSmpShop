package jp.matatabi.torismpshop;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import jp.matatabi.torismpshop.command.AdminShopCommand;
import jp.matatabi.torismpshop.command.AdminShopShortcutCommand;
import jp.matatabi.torismpshop.gui.MainMenuGui;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToriSmpShopBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> {
                    // 1. 通常の管理コマンドの登録
                    var adminShopNode = AdminShopCommand.build();
                    event.registrar().register(
                            adminShopNode,
                            "取引ショップの管理コマンド",
                            List.of("tshop")
                    );

                    // 🔥 2. ショートカットコマンドを一括自動登録
                    AdminShopShortcutCommand.buildAllShortcuts(adminShopNode).forEach(shortcut -> {
                        event.registrar().register(shortcut.node(), shortcut.description(), shortcut.aliases());
                    });
                }
        );
    }
}