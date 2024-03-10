package cc.commons.commentedyaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.error.YAMLException;

import cc.commons.commentedyaml.serialize.SerializableYamlObject;
import cc.commons.commentedyaml.serialize.convert.SerializableYamlUtils;

/**
 * 支持注释的节点
 * 
 * @author 聪聪
 *
 */
public class CommentedSection{

    /** 值存放处 */
    protected final Map<String,CommentedValue> mChild=Collections.synchronizedMap(new LinkedHashMap<String,CommentedValue>());
    private final CommentedYamlConfig mRoot;
    private final CommentedSection mParent;
    protected String mName;

    /**
     * 创建一个{@link CommentedYamlConfig}的实例
     */
    protected CommentedSection(){
        if(!(this instanceof CommentedYamlConfig))
            throw new IllegalStateException("此构造函数只能用于创建根节点");
        this.mName="";
        this.mParent=null;
        this.mRoot=(CommentedYamlConfig)this;
    }

    /**
     * 创建一个实例
     * 
     * @param pParent
     *            父亲节点,不能为空
     * @param pName
     *            节点名
     */
    protected CommentedSection(CommentedSection pParent,String pName){
        if(pParent==null)
            throw new IllegalArgumentException("父节点不能为null");
        if(pName==null)
            throw new IllegalArgumentException("路径不能为null");

        this.mName=pName;
        this.mParent=pParent;
        this.mRoot=pParent.getRoot();
        if(mRoot==null)
            throw new IllegalArgumentException("父节点的根节点不能为空");
    }

    /**
     * 获取该节点下,所有值的key
     * <p>
     * 深层次的key可以直接使用{@link CommentedSection#get(String)}获取值
     * </p>
     * 
     * @param pDeep
     *            是否获取更低层的节点的key
     * @return 所有值对应的key
     */
    public Set<String> getKeys(boolean pDeep){
        Set<String> result=new LinkedHashSet<String>();
        this.getChildKeys(result,"",pDeep);
        return result;
    }

    /**
     * 获取该节点下,所有key与值
     * 
     * @param pDeep
     *            是否获取更低层的节点的key与值
     * @return 所有key与值
     */
    public Map<String,Object> getValues(boolean pDeep){
        Map<String,Object> result=new LinkedHashMap<String,Object>();
        this.getChildValues(result,pDeep);
        return result;
    }

    /**
     * 当前路径下是否已经设置过值
     * 
     * @param pPath
     *            路径
     * @return 是否设置过值
     */
    public boolean contains(String pPath){
        return this.get(pPath)!=null;
    }

    /**
     * 获取当前节点到根节点的路径
     * 
     * @return 路径
     */
    public String getCurrentPath(){
        if(mParent==null)
            return this.mName;

        char tSeparator=this.mRoot.options().pathSeparator();
        StringBuilder tPath=new StringBuilder();
        tPath.append(this.mName);
        CommentedSection tSection=this;
        while(true){
            tSection=tSection.getParent();
            if(tSection==null||tSection==this.mRoot)
                break;

            tPath.insert(0,tSeparator).insert(0,tSection.mName);
        }
        return tPath.toString();
    }

    /** 获取当前节点的名字 */
    public String getName(){
        return this.mName;
    }

    /** 获取当前节点的根节点 */
    public CommentedYamlConfig getRoot(){
        return this.mRoot;
    }

    /** 获取当前节点的父节点 */
    public CommentedSection getParent(){
        return this.mParent;
    }

    /**
     * 获取指定路径节点的父节点,如果不存在,返回null
     * <p>
     * 比如当pPath=key1.key2时,函数将查找key2的值存储的节点,父节点
     * 然后将pWarpKey的值{@link WarpKey#mValue}设置为key2
     * </p>
     * 
     * @param pPath
     *            路径
     * @param pWarpKey
     *            用于存储该路径分割后的最后一个key
     * @return 父节点或null
     */
    protected CommentedSection getParentSection(String pPath,WarpKey pWarpKey){
        return this.getParentSection(pPath,pWarpKey,false);
    }

    /**
     * 获取指定路径节点的父节点,如果不存在,返回null
     * <p>
     * 比如当pPath=key1.key2时,函数将查找key2的值存储的节点,父节点
     * 然后将pWarpKey的值{@link WarpKey#mValue}设置为key2
     * </p>
     * 
     * @param pPath
     *            路径
     * @param pWarpKey
     *            用于存储该路径分割后的最后一个key
     * @return 父节点或null
     */
    protected CommentedSection getParentSection(List<String> pPath,WarpKey pWarpKey){
        return this.getParentSection(pPath,pWarpKey,false);
    }

