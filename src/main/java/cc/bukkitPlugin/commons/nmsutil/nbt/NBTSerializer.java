package cc.bukkitPlugin.commons.nmsutil.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTDeserializeException;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTSerializeException;
import cc.bukkitPlugin.commons.nmsutil.nbt.wapper.NBTWCompound;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.IOUtil;
import cc.commons.util.StringUtil;

public class NBTSerializer{

    public static class YamlKey{

        /**
         * 创建用于存储NBTTagCompound中的NBT在Yaml中的key
         * 
         * @param pNBTType
         *            NBT类型
         * @param pNBTKey
         *            NBT名字
         * @return Yaml Key
         */
        public static String creatYamlKey(byte pNBTType,String pNBTKey){
            return String.format("%02d|%s",pNBTType,pNBTKey.replace("$","\\$").replace('.','$'));
        }

        /**
         * 创建用于存储NBTTagList中的NBT在Yaml中的key
         * 
         * @param pNBTType
         *            NBT类型
         * @param pNBTKey
         *            NBT名字
         * @param pIndex
         *            当前NBT所在位置
         * @return Yaml Key
         */
        public static String creatYamlKey(byte pNBTType,int pIndex){
            return YamlKey.creatYamlKey(pNBTType,"index"+pIndex);
        }

        /**
         * 从Yaml Key中解析信息
         * 
         * @param pYamlKey
         *            Key
         * @return NBT信息
         */
        public static YamlKey decodeYamlKey(String pYamlKey){
            YamlKey tKey=new YamlKey();
            tKey.mStoreKey=pYamlKey;
            String[] tInfos=pYamlKey.split("[|]",2);
            try{
                tKey.mNBTType=Byte.parseByte(tInfos[0]);
            }catch(NumberFormatException|IndexOutOfBoundsException ignore){
            }
            if(tInfos.length>1){
                tKey.mNBTKey=tInfos[1].replaceAll("(?<!\\\\)\\$",".").replace("\\$","$");
            }
            return tKey;
        }

        /** NBT的类型值 */
        public byte mNBTType=-1;
        /** NBTCompound中的Key */
        public String mNBTKey="";
        /** 存储在Yaml中的Key */
        public String mStoreKey="";
    }

    // ----------------|| 序列化NBT到Yaml配置 ||----------------

    /**
     * 序列化NBT数据到配置节点
     * 
     * @param pItem
     *            物品
     * @param pSection
     *            配置节点
     */
    public static void serializeNBTToYaml(ItemStack pItem,CommentedSection pSection){
        NBTSerializer.serializeNBTToYaml_Tag(NBTUtil.getItemNBT(pItem),pSection);
    }

    /**
     * 序列化NBT数据到配置节点
     * 
     * @param pNBTTag
     *            NBT
     * @param pSection
     *            配置节点
     */
    public static void serializeNBTToYaml_Tag(Object pNBTTag,CommentedSection pSection){
        pSection.clear();
        if(pNBTTag!=null){
            Map<String,Object> tMapValue=NBTUtil.getNBTTagCompoundValue(pNBTTag);
            for(Map.Entry<String,Object> sEntry : tMapValue.entrySet()){
                NBTSerializer.saveNBTBaseToYaml(pSection,sEntry.getKey(),sEntry.getValue());
            }
        }
    }

