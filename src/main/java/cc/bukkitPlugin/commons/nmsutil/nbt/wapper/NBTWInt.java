package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWInt extends NBTWNumber<Integer>{

    private int mIntValue=0;

    public static NBTWInt wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagInt;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWInt(pNBTValue);
    }

    protected NBTWInt(){}

    public NBTWInt(int pValue){
        this.mIntValue=pValue;
    }

    private NBTWInt(Object pRawValue){
        super(pRawValue);
        this.mIntValue=NBTUtil.getNBTTagIntValue(pRawValue);
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&this.mIntValue==((NBTWInt)pObj).mIntValue;
    }

    @Override
    public int hashCode(){
        return super.hashCode()^this.mIntValue;
    }

    @Override
    public String toString(){
        return this.mIntValue+"";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Int;
    }

    @Override
    public NBTWInt copy(){
        return new NBTWInt(this.mIntValue);
    }

    @Override
    public Integer getValue(){
        return this.mIntValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagInt(this.mIntValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mIntValue=pSection.getInt(pKey,0);
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mIntValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mIntValue=pDIStream.readInt();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeInt(this.mIntValue);
    }

    @Override
    public byte byteValue(){
        return (byte)(this.mIntValue&0xFF);
    }
    
    @Override
    public short shortValue(){
        return (short)(this.mIntValue&0xFFFF);
    }
    
    @Override
    public Number getNumber(){
        return this.getValue();
    }

}
