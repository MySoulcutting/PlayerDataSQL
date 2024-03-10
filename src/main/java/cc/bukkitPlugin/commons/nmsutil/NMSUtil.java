package cc.bukkitPlugin.commons.nmsutil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTKey;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.ToolKit;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.FieldFilter;
import cc.commons.util.reflect.filter.MethodFilter;
import cc.commons.util.tools.CacheGettor;

/**
 * 一个用于获取NMS类的类
 * 
 * @author 聪聪
 */
public class NMSUtil {

    public static String mTestAPIVersion = "v1_7_R4";
    /** BukkitAPI版本 */
    private static CacheGettor<String> mAPIVersion = CacheGettor.create(() -> {
        if (Bukkit.getServer() != null) {
            String className = Bukkit.getServer().getClass().getPackage().getName();
            return className.substring(className.lastIndexOf('.') + 1);
        } else return mTestAPIVersion;
    });
    public static String mTestMCVersion = "1.7.10";
    /** Minecraft版本 */
    private static CacheGettor<String> mMCVersion = CacheGettor.create(() -> {
        if (Bukkit.getServer() != null) {
            String tVersionStr = Bukkit.getVersion();
            //(MC: " + this.console.getVersion() + ")"
            Matcher matcher = Pattern.compile("^.*?\\(MC: (.*?)\\)$").matcher(tVersionStr);
            if (matcher.find())
                return matcher.group(1);
        }
        return mTestMCVersion;
    });

    /**
     * NMS类
     */
    public static final Class<?> clazz_EntityPlayerMP;
    public static final Class<?> clazz_EntityPlayer;
    public static final Class<?> clazz_NMSEntity;
    public static final Class<?> clazz_NMSItemStack;
    public static final Class<?> clazz_IInventory;

    public static final Method method_CraftItemStack_asNMSCopy;
    public static final Method method_CraftItemStack_asCraftMirror;
    public static final Method method_CraftPlayer_getHandle;
    public static final Method method_CraftEntity_getHandle;
    public static final Method method_CraftWorld_getHandle;

    /**
     * CraftBukkit类
     */
    public static final Class<?> clazz_CraftItemStack;
    public static final Class<?> clazz_CraftInventory;
    public static final Class<?> clazz_CraftPlayer;

    public static final Field field_CraftItemStack_handle;

    static {
        // NMS ItemStck
        clazz_CraftItemStack = NMSUtil.getCBTClass("inventory.CraftItemStack");
        method_CraftItemStack_asNMSCopy = MethodUtil.getDeclaredMethod(clazz_CraftItemStack, "asNMSCopy", ItemStack.class);

        clazz_NMSItemStack = method_CraftItemStack_asNMSCopy.getReturnType();
        method_CraftItemStack_asCraftMirror = MethodUtil.getMethod(clazz_CraftItemStack, "asCraftMirror", clazz_NMSItemStack);
        field_CraftItemStack_handle = FieldUtil.getDeclaredField(clazz_CraftItemStack, FieldFilter.pt(clazz_NMSItemStack)).oneGet();

        clazz_CraftInventory = NMSUtil.getCBTClass("inventory.CraftInventory");
        Method method_CraftInventory_getInventory = MethodUtil.getMethod(clazz_CraftInventory, "getInventory", true);
        clazz_IInventory = method_CraftInventory_getInventory.getReturnType();

        Class<?> tEntityClazz = NMSUtil.getCBTClass("entity.CraftEntity");
        method_CraftEntity_getHandle = MethodUtil.getDeclaredMethod(tEntityClazz, "getHandle");
        clazz_NMSEntity = method_CraftEntity_getHandle.getReturnType();

        clazz_CraftPlayer = NMSUtil.getCBTClass("entity.CraftPlayer");
        method_CraftPlayer_getHandle = MethodUtil.getDeclaredMethod(clazz_CraftPlayer, MethodFilter.pn("getHandle")).oneGet();
        clazz_EntityPlayerMP = method_CraftPlayer_getHandle.getReturnType();
        clazz_EntityPlayer = clazz_EntityPlayerMP.getSuperclass();

        Class<?> tClazz = NMSUtil.getCBTClass("CraftWorld");
        method_CraftWorld_getHandle = MethodUtil.getDeclaredMethod(tClazz, "getHandle");
    }

    /**
     * 获取服务的BukkitAPI版本
     */
    public static String getBukkitAPIVersion() {
        return NMSUtil.mAPIVersion.get();
    }

    public static String getMinecraftVersion() {
        return NMSUtil.mMCVersion.get();
    }

    /**
     * 获取org.bukkit.craftbukkit类的全名
     * 
     * @param pName
     *            短名字
     * @return 完整名字
     */
    public static String getCBTName(String pName) {
        return "org.bukkit.craftbukkit." + NMSUtil.getBukkitAPIVersion() + "." + pName;
    }

    /**
     * 获取craftbukkit类的{@link Class}对象
     * 
     * @param pClazz
     *            craftbukkit类短名字,(org.bukkit.craftbukkit.version后的名字)
     */
    public static Class<?> getCBTClass(String pClazz) {
        return ClassUtil.getClass(getCBTName(pClazz));
    }

    /**
     * 获取NMS类的全名
     * 
     * @param pName
     *            短名字
     * @return 完整名字
     */
    public static String getNMSName(String pName) {
        return "net.minecraft.server." + NMSUtil.getBukkitAPIVersion() + "." + pName;
    }

    /**
     * 获取NMS类的{@link Class}对象
     * 
     * @param pClazz
     *            NMS类短名字
     */
    public static Class<?> getNMSClass(String pClazz) {
        return ClassUtil.getClass(getNMSName(pClazz));
    }

