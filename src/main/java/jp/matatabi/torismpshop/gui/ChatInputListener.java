package jp.matatabi.torismpshop.gui;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * チャット入力を傍受するリスナー
 * 個数入力モード中のプレイヤーの発言をキャッチする
 */
public class ChatInputListener implements Listener {

    // メインクラスの参照（GUIを開き直すのに必要）
    private final org.bukkit.plugin.Plugin plugin;

    public ChatInputListener(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // 🌅 ===== 検索入力モード =====
        if (NewItemSession.isWaitingSearchInput(player)) {
            event.setCancelled(true);

            String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            // モード解除
            NewItemSession.stopWaitingSearchInput(player);

            // ===== キャンセル =====
            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage("§c❌ 検索キャンセルしたよ");
                Bukkit.getScheduler().runTask(plugin, () -> ItemSelectGui.open(player, 0));
                return;
            }

            // ===== 空文字チェック =====
            if (message.trim().isEmpty()) {
                player.sendMessage("§c❌ 検索ワードを入力してね！");
                Bukkit.getScheduler().runTask(plugin, () -> ItemSelectGui.open(player, 0));
                return;
            }

            // ===== 検索ワード保存 =====
            NewItemSession.setSearchQuery(player, message.trim());
            player.sendMessage("§a🔍 「" + message.trim() + "」で検索するよ〜🌅");

            // アイテム選択GUIを開き直す（0ページ目から）
            Bukkit.getScheduler().runTask(plugin, () -> ItemSelectGui.open(player, 0));
            return;
        }

        // 入力待ちモードじゃなければ無視
        if (!NewItemSession.isAmountInput(player)) return;

        // チャットをキャンセル（他の人に見せない）
        event.setCancelled(true);

        // メッセージを文字列化
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // モード解除
        NewItemSession.stopAmountInput(player);

        // ===== キャンセル =====
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage("§c❌ キャンセルしたよ");
            // ItemAddGui を開き直す（メインスレッドで！）
            Bukkit.getScheduler().runTask(plugin, () -> ItemAddGui.open(player));
            return;
        }

        // ===== 数字チェック =====
// ===== 数字チェック =====
        int amount;
        try {
            // 1. 前後の空白を削除
            String cleanedMessage = message.trim();

            // 2. 全角数字を半角数字に変換（全角で入力しちゃった人への優しさ）
            cleanedMessage = convertFullWidthToHalfWidth(cleanedMessage);

            // 3. 数字以外の見えない文字やゴミを完全に排除 (正規表現)
            cleanedMessage = cleanedMessage.replaceAll("[^0-9]", "");

            // 変換後の文字列が空っぽになってしまったらエラーにする
            if (cleanedMessage.isEmpty()) {
                throw new NumberFormatException();
            }

            amount = Integer.parseInt(cleanedMessage);
        } catch (NumberFormatException e) {
            // 💡 デバッグ用: コンソールに実際にどんな文字列が届いていたか出力する（原因特定に便利）
            Bukkit.getLogger().warning("[ToriSmpShop] 数字変換に失敗した文字列: [" + message + "]");

            player.sendMessage("§c❌ 数字を入力してね！（例: 64）");
            Bukkit.getScheduler().runTask(plugin, () -> ItemAddGui.open(player));
            return;
        }

        // ===== 範囲チェック =====
        if (amount < 1) {
            player.sendMessage("§c❌ 1個以上にしてね！");
            Bukkit.getScheduler().runTask(plugin, () -> ItemAddGui.open(player));
            return;
        }
        if (amount > 3456) {  // 54スロット × 64個 = インベントリ最大
            player.sendMessage("§c❌ 3456個までにしてね！（インベントリ超えちゃう）");
            Bukkit.getScheduler().runTask(plugin, () -> ItemAddGui.open(player));
            return;
        }

        // ===== 一時保存（tempAmount へ）=====
        NewItemSession.setTempAmount(player, amount);
        player.sendMessage("§a✅ 個数を " + amount + " 個に設定したよ！");

        // ItemAddGui を開き直す（メインスレッドで！）
        Bukkit.getScheduler().runTask(plugin, () -> ItemAddGui.open(player));
    }
    // 全角数字を半角数字に変換する便利メソッド
    private String convertFullWidthToHalfWidth(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '０' && c <= '９') {
                sb.append((char) (c - '０' + '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
