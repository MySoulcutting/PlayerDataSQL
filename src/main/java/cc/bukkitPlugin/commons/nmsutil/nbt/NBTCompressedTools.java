package cc.bukkitPlugin.commons.nmsutil.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NBTCompressedTools{

    /**
     * 将NBTTagCompound序列化为byte[]
     * 
     * @param pNBTTag
     *            NBTTagCompound实例,允许为null
     * @return 序列化后的字节数据,非null,0长度表示无数据
     * @throws IOException
     *             序列化过程中发生错误
     */
    public static byte[] compressNBTCompound(Object pNBTTag) throws IOException{
        if(pNBTTag==null)
            return new byte[0];
        if(!NBTUtil.clazz_NBTTagCompound.isInstance(pNBTTag))
            throw new IOException("参数类型必须为NBTTagCompound");
        if(NBTUtil.getNBTTagCompoundValue(pNBTTag).isEmpty())
            return new byte[0];

        ByteArrayOutputStream tBAOStream=new ByteArrayOutputStream();
        DataOutputStream tDOStream=new DataOutputStream(new GZIPOutputStream(tBAOStream));

        try{
            NBTCompressedTools.compressNBTBase(pNBTTag,tDOStream);
            tDOStream.flush();
        }finally{
            tDOStream.close();
        }

        return tBAOStream.toByteArray();
    }

    /**
     * 将NBTBase序列化后写入流中
     * 
     * @param pNBTBase
     *            NBTBase的子类
     * @param pStream
     *            输出流
     * @throws IOException
     *             写入过程中发生错误
     */
    public static void compressNBTBase(Object pNBTBase,DataOutputStream pStream) throws IOException{
        if(!NBTUtil.clazz_NBTBase.isInstance(pNBTBase))
            throw new IllegalArgumentException("参数类型必须为NBTBase及其子类");

        byte typeId=NBTUtil.getNBTTagTypeId(pNBTBase);
        pStream.writeByte(typeId);
        switch(typeId){
            case NBTUtil.NBT_End: //NBTTagEnd
                return;
            case NBTUtil.NBT_Byte: // NBTTagByte 
                pStream.writeByte(NBTUtil.getNBTTagByteValue(pNBTBase));
                return;
            case NBTUtil.NBT_Short: // NBTTagShort
                pStream.writeShort(NBTUtil.getNBTTagShortValue(pNBTBase));
                return;
            case NBTUtil.NBT_Int: // NBTTagInt
                pStream.writeInt(NBTUtil.getNBTTagIntValue(pNBTBase));
                return;
            case NBTUtil.NBT_Long: // NBTTagLong
                pStream.writeLong(NBTUtil.getNBTTagLongValue(pNBTBase));
                return;
            case NBTUtil.NBT_Float: // NBTTagFloat
                pStream.writeFloat(NBTUtil.getNBTTagFloatValue(pNBTBase));
                return;
            case NBTUtil.NBT_Double: // NBTTagDouble
                pStream.writeDouble(NBTUtil.getNBTTagDoubleValue(pNBTBase));
                return;
            case NBTUtil.NBT_ByteArray: // NBTTagByteArray
                byte[] tByteArrValue=NBTUtil.getNBTTagByteArrayValue(pNBTBase);
                pStream.writeInt(tByteArrValue.length);
                pStream.write(tByteArrValue);
                return;
            case NBTUtil.NBT_String: // NBTTagString
                pStream.writeUTF(NBTUtil.getNBTTagStringValue(pNBTBase));
                return;
            case NBTUtil.NBT_List: // NBTTagList
                List<Object> tNBTBaseArrValue=NBTUtil.getNBTTagListValue(pNBTBase);
                pStream.writeInt(tNBTBaseArrValue.size());
                for(Object sNBTBase : tNBTBaseArrValue){
                    NBTCompressedTools.compressNBTBase(sNBTBase,pStream);
                }
                return;
            case NBTUtil.NBT_Compound: // NBTTagCompound
                Map<String,Object> tNBTBaseMapValue=NBTUtil.getNBTTagCompoundValue(pNBTBase);
                pStream.writeInt(tNBTBaseMapValue.size());
                for(Map.Entry<String,Object> sEntry : tNBTBaseMapValue.entrySet()){
                    pStream.writeUTF(String.valueOf(sEntry.getKey()));
                    NBTCompressedTools.compressNBTBase(sEntry.getValue(),pStream);
                }
                return;
            case NBTUtil.NBT_IntArray: // NBTTagIntArray
                int[] tIntArrValue=NBTUtil.getNBTTagIntArrayValue(pNBTBase);
                pStream.writeInt(tIntArrValue.length);
                for(int i=0;i<tIntArrValue.length;i++){
                    pStream.writeInt(tIntArrValue[i]);
                }
                return;
            default:
                //do nothing
        }
    }

    /**
     * 读取压缩的NBT数据
     * 
     * @param pCompressedData
     *            压缩的NBT数据
     * @return 反序列化的NBT实例,总是不为null
     * @throws IOException
     *             数据读取过程中发生异常,根节点不是NBTTagCompound
     */
    public static Object readCompressed(byte[] pCompressedData) throws IOException{
        if(pCompressedData==null||pCompressedData.length==0)
            return NBTUtil.newNBTTagCompound();

        DataInputStream tDIStream=new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(pCompressedData)));

        try{
            Object tNBTTag=NBTCompressedTools.readCompressed(tDIStream);
            if(!NBTUtil.clazz_NBTTagCompound.isInstance(tNBTTag)){
                throw new IOException("根NBT节点不是NBTTagCompound");
            }
            return tNBTTag;
        }finally{
            tDIStream.close();
        }
    }

    public static Object readCompressed(DataInputStream pStream) throws IOException{
        byte typeId=pStream.readByte();
        int length;
        switch(typeId){
            case NBTUtil.NBT_Byte: // NBTTagByte
                return NBTUtil.newNBTTagByte(pStream.readByte());
            case NBTUtil.NBT_Short: // NBTTagShort
                return NBTUtil.newNBTTagShort(pStream.readShort());
            case NBTUtil.NBT_Int: // NBTTagInt
                return NBTUtil.newNBTTagInt(pStream.readInt());
            case NBTUtil.NBT_Long: // NBTTagLong
                return NBTUtil.newNBTTagLong(pStream.readLong());
            case NBTUtil.NBT_Float: // NBTTagFloat
                return NBTUtil.newNBTTagFloat(pStream.readFloat());
            case NBTUtil.NBT_Double: // NBTTagDouble
                return NBTUtil.newNBTTagDouble(pStream.readDouble());
            case NBTUtil.NBT_ByteArray: // NBTTagByteArray
                length=pStream.readInt();
                byte[] tByteArrValue=new byte[length];
                pStream.read(tByteArrValue);
                return NBTUtil.newNBTTagByteArray(tByteArrValue);
            case NBTUtil.NBT_String: // NBTTagString
                return NBTUtil.newNBTTagString(pStream.readUTF());
            case NBTUtil.NBT_List: // NBTTagList
                Object tNBTTagList=NBTUtil.newNBTTagList();
                length=pStream.readInt();
                for(int i=0;i<length;i++){
                    NBTUtil.invokeNBTTagList_add(tNBTTagList,NBTCompressedTools.readCompressed(pStream));
                }
                return tNBTTagList;
            case NBTUtil.NBT_Compound: // NBTTagCompound
                Object tNBTTagCompound=NBTUtil.newNBTTagCompound();
                length=pStream.readInt();
                for(int i=0;i<length;i++){
                    NBTUtil.invokeNBTTagCompound_set(tNBTTagCompound,pStream.readUTF(),NBTCompressedTools.readCompressed(pStream));
                }
                return tNBTTagCompound;
            case NBTUtil.NBT_IntArray: // NBTTagIntArray
                length=pStream.readInt();
                int[] tIntArrValue=new int[length];
                for(int i=0;i<tIntArrValue.length;i++){
                    tIntArrValue[i]=pStream.readInt();
                }
                return NBTUtil.newNBTTagIntArray(tIntArrValue);
            default: //NBTTagEnd
                return NBTUtil.newNBTTagEnd();
        }
    }

}
