package jp.matatabi.torismpshop;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 🔗 Bindモードの状態管理
 */
public class BindModeManager {

    public enum Mode {
        NEW_BIND,      // 新規bind
        RENAME_ONLY,    // 表示名だけ変更]
        UNBIND
    }

    // 内部データクラス
    public static class BindSession {
        public final Mode mode;
        public final String shopId;       // NEW_BIND用（RENAME時はnull）
        public final String displayName;  // 新しい表示名
        public final BukkitTask timeoutTask;

        public BindSession(Mode mode, String shopId, String displayName, BukkitTask timeoutTask) {
            this.mode = mode;
            this.shopId = shopId;
            this.displayName = displayName;
            this.timeoutTask = timeoutTask;
        }
    }

    private static final Map<UUID, BindSession> sessions = new HashMap<>();

    private static final long TIMEOUT_TICKS = 20L * 60; // 60秒

    // ===== 新規bind開始 =====
    public static void setNewBind(Player player, String shopId, String displayName) {
        cancelExisting(player);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                ToriSmpShop.getInstance(),
                () -> onTimeout(player),
                TIMEOUT_TICKS
        );
        sessions.put(player.getUniqueId(), new BindSession(
                Mode.NEW_BIND, shopId, displayName, task
        ));
    }

    // ===== 表示名変更モード開始 =====
    public static void setRenameOnly(Player player, String newName) {
        cancelExisting(player);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                ToriSmpShop.getInstance(),
                () -> onTimeout(player),
                TIMEOUT_TICKS
        );

        sessions.put(player.getUniqueId(), new BindSession(
                Mode.RENAME_ONLY, null, newName, task
        ));
    }

    // ===== モード確認 =====
    public static boolean isBinding(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public static Mode getMode(Player player) {
        BindSession session = sessions.get(player.getUniqueId());
        return session == null ? null : session.mode;
    }

    public static String getShopId(Player player) {
        BindSession session = sessions.get(player.getUniqueId());
        return session == null ? null : session.shopId;
    }

    public static String getDisplayName(Player player) {
        BindSession session = sessions.get(player.getUniqueId());
        return session == null ? null : session.displayName;
    }

    // ===== クリア =====
    public static void clear(Player player) {
        BindSession session = sessions.remove(player.getUniqueId());
        if (session != null && session.timeoutTask != null) {
            session.timeoutTask.cancel();
        }
    }

    // ===== 既存セッションをキャンセル（新規開始時に呼ぶ）=====
    private static void cancelExisting(Player player) {
        BindSession existing = sessions.remove(player.getUniqueId());
        if (existing != null && existing.timeoutTask != null) {
            existing.timeoutTask.cancel();
        }
    }

    // ===== タイムアウト時の処理 =====
    private static void onTimeout(Player player) {
        BindSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        if (player.isOnline()) {
            player.sendMessage(Component.text("§e⏰ Bindモードがタイムアウトしたよ〜（60秒経過）"));
            player.sendMessage(Component.text("§7もう一度やり直してね〜🌅"));
        }
    }

    /**
     * 🗑️ Unbindモード開始
     */
    public static void setUnbind(Player player) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(
                ToriSmpShop.getInstance(),
                () -> onTimeout(player),
                TIMEOUT_TICKS
        );
        BindSession session = new BindSession(Mode.UNBIND, null, null, task);
        sessions.put(player.getUniqueId(), session);
    }
}