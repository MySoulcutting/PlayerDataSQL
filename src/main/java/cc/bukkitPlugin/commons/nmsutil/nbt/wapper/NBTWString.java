package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWString extends NBTWBase<String>{

    private String mStringValue="";

    public static NBTWString wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagString;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWString(pNBTValue);
    }

    protected NBTWString(){}

    public NBTWString(String pValue){
        this.mStringValue=pValue;
        if(pValue==null){
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    private NBTWString(Object pRawValue){
        super(pRawValue);
        this.mStringValue=NBTUtil.getNBTTagStringValue(pRawValue);
    }

    @Override
    public int hashCode(){
        return super.hashCode()^this.mStringValue.hashCode();
    }

    @Override
    public String toString(){
        return "\""+this.mStringValue.replace("\"","\\\"")+"\"";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_String;
    }

    @Override
    public NBTWString copy(){
        return new NBTWString(this.mStringValue);
    }

    @Override
    public String getValue(){
        return this.mStringValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagString(this.mStringValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        this.mStringValue=pSection.getString(pKey,"");
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mStringValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mStringValue=pDIStream.readUTF();
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeUTF(this.mStringValue);
    }

}
