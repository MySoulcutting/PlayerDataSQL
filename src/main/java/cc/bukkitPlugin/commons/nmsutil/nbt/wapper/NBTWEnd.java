package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

public class NBTWEnd extends NBTWBase<Object>{

    public static NBTWEnd wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagEnd;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWEnd(pNBTValue);
    }

    public NBTWEnd(){}

    private NBTWEnd(Object pRawValue){
        super(pRawValue);
    }

    @Override
    public String toString(){
        return "END";
    }

    public byte getTypeId(){
        return NBTUtil.NBT_End;
    }

    @Override
    public NBTWEnd copy(){
        return new NBTWEnd(NBTUtil.invokeNBTTagCopy(convertToNBTTag()));
    }

    @Override
    public Object getValue(){
        return null;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagEnd();
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){}

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){}

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{}

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
    }
}