    /**
     * 获取指定路径节点的父节点,如果不存在,则创建对应的节点
     * <p>
     * 比如当pPath=key1.key2时,函数将查找key2的值存储的节点,父节点
     * 然后将pWarpKey的值{@link WarpKey#mValue}设置为key2
     * </p>
     * 
     * @param pPath
     *            路径
     * @param pWarpKey
     *            用于存储该路径分割后的最后一个key
     * @return 父节点或null
     */
    protected CommentedSection getOrCreateParentSection(String pPath,WarpKey pWarpKey){
        return this.getParentSection(pPath,pWarpKey,true);
    }

    /**
     * 获取指定路径节点的父节点,如果不存在,则创建对应的节点
     * <p>
     * 比如当pPath=key1.key2时,函数将查找key2的值存储的节点,父节点
     * 然后将pWarpKey的值{@link WarpKey#mValue}设置为key2
     * </p>
     * 
     * @param pPath
     *            路径
     * @param pWarpKey
     *            用于存储该路径分割后的最后一个key
     * @return 父节点或null
     */
    protected CommentedSection getOrCreateParentSection(List<String> pPath,WarpKey pWarpKey){
        return this.getParentSection(pPath,pWarpKey,true);
    }

    /**
     * 使用指定字符分割字符串,并且分割结果去掉空字符串
     * 
     * @param pStr
     *            要分割的字符串
     * @param pSeparator
     *            用于分割的字符
     * @return 被分割后的不包括空字符的结果
     */
    private static ArrayList<String> splitNoEmpty(String pStr,char pSeparator){
        // 原代码参照于cc.commons.commons-util中的StringUtil包
        ArrayList<String> tSubStr=new ArrayList<>();
        if(pStr==null||pStr.isEmpty())
            return tSubStr;
        char[] tContent=pStr.toCharArray();
        int tLastIndex=-1,sIndex=0;
        for(;sIndex<tContent.length;sIndex++){
            if(tContent[sIndex]==pSeparator){
                if(sIndex>tLastIndex+1){
                    tSubStr.add(new String(tContent,tLastIndex+1,sIndex-tLastIndex-1));
                }
                tLastIndex=sIndex;
            }
        }
        if(tLastIndex<tContent.length-1){
            tSubStr.add(new String(tContent,tLastIndex+1,tContent.length-tLastIndex-1));
        }
        return tSubStr;
    }

    /**
     * 获取指定路径节点的父节点
     * 
     * @param pPath
     *            路径
     * @param pWarpKey
     *            用于存储最后一部分节点路径
     * @param pCreateNew
     *            如果节点不存在,是否创建
     * @return 获取的节点
     */
    private CommentedSection getParentSection(String pPath,WarpKey pWarpKey,boolean pCreateNew){
        if(pPath==null||pPath.isEmpty())
            return this;
        ArrayList<String> childPathes=splitNoEmpty(pPath,this.mRoot.options().pathSeparator());
        return this.getParentSection(childPathes,pWarpKey,pCreateNew);
    }

    /**
     * 获取指定路径节点的父节点
     * 
     * @param pPath
     *            路径
     * @param pWarpKey
     *            用于存储最后一部分节点路径
     * @param pCreateNew
     *            如果节点不存在,是否创建
     * @return 获取的节点
     */
    private CommentedSection getParentSection(List<String> pPath,WarpKey pWarpKey,boolean pCreateNew){
        if(pPath.isEmpty())
            return null;

        if(pWarpKey!=null){
            pWarpKey.mValue=pPath.get(pPath.size()-1);
        }
        if(pPath.size()==1)
            return this;

        CommentedSection section=this;
        int i=pPath.size()-1;
        for(String sChildPath : pPath){
            if(i--<=0){
                break;
            }
            Object tVal=section.getDirectChild(sChildPath);
            if(tVal==null||!(tVal instanceof CommentedSection)){
                if(pCreateNew){
                    tVal=new CommentedSection(section,sChildPath);
                    section.mChild.put(sChildPath,CommentedValue.wrapperValue(section,sChildPath,tVal));
                }else return null;
            }
            section=(CommentedSection)tVal;
        }
        return section;
    }

    /** 获取指定路径的值 */
    public Object get(String pPath){
        return this.get(pPath,null);
    }

