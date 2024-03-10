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

public class NBTWIntArray extends NBTWBase<int[]>{

    private int[] mIntArrayValue=new int[0];

    public static NBTWIntArray wapper(Object pNBTValue){
        Class<?> tTagClazz=NBTUtil.clazz_NBTTagIntArray;
        if(!tTagClazz.isInstance(pNBTValue))
            throw new IllegalArgumentException("Value must be type of "+tTagClazz.getName());
        return new NBTWIntArray(pNBTValue);
    }

    protected NBTWIntArray(){}

    public NBTWIntArray(int[] pValue){
        this.mIntArrayValue=pValue;
    }

    private NBTWIntArray(Object pRawValue){
        super(pRawValue);
        this.mIntArrayValue=NBTUtil.getNBTTagIntArrayValue(pRawValue);
    }

    @Override
    public boolean equals(Object pObj){
        return super.equals(pObj)&&Arrays.equals(this.mIntArrayValue,((NBTWIntArray)pObj).mIntArrayValue);
    }

    @Override
    public int hashCode(){
        return super.hashCode()^Arrays.hashCode(this.mIntArrayValue);
    }

    @Override
    public String toString(){
        StringBuilder sSB=new StringBuilder("[");
        int[] aint=this.mIntArrayValue;
        int i=aint.length;
        for(int j=0;j<i;j++){
            sSB.append(aint[j]).append(',');
        }
        return sSB.append(']').toString();
    }

    public byte getTypeId(){
        return NBTUtil.NBT_IntArray;
    }

    @Override
    public NBTWIntArray copy(){
        return new NBTWIntArray(this.mIntArrayValue);
    }

    @Override
    public int[] getValue(){
        return this.mIntArrayValue;
    }

    @Override
    public Object convertToNBTTag(){
        return NBTUtil.newNBTTagIntArray(this.mIntArrayValue);
    }

    @Override
    public void readYaml(CommentedSection pSection,String pKey){
        List<Integer> tIList=new ArrayList<>();
        List<String> tIStrValues=StringUtil.split(pSection.getString(pKey,"").trim(),',');
        for(String sValue : tIStrValues){
            if((sValue=sValue.trim()).isEmpty())
                continue;
            try{
                tIList.add(Integer.valueOf(sValue));
            }catch(NumberFormatException ignore){
            }
        }
        this.mIntArrayValue=new int[tIList.size()];
        for(int i=0;i<tIList.size();i++){
            this.mIntArrayValue[i]=tIList.get(i).intValue();
        }
        this.markCacheInvalid();
    }

    @Override
    public void writeYaml(CommentedSection pSection,String pKey){
        pSection.set(pKey,this.mIntArrayValue);
    }

    @Override
    public void readStream(DataInputStream pDIStream) throws IOException{
        this.mIntArrayValue=new int[pDIStream.readInt()];
        for(int i=0;i<this.mIntArrayValue.length;i++){
            this.mIntArrayValue[i]=pDIStream.readInt();
        }
        this.markCacheInvalid();
    }

    @Override
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        super.writeStream(pDOStream);
        pDOStream.writeInt(this.mIntArrayValue.length);
        for(int i=0;i<this.mIntArrayValue.length;i++){
            pDOStream.writeInt(this.mIntArrayValue[i]);
        }
    }

}
