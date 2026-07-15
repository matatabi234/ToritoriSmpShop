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
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AdminShopShortcutCommand {

    // 💡 すべてのショートカットをリスト化してメインクラスへ渡す関数
    public static List<ShortcutData> buildAllShortcuts(LiteralCommandNode<CommandSourceStack> mainNode) {
        return List.of(
                new ShortcutData(buildTgui(), "管理画面を直接開くよ", List.of()),
                new ShortcutData(buildTbind(mainNode), "ショップの紐付け画面を直接開くよ", List.of()),
                new ShortcutData(buildTunbind(mainNode), "ショップの紐付けを解除するよ", List.of()),
                new ShortcutData(buildTsettings(), "プレイヤー設定を直接変更するよ", List.of("tset")),
                new ShortcutData(buildToffline(), "オフラインプレイヤー設定を直接変更するよ", List.of("toff")),
                new ShortcutData(buildTreload(), "ショップ設定をリロードするよ", List.of("trl"))
        );
    }

    // 💡 /tbind 用のコマンド組み立て (大元の bind ノードを取得してリダイレクトで安全に合流させる)
    public static LiteralCommandNode<CommandSourceStack> buildTbind(LiteralCommandNode<CommandSourceStack> mainNode) {
        var bindSubNode = mainNode.getChild("bind");
        if (bindSubNode == null) throw new IllegalStateException("AdminShopCommand に bind サブコマンドが見つかりません！");

        return Commands.literal("tbind")
                .requires(bindSubNode.getRequirement())
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
                .redirect(bindSubNode)
                .build();
    }

    // 💡 /tunbind 用のコマンド組み立て (大元の unbind ノードから処理を引き継ぐ)
    public static LiteralCommandNode<CommandSourceStack> buildTunbind(LiteralCommandNode<CommandSourceStack> mainNode) {
        var unbindSubNode = mainNode.getChild("unbind");
        if (unbindSubNode == null) throw new IllegalStateException("AdminShopCommand に unbind サブコマンドが見つかりません！");

        return Commands.literal("tunbind")
                .requires(unbindSubNode.getRequirement())
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                        ctx.getSource().getSender().sendMessage(
                                Component.text("プレイヤーだけ使えるコマンドだよ！", NamedTextColor.RED)
                        );
                        return Command.SINGLE_SUCCESS;
                    }

                    BindModeManager.setUnbind(player);

                    player.sendMessage(Component.text("§e🗑️ 解除モード起動〜！").color(NamedTextColor.GOLD));
                    player.sendMessage(Component.text("§760秒以内に §e解除したい看板 §7を右クリックしてね〜🌅"));
                    player.sendMessage(Component.text("§8※ 自分の看板しか解除できないよ"));

                    return Command.SINGLE_SUCCESS;
                })
                .redirect(unbindSubNode)
                .build();
    }

    // 💡 /tgui 用のコマンド組み立て
    public static LiteralCommandNode<CommandSourceStack> buildTgui() {
        return Commands.literal("tgui")
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
                .build();
    }

    // 💡 /tsettings 用のコマンド組み立て
    public static LiteralCommandNode<CommandSourceStack> buildTsettings() {
        return Commands.literal("tsettings")
                .then(Commands.literal("setself")
                        .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setself"))
                        .then(Commands.argument("mcid", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            String playerName = StringArgumentType.getString(ctx, "mcid");
                                            Player target = Bukkit.getPlayer(playerName);
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
                        .then(Commands.argument("mcid", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
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
                .build();
    }

    // 💡 /toffline 用のコマンド組み立て
    public static LiteralCommandNode<CommandSourceStack> buildToffline() {
        return Commands.literal("toffline")
                // 🔥 【修正箇所】一般プレイヤーに入力補完すらさせないよう、ルートで権限を要求する
                .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setself")
                        || source.getSender().hasPermission("torismpshop.torishop.admin.settings.setshowall"))
                .then(Commands.literal("settings")
                        .then(Commands.literal("setself")
                                .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.settings.setself"))
                                .then(Commands.argument("mcid", StringArgumentType.word())
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    String targetName = StringArgumentType.getString(ctx, "mcid");
                                                    boolean allow = BoolArgumentType.getBool(ctx, "value");
                                                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
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
                                .then(Commands.argument("mcid", StringArgumentType.word())
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    String targetName = StringArgumentType.getString(ctx, "mcid");
                                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
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
                .build();
    }

    // 💡 /treload 用のコマンド組み立て
    public static LiteralCommandNode<CommandSourceStack> buildTreload() {
        return Commands.literal("treload")
                .requires(source -> source.getSender().hasPermission("torismpshop.torishop.admin.reload"))
                .executes(ctx -> {
                    try {
                        ShopStorage.reload();
                        PlayerSettingsManager.reload();
                        ctx.getSource().getSender().sendMessage(
                                Component.text("ショップ設定とプレイヤー設定をリロードしたよ！", NamedTextColor.GREEN)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        ctx.getSource().getSender().sendMessage(
                                Component.text("リロード中にエラーが発生したよ！コンソールを確認してね。", NamedTextColor.RED)
                        );
                        return 0;
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    // 💡 一括持ち運び用データクラス (レコード)
    public record ShortcutData(
            LiteralCommandNode<CommandSourceStack> node,
            String description,
            List<String> aliases
    ) {}
}