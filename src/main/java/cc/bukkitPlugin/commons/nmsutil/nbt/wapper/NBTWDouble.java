package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWDouble extends NBTWNumber<Double>{

    private double mDoubleValue=0;

    public static NBTWDouble wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagDouble;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWDouble(pNBTValue);
    }

    protected NBTWDouble(){}

    public NBTWDouble(double pValue){
        this.mDoubleValue=pValue;
    }

    private NBTWDouble(Object pRawValue){
        super(pRawValue);
        this.mDoubleValue=NBTUtil.getNBTTagDoubleValue(pRawValue);
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&this.mDoubleValue==((NBTWDouble)pObj).mDoubleValue;
    }

    @Override
    public int hashCode(){
        long t=Double.doubleToLongBits(this.mDoubleValue);
        return super.hashCode()^(int)(t^t>>>32);
    }

    @Override
    public String toString(){
        return this.mDoubleValue+"d";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_Double;
    }

    @Override
    public NBTWDouble copy(){
        return new NBTWDouble(this.mDoubleValue);
    }

    @Override
    public Double getValue(){
        return this.mDoubleValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagDouble(this.mDoubleValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mDoubleValue=pSection.getDouble(pKey,0D);
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mDoubleValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mDoubleValue=pDIStream.readDouble();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeDouble(this.mDoubleValue);
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
        int t=(int)this.mDoubleValue;
        return this.mDoubleValue<t?t-1:t;
    }

    @Override
    public long longValue(){
        return (long)Math.floor(this.mDoubleValue);
    }

    @Override
    public Number getNumber(){
        return this.getValue();
    }

}
