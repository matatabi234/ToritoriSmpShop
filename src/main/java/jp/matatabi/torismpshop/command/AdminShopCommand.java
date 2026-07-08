package jp.matatabi.torismpshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jp.matatabi.torismpshop.BindModeManager;
import jp.matatabi.torismpshop.ToriSmpShop;
import jp.matatabi.torismpshop.data.*;
import jp.matatabi.torismpshop.gui.MainMenuGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AdminShopCommand {

    public static LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("torishop")
                .requires(source -> source.getSender().hasPermission("torismpshop.torishop"))

                // /torishop → ヘルプ表示
                .executes(ctx -> {
                    showHelp(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                })
                // ================================
                // /torishop bind （引数なし → GUI開く）
                // ================================
                .then(Commands.literal("bind")
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.bind"))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("このコマンドはプレイヤーだけ使えるよ〜", NamedTextColor.RED)
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            String id = StringArgumentType.getString(ctx, "id");
                            String displayName = StringArgumentType.getString(ctx, "displayName");

                            // ===== 文字数チェック =====
                            if (displayName.length() > 12) {
                                player.sendMessage(Component.text(
                                        "§c表示名は12文字以内にしてね💦（今: " + displayName.length() + "文字）"
                                ));
                                return Command.SINGLE_SUCCESS;
                            }

                            // ===== ShopIDの存在チェック =====
                            ShopData shop = ShopStorage.findById(id);
                            if (shop == null) {
                                player.sendMessage(Component.text(
                                        "§cそのID (§f" + id + "§c) の取引は見つからないよ💦"
                                ));
                                return Command.SINGLE_SUCCESS;
                            }

                                    // ===== 自分の取引かチェック（他人のはbindできない）=====
                                    if (!shop.getOwnerUuid().equals(player.getUniqueId().toString())) {
                                        player.sendMessage(Component.text(
                                                "§c自分の取引しか bind できないよ〜🙈"
                                        ));
                                        return Command.SINGLE_SUCCESS;
                                    }

                            // ===== Bindモード開始 =====
                            BindModeManager.set(player, id, displayName);

                            player.sendMessage(Component.text("§a✅ Bind モード開始！"));
                            player.sendMessage(Component.text(
                                    "§7次にクリックした看板に §f[" + displayName + "] §7を紐付けるよ🔗"
                            ));
                            player.sendMessage(Component.text(
                                    "§7キャンセルは §e/torishop bind cancel §7でどうぞ〜"
                            ));

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("gui")
                                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.gui"))
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                                ctx.getSource().getSender().sendMessage(
                                                        Component.text("プレイヤーだけ使えるコマンドだよ！", NamedTextColor.RED)
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            MainMenuGui.open(player);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )


                                        .build();
                            }

                            // ヘルプメッセージを表示するメソッド
                            private static void showHelp (CommandSourceStack source){
                                source.getSender().sendMessage(Component.text("=== ToriSmpShop ヘルプ ===", NamedTextColor.GOLD));
                                source.getSender().sendMessage(Component.text("/torishop create <ID>  - 新しい取引を作成", NamedTextColor.GRAY));
                                source.getSender().sendMessage(Component.text("/torishop list         - 取引一覧を表示", NamedTextColor.GRAY));
                                source.getSender().sendMessage(Component.text("/torishop delete <ID>  - 取引を削除", NamedTextColor.GRAY));
                                source.getSender().sendMessage(Component.text("/torishop bind <ID>    - 看板に取引を紐付け", NamedTextColor.GRAY));
                                source.getSender().sendMessage(Component.text("/torishop gui          - 管理画面を開く", NamedTextColor.GRAY));
                            }
                        }