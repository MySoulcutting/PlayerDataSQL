package cc.bukkitPlugin.commons.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.commons.Log;
import cc.commons.util.StringUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.tools.CacheGettor;

public class BukkitUtil {

    /** 是否拥有副手 */
    private final static boolean mHasBothHand = MethodUtil.isMethodExist(PlayerInventory.class, "getItemInMainHand", true);
    private final static Method method_Bukkit_getOnlinePlayers = MethodUtil.getMethodIgnoreParam(Bukkit.class,
            "getOnlinePlayers", true).get(0);

    public static String mTestMCVersion = "1.7.10";
    /** Minecraft版本 */
    private static CacheGettor<String> mMCVersion = CacheGettor.create(() -> {
        if (Bukkit.getServer() != null) {
            String tVersionStr = Bukkit.getVersion();
            //(MC: " + this.console.getVersion() + ")"
            Matcher matcher = Pattern.compile("^.*?\\(MC: (.*?)\\)$").matcher(tVersionStr);
            if (matcher.find())
                return matcher.group(1);
            else Log.warn("未能从字符串 \"" + tVersionStr + "\" 中获取Minecraft版本,部分功能可能存在兼容性问题");
        }
        return mTestMCVersion;
    });

    /**
     * 获取主手上的物品
     * 
     * @param pPlayer
     *            玩家
     * @return 物品
     */
    public static ItemStack getItemInMainHand(HumanEntity pPlayer) {
        if (BukkitUtil.mHasBothHand) {
            return pPlayer.getInventory().getItemInMainHand();
        } else {
            return pPlayer.getItemInHand();
        }
    }

    /**
     * 设置主手上的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     */
    public static void setItemInMainHand(HumanEntity pPlayer, ItemStack pItem) {
        if (BukkitUtil.mHasBothHand) {
            pPlayer.getInventory().setItemInMainHand(pItem);
        } else {
            pPlayer.setItemInHand(pItem);
        }
    }

