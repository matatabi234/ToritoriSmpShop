package jp.matatabi.torismpshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AdminShopCommand {

    public static LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("adminshops")
                .requires(source -> source.getSender().hasPermission("torismpshop.admin"))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(
                            Component.text("ShopAdmin コマンドが動いたよ！", NamedTextColor.GREEN)
                    );
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}