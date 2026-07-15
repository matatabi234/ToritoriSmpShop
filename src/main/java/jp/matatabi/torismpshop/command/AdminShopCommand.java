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
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

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
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.bind"))
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
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.unbind"))
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
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.reload"))
                        .executes(ctx -> {
                            try {
                                // 🔄 リロード実行
                                ShopStorage.reload();
                                PlayerSettingsManager.reload();
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("ショップ設定とプレイヤー設定をリロードしたよ！", NamedTextColor.GREEN)
                                );
                            } catch (Exception e) {
                                // 💡 リロード失敗時にエラーを報告
                                e.printStackTrace();
                                ctx.getSource().getSender().sendMessage(
                                        Component.text("リロード中にエラーが発生したよ！コンソールを確認してね。", NamedTextColor.RED)
                                );
                                return 0; // 失敗
                            }
                            return Command.SINGLE_SUCCESS; // 成功
                        })
                )
                .then(Commands.literal("settings")
                        .then(Commands.literal("setself")
                                .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setself"))
                                // 💡 修正ポイント1: ArgumentTypes.player() の代わりに StringArgumentType.word() を使い、オンラインプレイヤーの名前を補完させる
                                .then(Commands.argument("mcid", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            // オンラインプレイヤーの名前をTAB補完に出す安全な処理
                                            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    // 💡 修正ポイント2: ここは文字列として安全に取得できる！
                                                    String playerName = StringArgumentType.getString(ctx, "mcid");
                                                    Player target = Bukkit.getPlayer(playerName);

                                                    // ログアウト対策
                                                    if (target == null) {
                                                        ctx.getSource().getSender().sendMessage(Component.text("§cそのプレイヤーはオンラインじゃないよ！", NamedTextColor.RED));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    boolean allow = BoolArgumentType.getBool(ctx, "value");

                                                    PlayerConfig config = PlayerSettingsManager.get(target.getUniqueId());
                                                    config.setAllowSelfTrade(allow);

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
                        .then(Commands.literal("setshowall")
                                .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setshowall"))
                                // 💡 修正ポイント1: ここも同様に補完付きの文字列引数にする
                                .then(Commands.argument("mcid", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    // 💡 修正ポイント2: 文字列として取得
                                                    String playerName = StringArgumentType.getString(ctx, "mcid");
                                                    Player target = Bukkit.getPlayer(playerName);

                                                    if (target == null) {
                                                        ctx.getSource().getSender().sendMessage(Component.text("§cそのプレイヤーはオンラインじゃないよ！", NamedTextColor.RED));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    boolean value = BoolArgumentType.getBool(ctx, "value");

                                                    PlayerConfig config = PlayerSettingsManager.get(target.getUniqueId());
                                                    config.setShowAllTrades(value);

                                                    try {
                                                        PlayerSettingsManager.save();
                                                        ctx.getSource().getSender().sendMessage(Component.text("§a" + target.getName() + " の設定を " + value + " にしたよ！"));
                                                    } catch (Exception e) {
                                                        ctx.getSource().getSender().sendMessage(Component.text("§c保存失敗！"));
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("offline")
                        .then(Commands.literal("settings")
                                .then(Commands.literal("setself")
                                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setself"))
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
                                .then(Commands.literal("setshowall")
                                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setshowall"))
                                        .then(Commands.argument("mcid", StringArgumentType.word())
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            String targetName = StringArgumentType.getString(ctx, "mcid");
                                                            boolean value = BoolArgumentType.getBool(ctx, "value");

                                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                                            // PlayerSettingsManager にメソッドを作成済みと仮定
                                                            PlayerSettingsManager.setAllowSelfTrade(target.getName(), value);

                                                            try {
                                                                PlayerSettingsManager.save();
                                                                ctx.getSource().getSender().sendMessage(
                                                                        Component.text("§a" + target.getName() + " の全表示設定を " + value + " にしたよ！")
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
                        )
                )
                .build();
    }

    // ヘルプメッセージを表示するメソッド
    private static void showHelp(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("=== ToriSmpShop ヘルプ ===", NamedTextColor.GOLD));
        source.getSender().sendMessage(Component.text("/torishop gui             - 管理画面を開く", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop bind            - 紐づけ画面を開く", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop bind name <名前> - ショップの名前を変更", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop unbind          - 紐づけ画面を開く", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop settings        - プレイヤー設定", NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("/torishop offline settings- オフラインプレイヤー設定", NamedTextColor.GRAY));

    }
}