    /**
     * 获取指定路径的值,如果指定路径不存在,则返回默认值
     * 
     * @param pPath
     *            路径
     * @param pDefValue
     *            默认值
     * @return 指定路径的值或默认值
     */
    public Object get(String pPath,Object pDefValue){
        CommentedValue tWarpValue=this.getCommentedValue(pPath);
        return (tWarpValue==null||tWarpValue.getValue()==null)?pDefValue:tWarpValue.getValue();
    }

    /**
     * 获取包装过的值
     * <p>
     * 包装值包括的值本身以及注释,使用{@link CommentedValue#getValue()}可以获取原值
     * </p>
     * 
     * @param pPath
     *            路径
     * @return 包装的值
     */
    public CommentedValue getCommentedValue(String pPath){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tSection=this.getParentSection(pPath,tWarpKey);
        return tSection==null?null:tSection.mChild.get(tWarpKey.mValue);
    }

    /**
     * 获取包装过的值
     * <p>
     * 包装值包括的值本身以及注释,使用{@link CommentedValue#getValue()}可以获取原值
     * </p>
     * 
     * @param pPath
     *            路径
     * @return 包装的值
     */
    public CommentedValue getCommentedValue(List<String> pPath){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tSection=this.getParentSection(pPath,tWarpKey);
        return tSection==null?null:tSection.mChild.get(tWarpKey.mValue);
    }

    /**
     * 移除包装过的值
     * <p>
     * 包装值包括的值本身以及注释,使用{@link CommentedValue#getValue()}可以获取原值
     * </p>
     * 
     * @param pPath
     *            路径
     * @return 包装的值
     */
    public CommentedValue removeCommentedValue(String pPath){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tSection=this.getParentSection(pPath,tWarpKey);
        return tSection==null?null:tSection.mChild.remove(tWarpKey.mValue);
    }

    /**
     * 设置指定路径的值
     * <p>
     * 将会对进行包装与转换<br>
     * 普通值直接使用{@link CommentedValue}进行包装<br>
     * {@link CommentedSection}值,先检查父节点是否匹配,如果不匹配,进行递归更改父节点
     * </p>
     * 
     * @param pPath
     *            路径
     * @param pValue
     *            值
     */
    public void set(String pPath,Object pValue,String...pComments){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tSection=this.getOrCreateParentSection(pPath,tWarpKey);

        if(pValue==null){
            tSection.mChild.remove(tWarpKey.mValue);
        }else{
            CommentedValue tWarpValue=tSection.mChild.get(tWarpKey.mValue);
            if(tWarpValue!=null){ // 保留注释
                tWarpValue.setValue(tSection,tWarpKey.mValue,pValue);
            }else{
                tWarpValue=CommentedValue.wrapperValue(tSection,tWarpKey.mValue,pValue);
                tSection.mChild.put(tWarpKey.mValue,tWarpValue);
            }

            if(pComments!=null&&pComments.length>0){
                tWarpValue.setComments(pComments);
            }
        }
    }

    /**
     * 移除指定路径下的值
     * 
     * @param pPath
     *            路径
     * @return 移除的值
     */
    public Object remove(String pPath){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tSection=this.getOrCreateParentSection(pPath,tWarpKey);
        CommentedValue tRemoved=tSection.mChild.remove(tWarpKey.mValue);
        return tRemoved==null?null:tRemoved.getValue();
    }

    /**
     * 在指定路径下创建一个新的节点
     * 
     * @param pPath
     *            路径
     * @return 创建的节点
     */
    public CommentedSection createSection(String pPath,String...pComments){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tSection=this.getOrCreateParentSection(pPath,tWarpKey);

        CommentedSection putSec=new CommentedSection(tSection,tWarpKey.mValue);
        CommentedValue tValue=CommentedValue.wrapperValue(tSection,tWarpKey.mValue,putSec);
        tValue.setComments(pComments);
        tSection.mChild.put(tWarpKey.mValue,tValue);
        return putSec;
    }

    /**
     * 在指定路径下创建一个新的节点,并存入值
     * 
     * @param pPath
     *            路径
     * @param pContents
     *            值
     * @return 创建的节点
     */
    public CommentedSection createSection(String pPath,Map<?,?> pContents){
        CommentedSection pSection=this.createSection(pPath);

        for(Map.Entry<?,?> entry : pContents.entrySet()){
            if(entry.getValue() instanceof Map){
                pSection.createSection(entry.getKey().toString(),(Map<?,?>)entry.getValue());
            }else{
                pSection.set(entry.getKey().toString(),entry.getValue());
            }
        }

        return pSection;
    }

