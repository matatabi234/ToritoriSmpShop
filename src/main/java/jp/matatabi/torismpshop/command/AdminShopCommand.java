package jp.matatabi.torismpshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jp.matatabi.torismpshop.BindModeManager;
import jp.matatabi.torismpshop.data.PlayerConfig;
import jp.matatabi.torismpshop.data.PlayerSettingsManager;
import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
import jp.matatabi.torismpshop.gui.BindGui;
import jp.matatabi.torismpshop.gui.MainMenuGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
                        // /torishop → ヘルプ表示
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("プレイヤーだけ使えるコマンドだよ！", NamedTextColor.RED)
                                );
                                return Command.SINGLE_SUCCESS;
                            }
                            BindGui.open(player);
                            return Command.SINGLE_SUCCESS;
                        })
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.bind"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("displayName", StringArgumentType.greedyString())
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
                                            ShopData shop = ShopStorage.getById(id);
                                            if (shop == null) {
                                                player.sendMessage(Component.text(
                                                        "§cそのID (§f" + id + "§c) の取引は見つからないよ💦"
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            // ===== 自分の取引かチェック（他人のはbindできない）=====
                                            if (!shop.getOwnerUuid().equals(player.getUniqueId())) {
                                                player.sendMessage(Component.text(
                                                        "§c自分の取引しか bind できないよ〜🙈"
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            // ===== Bindモード開始 =====
                                            BindModeManager.setNewBind(player, id, displayName);

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
                        )
                        // ===== 新規: /torishop bind name <displayName> =====
                        .then(Commands.literal("name")
                                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                                ctx.getSource().getSender().sendMessage(
                                                        Component.text("このコマンドはプレイヤーだけ使えるよ〜", NamedTextColor.RED)
                                                );
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String newName = StringArgumentType.getString(ctx, "displayName");

                                            // ===== 文字数チェック =====
                                            if (newName.length() > 12) {
                                                player.sendMessage(Component.text(
                                                        "§c表示名は12文字以内にしてね💦（今: " + newName.length() + "文字）"
                                                ));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            // ===== 表示名変更モード開始 =====
                                            BindModeManager.setRenameOnly(player, newName);

                                            player.sendMessage(Component.text("§a✏️ 表示名変更モード開始！"));
                                            player.sendMessage(Component.text(
                                                    "§760秒以内に §f変更したい看板をクリック §7してね〜🌅"
                                            ));
                                            player.sendMessage(Component.text(
                                                    "§7新しい表示名: §b[" + newName + "]"
                                            ));
                                            player.sendMessage(Component.text(
                                                    "§7キャンセルは §e/torishop bind cancel §7でどうぞ〜"
                                            ));

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("unbind")
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.unbind"))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("プレイヤーだけ使えるコマンドだよ！", NamedTextColor.RED)
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            // Unbindモード起動
                            BindModeManager.setUnbind(player);

                            // 案内メッセージ
                            player.sendMessage(Component.text("§e🗑️ 解除モード起動〜！")
                                    .color(NamedTextColor.GOLD));
                            player.sendMessage(Component.text(
                                    "§760秒以内に §e解除したい看板 §7を右クリックしてね〜🌅"
                            ));
                            player.sendMessage(Component.text(
                                    "§8※ 自分の看板しか解除できないよ"
                            ));

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("gui")
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.gui"))
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
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.load"))
                        .executes(ctx -> {
                            // 🔄 ここで ShopStorage.reload() を呼ぶ
                            ShopStorage.reload();
                            ctx.getSource().getSender().sendMessage(
                                    Component.text("ショップ設定をリロードしたよ！", NamedTextColor.GREEN)
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                        .executes(ctx -> {
                            // 🔄 ここで ShopStorage.reload() を呼ぶ
                            ShopStorage.reload();
                            ctx.getSource().getSender().sendMessage(
                                    Component.text("ショップ設定をリロードしたよ！", NamedTextColor.GREEN)
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("setself")
                        .requires(source -> source.getSender().hasPermission("torismpshop.admin"))
                        .then(Commands.argument("mcid", StringArgumentType.word()) // 文字列として受け取る
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            String targetName = StringArgumentType.getString(ctx, "mcid");
                                            boolean allow = BoolArgumentType.getBool(ctx, "value");

                                            // 💡 ここでプレイヤーを解決
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                            // UUIDも名前もこれで取得できる
                                            PlayerConfig config = PlayerSettingsManager.get(target.getUniqueId());
                                            config.setAllowSelfTrade(allow);

                                            // 💡 例外処理: try-catch で囲む（IOException対策）
                                            try {
                                                PlayerSettingsManager.save();
                                                ctx.getSource().getSender().sendMessage(
                                                        Component.text("§a" + target.getName() + " の設定を " + allow + " にしたよ！")
                                                );
                                            } catch (java.io.IOException e) {
                                                e.printStackTrace();
                                                ctx.getSource().getSender().sendMessage(Component.text("§c保存に失敗したよ！"));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )

                .build();
    }

    // ヘルプメッセージを表示するメソッド
    private static void showHelp(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("=== ToriSmpShop ヘルプ ===", NamedTextColor.GOLD));
        source.getSender().sendMessage(Component.text("/torishop bind <ID>    - 看板に取引を紐付け", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop gui          - 管理画面を開く", NamedTextColor.GRAY));
    }
}