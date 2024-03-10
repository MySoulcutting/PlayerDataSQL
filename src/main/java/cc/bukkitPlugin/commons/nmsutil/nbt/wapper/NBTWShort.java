package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWShort extends NBTWNumber<Short>{

    private short mShortValue=0;

    public static NBTWShort wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagShort;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWShort(pNBTValue);
    }

    protected NBTWShort(){}

    public NBTWShort(short pValue){
        this.mShortValue=pValue;
    }

    private NBTWShort(Object pRawValue){
        super(pRawValue);
        this.mShortValue=NBTUtil.getNBTTagShortValue(pRawValue);
    }

    @Override
    public int hashCode(){
        return super.hashCode()^this.mShortValue;
    }

    @Override
    public String toString(){
        return this.mShortValue+"s";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Short;
    }

    @Override
    public NBTWShort copy(){
        return new NBTWShort(this.mShortValue);
    }

    @Override
    public Short getValue(){
        return this.mShortValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagShort(this.mShortValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mShortValue=pSection.getShort(pKey,(short)0);
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mShortValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mShortValue=pDIStream.readShort();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeShort(this.mShortValue);
    }
    
    @Override
    public byte byteValue(){
        return (byte)(this.mShortValue&0xFF);
    }

    @Override
    public Number getNumber(){
        return this.getValue();
    }

}
