package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWFloat extends NBTWNumber<Float>{

    private float mFloatValue=0;

    public static NBTWFloat wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagFloat;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWFloat(pNBTValue);
    }

    protected NBTWFloat(){}

    public NBTWFloat(float pValue){
        this.mFloatValue=pValue;
    }

    private NBTWFloat(Object pRawValue){
        super(pRawValue);
        this.mFloatValue=NBTUtil.getNBTTagFloatValue(pRawValue);
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&this.mFloatValue==((NBTWFloat)pObj).mFloatValue;
    }

    @Override
    public int hashCode(){
        return super.hashCode()^Float.floatToIntBits(this.mFloatValue);
    }

    @Override
    public String toString(){
        return this.mFloatValue+"f";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Float;
    }

    @Override
    public NBTWFloat copy(){
        return new NBTWFloat(this.mFloatValue);
    }

    @Override
    public Float getValue(){
        return this.mFloatValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagFloat(this.mFloatValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mFloatValue=pSection.getFloat(pKey,0F);
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mFloatValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mFloatValue=pDIStream.readFloat();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeFloat(this.mFloatValue);
    }

    @Override
    public byte byteValue(){
        return (byte)(this.intValue()&0xFF);
    }

    @Override
    public short shortValue(){
        return (short)(this.intValue()&0xFFFF);
    }

    @Override
    public int intValue(){
        int t=(int)this.mFloatValue;
        return this.mFloatValue<t?t-1:t;
    }

    @Override
    public Number getNumber(){
        return this.getValue();
    }

}