    /**
     * 获取Bukkit物品对应的MC物品,可能获取到被包装的NMS实例
     * 
     * @param pItem
     *            Bukkit物品实例
     * @return NMS ItemStack实例或者null
     */
    public static Object getNMSItem(ItemStack pItem) {
        if (pItem == null || pItem.getType() == Material.AIR)
            return null;
        Object tNMSItem = NMSUtil.getItemHandle(pItem);
        if (tNMSItem == null) {
            tNMSItem = NMSUtil.asNMSItemCopy(pItem);
        }
        return tNMSItem;
    }

    /**
     * 获取Bukkit包装的NMS物品实例
     * 
     * @param pItem
     *            Bukkit物品
     * @return NMS物品或null
     */
    public static Object getItemHandle(ItemStack pItem) {
        return field_CraftItemStack_handle.getDeclaringClass().isInstance(pItem) ? FieldUtil.getFieldValue(field_CraftItemStack_handle, pItem) : null;
    }

    /**
     * 获取Bukkit包装的NMS物品实例
     * 
     * @param pItem
     *            Bukkit物品
     * @return NMS物品或null
     */
    public static Object asNMSItemCopy(ItemStack pItem) {
        try {
            return pItem == null ? null : MethodUtil.invokeStaticMethod(method_CraftItemStack_asNMSCopy, pItem);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取NMS物品对应的Bukkit物品
     * 
     * @param pItem
     *            NMS物品实例
     * @return Bukkit物品实例或者null
     */
    public static ItemStack getCBTItem(Object pNMSItem) {
        if (!NMSUtil.clazz_NMSItemStack.isInstance(pNMSItem))
            return null;

        Object tItem;
        try {
            tItem = MethodUtil.invokeStaticMethod(method_CraftItemStack_asCraftMirror, pNMSItem);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        return ItemStack.class.isInstance(tItem) ? (ItemStack)tItem : null;

    }

    /**
     * 转换一个纯bukkit物品为由NMS物品镜像后的物品
     * 
     * @param pItem
     *            bukkit物品
     * @return 转换失败则返回源物品
     */
    public static ItemStack convertItemToNMSFormat(ItemStack pItem) {
        if (pItem == null)
            return null;

        ItemStack tItem = pItem.clone();
        int tAmount = tItem.getAmount();
        tItem.setAmount(1);

        Object tNMSItem = NMSUtil.getNMSItem(tItem);
        if (tNMSItem == null || (tItem = NMSUtil.getCBTItem(tNMSItem)) == null)
            return pItem;

        tItem.setAmount(tAmount);
        return tItem;
    }

    /**
     * 获取物品用于发送Tellraw的Json字符串
     * <p>
     * 由于Tellraw的Json中,部分值可能被修改,因此该Json不适合用作存储与序列化
     * </p>
     */
    public static String getItemTellrawJson(ItemStack pItem) {
        if (pItem == null || pItem.getType() == Material.AIR)
            return "{}";

        Map<String, Object> tContent = null;
        Object tNMSItem = NMSUtil.getNMSItem(pItem);
        Object tTag = null;
        if (tNMSItem != null && (tTag = NBTUtil.saveItemToNBT_NMS(tNMSItem)) != null) {
            if (ToolKit.compareVersion(NMSUtil.mMCVersion.get(), "1.13") >= 0) {
                return tTag.toString();
            }
            tContent = NBTUtil.getNBTTagCompoundValue(tTag);
            Object tItemNBT = tContent.remove(NBTKey.ItemTag);
            if (NBTUtil.isNBTTagCompound(tItemNBT)) {
                tContent.put(NBTKey.ItemTag, NBTSerializer.serializeNBTToTellrawJson(tItemNBT));
            }
        } else {
            tContent = new HashMap<>();
            tContent.put(NBTKey.ItemId, pItem.getTypeId() + "s");
            tContent.put(NBTKey.ItemDamage, pItem.getDurability() + "s");
            tContent.put(NBTKey.ItemCount, pItem.getAmount() + "b");
        }

        StringBuilder tItemJson = new StringBuilder("{");
        for (Map.Entry<String, Object> sEntry : tContent.entrySet()) {
            if (tItemJson.length() != 1) {
                tItemJson.append(',');
            }
            tItemJson.append(sEntry.getKey()).append(':').append(sEntry.getValue().toString());
        }

        return tItemJson.append('}').toString();
    }

    /**
     * 获取NMS玩家
     * 
     * @param pPlayer
     *            Bukkit玩家
     * @return NMS玩家或null
     */
    public static Object getNMSPlayer(Player pPlayer) {
        if (pPlayer != null) {
            if (NMSUtil.clazz_CraftPlayer.isInstance(pPlayer)) {
                try {
                    return MethodUtil.invokeMethod(method_CraftPlayer_getHandle, pPlayer);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            } else {
                return NMSUtil.getNMSEntity(pPlayer);
            }
        }
        return null;
    }

    public static Object getNMSEntity(Entity pEntity) {
        if (pEntity != null && NMSUtil.method_CraftEntity_getHandle.getDeclaringClass().isInstance(pEntity)) {
            try {
                return MethodUtil.invokeMethod(method_CraftEntity_getHandle, pEntity);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    /**
     * 获取NMS的world
     * 
     * @param pWorld
     *            Bukkit的world实例
     * @return NMS的world实例,类型一般为WorldServer
     */
    public static Object getNMSWorld(World pWorld) {
        if (pWorld != null && NMSUtil.method_CraftWorld_getHandle.getDeclaringClass().isInstance(pWorld)) {
            try {
                return MethodUtil.invokeMethod(method_CraftWorld_getHandle, pWorld);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

}
