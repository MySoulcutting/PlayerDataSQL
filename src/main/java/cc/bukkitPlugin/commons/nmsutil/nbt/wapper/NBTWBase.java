package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.commentedyaml.CommentedSection;

/**
 * 继承自本类的必须有一个无参构造函数
 */
public abstract class NBTWBase<T>{

    /**
     * 包装指定的NMS NBT
     * 
     * @param pNMSNBTTag
     *            要包装的NBT
     * @return 包装的NBT,或者null
     */
    public static NBTWBase<?> tryWapper(Object pNMSNBTTag){
        if(!NBTUtil.clazz_NBTBase.isInstance(pNMSNBTTag))
            return null;

        if(NBTUtil.clazz_NBTTagEnd.isInstance(pNMSNBTTag)){
            return NBTWEnd.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagByte.isInstance(pNMSNBTTag)){
            return NBTWByte.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagShort.isInstance(pNMSNBTTag)){
            return NBTWShort.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagInt.isInstance(pNMSNBTTag)){
            return NBTWInt.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagLong.isInstance(pNMSNBTTag)){
            return NBTWLong.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagFloat.isInstance(pNMSNBTTag)){
            return NBTWFloat.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagDouble.isInstance(pNMSNBTTag)){
            return NBTWDouble.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagByteArray.isInstance(pNMSNBTTag)){
            return NBTWByteArray.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagString.isInstance(pNMSNBTTag)){
            return NBTWString.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagList.isInstance(pNMSNBTTag)){
            return NBTWList.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagCompound.isInstance(pNMSNBTTag)){
            return NBTWCompound.wapper(pNMSNBTTag);
        }else if(NBTUtil.clazz_NBTTagIntArray.isInstance(pNMSNBTTag)){
            return NBTWIntArray.wapper(pNMSNBTTag);
        }
        return null;
    }

    /**
     * 根据NBT类型值创建对应的NBT包装实例
     * 
     * @param pTypeId
     *            NBT类型id
     * @return 创建的实例或者null
     */
    public static NBTWBase<?> createTag(byte pTypeId){
        switch(pTypeId){
            case NBTUtil.NBT_End:
                return new NBTWEnd();
            case NBTUtil.NBT_Byte:
                return new NBTWByte();
            case NBTUtil.NBT_Short:
                return new NBTWShort();
            case NBTUtil.NBT_Int:
                return new NBTWInt();
            case NBTUtil.NBT_Long:
                return new NBTWLong();
            case NBTUtil.NBT_Float:
                return new NBTWFloat();
            case NBTUtil.NBT_Double:
                return new NBTWDouble();
            case NBTUtil.NBT_ByteArray:
                return new NBTWByteArray();
            case NBTUtil.NBT_String:
                return new NBTWString();
            case NBTUtil.NBT_List:
                return new NBTWList();
            case NBTUtil.NBT_Compound:
                return new NBTWCompound();
            case NBTUtil.NBT_IntArray:
                return new NBTWIntArray();
        }
        return null;
    }

    /** NMS NBT值缓存 */
    protected Object mCachedNMSNBTValue=null;

    protected NBTWBase(){}

    protected NBTWBase(Object pRawValue){
        if(!NBTUtil.clazz_NBTBase.isInstance(pRawValue)){
            throw new IllegalArgumentException("包装值类型必须为"+NBTUtil.clazz_NBTBase.getName()+"的子类,而实际类型为"+(pRawValue==null?"null":pRawValue.getClass().getName()));
        }

        this.mCachedNMSNBTValue=pRawValue;
    }

    /**
     * 构建NBT实例(可能为缓存)
     * 
     * @return NBTTag值,非null,保证为NMS的NBTBase的子类
     */
    public Object builder(){
        if(this.mCachedNMSNBTValue==null){
            this.mCachedNMSNBTValue=this.convertToNBTTag();
        }
        return this.mCachedNMSNBTValue;
    }

    /**
     * 标记缓存的NMS的NBT无效,一般只在对容器类的内容进行外部编辑时才需要调用,
     * 一般情况下,类本身的变更方法在被调用后,都会自动调用该函数,因此不需要二次调用
     */
    public void markCacheInvalid(){
        this.mCachedNMSNBTValue=null;
    }

    @Override
    public boolean equals(Object pObj){
        if(!(pObj instanceof NBTWBase)){
            return false;
        }
        if(this.getTypeId()!=((NBTWBase)pObj).getTypeId()){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        return this.getTypeId();
    }

    @Override
    public abstract String toString();

    /**
     * 获取NBTTag的类型id
     * 
     * @return 类型id
     */
    public abstract byte getTypeId();

    /**
     * 深拷贝NBTTag内容
     * 
     * @return NBTTag的复制
     */
    public abstract NBTWBase<T> copy();

    /**
     * 获取NBTTag的内部值
     * 
     * @return 值,可能为null,例如{@link NBTWEnd}
     */
    public abstract T getValue();

    /**
     * 转换当前的数据为对应的NMS NBTTag实例
     * 
     * @return NBTTag值,非null,保证为NMS的NBTBase的子类
     */
    public abstract Object convertToNBTTag();

    /**
     * 从Yaml中读取数据
     * <p>
     * 节点名字格式应该为 '类型数字|NBT名字'<br>
     * '类型数字' 是长度为2的数字(不足两位前面补0),用于指示NBT的类型<br>
     * 'NBT名字' 就是NMS NBT中存储的key,字符点被转义
     * </p>
     * 
     * @param pSection
     *            读取的节点
     * @param pKey
     *            节点名字
     */
    public abstract void readYaml(CommentedSection pSection,String pKey);

    /**
     * 写入数据到Yaml中 *
     * <p>
     * 节点名字格式应该为 '类型数字|NBT名字'<br>
     * '类型数字' 是长度为2的数字(不足两位前面补0),用于指示NBT的类型<br>
     * 'NBT名字' 就是NMS NBT中存储的key,字符点被转义
     * </p>
     * 
     * @param pSection
     *            写入的节点
     * @param pKey
     *            节点名字
     */
    public abstract void writeYaml(CommentedSection pSection,String pKey);

    /**
     * 从流中读取数据
     * 
     * @param pDIStream
     *            输入流
     * @throws IOException
     *             读取过程时发生异常
     */
    public abstract void readStream(DataInputStream pDIStream) throws IOException;

    /**
     * 写入数据到流
     * 
     * @param pDOStream
     *            输出流
     * @throws IOException
     *             写入过程中发生异常
     */
    public void writeStream(DataOutputStream pDOStream) throws IOException{
        pDOStream.writeByte(this.getTypeId());
    }

}
