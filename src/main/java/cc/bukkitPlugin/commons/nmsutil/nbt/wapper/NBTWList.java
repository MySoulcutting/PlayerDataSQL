package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer.YamlKey;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWList extends NBTWBase<List>{

    /** NBTTagList的内容中包含的数据类型 */
    private int mContentType=0;

    private List<NBTWBase<?>> mListValue=new ArrayList<>();

    public static NBTWList wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagList;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWList(pNBTValue);
    }

    public NBTWList(){}

    public NBTWList(List<NBTWBase<?>> pValue){
        this.mListValue.addAll(pValue);
    }

    private NBTWList(Object pRawValue){
        super(pRawValue);
        List<Object> tValue=NBTUtil.getNBTTagListValue(pRawValue);
        NBTWBase<?> tWNBT;
        for(Object sObj : tValue){
            if((tWNBT=NBTWBase.tryWapper(sObj))!=null){
                this.mListValue.add(tWNBT);
            }
        }
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&this.mListValue.equals(((NBTWList)pObj).mListValue);
    }

    @Override
    public int hashCode(){
        return super.hashCode()^this.mListValue.hashCode();
    }

    @Override
    public String toString(){
        StringBuilder tSB=new StringBuilder("[");
        for(int i=0;i<this.mListValue.size();i++){
            if(i!=0){
                tSB.append(',');
            }
            tSB.append(i).append(':').append(this.mListValue.get(i));
        }
        return tSB.append(']').toString();
    }

    public byte getTypeId(){
        return NBTUtil.NBT_List;
    }

    @Override
    public NBTWList copy(){
        return new NBTWList(this.mListValue);
    }

    @Override
    public List<NBTWBase<?>> getValue(){
        return this.mListValue;
    }

    public int getSize(){
        return this.mListValue.size();
    }

    public boolean isEmpty(){
        return this.mListValue.isEmpty();
    }

    /**
     * 获取NBTTagList中的NBT类型
     * <p>
     * 一般来说,当返回值为{@link NBTUtil#NBT_End}时,该List中无内容
     * </p>
     * 
     * @return NBT类型
     */
    public int getContentType(){
        return this.mContentType;
    }

    /**
     * 获取包装实例
     * 
     * @param pIndex
     *            位置
     * @return 包装实例或null
     */
    public NBTWBase<?> get(int pIndex){
        try{
            return this.mListValue.get(pIndex);
        }catch(IndexOutOfBoundsException ignore){
        }
        return null;
    }

    public byte getByte(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWNumber?((NBTWNumber)tWNBTTag).byteValue():0;
    }

    public short getShort(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWNumber?((NBTWNumber)tWNBTTag).shortValue():0;
    }

    public int getInt(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWNumber?((NBTWNumber)tWNBTTag).intValue():0;
    }

    public long getLong(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWNumber?((NBTWNumber)tWNBTTag).longValue():0L;
    }

    public float getFloat(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWNumber?((NBTWNumber)tWNBTTag).floatValue():0.0F;
    }

    public double getDouble(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWNumber?((NBTWNumber)tWNBTTag).doubleValue():0.0D;
    }

    public String getString(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWString?((NBTWString)tWNBTTag).getValue():"";
    }

    /**
     * 获取NBTWCompound实例,如果相应位置不存在或不是该类型的NBT,将会返回一个新的NBTWCompound
     * 
     * @param pIndex
     *            位置
     * @return NBTWCompound实例
     */
    public NBTWCompound getCompound(int pIndex){
        NBTWBase<?> tWNBTTag=this.get(pIndex);
        return tWNBTTag instanceof NBTWCompound?(NBTWCompound)tWNBTTag:new NBTWCompound();
    }

    /**
     * 添加一个包装实例
     * <p>
     * 注意,添加的包装实例必须与已经存在的实例类型相同,不允许添加{@link NBTWEnd}
     * </p>
     * 
     * @param pWNBTTag
     *            包装实例
     */
    public void add(NBTWBase<?> pWNBTTag){
        this.set(this.getSize(),pWNBTTag);
    }

    /**
     * 设置包装实例到指定位置 *
     * <p>
     * 注意,添加的包装实例必须与已经存在的实例类型相同,不允许添加{@link NBTWEnd}
     * </p>
     * 
     * @param pIndex
     *            位置
     * @param pWNBTTag
     *            包装实例
     * @return 被替换的包装实例
     */
    public NBTWBase<?> set(int pIndex,NBTWBase<?> pWNBTTag){
        if(pWNBTTag==null||pWNBTTag.getTypeId()==NBTUtil.NBT_End)
            return null;

        if(this.mContentType==0){
            this.mContentType=pWNBTTag.getTypeId();
        }else if(this.mContentType!=pWNBTTag.getTypeId()||pIndex<0||pIndex>this.getSize()){
            return null;
        }
        return this.mListValue.set(pIndex,pWNBTTag);
    }

    public NBTWBase<?> remove(int pIndex){
        return this.mListValue.remove(pIndex);
    }

    @Override
    public Object builder(){
        // 总是重建,不允许缓存
        return this.convertToNBTTag();
    }

    @Override
    public Object convertToNBTTag(){
        Object tNBTTagList=NBTUtil.newNBTTagList();
        for(NBTWBase<?> sWNBT : this.mListValue){
            NBTUtil.invokeNBTTagList_add(tNBTTagList,sWNBT.builder());
        }
        return tNBTTagList;
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mListValue.clear();
        CommentedSection tSec=pSection.getOrCreateSection(pKey);
        for(String sKey : tSec.getKeys(false)){
            YamlKey tYamlKey=YamlKey.decodeYamlKey(sKey);
            NBTWBase<?> tWNBT=NBTWBase.createTag(tYamlKey.mNBTType);
            if(tWNBT!=null){
                tWNBT.readYaml(tSec,sKey);
                this.add(tWNBT);
            }
        }
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        CommentedSection tSec=pSection.createSection(pKey);
        int i=0;
        for(NBTWBase sWNBT : this.mListValue){
            String tStoreKey=YamlKey.creatYamlKey(sWNBT.getTypeId(),i);
            sWNBT.writeYaml(tSec,tStoreKey);
            i++;
        }
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mListValue.clear();
        int tSize=pDIStream.readInt();
        while(tSize-->0){
            NBTWBase<?> tWNBT=NBTWBase.createTag(pDIStream.readByte());
            if(tWNBT!=null){
                tWNBT.readStream(pDIStream);
                this.add(tWNBT);
            }
        }
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeInt(this.mListValue.size());
        for(NBTWBase sWNBT : this.mListValue){
            pDOStream.writeByte(sWNBT.getTypeId());
            sWNBT.writeStream(pDOStream);
        }
    }

}