    public String getString(String pPath){
        return getString(pPath,null);
    }

    public String getString(String pPath,String pDefValue){
        Object val=this.get(pPath,pDefValue);
        return (val!=null)?val.toString():pDefValue;
    }

    public boolean isString(String pPath){
        Object val=this.get(pPath);
        return val instanceof String;
    }

    public Byte getByte(String pPath){
        return getByte(pPath,(byte)0);
    }

    public Byte getByte(String pPath,Byte pDefValue){
        Object tVal=get(pPath,pDefValue);
        if(tVal==null)
            return pDefValue;
        if(tVal instanceof Number){
            return ((Number)tVal).byteValue();
        }

        try{
            return Byte.valueOf(String.valueOf(tVal));
        }catch(NumberFormatException ignore){
            return pDefValue;
        }
    }

    public Short getShort(String pPath){
        return getShort(pPath,(short)0);
    }

    public Short getShort(String pPath,Short pDefValue){
        Object tVal=get(pPath,pDefValue);
        if(tVal==null)
            return pDefValue;
        if(tVal instanceof Number){
            return ((Number)tVal).shortValue();
        }

        try{
            return Short.valueOf(String.valueOf(tVal));
        }catch(NumberFormatException ignore){
            return pDefValue;
        }
    }

    public Integer getInt(String pPath){
        return getInt(pPath,0);
    }

    public Integer getInt(String pPath,Integer pDefValue){
        Object tVal=get(pPath,pDefValue);
        if(tVal==null)
            return pDefValue;
        if(tVal instanceof Number){
            return ((Number)tVal).intValue();
        }

        try{
            return Integer.valueOf(String.valueOf(tVal));
        }catch(NumberFormatException ignore){
            return pDefValue;
        }
    }

    public boolean isInt(String pPath){
        Object val=get(pPath);
        return val instanceof Integer;
    }

    public Boolean getBoolean(String pPath){
        return getBoolean(pPath,false);
    }

    public Boolean getBoolean(String pPath,Boolean pDefValue){
        Object val=get(pPath,pDefValue);
        return (val instanceof Boolean)?(Boolean)val:pDefValue;
    }

    public boolean isBoolean(String pPath){
        Object val=get(pPath);
        return val instanceof Boolean;
    }

    public Float getFloat(String pPath){
        return getFloat(pPath,0F);
    }

    public Float getFloat(String pPath,Float pDefValue){
        Object tVal=get(pPath,pDefValue);
        if(tVal==null)
            return pDefValue;
        if(tVal instanceof Number){
            return ((Number)tVal).floatValue();
        }

        try{
            return Float.valueOf(String.valueOf(tVal));
        }catch(NumberFormatException ignore){
            return pDefValue;
        }
    }

    public Double getDouble(String pPath){
        return getDouble(pPath,0D);
    }

    public Double getDouble(String pPath,Double pDefValue){
        Object tVal=get(pPath,pDefValue);
        if(tVal==null)
            return pDefValue;
        if(tVal instanceof Number){
            return ((Number)tVal).doubleValue();
        }

        try{
            return Double.valueOf(String.valueOf(tVal));
        }catch(NumberFormatException ignore){
            return pDefValue;
        }
    }

    public boolean isDouble(String pPath){
        Object val=get(pPath);
        return val instanceof Double;
    }

    public Long getLong(String pPath){
        return getLong(pPath,0L);
    }

    public Long getLong(String pPath,Long pDefValue){
        Object tVal=get(pPath,pDefValue);
        if(tVal==null)
            return pDefValue;
        if(tVal instanceof Number){
            return ((Number)tVal).longValue();
        }

        try{
            return Long.valueOf(String.valueOf(tVal));
        }catch(NumberFormatException ignore){
            return pDefValue;
        }
    }

    public boolean isLong(String pPath){
        Object val=get(pPath);
        return val instanceof Long;
    }

    public List<?> getList(String pPath){
        return getList(pPath,null);
    }

    public List<?> getList(String pPath,List<?> pDefValue){
        Object val=get(pPath,pDefValue);
        return (List<?>)((val instanceof List)?val:pDefValue);
    }

    public List<Map<?,?>> getMapList(String pPath){
        List<?> list=getList(pPath);
        List<Map<?,?>> result=new ArrayList<>();

        if(list==null)
            return result;

        for(Object object : list){
            if(object instanceof Map){
                result.add((Map<?,?>)object);
            }
        }

        return result;
    }

