package jp.matatabi.torismpshop.gui;

import jp.matatabi.torismpshop.BindModeManager;
import jp.matatabi.torismpshop.data.ShopData;
import jp.matatabi.torismpshop.data.ShopStorage;
import jp.matatabi.torismpshop.util.SignKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * 🔗 看板クリック時のBind処理
 * - 新規Bind：/torishop bind <id> <name>
 * - 表示名変更：/torishop bind name <name>
 */
public class SignBindListener implements Listener {

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 右クリックのみ
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign sign)) return;

        PersistentDataContainer pdc = sign.getPersistentDataContainer();

        // ==========================================
        // 🅰 Bindモード中 → Bind処理
        // ==========================================
        if (BindModeManager.isBinding(player)) {
            event.setCancelled(true);

            BindModeManager.Mode mode = BindModeManager.getMode(player);
            String newDisplayName = BindModeManager.getDisplayName(player);

            if (mode == BindModeManager.Mode.NEW_BIND) {
                handleNewBind(player, sign, pdc, newDisplayName);
            } else if (mode == BindModeManager.Mode.RENAME_ONLY) {
                handleRename(player, sign, pdc, newDisplayName);
            } else if (mode == BindModeManager.Mode.UNBIND) {
                handleUnbind(player, sign, pdc);   // ← 追加！
            }
            return;
        }

        // ==========================================
        // 🅱 Bindモード外 → bind済みなら GUI 開く
        // ==========================================
        if (pdc.has(SignKeys.SHOP_ID, PersistentDataType.STRING)) {
            event.setCancelled(true);  // 看板編集画面を開かない

            String shopId = pdc.get(SignKeys.SHOP_ID, PersistentDataType.STRING);
            ShopData shop = ShopStorage.getById(shopId);

            if (shop == null) {
                player.sendMessage(Component.text(
                        "§cこの取引はもう存在しないよ💦"
                ));
                player.sendMessage(Component.text(
                        "§7看板が古い可能性があるよ〜"
                ));
                return;
            }

            // 🌅 TradeGui 開く〜！
            TradeGui.open(player, shop);
        }
    }

    // ==========================================
    // 🆕 新規Bind処理
    // ==========================================
    private void handleNewBind(Player player, Sign sign, PersistentDataContainer pdc, String displayName) {
        String shopId = BindModeManager.getShopId(player);

        if (shopId == null) {
            player.sendMessage(Component.text("§cBindモードのデータが見つからないよ💦"));
            BindModeManager.clear(player);
            return;
        }

        // ShopData 取得
        ShopData shop = ShopStorage.getById(shopId);
        if (shop == null) {
            player.sendMessage(Component.text("§cその取引はもう存在しないみたい💦"));
            BindModeManager.clear(player);
            return;
        }

        // 所有者チェック
        if (!shop.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("§c自分の取引しか bind できないよ〜🙈"));
            BindModeManager.clear(player);
            return;
        }

        // すでに bind 済みかチェック
        if (pdc.has(SignKeys.SHOP_ID, PersistentDataType.STRING)) {
            String existingId = pdc.get(SignKeys.SHOP_ID, PersistentDataType.STRING);
            player.sendMessage(Component.text(
                    "§eこの看板はもう bind されてるよ〜（ID: " + existingId + "）"
            ));
            player.sendMessage(Component.text(
                    "§7上書きしたい場合は先に §e/torishop unbind §7で解除してね〜"
            ));
            return;
        }

        // PDC に保存
        pdc.set(SignKeys.SHOP_ID, PersistentDataType.STRING, shopId);
        pdc.set(SignKeys.DISPLAY_NAME, PersistentDataType.STRING, displayName);

        // 看板テキスト書き換え
        applySignDesign(sign, displayName, shop.getOwnerName());

        // Bindモード終了
        BindModeManager.clear(player);

        // 通知
        player.sendMessage(Component.text("§a✅ Bind 完了！🔗"));
        player.sendMessage(Component.text(
                "§7看板に §f[" + displayName + "] §7を紐付けたよ〜🌅"
        ));
        player.sendMessage(Component.text("§7取引ID: §8" + shopId));
    }

    // ==========================================
    // ✏️ 表示名変更処理
    // ==========================================
    private void handleRename(Player player, Sign sign, PersistentDataContainer pdc, String newDisplayName) {

        // ===== bind済みか確認 =====
        if (!pdc.has(SignKeys.SHOP_ID, PersistentDataType.STRING)) {
            player.sendMessage(Component.text("§cこの看板はまだ bind されてないよ〜💦"));
            player.sendMessage(Component.text(
                    "§7先に §e/torishop bind <id> <名前> §7で紐付けてね🌅"
            ));
            return;
        }

        String shopId = pdc.get(SignKeys.SHOP_ID, PersistentDataType.STRING);

        // ===== ShopData 取得 =====
        ShopData shop = ShopStorage.getById(shopId);
        if (shop == null) {
            player.sendMessage(Component.text("§cこの看板の取引データが見つからないよ💦"));
            player.sendMessage(Component.text("§7取引が削除されてる可能性があるよ〜"));
            return;
        }

        // ===== 所有者チェック =====
        if (!shop.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("§c自分の看板の表示名しか変更できないよ〜🙈"));
            BindModeManager.clear(player);
            return;
        }

        // ===== 旧表示名を取得（ログ用）=====
        String oldName = pdc.has(SignKeys.DISPLAY_NAME, PersistentDataType.STRING)
                ? pdc.get(SignKeys.DISPLAY_NAME, PersistentDataType.STRING)
                : "(なし)";

        // ===== PDC 更新 =====
        pdc.set(SignKeys.DISPLAY_NAME, PersistentDataType.STRING, newDisplayName);

        // ===== 看板テキスト書き換え =====
        applySignDesign(sign, newDisplayName, shop.getOwnerName());

        // ===== Bindモード終了 =====
        BindModeManager.clear(player);

        // ===== 通知 =====
        player.sendMessage(Component.text("§a✏️ 表示名を変更したよ〜！"));
        player.sendMessage(Component.text(
                "§7§m[" + oldName + "]§r §7→ §f[" + newDisplayName + "]"
        ));
    }

    // ==========================================
    // 🎨 看板デザイン適用（共通処理）
    // ==========================================
    private void applySignDesign(Sign sign, String displayName, String ownerName) {
        SignSide side = sign.getSide(Side.FRONT);

        // 1行目：SHOP（装飾ヘッダー）
        side.line(0, Component.text("=== SHOP ===")
                .color(NamedTextColor.BLACK)
                .decoration(TextDecoration.BOLD, true)
        );

        // 2行目：表示名（メイン）
        side.line(1, Component.text(displayName)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
        );

        // 3行目：出品者名
        side.line(2, Component.text("by " + ownerName)
                .color(NamedTextColor.GRAY)
        );

        // 4行目：クリック案内
        side.line(3, Component.text("[右クリックで取引]")
                .color(NamedTextColor.AQUA)
        );

        // 文字色を光らせる（グロー効果）
//        side.setGlowingText(true);

        // 背景色（オプション：黒背景で見やすく）
        side.setColor(DyeColor.BLACK);

        // 変更を反映
        sign.update();
    }

    // ==========================================
    // 🗑️ Unbind処理
    // ==========================================
    private void handleUnbind(Player player, Sign sign, PersistentDataContainer pdc) {

        // ===== bind済みか確認 =====
        if (!pdc.has(SignKeys.SHOP_ID, PersistentDataType.STRING)) {
            player.sendMessage(Component.text(
                    "§cこの看板はまだ bind されてないよ〜💦"
            ));
            player.sendMessage(Component.text(
                    "§7解除するものがないみたい〜🌌"
            ));
            return;
        }

        String shopId = pdc.get(SignKeys.SHOP_ID, PersistentDataType.STRING);

        // ===== ShopData 取得（所有者チェック用）=====
        ShopData shop = ShopStorage.getById(shopId);

        // ===== 所有者チェック =====
        // shopが存在する場合のみチェック（削除済みなら誰でも解除OK）
        if (shop != null && !shop.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage(Component.text(
                    "§c自分の看板しか解除できないよ〜🙈"
            ));
            BindModeManager.clear(player);
            return;
        }

        // ===== 旧表示名を取得（ログ用）=====
        String oldName = pdc.has(SignKeys.DISPLAY_NAME, PersistentDataType.STRING)
                ? pdc.get(SignKeys.DISPLAY_NAME, PersistentDataType.STRING)
                : "(なし)";

        // ===== PDC からデータ削除 =====
        pdc.remove(SignKeys.SHOP_ID);
        pdc.remove(SignKeys.DISPLAY_NAME);

        // ===== 看板テキストを完全クリア =====
        clearSignText(sign);

        // ===== Bindモード終了 =====
        BindModeManager.clear(player);

        // ===== 通知 =====
        player.sendMessage(Component.text("§a🗑️ Bind 解除完了〜！"));
        player.sendMessage(Component.text(
                "§7§m[" + oldName + "]§r §7の紐付けを外したよ〜🌅"
        ));
        player.sendMessage(Component.text(
                "§8取引ID: " + shopId
        ));
        player.sendMessage(Component.text(
                "§7看板は自由に編集できる状態になったよ✨"
        ));
    }

    // ==========================================
// 🧹 看板テキスト完全クリア（共通処理）
// ==========================================
    private void clearSignText(Sign sign) {
        SignSide side = sign.getSide(Side.FRONT);

        // 全4行を空にする
        side.line(0, Component.empty());
        side.line(1, Component.empty());
        side.line(2, Component.empty());
        side.line(3, Component.empty());

        // グロー効果もOFF
        side.setGlowingText(false);

        // 背景色もデフォに戻す
        side.setColor(DyeColor.BLACK);

        // 変更を反映
        sign.update();
    }
}