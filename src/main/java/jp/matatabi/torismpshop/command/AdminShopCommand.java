package jp.matatabi.torismpshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jp.matatabi.torismpshop.ToriSmpShop;
import jp.matatabi.torismpshop.data.Trade;
import jp.matatabi.torismpshop.data.TradeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("UnstableApiUsage")
public class AdminShopCommand {

    public static LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("torishop")
                // .requires(source -> source.getSender().hasPermission("torismpshop.admin"))

                // /torishop → ヘルプ表示
                .executes(ctx -> {
                    showHelp(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                })

            // /torishop create <ID>
                .then(Commands.literal("create")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    TradeManager manager = ToriSmpShop.getInstance().getTradeManager();

                                    Trade trade = manager.createTrade(id);
                                    if (trade == null) {
                                        ctx.getSource().getSender().sendMessage(
                                                Component.text("取引 [" + id + "] はすでに存在するよ！", NamedTextColor.RED)
                                        );
                                    } else {
                                        ctx.getSource().getSender().sendMessage(
                                                Component.text("取引 [" + id + "] を作成したよ！🌅", NamedTextColor.GREEN)
                                        );
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                // /torishop list
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            TradeManager manager = ToriSmpShop.getInstance().getTradeManager();

                            if (manager.size() == 0) {
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("取引はまだ1つも登録されてないよ〜", NamedTextColor.YELLOW)
                                );
                            } else {
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("=== 取引一覧（" + manager.size() + "件） ===", NamedTextColor.GOLD)
                                );
                                for (Trade trade : manager.getAllTrades()) {
                                    String status = trade.isReady() ? "✅" : "⚠️未完成";
                                    ctx.getSource().getSender().sendMessage(
                                            Component.text(status + " " + trade.getId() + " (" + trade.getDisplayName() + ")", NamedTextColor.GRAY)
                                    );
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                // /torishop delete <ID>
                .then(Commands.literal("delete")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    TradeManager manager = ToriSmpShop.getInstance().getTradeManager();

                                    boolean deleted = manager.deleteTrade(id);
                                    if (deleted) {
                                        ctx.getSource().getSender().sendMessage(
                                                Component.text("取引 [" + id + "] を削除したよ！", NamedTextColor.GREEN)
                                        );
                                    } else {
                                        ctx.getSource().getSender().sendMessage(
                                                Component.text("取引 [" + id + "] は存在しないよ〜💦", NamedTextColor.RED)
                                        );
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                // /torishop bind <ID>
                .then(Commands.literal("bind")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    ctx.getSource().getSender().sendMessage(
                                            Component.text("次にクリックした看板に [" + id + "] を紐付けるよ！", NamedTextColor.YELLOW)
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .build();
    }

    // ヘルプメッセージを表示するメソッド
    private static void showHelp(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("=== ToriSmpShop ヘルプ ===", NamedTextColor.GOLD));
        source.getSender().sendMessage(Component.text("/torishop create <ID>  - 新しい取引を作成", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop list         - 取引一覧を表示", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop delete <ID>  - 取引を削除", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop bind <ID>    - 看板に取引を紐付け", NamedTextColor.GRAY));
    }
}