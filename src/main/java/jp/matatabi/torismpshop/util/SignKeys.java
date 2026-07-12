package jp.matatabi.torismpshop.util;

import jp.matatabi.torismpshop.ToriSmpShop;
import org.bukkit.NamespacedKey;

/**
 * 🔑 看板PDC用の NamespacedKey 定数
 */
public class SignKeys {

    /**
     * 取引ID保存用
     */
    public static final NamespacedKey SHOP_ID =
            new NamespacedKey(ToriSmpShop.getInstance(), "shop_id");

    /**
     * 表示名保存用
     */
    public static final NamespacedKey DISPLAY_NAME =
            new NamespacedKey(ToriSmpShop.getInstance(), "display_name");
}