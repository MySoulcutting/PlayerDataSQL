package cc.bukkitPlugin.pds.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.FieldUtil;
import cc.commons.util.reflect.MethodUtil;

public class CapabilityHelper {

    private static boolean mInitSuccess = true;

    /** CapabilityDispatcher capabilities */
    private static Field field_NMSEntity_capabilities;
    /** ICapabilityProvider[] caps; */
    private static Field field_CapabilityDispatcher_caps;
    /** INBTSerializable<NBTBase>[] writers; */
    private static Field field_CapabilityDispatcher_writers;

    /** T serializeNBT(); */
    private static Method method_INBTSerializable_serializeNBT;
    /** void deserializeNBT(T); */
    private static Method method_INBTSerializable_deserializeNBT;

    /** T getCapability(Capability<T>,EnumFacing) */
    private static Method method_NMSEntity_getCapability;
    /** NBTBase writeNBT(T instance, EnumFacing side); */
    private static Method method_Capability_writeNBT;
    /** void readNBT(T instance, EnumFacing side, NBTBase nbt); */
    private static Method method_Capability_readNBT;

    static {
        try {
            field_NMSEntity_capabilities = FieldUtil.getDeclaredField(NMSUtil.clazz_NMSEntity, "capabilities");
        } catch (IllegalStateException exp) {
            mInitSuccess = false;
        }

        try {
            field_CapabilityDispatcher_caps = FieldUtil.getDeclaredField(field_NMSEntity_capabilities.getType(), "caps");
            field_CapabilityDispatcher_writers = FieldUtil.getDeclaredField(field_NMSEntity_capabilities.getType(), "writers");
        } catch (IllegalStateException | NullPointerException exp) {
            mInitSuccess = false;
        }

        try {
            Class<?> tClazz = ClassUtil.getClass("net.minecraftforge.common.util.INBTSerializable");
            method_INBTSerializable_serializeNBT = MethodUtil.getMethodIgnoreParam(tClazz, "serializeNBT", true).oneGet();
            method_INBTSerializable_deserializeNBT = MethodUtil.getMethodIgnoreParam(tClazz, "deserializeNBT", true).oneGet();
        } catch (IllegalStateException | NullPointerException exp) {
            mInitSuccess = false;
        }

        try {
            method_NMSEntity_getCapability = MethodUtil.getMethodIgnoreParam(NMSUtil.clazz_NMSEntity, "getCapability", true).oneGet();
        } catch (IllegalStateException exp) {
            mInitSuccess = false;
        }

        try {
            Class<?> tClazz = ClassUtil.getClass("net.minecraftforge.common.capabilities.Capability");
            method_Capability_writeNBT = MethodUtil.getMethodIgnoreParam(tClazz, "writeNBT", true).oneGet();
            method_Capability_readNBT = MethodUtil.getMethodIgnoreParam(tClazz, "readNBT", true).oneGet();
        } catch (IllegalStateException | NullPointerException exp) {
            mInitSuccess = false;
        }
    }

    public static boolean isInisSuccess() {
        return CapabilityHelper.mInitSuccess;
    }

    public static Object getCapabilityProvider(Object pNMSEntity, Class<?> pProvider) {
        if (!isInisSuccess()) return null;
        // capabilities
        Object tObj = FieldUtil.getFieldValue(field_NMSEntity_capabilities, pNMSEntity);
        // providers
        tObj = FieldUtil.getFieldValue(field_CapabilityDispatcher_caps, tObj);
        for (Object sObj : (Object[])tObj) {
            if (pProvider.isInstance(sObj)) return sObj;
        }

        return null;
    }

    public static Object getCapabilityStorage(Object pNMSEntity, Class<?> pProvider) {
        if (!isInisSuccess()) return null;
        // capabilities
        Object tObj = FieldUtil.getFieldValue(field_NMSEntity_capabilities, pNMSEntity);
        // writers
        tObj = FieldUtil.getFieldValue(field_CapabilityDispatcher_writers, tObj);
        for (Object sObj : (Object[])tObj) {
            if (pProvider.isInstance(sObj)) return sObj;
        }

        return null;
    }

    public static Object serializeCapability(Object pNMSEntity, Class<?> pProvider) {
        if (!isInisSuccess()) return NBTUtil.newNBTTagCompound();

        Object tStorage = getCapabilityStorage(pNMSEntity, pProvider);
        if (tStorage != null) {
            return MethodUtil.invokeMethod(method_INBTSerializable_serializeNBT, tStorage);
        }

        return NBTUtil.newNBTTagCompound();
    }

    public static void deserializeCapability(Object pNMSEntity, Class<?> pProvider, Object pNBT) {
        if (!isInisSuccess()) return;

        Object tStorage = getCapabilityStorage(pNMSEntity, pProvider);
        if (tStorage != null) {
            MethodUtil.invokeMethod(method_INBTSerializable_deserializeNBT, tStorage, pNBT);
        }
    }

    /**
     * @param pNMSEntity
     *            NMS实体实例
     * @param pCapabilityEntry
     *            Capability的Key(net.minecraftforge.common.capabilities.Capability)
     * @param pFacing
     *            朝向(EnumFacing)
     * @return
     */
    public static Object getCapability(Object pNMSEntity, Object pCapabilityEntry, @Nullable Object pFacing) {
        if (!isInisSuccess()) return null;

        return MethodUtil.invokeMethod(method_NMSEntity_getCapability, pNMSEntity, pCapabilityEntry, pFacing);
    }

    /**
     * 从NBT还原Capability的数据
     * 
     * @param pNMSEntity
     *            NMS实体实例
     * @param pCapabilityEntry
     *            Capability的Key(net.minecraftforge.common.capabilities.Capability)
     * @param pFacing
     *            朝向(EnumFacing)
     * @return
     */
    public static void readCapabilityNBT(Object pNMSEntity, Object pCapabilityEntry, @Nullable Object pFacing, Object pNBT) {
        if (!isInisSuccess()) return;

        Object tCap = getCapability(pNMSEntity, pCapabilityEntry, pFacing);
        if (tCap == null) return;
        MethodUtil.invokeMethod(method_Capability_readNBT, pCapabilityEntry, tCap, pFacing, pNBT);
    }

    /**
     * 将Capability的数据序列化到NBT中
     * 
     * @param pNMSEntity
     *            NMS实体实例
     * @param pCapabilityEntry
     *            Capability的Key(net.minecraftforge.common.capabilities.Capability)
     * @param pFacing
     *            朝向(EnumFacing)
     * @return
     */
    public static Object writeCapabilityNBT(Object pNMSEntity, Object pCapabilityEntry, @Nullable Object pFacing) {
        if (!isInisSuccess()) return NBTUtil.newNBTTagCompound();

        Object tCap = getCapability(pNMSEntity, pCapabilityEntry, pFacing);
        if (tCap == null) return NBTUtil.newNBTTagCompound();
        return MethodUtil.invokeMethod(method_Capability_writeNBT, pCapabilityEntry, tCap, pFacing);
    }

}