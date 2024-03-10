package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.StringUtil;

public class NBTWByteArray extends NBTWBase<byte[]>{

    private byte[] mByteArrayValue=new byte[0];

    public static NBTWByteArray wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagByteArray;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWByteArray(pNBTValue);
    }

    protected NBTWByteArray(){}

    public NBTWByteArray(byte[] pValue){
        this.mByteArrayValue=pValue;
    }

    private NBTWByteArray(Object pRawValue){
        super(pRawValue);
        this.mByteArrayValue=NBTUtil.getNBTTagByteArrayValue(pRawValue);
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&Arrays.equals(this.mByteArrayValue,((NBTWByteArray)pObj).mByteArrayValue);
    }

    @Override
    public int hashCode(){
        return super.hashCode()^Arrays.hashCode(this.mByteArrayValue);
    }

    @Override
    public String toString(){
        return "["+this.mByteArrayValue.length+" bytes]";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_ByteArray;
    }

    @Override
    public NBTWByteArray copy(){
        return new NBTWByteArray(this.mByteArrayValue);
    }

    @Override
    public byte[] getValue(){
        return this.mByteArrayValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagByteArray(this.mByteArrayValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        List<String> tBStrVals=StringUtil.split(pSection.getString(pKey,""),',');
        List<Byte> tBList=new ArrayList<>();
        for(String sValue : tBStrVals){
            if((sValue=sValue.trim()).isEmpty())
                continue;
            try{
                tBList.add(Byte.parseByte(sValue));
            }catch(NumberFormatException ignore){
            }
        }
        this.mByteArrayValue=new byte[tBList.size()];
        for(int i=0;i<tBList.size();i++){
            this.mByteArrayValue[i]=tBList.get(i).byteValue();
        }
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mByteArrayValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mByteArrayValue=new byte[pDIStream.readInt()];
        pDIStream.read(this.mByteArrayValue);
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeInt(this.mByteArrayValue.length);
        pDOStream.write(this.mByteArrayValue);
    }

}