    public boolean isList(String pPath){
        Object val=get(pPath);
        return val instanceof List;
    }

    public List<String> getStringList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<String> result=new ArrayList<>();
        for(Object object : list){
            if((object instanceof String)||(isPrimitiveWrapper(object))){
                result.add(String.valueOf(object));
            }
        }

        return result;
    }

    public List<Integer> getIntegerList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Integer> result=new ArrayList<>();
        for(Object object : list){
            if(object instanceof Integer){
                result.add((Integer)object);
            }else if(object instanceof String){
                try{
                    result.add(Integer.valueOf((String)object));
                }catch(Exception ex){
                    // ignore
                }
            }else if(object instanceof Character){
                result.add((int)((Character)object).charValue());
            }else if(object instanceof Number){
                result.add(((Number)object).intValue());
            }
        }

        return result;
    }

    public List<Boolean> getBooleanList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Boolean> result=new ArrayList<>();

        for(Object object : list){
            if(object instanceof Boolean){
                result.add((Boolean)object);
            }else if(object instanceof String){
                if(Boolean.TRUE.toString().equals(object)){
                    result.add(true);
                }else if(Boolean.FALSE.toString().equals(object)){
                    result.add(false);
                }
            }
        }

        return result;
    }

    public List<Double> getDoubleList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<Double>(0);

        List<Double> result=new ArrayList<>();
        for(Object object : list){
            if(object instanceof Double){
                result.add((Double)object);
            }else if(object instanceof String){
                try{
                    result.add(Double.valueOf((String)object));
                }catch(Exception ex){
                    // ignore
                }
            }else if(object instanceof Character){
                result.add((double)((Character)object).charValue());
            }else if(object instanceof Number){
                result.add(((Number)object).doubleValue());
            }
        }

        return result;
    }

    public List<Float> getFloatList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Float> result=new ArrayList<>();
        for(Object object : list){
            if(object instanceof Float){
                result.add((Float)object);
            }else if(object instanceof String){
                try{
                    result.add(Float.valueOf((String)object));
                }catch(Exception ex){
                    // ignore
                }
            }else if(object instanceof Character){
                result.add((float)((Character)object).charValue());
            }else if(object instanceof Number){
                result.add(((Number)object).floatValue());
            }
        }

        return result;
    }

    public List<Long> getLongList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Long> result=new ArrayList<>();
        for(Object object : list){
            if(object instanceof Long){
                result.add((Long)object);
            }else if(object instanceof String){
                try{
                    result.add(Long.valueOf((String)object));
                }catch(Exception ex){
                    // ignore
                }
            }else if(object instanceof Character){
                result.add((long)((Character)object).charValue());
            }else if(object instanceof Number){
                result.add(((Number)object).longValue());
            }
        }

        return result;
    }

    public List<Byte> getByteList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Byte> result=new ArrayList<>();
        for(Object object : list){
            if(object instanceof Byte){
                result.add((Byte)object);
            }else if(object instanceof String){
                try{
                    result.add(Byte.valueOf((String)object));
                }catch(Exception ex){
                    // ignore
                }
            }else if(object instanceof Character){
                result.add((byte)((Character)object).charValue());
            }else if(object instanceof Number){
                result.add(((Number)object).byteValue());
            }
        }

        return result;
    }

    public List<Character> getCharacterList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Character> result=new ArrayList<>();
        for(Object object : list){
            if(object instanceof Character){
                result.add((Character)object);
            }else if(object instanceof String){
                String str=(String)object;

                if(str.length()==1){
                    result.add(str.charAt(0));
                }
            }else if(object instanceof Number){
                result.add((char)((Number)object).intValue());
            }
        }

        return result;
    }

    public List<Short> getShortList(String pPath){
        List<?> list=getList(pPath);
        if(list==null)
            return new ArrayList<>(0);

        List<Short> result=new ArrayList<>();
        for(Object sObject : list){
            if(sObject instanceof Short){
                result.add((Short)sObject);
            }else if(sObject instanceof String){
                try{
                    result.add(Short.valueOf((String)sObject));
                }catch(Exception ex){
                    // ignore
                }
            }else if(sObject instanceof Character){
                result.add((short)((Character)sObject).charValue());
            }else if(sObject instanceof Number){
                result.add(((Number)sObject).shortValue());
            }
        }

        return result;
    }

    /**
     * 获取指定路径下的节点,如果该节点下的值不是{@link CommentedSection},也将返回null
     * 
     * @param pPath
     *            路径
     * @return 获取到的节点或null
     */
    public CommentedSection getSection(String pPath){
        Object tVal=get(pPath,null);
        return (tVal instanceof CommentedSection)?(CommentedSection)tVal:null;

    }

    /**
     * 获取指定路径下的节点,如果该节点下的值不存在或不是{@link CommentedSection},将创建一个新的节点
     * 
     * @param pPath
     *            路径
     * @return 获取到或创建的节点
     */
    public CommentedSection getOrCreateSection(String pPath,String...pDefComments){
        WarpKey tWarpKey=new WarpKey();
        return this.getOrCreateSection(this.getOrCreateParentSection(pPath,tWarpKey),tWarpKey,pDefComments);
    }

    /**
     * 获取指定路径下的节点,如果该节点下的值不存在或不是{@link CommentedSection},将创建一个新的节点
     * 
     * @param pPath
     *            路径
     * @return 获取到或创建的节点
     */
    public CommentedSection getOrCreateSection(List<String> pPath,String...pDefComments){
        WarpKey tWarpKey=new WarpKey();
        return this.getOrCreateSection(this.getOrCreateParentSection(pPath,tWarpKey),tWarpKey,pDefComments);
    }

    /**
     * 获取节点,如果节点不存在则创建
     * 
     * @param tParentSec
     *            父节点
     * @param pKey
     *            节点Key
     * @param pDefComments
     *            默认注释
     * @return 获取或创建的节点
     */
    protected CommentedSection getOrCreateSection(CommentedSection tParentSec,WarpKey pKey,String...pDefComments){
        if(tParentSec==null)
            return null;

        CommentedValue tWarpValue=tParentSec.mChild.get(pKey.mValue);
        if(tWarpValue==null||!(tWarpValue.getValue() instanceof CommentedSection)){
            tWarpValue=CommentedValue.wrapperValue(tParentSec,pKey.mValue,new CommentedSection(tParentSec,pKey.mValue));
            tParentSec.mChild.put(pKey.mValue,tWarpValue);
        }
        tWarpValue.addDefaultComments(pDefComments);
        return (CommentedSection)tWarpValue.getValue();
    }

    /** 指定路径下的值是不是一个节点 */
    public boolean isSection(String pPath){
        Object tVal=get(pPath);
        return tVal instanceof CommentedSection;
    }

    /** 当前值是不是一个等价字符串 */
    public static boolean isPrimitiveWrapper(Object pValue){
        return pValue instanceof Integer
                ||pValue instanceof Boolean
                ||pValue instanceof Character
                ||pValue instanceof Byte
                ||pValue instanceof Short
                ||pValue instanceof Double
                ||pValue instanceof Long
                ||pValue instanceof Float;
    }

    /**
     * 为指定路径添加默认值
     * 
     * @param pPath
     *            路径
     * @param pValue
     *            默认值
     * @param pComments
     *            默认注释
     */
    public void addDefault(String pPath,Object pValue,String...pComments){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tParentSection=this.getOrCreateParentSection(pPath,tWarpKey);

        CommentedValue tValue=tParentSection.mChild.get(tWarpKey.mValue);
        if(tValue==null||tValue.getValue()==null){
            CommentedValue tNewValue=CommentedValue.wrapperValue(tParentSection,tWarpKey.mValue,pValue);
            tParentSection.mChild.put(tWarpKey.mValue,tNewValue);
            if(tValue!=null&&tValue.hasComments()){
                tNewValue.setComments(tValue.getComments());
            }
            tValue=tNewValue;
        }

        tValue.addDefaultComments(pComments);
    }

    /**
     * 获取该节点下所有值对应的key
     * 
     * @param pKeys
     *            用于存储获取的key
     * @param pParentPath
     *            所有父节点的构成的路径
     * @param pDeep
     *            是否获取更低层的节点的key
     */
    protected void getChildKeys(Set<String> pKeys,String pParentPath,boolean pDeep){
        String formatPath=(pParentPath==null||pParentPath.isEmpty())?"":pParentPath+this.mRoot.options().pathSeparator();

        for(Map.Entry<String,CommentedValue> sEntry : this.mChild.entrySet()){
            pKeys.add(formatPath+sEntry.getKey());
            if(pDeep&&(sEntry.getValue().getValue() instanceof CommentedSection)){
                CommentedSection subsection=(CommentedSection)sEntry.getValue().getValue();
                subsection.getChildKeys(pKeys,formatPath+sEntry.getKey(),pDeep);
            }
        }
    }

    /**
     * 获取该节点下所有key与值
     * 
     * @param pValues
     *            用于存储获取的key与值
     * @param pDeep
     *            是否获取更低层的节点的key与值
     */
    protected void getChildValues(Map<String,Object> pValues,boolean pDeep){
        for(Map.Entry<String,CommentedValue> sEntry : this.mChild.entrySet()){
            Object tValue=sEntry.getValue().getValue();
            if(tValue instanceof CommentedSection&&pDeep){
                LinkedHashMap<String,Object> tMap=new LinkedHashMap<>();
                pValues.put(sEntry.getKey(),tMap);
                ((CommentedSection)tValue).getChildValues(tMap,pDeep);
            }else pValues.put(sEntry.getKey(),tValue);
        }
    }

    /**
     * 获取该节点下的值,非deep,Map不可编辑
     */
    public Map<String,CommentedValue> values(){
        return Collections.unmodifiableMap(this.mChild);
    }

    @Override
    public String toString(){
        CommentedYamlConfig root=this.getRoot();
        StringBuilder builder=new StringBuilder("{path='"+getCurrentPath()+"'");
        builder.append(", root='").append(root==null?null:root.getClass().getSimpleName());
        builder.append(", MapValue=").append(this.mChild).append("}");
        return builder.toString();
    }

    /**
     * 为指定路径下的节点添加注释
     * <p>
     * 如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位
     * </p>
     */
    public void addComments(String pPath,String...pComments){
        WarpKey tWarpKey=new WarpKey();
        CommentedSection tParentSection=this.getOrCreateParentSection(pPath,tWarpKey);

        CommentedValue tWarpValue=tParentSection.mChild.get(tWarpKey.mValue);
        if(tWarpValue==null){
            tWarpValue=CommentedValue.wrapperValue(tParentSection,tWarpKey.mValue,null);
            tParentSection.mChild.put(tWarpKey.mValue,tWarpValue);
        }
        tWarpValue.addComments(pComments);
    }

    /**
     * 为指定路径下的节点添加默认注释
     * <p>
     * 如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位<br>
     * 如果该节点已经有注释,不做任何操作
     * </p>
     */
    public void addDefaultComments(String pPath,String...pComments){
        CommentedValue tValue=this.getCommentedValue(pPath);
        if(tValue!=null)
            tValue.addDefaultComments(pComments);
        else this.setComments(pPath,pComments);
    }

    /**
     * 设置指定路径下节点的注释
     * <p>
     * 如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位
     * </p>
     */
    public void setComments(String pPath,String...pComments){
        Collection<String> tComments=new ArrayList<>();
        for(String sComment : pComments){
            tComments.add(sComment);
        }
        this.setComments(pPath,tComments);
    }

    /**
     * 设置指定路径下节点的注释
     * <p>
     * 如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位
     * </p>
     */
    public void setComments(String pPath,Collection<String> pComments){
        WarpKey tKey=new WarpKey();
        this.setComment(this.getOrCreateParentSection(pPath,tKey),tKey,pComments);
    }

    /**
     * 为节点设置注释
     * <p>
     * 如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位
     * </p>
     * 
     * @param pPath
     *            分割的路径集合
     * @param pComments
     *            注释
     */
    public void setComments(List<String> pPath,Collection<String> pComments){
        WarpKey tKey=new WarpKey();
        this.setComment(this.getOrCreateParentSection(pPath,tKey),tKey,pComments);
    }

    /**
     * 为节点设置注释
     * <p>
     * 如果此路径上存在其他值,注释将不会被设置<br>
     * 其次,如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位
     * </p>
     * 
     * @param pPath
     *            分割的路径集合
     * @param pComments
     *            注释
     * @return 是否设置了注释
     */
    public boolean setCommentsNoReplace(List<String> pPath,Collection<String> pComments){
        if(pPath.isEmpty()||pComments.isEmpty())
            return true;

        CommentedValue tValue=null;
        CommentedSection tSection=this;
        Iterator<String> sIt=pPath.iterator();
        while(true){
            String tKey=sIt.next();
            tValue=tSection.mChild.get(tKey);
            if(tValue==null){
                if(sIt.hasNext()){
                    tValue=CommentedValue.wrapperValue(tSection,tKey,new CommentedSection(tSection,tKey));
                }else{
                    tValue=CommentedValue.wrapperValue(tSection,tKey,null);
                    break;
                }
            }else{
                if(sIt.hasNext()&&!(tValue.getValue() instanceof CommentedSection)){
                    return false;
                }
            }

            if(!sIt.hasNext())
                break;
            tSection=(CommentedSection)tValue.getValue();
        }
        tValue.setComments(pComments);
        return true;
    }

    /**
     * 为节点设置注释
     * <p>
     * 如果节点不存在,将创建一个{@link CommentedValue}值为null的包装值用于占位
     * </p>
     * 
     * @param pParentSec
     *            父节点
     * @param pKey
     *            节点
     * @param pComments
     *            注释
     */
    protected void setComment(CommentedSection pParentSec,WarpKey pKey,Collection<String> pComments){
        if(pParentSec==null)
            return;

        CommentedValue tValue=pParentSec.mChild.get(pKey.mValue);
        if(tValue!=null)
            tValue.setComments(pComments);
        else if(pComments!=null&&!pComments.isEmpty()){
            tValue=CommentedValue.wrapperValue(pParentSec,pKey.mValue,null);
            pParentSec.mChild.put(pKey.mValue,tValue);
            tValue.setComments(pComments);
        }
    }

    /**
     * 获取注释
     * 
     * @param pPath
     *            路径
     * @return 注释,非null
     */
    public ArrayList<String> getComments(String pPath){
        CommentedValue tValue=this.getCommentedValue(pPath);
        if(tValue!=null)
            return tValue.getComments();
        else return new ArrayList<>();
    }

    /**
     * 获取节点设置注释
     * 
     * @param pPath
     *            分割的路径集合
     * @return 注释,如果Path正确,Comment不会为null
     */
    public ArrayList<String> getComments(List<String> pPath){
        if(pPath.isEmpty())
            return null;

        CommentedValue tValue=null;
        CommentedSection tSection=this;
        Iterator<String> sIt=pPath.iterator();
        while(true){
            tValue=tSection.mChild.get(sIt.next());
            if(tValue==null||(sIt.hasNext()&&!(tValue.getValue() instanceof CommentedSection)))
                return new ArrayList<>(0);

            if(!sIt.hasNext())
                break;
            tSection=(CommentedSection)tValue.getValue();
        }
        return tValue.getComments();
    }

    /**
     * 检查是否有注释
     * 
     * @param pPath
     *            路径
     * @return 是否有注释
     */
    public boolean hasComments(String pPath){
        CommentedValue tValue=this.getCommentedValue(pPath);
        if(tValue!=null)
            return tValue.hasComments();
        else return false;
    }

    /**
     * 清空此路径上的注释
     * <p>
     * 如果此路径上的注释值为占位符,将同时移除此占位符
     * </p>
     * 
     * @param pPath
     *            路径
     * @return 被清理的注释,非null
     */
    public ArrayList<String> clearComments(String pPath){
        WarpKey tKey=new WarpKey();
        CommentedSection tParentSec=this.getOrCreateParentSection(pPath,tKey);
        if(tParentSec!=null){
            CommentedValue tValue=tParentSec.mChild.get(tKey.mValue);
            if(tValue!=null){
                if(tValue.getValue()==null){
                    tParentSec.mChild.remove(tKey.mValue);
                }
                return tValue.clearComments();
            }
        }
        return new ArrayList<>();
    }

    /**
     * 清空该配置节点下的所有数据
     * 
     * @return 被清理的数据
     */
    public Map<String,CommentedValue> clear(){
        HashMap<String,CommentedValue> tOldData=new HashMap<>(this.mChild);
        this.mChild.clear();
        return tOldData;
    }

    /**
     * 该配置节点下是否有数据
     * 
     * @return 是否
     */
    public boolean isEmpty(){
        return this.mChild.isEmpty();
    }

    @Deprecated
    public Map<String,CommentedValue> getChildDirect(){
        return this.mChild;
    }

    static class WarpKey{

        String mValue=null;
    }

    /**
     * 序列化指定的数据到指定的类型,如果类型不存在你就会狗带
     * <p>
     * 保存数据过程中的任何错误都会被记录到控制台然后忽视
     * </p>
     *
     * @param pObj
     *            指定的对象
     * @param pClass
     *            指定的类型
     * @return CommentedSection
     */
    public <T extends SerializableYamlObject> CommentedSection loadObject(T pObj,Class<T> pClass) throws YAMLException{
        SerializableYamlUtils.loadObject(this,pObj,pClass);
        return this;
    }

    public Object getDirectChild(String pKey) {
        CommentedValue tVal = this.mChild.get(pKey);
        return tVal == null ? null : tVal.getValue();
    }

}