    /**
     * 序列化NBT到配置节点
     * 
     * @param pSection
     *            配置节点
     * @param pKey
     *            NBT的Key
     * @param pNBTBase
     *            NBT
     */
    private static void saveNBTBaseToYaml(CommentedSection pSection,String pKey,Object pNBTBase){
        String tSaveKey=YamlKey.creatYamlKey(NBTUtil.getNBTTagTypeId(pNBTBase),pKey);
        if(NBTUtil.clazz_NBTTagEnd.isInstance(pNBTBase)){
            pSection.set(tSaveKey,"null");
        }else if(NBTUtil.clazz_NBTTagByte.isInstance(pNBTBase)){
            pSection.set(tSaveKey,NBTUtil.getNBTTagByteValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagShort.isInstance(pNBTBase)){
            pSection.set(tSaveKey,NBTUtil.getNBTTagShortValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagInt.isInstance(pNBTBase)){
            pSection.set(tSaveKey,NBTUtil.getNBTTagIntValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagLong.isInstance(pNBTBase)){
            pSection.set(tSaveKey,NBTUtil.getNBTTagLongValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagFloat.isInstance(pNBTBase)){
            pSection.set(tSaveKey,NBTUtil.getNBTTagFloatValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagDouble.isInstance(pNBTBase)){
            pSection.set(tSaveKey,NBTUtil.getNBTTagDoubleValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagByteArray.isInstance(pNBTBase)){
            byte[] tByteArray=NBTUtil.getNBTTagByteArrayValue(pNBTBase);
            String tValueStr="";
            for(byte sByte : tByteArray){
                tValueStr+=sByte+",";
            }
            if(tValueStr.endsWith(",")){
                tValueStr=tValueStr.substring(0,tValueStr.length()-1);
            }
            pSection.set(tSaveKey,tValueStr);
        }else if(NBTUtil.clazz_NBTTagString.isInstance(pNBTBase)){ // 8
            pSection.set(tSaveKey,NBTUtil.getNBTTagStringValue(pNBTBase));
        }else if(NBTUtil.clazz_NBTTagList.isInstance(pNBTBase)){ //9
            List<Object> tListValue=NBTUtil.getNBTTagListValue(pNBTBase);
            CommentedSection tChildSec=pSection.createSection(tSaveKey);
            int i=0;
            for(Object sChildNBTBase : tListValue){
                NBTSerializer.saveNBTBaseToYaml(tChildSec,String.format("index%02d",i),sChildNBTBase);
                i++;
            }
        }else if(NBTUtil.clazz_NBTTagCompound.isInstance(pNBTBase)){//10
            Map<String,Object> tMapValue=NBTUtil.getNBTTagCompoundValue(pNBTBase);
            CommentedSection tChildSec=pSection.createSection(tSaveKey);
            for(Map.Entry<String,Object> sEntry : tMapValue.entrySet()){
                NBTSerializer.saveNBTBaseToYaml(tChildSec,sEntry.getKey(),sEntry.getValue());
            }
        }else if(NBTUtil.clazz_NBTTagIntArray.isInstance(pNBTBase)){// 11
            int[] tIntArray=NBTUtil.getNBTTagIntArrayValue(pNBTBase);
            String tValueStr="";
            for(int sInt : tIntArray){
                tValueStr+=sInt+",";
            }
            if(tValueStr.endsWith(",")){
                tValueStr=tValueStr.substring(0,tValueStr.length()-1);
            }
            pSection.set(tSaveKey,tValueStr);
        }
        // do nothing
    }

    /**
     * 从配置节点载入NBT序列化数据
     * 
     * @param pSection
     *            节点
     * @return NBTTagCompound实例
     * @throws NBTDeserializeException
     */
    public static Object deserializeNBTFromYaml(CommentedSection pSection) throws NBTDeserializeException{
        try{
            Object tTag=NBTUtil.newNBTTagCompound();
            if(pSection==null)
                return tTag;

            for(String sKey : pSection.getKeys(false)){
                YamlKey tYamlKey=YamlKey.decodeYamlKey(sKey);
                Object tChildNBT=NBTSerializer.loadNBTBaseFromSection(pSection,tYamlKey);
                if(tChildNBT!=null){
                    NBTUtil.invokeNBTTagCompound_set(tTag,tYamlKey.mNBTKey,tChildNBT);
                }
            }
            return tTag;
        }catch(NBTDeserializeException exp){
            throw exp;
        }catch(Throwable exp){
            throw new NBTDeserializeException(exp);
        }
    }

    /**
     * 从配置节点反序列化NBT
     * <p>
     * 无视所有错误配置
     * </p>
     * 
     * @param pSection
     *            配置节点
     * @param pKey
     *            当前配置key
     * @return 反序列化的NBT,可能为null
     * @throws NBTDeserializeException
     */
    private static Object loadNBTBaseFromSection(CommentedSection pSection,YamlKey pKey) throws NBTDeserializeException{
        if(pKey.mNBTType==NBTUtil.NBT_End){
            return NBTUtil.newNBTTagEnd();
        }else if(pKey.mNBTType==NBTUtil.NBT_Byte){
            return NBTUtil.newNBTTagByte(pSection.getByte(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_Short){
            return NBTUtil.newNBTTagShort(pSection.getShort(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_Int){
            return NBTUtil.newNBTTagInt(pSection.getInt(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_Long){
            return NBTUtil.newNBTTagLong(pSection.getLong(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_Float){
            return NBTUtil.newNBTTagFloat(pSection.getFloat(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_Double){
            return NBTUtil.newNBTTagDouble(pSection.getDouble(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_ByteArray){
            List<Byte> tBList=new ArrayList<>();
            String[] tBStrVals=pSection.getString(pKey.mStoreKey,"").trim().split(",");
            for(String sValue : tBStrVals){
                if((sValue=sValue.trim()).isEmpty())
                    continue;
                try{
                    tBList.add(Byte.parseByte(sValue));
                }catch(NumberFormatException exp){
                    //ignore
                }
            }
            byte[] tBArray=new byte[tBList.size()];
            for(int i=0;i<tBList.size();i++)
                tBArray[i]=tBList.get(i).byteValue();
            return NBTUtil.newNBTTagByteArray(tBArray);
        }else if(pKey.mNBTType==NBTUtil.NBT_String){
            return NBTUtil.newNBTTagString(pSection.getString(pKey.mStoreKey,""));
        }else if(pKey.mNBTType==NBTUtil.NBT_List){
            Object tRestoreNBT=NBTUtil.newNBTTagList();
            CommentedSection tChildSec=pSection.getSection(pKey.mStoreKey);
            if(tChildSec!=null){
                for(String sChildKey : tChildSec.getKeys(false)){
                    Object tChildNBT=NBTSerializer.loadNBTBaseFromSection(tChildSec,YamlKey.decodeYamlKey(sChildKey));
                    if(tChildNBT!=null){
                        NBTUtil.invokeNBTTagList_add(tRestoreNBT,tChildNBT);
                    }
                }
            }
            return tRestoreNBT;
        }else if(pKey.mNBTType==NBTUtil.NBT_Compound){
            return NBTSerializer.deserializeNBTFromYaml(pSection.getSection(pKey.mStoreKey));
        }else if(pKey.mNBTType==NBTUtil.NBT_IntArray){
            List<Integer> tIList=new ArrayList<>();
            String[] tIStrValues=pSection.getString(pKey.mStoreKey,"").trim().split(",");
            for(String sValue : tIStrValues){
                if((sValue=sValue.trim()).isEmpty())
                    continue;
                try{
                    tIList.add(Integer.valueOf(sValue));
                }catch(NumberFormatException exp){
                    //ignore
                }
            }
            int[] tIArray=new int[tIList.size()];
            for(int i=0;i<tIList.size();i++)
                tIArray[i]=tIList.get(i).intValue();
            return NBTUtil.newNBTTagIntArray(tIArray);
        }
        return null;
    }

    // ----------------|| 序列化NBT到Json ||----------------

    /**
     * 获取NBT的Json串
     * <p>
     * 只对NBTTagString的toString方法重写<br />
     * 其他方法不变,不替换特殊字符
     * </p>
     *
     * @param pNBTBase
     *            NBT值
     * @return json字符串
     */
    public static String serializeNBTToJson(Object pNBTBase){
        return NBTSerializer.getNBTJson(pNBTBase,false);
    }

    /**
     * 获取NBT的Json串
     * <p>
     * 只对NBTTagString的toString方法重写<br />
     * 其他方法不变
     * </p>
     *
     * @param pNBTBase
     *            NBT值
     * @return json字符串
     */
    public static String serializeNBTToTellrawJson(Object pNBTBase){
        return NBTSerializer.getNBTJson(pNBTBase,true);
    }

    /**
     * 获取NBT的Json字符串
     * 
     * @param pNBTBase
     *            NBT
     * @param pTellraw
     *            此Json是否为发送Tellraw
     * @return Json字符串
     */
    private static String getNBTJson(Object pNBTBase,boolean pTellraw){
        if(!NBTUtil.clazz_NBTBase.isInstance(pNBTBase)){
            return "{}";
        }
        if(NBTUtil.clazz_NBTTagCompound.isInstance(pNBTBase)){
            Map<String,Object> tNBTContents=NBTUtil.getNBTTagCompoundValue(pNBTBase);
            if(tNBTContents==null||tNBTContents.isEmpty())
                return "{}";
            String tContentJson="{";
            for(String sKey : tNBTContents.keySet()){
                Object tContentNode=tNBTContents.get(sKey);
                if(tContentNode!=null){
                    tContentJson+=sKey+':'+NBTSerializer.getNBTJson(tContentNode,pTellraw)+',';
                }
            }
            if(tContentJson.lastIndexOf(",")!=-1){
                tContentJson=tContentJson.substring(0,tContentJson.length()-1);
            }
            return tContentJson+"}";
        }else if(NBTUtil.clazz_NBTTagList.isInstance(pNBTBase)){
            List<Object> tNBTContents=NBTUtil.getNBTTagListValue(pNBTBase);
            if(tNBTContents==null||tNBTContents.isEmpty())
                return "[]";
            String tContentJson="[";
            int i=0;
            for(Object tContentNode : tNBTContents){
                if(tContentNode!=null){
                    tContentJson+=i+":"+NBTSerializer.getNBTJson(tContentNode,pTellraw)+',';
                }
                i++;
            }
            if(tContentJson.lastIndexOf(",")!=-1){
                tContentJson=tContentJson.substring(0,tContentJson.length()-1);
            }
            return tContentJson+"]";
        }else if(NBTUtil.clazz_NBTTagString.isInstance(pNBTBase)){
            String tValue=NBTUtil.getNBTTagStringValue(pNBTBase);
            if(pTellraw){
                tValue=tValue.replace("\"","\\\"");
                if(StringUtil.isNotBlank(tValue)&&tValue.charAt(tValue.length()-1)=='\\'){
                    tValue=tValue+" ";
                }
            }
            return "\""+tValue+"\"";
        }else return pNBTBase.toString();
    }

    // ----------------|| 序列化NBT到GZip字节流 ||----------------

    /**
     * 序列化物品的NBT 序列化物品的NBT
     * <p>
     * 返回数据长度为0表示物品不存在NBT
     * </p>
     * 
     * @param pItem
     *            要序列化的NBT的物品来源
     * @return 序列化的NBT 字节数据,非null
     * @throws NBTSerializeException
     */
    public static byte[] serializeNBTToByte(ItemStack pItem) throws NBTSerializeException{
        return NBTSerializer.serializeNBTToByte_Tag(NBTUtil.getItemNBT(pItem));
    }

    /**
     * 序列化物品的NBT
     * <p>
     * 返回数据长度为0表示物品不存在NBT
     * </p>
     * 
     * @param pNBTTag
     *            要序列化的NBT
     * @return 序列化的NBT 字节数据,非null
     * @throws NBTSerializeException
     */
    public static byte[] serializeNBTToByte_Tag(Object pNBTTag) throws NBTSerializeException{
        if(pNBTTag!=null){
            try{
                return NBTCompressedTools.compressNBTCompound(pNBTTag);
            }catch(Throwable exp){
                throw new NBTSerializeException(exp);
            }
        }
        return new byte[0];
    }

    /**
     * 反序列化由{@link #serializeNBT(ItemStack)创建的序列化的数据}
     * 
     * @param pData
     *            序列化的NBT 字节数据
     * @return 反序列化的NBTCompound实例,非null
     * @throws NBTDeserializeException
     */
    public static Object deserializeNBTFromByte(byte[] pData) throws NBTDeserializeException{
        try{
            return NBTCompressedTools.readCompressed(pData);
        }catch(Throwable exp){
            throw new NBTDeserializeException(exp);
        }
    }

    // ----------------|| 包装NBT的序列化 ||----------------

    /**
     * 序列化包装NBT数据到配置节点,此数据通用
     * 
     * @param pNBTTag
     *            NBT包装实例,允许为null
     * @param pSection
     *            配置节点
     */
    public static void serializeWNBTToYaml(NBTWCompound pTag,CommentedSection pSection){
        pSection.clear();
        if(pTag!=null){
            pTag.writeYaml(pSection,(String)null);
        }
    }

    /**
     * 从配置节点载入NBT序列化数据
     * 
     * @param pSection
     *            节点
     * @return NBTWCompound实例
     * @throws NBTDeserializeException
     */
    public static NBTWCompound deserializeWNBTFromYaml(CommentedSection pSection) throws NBTDeserializeException{
        try{
            NBTWCompound tWTag=new NBTWCompound();
            tWTag.readYaml(pSection,(String)null);
            return tWTag;
        }catch(Throwable exp){
            throw new NBTDeserializeException(exp);
        }
    }

    /**
     * 序列化物品的NBT
     * <p>
     * 返回数据长度为0表示物品不存在NBT
     * </p>
     * 
     * @param pWTag
     *            要序列化的包装NBT,可以为null
     * @return 序列化的NBT 字节数据
     */
    public static byte[] serializeWNBTToByte(NBTWCompound pWTag) throws NBTSerializeException{
        if(pWTag==null)
            return new byte[0];

        ByteArrayOutputStream tBAOStream=new ByteArrayOutputStream();
        DataOutputStream tDOStream=null;
        try{
            tDOStream=new DataOutputStream(new GZIPOutputStream(tBAOStream));
            pWTag.writeStream(tDOStream);
            tDOStream.flush();
        }catch(Throwable exp){
            throw new NBTSerializeException(exp);
        }finally{
            IOUtil.closeStream(tDOStream);
        }
        return tBAOStream.toByteArray();
    }

    /**
     * 反序列化由{@link #serializeWNBTToByte(NBTWCompound)创建的序列化的数据}
     * 
     * @param pData
     *            序列化的NBT 字节数据
     * @return 反序列化的NBTWCompound实例,非null
     * @throws NBTDeserializeException
     */
    public static NBTWCompound deserializeWNBTFromByte(byte[] pData) throws NBTDeserializeException{
        NBTWCompound tWTag=new NBTWCompound();
        if(pData==null||pData.length==0)
            return tWTag;

        DataInputStream tDIStream=null;
        try{
            tDIStream=new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(pData)));
            tWTag.readStream(tDIStream);
        }catch(Throwable exp){
            throw new NBTDeserializeException(exp);
        }finally{
            IOUtil.closeStream(tDIStream);
        }
        return tWTag;
    }

}
