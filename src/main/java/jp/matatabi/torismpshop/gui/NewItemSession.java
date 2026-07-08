package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.data.ShopData;
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

    // 🗑️ 削除モード中のプレイヤー
    private static final Set<UUID> deleteMode = new HashSet<>();

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


    // 🔍 検索キーワード保存
    private static final Map<UUID, String> searchQuery = new HashMap<>();
    // 🔍 検索入力待ちフラグ
    private static final Set<UUID> waitingSearchInput = new HashSet<>();

    public static void setSearchQuery(Player player, String query) {
        searchQuery.put(player.getUniqueId(), query.toLowerCase());
    }

    public static String getSearchQuery(Player player) {
        return searchQuery.get(player.getUniqueId());
    }

    public static void clearSearchQuery(Player player) {
        searchQuery.remove(player.getUniqueId());
    }

    public static boolean hasSearchQuery(Player player) {
        return searchQuery.containsKey(player.getUniqueId());
    }

    public static void startWaitingSearchInput(Player player) {
        waitingSearchInput.add(player.getUniqueId());
    }

    public static void stopWaitingSearchInput(Player player) {
        waitingSearchInput.remove(player.getUniqueId());
    }

    public static boolean isWaitingSearchInput(Player player) {
        return waitingSearchInput.contains(player.getUniqueId());
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


    // 🌅 編集中のインデックス（-1 = 新規追加モード）
    private static final Map<UUID, Integer> editingIndex = new HashMap<>();

    // 🌅 編集中の ShopData ID（null = 新規作成モード）
    private static final Map<UUID, String> editingShopId = new HashMap<>();

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
        editingIndex.remove(player.getUniqueId());
        deleteMode.remove(player.getUniqueId());
        searchQuery.remove(player.getUniqueId());
        waitingSearchInput.remove(player.getUniqueId());
        editingShopId.remove(uuid);
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

    /**
     * 編集モード開始（既存アイテムの編集）
     */
    public static void startEditing(Player player, int index, Material mat, int amount) {
        editingIndex.put(player.getUniqueId(), index);
        // 一時保存に既存の値を入れておく（初期表示用）
        setTempMaterial(player, mat);
        setTempAmount(player, amount);
    }

    /**
     * 編集中のインデックス取得（-1 なら新規追加モード）
     */
    public static int getEditingIndex(Player player) {
        return editingIndex.getOrDefault(player.getUniqueId(), -1);
    }

    /**
     * 編集モード終了
     */
    public static void clearEditingIndex(Player player) {
        editingIndex.remove(player.getUniqueId());
    }

    /**
     * リスト内のアイテムを更新（編集用）
     */
    public static void updateItem(Player player, EditTarget target, int index, Material mat, int amount) {
        List<TradeItem> list = (target == EditTarget.PAY)
                ? getPayItems(player)
                : getReceiveItems(player);
        if (index >= 0 && index < list.size()) {
            list.set(index, new TradeItem(mat, amount));
        }
    }

    /**
     * 削除モードON
     */
    public static void enableDeleteMode(Player player) {
        deleteMode.add(player.getUniqueId());
    }

    /**
     * 削除モードOFF
     */
    public static void disableDeleteMode(Player player) {
        deleteMode.remove(player.getUniqueId());
    }

    /**
     * 削除モード中か？
     */
    public static boolean isDeleteMode(Player player) {
        return deleteMode.contains(player.getUniqueId());
    }

    /**
     * リストからアイテムを削除
     */
    public static void removeItem(Player player, EditTarget target, int index) {
        List<TradeItem> list = (target == EditTarget.PAY)
                ? getPayItems(player)
                : getReceiveItems(player);
        if (index >= 0 && index < list.size()) {
            list.remove(index);
        }
    }

    /**
     * 編集中の ShopData ID をセット（既存取引の編集開始）
     */
    public static void setEditingShopId(Player player, String shopId) {
        editingShopId.put(player.getUniqueId(), shopId);
    }

    /**
     * 編集中の ShopData ID 取得（null なら新規作成モード）
     */
    public static String getEditingShopId(Player player) {
        return editingShopId.get(player.getUniqueId());
    }

    /**
     * 編集モード終了
     */
    public static void clearEditingShopId(Player player) {
        editingShopId.remove(player.getUniqueId());
    }

    /**
     * 既存の ShopData をセッションにロード（編集開始）
     */
    public static void loadFromShopData(Player player, ShopData shop) {
        UUID uuid = player.getUniqueId();
        // 既存セッションクリア
        clear(player);
        // ShopData の中身をコピー
        payItems.put(uuid, new ArrayList<>(shop.getPayItems()));
        receiveItems.put(uuid, new ArrayList<>(shop.getReceiveItems()));
        // 編集中IDセット
        editingShopId.put(uuid, shop.getId());
    }
}