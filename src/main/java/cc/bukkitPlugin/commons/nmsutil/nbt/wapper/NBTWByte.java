package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWByte extends NBTWNumber<Byte>{

    private byte mByteValue=0;

    public static NBTWByte wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagByte;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWByte(pNBTValue);
    }

    protected NBTWByte(){}

    public NBTWByte(byte pValue){
        this.mByteValue=pValue;
    }

    private NBTWByte(Object pRawValue){
        super(pRawValue);
        this.mByteValue=NBTUtil.getNBTTagByteValue(pRawValue);
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&this.mByteValue==((NBTWByte)pObj).mByteValue;
    }

    @Override
    public int hashCode(){
        return super.hashCode()^this.mByteValue;
    }

    @Override
    public String toString(){
        return this.mByteValue+"b";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Byte;
    }

    @Override
    public NBTWByte copy(){
        return new NBTWByte(this.mByteValue);
    }

    @Override
    public Byte getValue(){
        return this.mByteValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagByte(this.mByteValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mByteValue=pSection.getByte(pKey,(byte)0);
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mByteValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mByteValue=pDIStream.readByte();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeByte(this.mByteValue);
    }

    @Override
    public Number getNumber(){
        return this.getValue();
    }

}
