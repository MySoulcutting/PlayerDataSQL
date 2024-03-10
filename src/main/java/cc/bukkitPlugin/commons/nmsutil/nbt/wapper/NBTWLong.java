package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWLong extends NBTWNumber<Long>{

    private long mLongValue=0;

    public static NBTWLong wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagLong;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWLong(pNBTValue);
    }

    protected NBTWLong(){}

    public NBTWLong(long pValue){
        this.mLongValue=pValue;
    }

    private NBTWLong(Object pRawValue){
        super(pRawValue);
        this.mLongValue=NBTUtil.getNBTTagLongValue(pRawValue);
    }

    @Override
    public int hashCode(){
        return super.hashCode()^(int)(this.mLongValue^this.mLongValue>>>32);
    }

    @Override
    public String toString(){
        return this.mLongValue+"L";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Long;
    }

    @Override
    public NBTWLong copy(){
        return new NBTWLong(this.mLongValue);
    }

    @Override
    public Long getValue(){
        return this.mLongValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagLong(this.mLongValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mLongValue=pSection.getLong(pKey,0L);
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mLongValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mLongValue=pDIStream.readLong();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeLong(this.mLongValue);
    }

    @Override
    public byte byteValue(){
        return (byte)(this.mLongValue&0xFF);
    }

    @Override
    public short shortValue(){
        return (short)(this.mLongValue&0xFFFF);
    }

    @Override
    public int intValue(){
        return (int)(this.mLongValue&0xFFFFFFFF);
    }

    @Override
    public Number getNumber(){
        return this.getValue();
    }

}
