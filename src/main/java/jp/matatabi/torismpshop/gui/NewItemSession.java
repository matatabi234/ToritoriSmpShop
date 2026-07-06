package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.TradeItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 新規取引作成時のセッション管理
 * プレイヤーごとに「支払いリスト」「受取リスト」を保持する
 */
public class NewItemSession {

    // ===== 支払いアイテムリスト（複数） =====
    private static final Map<UUID, List<TradeItem>> payItems = new HashMap<>();

    // ===== 受取アイテムリスト（複数） =====
    private static final Map<UUID, List<TradeItem>> receiveItems = new HashMap<>();

    // ===== モード管理 =====
    private static final Set<UUID> selectingMode = new HashSet<>();      // アイテム選択中
    private static final Set<UUID> amountInputMode = new HashSet<>();    // 個数入力中

    // 🌅 今どっちを編集中か？（支払い or 受取）
    private static final Map<UUID, EditTarget> editTarget = new HashMap<>();

    // 🌅 追加中のアイテム（一時保存用）
    private static final Map<UUID, org.bukkit.Material> tempMaterial = new HashMap<>();

    // 🌅 追加中の個数（一時保存用）
    private static final Map<UUID, Integer> tempAmount = new HashMap<>();

    /**
     * アイテム追加時の個数の一時保存
     */
    public static void setTempAmount(Player player, int amount) {
        tempAmount.put(player.getUniqueId(), amount);
    }

    public static int getTempAmount(Player player) {
        return tempAmount.getOrDefault(player.getUniqueId(), 1);  // デフォルト1個
    }

    public static void clearTempAmount(Player player) {
        tempAmount.remove(player.getUniqueId());
    }


    /**
     * 編集対象（支払い or 受取）
     */
    public enum EditTarget {
        PAY,      // 支払い側を編集中
        RECEIVE   // 受取側を編集中
    }

    // ===================================
    // 📦 リスト取得（無ければ空リスト作成）
    // ===================================

    /**
     * 支払いアイテムリスト取得
     */
    public static List<TradeItem> getPayItems(Player player) {
        return payItems.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
    }

    /**
     * 受取アイテムリスト取得
     */
    public static List<TradeItem> getReceiveItems(Player player) {
        return receiveItems.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
    }


    // ===================================
    // ➕ アイテム追加
    // ===================================

    /**
     * 支払いリストにアイテム追加
     */
    public static void addPayItem(Player player, TradeItem item) {
        getPayItems(player).add(item);
    }

    /**
     * 受取リストにアイテム追加
     */
    public static void addReceiveItem(Player player, TradeItem item) {
        getReceiveItems(player).add(item);
    }


    // ===================================
    // ❌ アイテム削除
    // ===================================

    /**
     * 支払いリストから指定インデックスのアイテム削除
     */
    public static void removePayItem(Player player, int index) {
        List<TradeItem> list = getPayItems(player);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
        }
    }

    /**
     * 受取リストから指定インデックスのアイテム削除
     */
    public static void removeReceiveItem(Player player, int index) {
        List<TradeItem> list = getReceiveItems(player);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
        }
    }


    // ===================================
    // 📊 合計個数計算（3456チェック用）
    // ===================================

    /**
     * 支払いリストの合計個数
     */
    public static int getPayTotalAmount(Player player) {
        return getPayItems(player).stream()
                .mapToInt(TradeItem::getAmount)
                .sum();
    }

    /**
     * 受取リストの合計個数
     */
    public static int getReceiveTotalAmount(Player player) {
        return getReceiveItems(player).stream()
                .mapToInt(TradeItem::getAmount)
                .sum();
    }


    // ===================================
    // 🎯 編集対象（支払い or 受取）
    // ===================================

    public static void setEditTarget(Player player, EditTarget target) {
        editTarget.put(player.getUniqueId(), target);
    }

    public static EditTarget getEditTarget(Player player) {
        return editTarget.get(player.getUniqueId());
    }

    // ===================================
    // 🎮 モード管理
    // ===================================

    // ----- アイテム選択モード -----
    public static void startSelecting(Player player) {
        selectingMode.add(player.getUniqueId());
    }

    public static boolean isSelecting(Player player) {
        return selectingMode.contains(player.getUniqueId());
    }

    public static void stopSelecting(Player player) {
        selectingMode.remove(player.getUniqueId());
    }


    // ----- 個数入力モード -----
    public static void startAmountInput(Player player) {
        amountInputMode.add(player.getUniqueId());
    }

    public static boolean isAmountInput(Player player) {
        return amountInputMode.contains(player.getUniqueId());
    }

    public static void stopAmountInput(Player player) {
        amountInputMode.remove(player.getUniqueId());
    }


    // ===================================
    // 📌 一時保存（追加中のアイテム）
    // ===================================

    /**
     * アイテム選択後、個数入力するまでの間の一時保存
     */
    public static void setTempMaterial(Player player, org.bukkit.Material material) {
        tempMaterial.put(player.getUniqueId(), material);
    }

    public static org.bukkit.Material getTempMaterial(Player player) {
        return tempMaterial.get(player.getUniqueId());
    }

    public static void clearTempMaterial(Player player) {
        tempMaterial.remove(player.getUniqueId());
    }


    // ===================================
    // 🧹 セッション全消去
    // ===================================

    /**
     * プレイヤーのセッションを全部リセット
     * （取引作成完了時 or キャンセル時に呼ぶ）
     */
    public static void clear(Player player) {
        UUID uuid = player.getUniqueId();
        payItems.remove(uuid);
        receiveItems.remove(uuid);
        selectingMode.remove(uuid);
        amountInputMode.remove(uuid);
        editTarget.remove(uuid);
        tempMaterial.remove(uuid);
        tempAmount.remove(uuid);  // ← 追加！
    }

    /**
     * 🌅 リストにアイテムを追加
     * @param player プレイヤー
     * @param target PAY か RECEIVE
     * @param material 追加するアイテム
     * @param amount 個数
     */
    public static void addItem(Player player, EditTarget target, Material material, int amount) {
        TradeItem newItem = new TradeItem(material, amount);

        if (target == EditTarget.PAY) {
            getPayItems(player).add(newItem);
        } else if (target == EditTarget.RECEIVE) {
            getReceiveItems(player).add(newItem);
        }
    }
}