    /**
     * 获取在线玩家
     * 
     * @return 在线的玩家
     */
    public static Collection<Player> getOnlinePlayers() {
        try {
            Object tObject = MethodUtil.invokeStaticMethod(method_Bukkit_getOnlinePlayers);
            if (tObject instanceof Player[])
                return Arrays.asList((Player[])tObject);
            else {
                return (Collection<Player>)tObject;
            }
        } catch (Throwable exp) {
            Log.severe("获取在线玩家列表时发生了错误", exp);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 获取在线玩家名字列表
     * 
     * @return 在线玩家名字列表
     */
    public static ArrayList<String> getOnlinePlayersName() {
        ArrayList<String> tPlayerNames = new ArrayList<>();
        for (Player sPlayer : BukkitUtil.getOnlinePlayers())
            tPlayerNames.add(sPlayer.getName());
        return tPlayerNames;
    }

    /**
     * 获取所有离线的玩家
     * <p>
     * 数据较多时可能造成服务器卡顿
     * </p>
     * 
     * @return 离线的玩家
     */
    @Deprecated
    public static OfflinePlayer[] getOfflinePlayers() {
        Method tMethod;
        try {
            tMethod = Bukkit.class.getDeclaredMethod("getOfflinePlayers");
            Object tObject = tMethod.invoke(null);
            OfflinePlayer[] tPlayers;
            if (tObject instanceof OfflinePlayer[])
                tPlayers = (OfflinePlayer[])tObject;
            else if (tObject instanceof Collection) {
                Collection<OfflinePlayer> tcPlayers = (Collection<OfflinePlayer>)tObject;
                tPlayers = new OfflinePlayer[tcPlayers.size()];
                tcPlayers.toArray(tPlayers);
            } else tPlayers = new OfflinePlayer[0];
            return tPlayers;
        } catch (Throwable exp) {
            Log.severe("获取离线玩家列表时发生了错误", exp);
            return new OfflinePlayer[0];
        }
    }

    public static ArrayList<String> getOfflinePlayersName() {
        ArrayList<String> playerNames = new ArrayList<>();
        for (OfflinePlayer sPlayer : BukkitUtil.getOfflinePlayers())
            playerNames.add(sPlayer.getName());
        return playerNames;
    }

    public static boolean isItemMetaEmpty(ItemMeta pMeat) {
        Method tMethod;
        try {
            if (pMeat == null)
                return true;
            tMethod = pMeat.getClass().getDeclaredMethod("isEmpty");
            tMethod.setAccessible(true);
            Boolean result = (Boolean)tMethod.invoke(pMeat);
            if (result == null || result)
                return true;
            else return false;
        } catch (Throwable exp) {
            return true;
        }
    }

    /**
     * 安全的添加物品到玩家背包,如果玩家背包满了,会将物品丢弃到地上
     *
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     * @param pCount
     *            物品数量
     */
    public static void giveItem(HumanEntity pPlayer, ItemStack pItem, int pCount) {
        if (!BukkitUtil.isValidItem(pItem) || pCount <= 0)
            return;
        pItem = pItem.clone();
        pItem.setAmount(pCount);
        BukkitUtil.giveItem(pPlayer, pItem);
    }

    /**
     * 安全的添加物品到玩家背包,如果玩家背包满了,会返回未添加的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     * @return 为能添加到背包的物品
     */
    public static ItemStack giveItemWithoutDrop(HumanEntity pPlayer, ItemStack pItem) {
        if (!BukkitUtil.isValidItem(pItem) || pPlayer == null)
            return null;

        pItem = pItem.clone();
        if (pPlayer.getInventory().firstEmpty() == -1) { // 背包满了
            if (pItem.getMaxStackSize() == 1) {
                return pItem;
            }
        }
        int tAllowCount = 0;
        for (ItemStack sInvItem : pPlayer.getInventory().getContents()) {
            if (BukkitUtil.isValidItem(sInvItem)) {
                if (sInvItem.isSimilar(pItem)) {
                    tAllowCount += Math.max(0, sInvItem.getMaxStackSize() - sInvItem.getAmount());
                }
            } else {
                tAllowCount += pItem.getMaxStackSize();
            }
            if (tAllowCount >= pItem.getAmount())
                break;
        }

        ItemStack tLeftItems = null;
        if (tAllowCount < pItem.getAmount()) {
            tLeftItems = pItem.clone();
            tLeftItems.setAmount(pItem.getAmount() - tAllowCount);
            pItem.setAmount(tAllowCount);
        }

        int tMaxStackSize = pItem.getMaxStackSize();
        if (tMaxStackSize <= 0)
            tMaxStackSize = 1;
        for (int i = 0; i < pItem.getAmount() / tMaxStackSize; i++) {
            ItemStack tGiveItem = pItem.clone();
            tGiveItem.setAmount(tMaxStackSize);
            pPlayer.getInventory().addItem(tGiveItem);
        }
        if (tMaxStackSize > 1) {
            int tLeftAmount = pItem.getAmount() % tMaxStackSize;
            if (tLeftAmount != 0) {
                ItemStack tGiveItem = pItem.clone();
                tGiveItem.setAmount(tLeftAmount);
                pPlayer.getInventory().addItem(tGiveItem);
            }
        }

        return tLeftItems;
    }

    /**
     * 安全的添加物品到玩家背包,如果玩家背包满了,会将物品丢弃到地上
     * <p>
     * 物品的数量由{@link ItemStack#getAmount}来决定
     * </p>
     *
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     */
    public static void giveItem(HumanEntity pPlayer, ItemStack pItem) {
        if (!BukkitUtil.isValidItem(pItem) || pPlayer == null)
            return;

        pItem = BukkitUtil.convertToCraftItem(pItem.clone());
        if (pPlayer.getInventory().firstEmpty() == -1) { // 背包满了
            if (pItem.getMaxStackSize() == 1) {
                BukkitUtil.dropItem(pPlayer.getLocation(), pItem);
                return;
            }
        }

        int tAllowCount = 0;
        for (ItemStack sInvItem : pPlayer.getInventory().getContents()) {
            if (BukkitUtil.isValidItem(sInvItem)) {
                if (sInvItem.isSimilar(pItem)) {
                    tAllowCount += Math.max(0, sInvItem.getMaxStackSize() - sInvItem.getAmount());
                }
            } else {
                tAllowCount += pItem.getMaxStackSize();
            }
            if (tAllowCount >= pItem.getAmount())
                break;
        }
        if (tAllowCount < pItem.getAmount()) {
            ItemStack tDropItems = pItem.clone();
            tDropItems.setAmount(pItem.getAmount() - tAllowCount);
            pItem.setAmount(tAllowCount);
            BukkitUtil.dropItem(pPlayer.getLocation(), tDropItems);
        }

        int tMaxStackSize = pItem.getMaxStackSize();
        if (tMaxStackSize <= 0)
            tMaxStackSize = 1;
        for (int i = 0; i < pItem.getAmount() / tMaxStackSize; i++) {
            ItemStack giveItem = pItem.clone();
            giveItem.setAmount(tMaxStackSize);
            pPlayer.getInventory().addItem(giveItem);
        }
        if (tMaxStackSize > 1) {
            int tLeftAmount = pItem.getAmount() % tMaxStackSize;
            if (tLeftAmount != 0) {
                ItemStack tGiveItem = pItem.clone();
                tGiveItem.setAmount(tLeftAmount);
                pPlayer.getInventory().addItem(tGiveItem);
            }
        }
    }

    /**
     * 生成掉落物 *
     * <p>
     * 物品的数量由{@link ItemStack#getAmount}来决定
     * </p>
     * 
     * @param pLoc
     *            位置
     * @param pItem
     *            物品
     */
    public static void dropItem(Location pLoc, ItemStack pItem) {
        if (!BukkitUtil.isValidItem(pItem) || pLoc == null || pLoc.getWorld() == null)
            return;

        pItem = BukkitUtil.convertToCraftItem(pItem);
        int tMaxstackSize = pItem.getMaxStackSize();
        if (tMaxstackSize <= 0)
            tMaxstackSize = 1;
        ItemStack tDropItem = pItem.clone();

        tDropItem.setAmount(tMaxstackSize);
        for (int i = 0; i < pItem.getAmount() / tMaxstackSize; i++) {
            pLoc.getWorld().dropItem(pLoc, tDropItem.clone());
        }
        if (tMaxstackSize > 1) {
            int tLeftAmount = pItem.getAmount() % tMaxstackSize;
            if (tLeftAmount != 0) {
                tDropItem = pItem.clone();
                tDropItem.setAmount(tLeftAmount);
                pLoc.getWorld().dropItem(pLoc, tDropItem);
            }
        }
    }

    /**
     * 快速设置物品信息
     * 
     * @param pItem
     *            物品
     * @param pDisplayName
     *            名字
     * @param pLores
     *            Lore
     * @return 设置信息后的物品
     */
    public static ItemStack setItemInfo(ItemStack pItem, String pDisplayName, String...pLores) {
        return BukkitUtil.setItemInfo(pItem, pDisplayName, Arrays.asList(pLores));
    }

    /**
     * 快速设置物品信息
     * <p>
     * 不翻译颜色
     * </p>
     * 
     * @param pItem
     *            物品
     * @param pDisplayName
     *            名字
     * @param pLores
     *            Lore
     * @return 设置信息后的物品
     */
    public static ItemStack setItemInfo(ItemStack pItem, String pDisplayName, List<String> pLores) {
        if (pItem == null)
            return null;

        ItemMeta tMeta = pItem.getItemMeta();
        if (StringUtil.isNotEmpty(pDisplayName)) {
            tMeta.setDisplayName(pDisplayName);
        }

        if (pLores != null && pLores.size() != 0) {
            ArrayList<String> newLore = new ArrayList<>(pLores.size());
            for (String sSigleLore : pLores) {
                if (sSigleLore == null) {
                    sSigleLore = "";
                }
                newLore.add(sSigleLore);
            }
            tMeta.setLore(newLore);
        }

        pItem.setItemMeta(tMeta);
        return pItem;

    }

    /**
     * 物品是否有效
     * <p>
     * 不为null且不为空气且数量大于0
     * </p>
     * 
     * @param pItem
     *            物品
     * @return 是否是正常的物品
     */
    public static boolean isValidItem(ItemStack pItem) {
        return pItem != null && pItem.getType() != Material.AIR && pItem.getAmount() > 0;
    }

    /**
     * 物品是否无效
     * 
     * @param pItem
     * @return 是否
     * @see #isValidItem(ItemStack)
     */
    public static boolean isInvalidItem(ItemStack pItem) {
        return !BukkitUtil.isValidItem(pItem);
    }

    /**
     * 方块不为null且不为空气
     * 
     * @param pBlock
     *            方块
     * @return 是否是正常的方块
     */
    public static boolean isValidBlock(Block pBlock) {
        return pBlock != null && pBlock.getType() != Material.AIR;
    }

    /**
     * 方块是否无效
     * 
     * @param pBlock
     *            方块
     * @return 是否
     * @see #isValidBlock(Block)
     */
    public static boolean isIvalidBlock(Block pBlock) {
        return !BukkitUtil.isValidBlock(pBlock);
    }

    /**
     * 获取物品类型
     * <p>
     * 根据所给的字符串获取物品类型,字符串可以为{@link Material}的枚举值,或者{@link Material#getId()}的值
     * </p>
     * 
     * @param pTypeStr
     *            物品类型字符串
     * @return 物品类型如果不存在则null
     */
    public static Material getItemType(String pTypeStr) {
        pTypeStr = pTypeStr.trim();
        if (pTypeStr.matches("\\d+")) {
            try {
                Material tMate = Material.getMaterial(Integer.parseInt(pTypeStr));
                if (tMate != null && tMate != Material.AIR)
                    return tMate;
            } catch (NumberFormatException ignore) {
            }
        }
        Material tMate = Material.getMaterial(pTypeStr.toUpperCase());
        return (tMate != null && tMate != Material.AIR) ? tMate : null;
    }

    /**
     * 获取附魔类型
     * <p>
     * 根据所给的字符串获取附魔类型,字符串可以为{@link Enchantment}的枚举值,或者{@link Enchantment#getId()}的值
     * </p>
     * 
     * @param pEnchantStr
     *            物品类型字符串
     * @return 附魔类型如果不存在则null
     */
    public static Enchantment getEnchantment(String pEnchantStr) {
        pEnchantStr = pEnchantStr.trim();
        if (pEnchantStr.matches("\\d+")) {
            try {
                Enchantment tEnchant = Enchantment.getById(Integer.parseInt(pEnchantStr));
                if (tEnchant != null)
                    return tEnchant;
            } catch (NumberFormatException ignore) {
            }
        }
        Enchantment tEnchant = Enchantment.getByName(pEnchantStr.toUpperCase());
        return tEnchant != null ? tEnchant : null;
    }

    /**
     * 获取物品当前设置的名字
     * 
     * @param pItem
     *            物品
     * @return 设置的名字或null
     */
    public static String getItemDisplayName(ItemStack pItem) {
        if (!BukkitUtil.isValidItem(pItem) || !pItem.hasItemMeta())
            return null;

        ItemMeta tMeta = pItem.getItemMeta();
        return tMeta.hasDisplayName() ? tMeta.getDisplayName() : null;
    }

    /**
     * 获取物品的Lore
     * 
     * @param pItem
     *            物品
     * @return 物品的Lore,非null
     */
    public static List<String> getItemLore(ItemStack pItem) {
        if (!BukkitUtil.isValidItem(pItem) || !pItem.hasItemMeta())
            return null;

        ItemMeta tMeta = pItem.getItemMeta();
        return tMeta.hasLore() ? tMeta.getLore() : new ArrayList<String>(0);
    }

    public static interface Task<T> {

        T perform(T pObj);
    }

    /**
     * 是否应该将物品设置回背包位置
     * <p>
     * 不需要设置回背包的情况:<br>
     * 1.物品数据一致<br>
     * 2.物品的包装NMS物品实例为同一个<br>
     * 3.两个物品在{@link #isInvalidItem(ItemStack)中的判定都为false}
     * </p>
     * 
     * @param pOrigin
     *            原物品
     * @param pNewItem
     *            新物品
     * @return 是否
     */
    public static boolean shouldSetBack(ItemStack pOrigin, ItemStack pNewItem) {
        boolean tInvalid1 = BukkitUtil.isInvalidItem(pOrigin);
        if (tInvalid1 == BukkitUtil.isInvalidItem(pNewItem)) {
            if (tInvalid1) { // 都为非法物品
                return false;
            }
            return !(pOrigin.getAmount() == pNewItem.getAmount() && pOrigin.isSimilar(pNewItem));
        } else { // 一个是非法物品,一个不是
            return true;
        }

    }

    /**
     * 使用指定动作处理玩家背包内的所有物品
     * 
     * @param pPlayer
     *            玩家
     * @param pTask
     *            处理动作
     * @return 更改的物品数量
     */
    public static int loopPlayerItem(Player pPlayer, Task<ItemStack> pTask) {
        int tModifer = BukkitUtil.loopArmorItem(pPlayer, pTask);
        tModifer += BukkitUtil.loopInvItem(pPlayer.getInventory(), pTask);
        ItemStack tItem = pPlayer.getOpenInventory().getCursor();
        if (BukkitUtil.isValidItem(tItem)) {
            ItemStack tNewItem = pTask.perform(tItem);
            if (BukkitUtil.shouldSetBack(tItem, tNewItem)) {
                pPlayer.getOpenInventory().setCursor(tNewItem);
                tModifer++;
            }
        }
        return tModifer;
    }

    /**
     * 使用指定动作处理背包内的所有物品
     * 
     * @param pPlayer
     *            玩家
     * @param pTask
     *            处理动作
     * @return 更改的物品数量
     */
    public static int loopInvItem(Inventory pInv, Task<ItemStack> pTask) {
        int tModifer = 0;
        ItemStack tItem, tNewItem;
        int tInvSize = pInv.getSize();
        for (int i = 0; i < tInvSize; i++) {
            tItem = pInv.getItem(i);
            if (BukkitUtil.isValidItem(tItem)) {
                tNewItem = pTask.perform(tItem);
                if (BukkitUtil.shouldSetBack(tItem, tNewItem)) {
                    pInv.setItem(i, tNewItem);
                    tModifer++;
                }
            }
        }
        return tModifer;
    }

    /**
     * 使用指定动作处理玩家装备背包内的所有物品
     * 
     * @param pPlayer
     *            玩家
     * @param pTask
     *            处理动作
     * @return 更改的物品数量
     */
    public static int loopArmorItem(Player pPlayer, Task<ItemStack> pTask) {
        int tModifer = 0;
        PlayerInventory tInv = pPlayer.getInventory();
        ItemStack tItem, tNewItem;
        tItem = tInv.getHelmet();
        if (BukkitUtil.isValidItem(tItem)) {
            tNewItem = pTask.perform(tItem);
            if (BukkitUtil.shouldSetBack(tItem, tNewItem)) {
                tInv.setHelmet(tNewItem);
                tModifer++;
            }
        }
        tItem = tInv.getChestplate();
        if (BukkitUtil.isValidItem(tItem)) {
            tNewItem = pTask.perform(tItem);
            if (BukkitUtil.shouldSetBack(tItem, tNewItem)) {
                tInv.setChestplate(tNewItem);
                tModifer++;
            }
        }
        tItem = tInv.getLeggings();
        if (BukkitUtil.isValidItem(tItem)) {
            tNewItem = pTask.perform(tItem);
            if (BukkitUtil.shouldSetBack(tItem, tNewItem)) {
                tInv.setLeggings(tNewItem);
                tModifer++;
            }
        }
        tItem = tInv.getBoots();
        if (BukkitUtil.isValidItem(tItem)) {
            tNewItem = pTask.perform(tItem);
            if (BukkitUtil.shouldSetBack(tItem, tNewItem)) {
                tInv.setBoots(tNewItem);
                tModifer++;
            }
        }
        return tModifer;
    }

    /**
     * 获取MC版本
     */
    public static String getMinecraftVersion() {
        return BukkitUtil.mMCVersion.get();
    }

    private static Inventory mInv = Bukkit.createInventory(null, 9);

    /**
     * 转换ItemStack为CraftItemStack
     * 
     * @param pItem
     * @return
     */
    public static ItemStack convertToCraftItem(ItemStack pItem) {
        BukkitUtil.mInv.setItem(0, pItem);
        ItemStack tItem = BukkitUtil.mInv.getItem(0);
        return BukkitUtil.isValidItem(tItem) ? tItem : pItem;
    }

}
