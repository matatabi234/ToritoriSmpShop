package jp.matatabi.torismpshop.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TradeManager {

    // ID をキーにして Trade を保存するマップ
    private final Map<String, Trade> trades = new HashMap<>();

    /**
     * 新しい取引を作成する
     * @param id 取引ID
     * @return 作成した Trade（すでに存在してたら null）
     */
    public Trade createTrade(String id) {
        if (trades.containsKey(id)) {
            return null;  // 重複NG
        }
        Trade trade = new Trade(id);
        trades.put(id, trade);
        return trade;
    }

    /**
     * 取引を取得する
     * @param id 取引ID
     * @return 見つかれば Trade、無ければ null
     */
    public Trade getTrade(String id) {
        return trades.get(id);
    }

    /**
     * 取引を削除する
     * @param id 取引ID
     * @return 削除成功なら true
     */
    public boolean deleteTrade(String id) {
        return trades.remove(id) != null;
    }

    /**
     * 取引が存在するかチェック
     */
    public boolean exists(String id) {
        return trades.containsKey(id);
    }

    /**
     * 全ての取引を取得（一覧表示用）
     */
    public Collection<Trade> getAllTrades() {
        return trades.values();
    }

    /**
     * 取引の総数
     */
    public int size() {
        return trades.size();
    }
}