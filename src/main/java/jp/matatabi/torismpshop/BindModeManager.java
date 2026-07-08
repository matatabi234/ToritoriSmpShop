package jp.matatabi.torismpshop;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 🌅 Bindモード管理クラス
 * 誰が今どのShopIDをbind待ちしてるかを覚えとく係〜
 */
public class BindModeManager {

    /**
     * 🔗 Bindモード情報
     * shopId：紐付けたい取引ID
     * displayName：看板2行目に表示する名前
     */
    public static class BindData {
        private final String shopId;
        private final String displayName;

        public BindData(String shopId, String displayName) {
            this.shopId = shopId;
            this.displayName = displayName;
        }

        public String getShopId() {
            return shopId;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ===== 誰がBindモード中かの記憶 =====
    // Key: プレイヤーUUID / Value: BindData
    private static final Map<UUID, BindData> bindMap = new HashMap<>();

    /**
     * 🔥 Bindモード開始
     */
    public static void set(Player player, String shopId, String displayName) {
        bindMap.put(player.getUniqueId(), new BindData(shopId, displayName));
    }

    /**
     * 🌅 今Bindモード中か？
     */
    public static boolean isInBindMode(Player player) {
        return bindMap.containsKey(player.getUniqueId());
    }

    /**
     * 🔗 Bindデータ取得
     */
    public static BindData get(Player player) {
        return bindMap.get(player.getUniqueId());
    }

    /**
     * ❌ Bindモード解除
     */
    public static void clear(Player player) {
        bindMap.remove(player.getUniqueId());
    }

    /**
     * 🧹 全員のBindモードをクリア（サーバー停止時とかに使うやつ）
     */
    public static void clearAll() {
        bindMap.clear();
    }
}