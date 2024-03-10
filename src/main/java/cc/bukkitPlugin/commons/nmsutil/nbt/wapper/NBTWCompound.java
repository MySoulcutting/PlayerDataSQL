package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer.YamlKey;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.StringUtil;

public class NBTWCompound extends NBTWBase<Map>{

    private Map<String,NBTWBase<?>> mMapValue=new HashMap<>(0);

    public static NBTWCompound wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagCompound;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWCompound(pNBTValue);
    }

    public NBTWCompound(){}

    public NBTWCompound(Map<String,NBTWBase<?>> pValue){
        this.mMapValue=new HashMap<>(pValue);
    }

    private NBTWCompound(Object pRawValue){
        super(pRawValue);
        for(Map.Entry<String,Object> sEntry : NBTUtil.getNBTTagCompoundValue(pRawValue).entrySet()){
            NBTWBase<?> tWNBT=NBTWBase.tryWapper(sEntry.getValue());
            if(tWNBT!=null){
                this.mMapValue.put(sEntry.getKey(),tWNBT);
            }
        }
    }

    @Override
    public int hashCode(){
        return super.hashCode()^this.mMapValue.hashCode();
    }

    @Override
    public String toString(){
        StringBuilder tSB=new StringBuilder("{");
        for(Map.Entry<String,NBTWBase<?>> sEntry : this.mMapValue.entrySet()){
            if(tSB.length()!=1){
                tSB.append(',');
            }
            tSB.append(sEntry.getKey()).append(':').append(sEntry.getValue().toString());
        }
        return tSB.append('}').toString();
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Compound;
    }

    @Override
    public NBTWCompound copy(){
        return new NBTWCompound(this.mMapValue);
    }

    @Override
    public Map<String,NBTWBase<?>> getValue(){
        return this.mMapValue;
    }

    public NBTWBase<?> get(String pKey){
        return this.mMapValue.get(pKey);
    }

    public void set(String pKey,NBTWBase<?> pNBTWTag){
        if(StringUtil.isNotEmpty(pKey)&&pNBTWTag!=null){
            this.mMapValue.put(pKey,pNBTWTag);
        }
    }

    public boolean getBoolean(String pKey){
        return this.getByte(pKey)==1?true:false;
    }

    public void setBoolean(String pKey,boolean pValue){
        this.setByte(pKey,(byte)(pValue?1:0));
    }

    public byte getByte(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWNumber?((NBTWNumber)tNBTWTag).byteValue():0;
    }

    public void setByte(String pKey,byte pValue){
        this.set(pKey,new NBTWByte(pValue));
    }

    public short getShort(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWNumber?((NBTWNumber)tNBTWTag).shortValue():0;
    }

    public void setShort(String pKey,short pValue){
        this.set(pKey,new NBTWShort(pValue));
    }

    public int getInt(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWNumber?((NBTWNumber)tNBTWTag).intValue():0;
    }

    public void setInt(String pKey,int pValue){
        this.set(pKey,new NBTWInt(pValue));
    }

    public long getLong(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWNumber?((NBTWNumber)tNBTWTag).longValue():0L;
    }

    public void setLong(String pKey,long pValue){
        this.set(pKey,new NBTWLong(pValue));
    }

    public float getFloat(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWNumber?((NBTWNumber)tNBTWTag).floatValue():0.0F;
    }

    public void setFloat(String pKey,float pValue){
        this.set(pKey,new NBTWFloat(pValue));
    }

    public double getDouble(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWNumber?((NBTWNumber)tNBTWTag).doubleValue():0.0D;
    }

    public void setDouble(String pKey,double pValue){
        this.set(pKey,new NBTWDouble(pValue));
    }

    public byte[] getByteArray(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWByteArray?((NBTWByteArray)tNBTWTag).getValue():new byte[0];
    }

    public void setByteArray(String pKey,byte[] pValue){
        this.set(pKey,new NBTWByteArray(pValue));
    }

    public String getString(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWString?((NBTWString)tNBTWTag).getValue():"";
    }

    public void setString(String pKey,String pValue){
        this.set(pKey,new NBTWString(pValue));
    }

    /**
     * 获取NBTTagList的包装实例
     * 
     * @param pKey
     *            NBT Key
     * @param pNBTType
     *            NBTTagList中的NBT类型
     * @return 非空
     */
    public NBTWList getList(String pKey,int pNBTType){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        if(tNBTWTag instanceof NBTWList){
            NBTWList tWList=(NBTWList)tNBTWTag;
            if(tWList.isEmpty()||tWList.getContentType()==pNBTType)
                return tWList;
        }
        return new NBTWList();
    }

    public NBTWCompound getCompound(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWCompound?(NBTWCompound)tNBTWTag:new NBTWCompound();
    }

    public int[] getIntArray(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag instanceof NBTWIntArray?((NBTWIntArray)tNBTWTag).getValue():new int[0];
    }

    public void setIntArray(String pKey,int[] pValue){
        this.set(pKey,new NBTWIntArray(pValue));
    }

    public UUID getUUID(String pKey){
        return new UUID(getLong(pKey+"Most"),getLong(pKey+"Least"));
    }

    public void setUUID(String pKey,UUID pUUID){
        this.setLong(pKey+"Most",pUUID.getMostSignificantBits());
        this.setLong(pKey+"Least",pUUID.getLeastSignificantBits());
    }

    public boolean isUUID(String pKey){
        return (hasKeyOfType(pKey+"Most",99))&&(hasKeyOfType(pKey+"Least",99));
    }

    /**
     * 获取对应名字所存储的NBT的类型
     * 
     * @param pKey
     *            存储key
     * @return NBT类型,如果不存在则返回{@link NBTUtil#NBT_End}
     */
    public int getType(String pKey){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        return tNBTWTag==null?NBTUtil.NBT_End:tNBTWTag.getTypeId();
    }

    public boolean hasKeyOfType(String pKey,int pTypeId){
        NBTWBase<?> tNBTWTag=this.get(pKey);
        if(tNBTWTag==null)
            return false;

        return tNBTWTag.getTypeId()==pTypeId||(pTypeId==NBTUtil.NBT_Number&&tNBTWTag instanceof NBTWNumber);
    }

    public boolean hasKey(String pKey){
        return this.mMapValue.containsKey(pKey);
    }

    public int getSize(){
        return this.mMapValue.size();
    }

    public Set<String> keySet(){
        return this.mMapValue.keySet();
    }

    @Override
    public Object builder(){
        // 总是重建,不使用缓存
        return this.convertToNBTTag();
    }

    @Override
    public Object convertToNBTTag(){
        Object tNBTCompound=NBTUtil.newNBTTagCompound();
        Map<String,Object> tNBTMapValue=NBTUtil.getNBTTagCompoundValue(tNBTCompound);
        for(Map.Entry<String,NBTWBase<?>> sEntry : this.mMapValue.entrySet()){
            tNBTMapValue.put(sEntry.getKey(),sEntry.getValue().builder());
        }
        return tNBTCompound;
    }

    /**
     * @param pKey
     *            要反序列化到的节点,如果为null,则从参数节点反序列化
     */
    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mMapValue.clear();
        CommentedSection tSec;
        if(pKey==null){
            tSec=pSection;
        }else{
            tSec=pSection.getOrCreateSection(pKey);
        }
        for(String sKey : tSec.getKeys(false)){
            YamlKey tYamlKey=YamlKey.decodeYamlKey(sKey);
            NBTWBase<?> tWNBT=NBTWBase.createTag(tYamlKey.mNBTType);
            if(tWNBT!=null){
                tWNBT.readYaml(tSec,sKey);
                this.mMapValue.put(tYamlKey.mNBTKey,tWNBT);
            }
        }
        this.markCacheInvalid();
    }

    /**
     * @param pKey
     *            要序列化到的节点,如果为null,则序列化到参数节点
     */
    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        CommentedSection tSec;
        if(pKey==null){
            pSection.clear();
            tSec=pSection;
        }else{
            tSec=pSection.createSection(pKey);
        }
        for(Map.Entry<String,NBTWBase<?>> sEntry : this.mMapValue.entrySet()){
            String tStoreKey=YamlKey.creatYamlKey(sEntry.getValue().getTypeId(),sEntry.getKey());
            sEntry.getValue().writeYaml(tSec,tStoreKey);
        }
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mMapValue.clear();
        int tSize=pDIStream.readInt();
        while(tSize-->0){
            String tStoreKey=pDIStream.readUTF();
            NBTWBase<?> tWNBT=NBTWBase.createTag(pDIStream.readByte());
            if(tWNBT!=null){
                tWNBT.readStream(pDIStream);
                this.mMapValue.put(tStoreKey,tWNBT);
            }
        }
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeInt(this.mMapValue.size());
        for(Map.Entry<String,NBTWBase<?>> sEntry : this.mMapValue.entrySet()){
            pDOStream.writeUTF(sEntry.getKey());
            sEntry.getValue().writeStream(pDOStream);
        }
    }

}
