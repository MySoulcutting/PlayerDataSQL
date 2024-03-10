package cc.commons.commentedyaml.serialize.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.error.YAMLException;

import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedValue;
import cc.commons.commentedyaml.serialize.SerializableYamlObject;
import cc.commons.commentedyaml.serialize.annotation.Tag;
import cc.commons.commentedyaml.serialize.annotation.TagGenerics;


/**
 * Created by xjboss on 2017/5/30.
 */
public class SerializableYamlUtils {
    /*
    public interface ObjectHandler{
        void handle(Object pObj,Object pValue) throws YAMLException;
    }
    public static class InstanceHandler{
        public final Class CLASS;
        public final ObjectHandler oh;
        public InstanceHandler(Class pC,ObjectHandler pOh){
            this.CLASS=pC;
            this.oh=pOh;
        }
    }
    public static <T extends SerializableYamlObject> T toYaml(String pString,Class<T> pClass) throws YAMLException{
        try {
            return toYaml((Map<?, ?>) new Yaml().load(pString),pClass,pString);
        }catch(Exception e){
            throw new YAMLException("你在干什么？",e);
        }
    }
    private static ConcurrentHashMap<Class,ObjectHandler> ToYamlSuperClassMap =new ConcurrentHashMap();
    private static ConcurrentHashMap<Class,ObjectHandler> ToYamlClassMap =new ConcurrentHashMap();
    private static ArrayList<InstanceHandler> InstanceHandler=new ArrayList<>();
    private static void doToYaml(Object pObj,Object pValue){
        Class oClass=pObj.getClass();
        Class vClass=pValue.getClass();
        ObjectHandler OH;
        if((OH=ToYamlClassMap.get(oClass))!=null||((OH=ToYamlSuperClassMap.get(oClass.getSuperclass()))!=null)){
            OH.handle(pObj,pValue);
        }else{
            for(InstanceHandler ih:InstanceHandler){
                if(ih.CLASS.isInstance(oClass)){
                    ih.oh.handle(pObj,pValue);
                    break;
                }
            }
        }
    }
    static{
        RegisterToYamlInstanceClassHandler(Map.class,(pObj, pValue)->{
            try {
                if (pValue == null) return;
                Class po = pObj.getClass();
                Type[] ts = po.getGenericInterfaces();
                Type K = ts[0];
                Type V = ts[1];
                Class po2 = pValue.getClass();
                Type[] ts2 = po2.getGenericInterfaces();
                Type K2 = ts2[0];
                Type V2 = ts2[1];
                final Map<Object, Object> pmo = (Map<Object, Object>) pObj;
                final Map<Object, Object> pmv = (Map<Object, Object>) pValue;
                Constructor ctorK;
                Constructor ctorK2;
                if (K instanceof Number) {
                    ctorK = K.getClass().getConstructor(String.class);
                } else {
                    ctorK = null;
                }
                if (K2 instanceof Number) {
                    ctorK2 = K2.getClass().getConstructor(String.class);
                } else {
                    ctorK2 = null;
                }
                final boolean VYML;
                final boolean VMAP;
                final boolean VCollection;
                final boolean VArray;
                final boolean VOther;
                if(V2 instanceof Map){
                    if(V instanceof Serializable){
                        VYML=true;
                        VMAP=false;
                        VOther=false;
                    }else{
                        VMAP=true;
                        VYML=false;
                        VOther=true;
                    }
                    VCollection=false;
                    VArray=false;
                }else{
                    VMAP=false;
                    VYML=false;
                    if(V2 instanceof Collection){
                        if(V instanceof Collection){
                            VCollection=true;
                            VArray=false;
                        }else if(V.getClass().isArray()){
                            VArray=true;
                            VCollection=false;
                        }else{
                            VArray=false;
                            VCollection=false;
                        }
                        VOther=true;
                    }else{
                        VCollection=false;
                        VArray=false;
                        VOther=!(V instanceof Number||V instanceof CharSequence);
                    }
                }

                pmv.forEach((k, v) -> {
                    try {
                        if (VYML) {
                            pmo.put(k, toYaml((Map<?, ?>) v, (Class<Serializable>) V.getClass()));
                        } else if (VOther) {
                            Object nO;
                            if(VArray){
                                nO = Array.newInstance(V.getClass().getComponentType(),((Collection)v).size());
                                doToYaml(nO,v);
                            }else{
                                nO = V.getClass().newInstance();
                                doToYaml(nO, v);
                            }
                            pmo.put(k, nO);
                        }else{
                            pmo.put(k,v);
                        }
                    }catch(Exception e){

                    }
                });
            }catch(Exception e){
                throw new YAMLException("Map Handle Error",e);
            }
        });
        RegisterToYamlInstanceClassHandler(Array.class,(pObj, pValue) -> {
            try {
                final boolean VYML;
                final boolean VOTHER;
                final boolean VNORMAL;
                Class<?> VT = pObj.getClass().getComponentType();
                Object no = VT.newInstance();
                Boolean b;
                ((Collection) pValue).forEach(v -> {

                });
            }catch(Exception e){

            }
        });
    }
    public static void RegisterToYamlObjectClassHandler(Class pClass,ObjectHandler pObjH){
        ToYamlClassMap.put(pClass,pObjH);
    }
    public static void RegisterToYamlObjectSuperClassHandler(Class pClass,ObjectHandler pObjH){
        ToYamlSuperClassMap.put(pClass,pObjH);
    }
    public static void RegisterToYamlInstanceClassHandler(Class pClass, ObjectHandler pObjH){
        ToYamlSuperClassMap.put(pClass,pObjH);
    }
    private static <T extends Serializable> T toYaml(Map<?,?> pMap,Class<T> pClass) throws YAMLException{
        try {
            T obj = pClass.newInstance();
            Field[] pFields = pClass.getFields();
            ConcurrentHashMap<String, Field> map = new ConcurrentHashMap<>();
            for (Field f : pFields) {
                int mod = f.getModifiers();
                if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
                    continue;
                }
                Tag t = f.getAnnotation(Tag.class);
                if (t != null && !t.name().isEmpty()) {
                    map.put(t.name(), f);
                } else {
                    map.put(f.getName(), f);
                }
            }
            Type[] TP = pMap.getClass().getGenericInterfaces();
            Type TA = TP[0];
            Type TB = TP[1];
            boolean isN = TA instanceof Number;
            if (isN) throw new Exception("数字？是不可能的");
            pMap.forEach((a, b) -> {
                Field field;
                if ((field = map.get(a)) != null) {
                    try {
                        Class C = field.getClass();
                        C.getSuperclass();
                        field.set(obj, b);
                    } catch (Exception e) {
                        throw new YAMLException("对象错误？", e);
                    }
                }
            });
            return obj;
        }catch(Exception e){
            throw new YAMLException("格式错误？",e);
        }
    }
    private static <T extends SerializableYamlObject> T toYaml(Map<?,?> pMap,Class<T> pClass,String pString) throws YAMLException{
            T obj=toYaml(pMap,pClass);
            Composer.collectCommentFromString(obj,pString);
            return obj;
    }
/*
    /**
     * 反序列化指定的数据到指定的类型,如果类型不存在你就会狗带
     * <p>
     * 保存数据过程中的任何错误都会被记录到控制台然后忽视
     * </p>
     *
     * @param pSection
     *            指定的节点
     * @param pObj
     *            指定的对象
     * @param pClass
     *            指定的类型
     * @return T
     */
    public static <T extends SerializableYamlObject> T saveToObject(CommentedSection pSection, T pObj, Class<T> pClass) throws YAMLException {
        return saveToObject(pSection.values(), pObj, pClass);
    }
    private static  <T> T saveToObject(Map<?,?> pInput, T pObj, Class<?> pClass) throws YAMLException {
        try {
            SerializableYamlObject obj=(SerializableYamlObject)pClass.newInstance();
            Field[] fields = pClass.getDeclaredFields();
            HashSet<Integer> founded = new HashSet<>();
            HashSet<Integer> all = new HashSet<>();
            for (Map.Entry<String, Object> me:((Map<String, Object>)(pInput)).entrySet()) {
                String k = me.getKey();
                Object v=me.getValue();
                Object vv=v instanceof CommentedValue?((CommentedValue)(v)).getValue():v;
                if(vv==null){
                    continue;
                }
                for (int i = 0; i < fields.length; i++) {
                    if (founded.contains(i)) continue;
                    all.add(i);
                    Field f = fields[i];
                    int mod=f.getModifiers();
                    if(Modifier.isFinal(mod)||Modifier.isStatic(mod)||Modifier.isTransient(mod)){
                        founded.add(i);
                        continue;
                    }
                    Tag tag=f.getAnnotation(Tag.class);
                    TagGenerics TG=f.getAnnotation(TagGenerics.class);
                    String tagname=tag!=null&&!tag.name().isEmpty()?tag.name():f.getName();
                    f.setAccessible(true);
                    Object fo=f.get(obj);
                    ArrayList<String> comments=v instanceof CommentedValue?((CommentedValue)(v)).getComments():null;
                    if (tagname.equals(k)) {
                        if(comments!=null) ((SerializableYamlObject)(obj)).getComments().put(k,comments);
                        founded.add(i);
                        if(vv instanceof CommentedSection){
                            if(fo instanceof Map) {
                                ((Map) fo).clear();
                                Map mv = ((CommentedSection) vv).values();
                                ((Map) fo).putAll(ConvertMapObject( k, TG,f.getType(), (Map<String, Object>) mv, obj));
                            }else if(fo instanceof SerializableYamlObject){
                                fo=saveToObject(((CommentedSection) vv).values(),fo,f.getType());
                            }
                        }else if(vv instanceof Map){
                            if(fo instanceof Map) {
                                ((Map) fo).putAll(ConvertMapObject(k, TG,f.getType(), (Map<String, Object>) vv, obj));
                            }else if(fo instanceof SerializableYamlObject){
                                fo=saveToObject((Map<?, ?>) vv,fo,f.getType());
                            }
                        }else if(vv instanceof Collection &&fo instanceof Collection) {
                            ((Collection) fo).clear();
                            ((Collection) fo).addAll(ConvertCollectionObject(f.getType(),TG, (Collection<Object>) vv));
                            break;
                        }else if(vv.getClass()!=fo.getClass()){
                            if((vv instanceof Number&&fo instanceof Number)) {

                            }else{
                                throw new YAMLException(String.format("节点%s类型不对", k));
                            }
                        }else{
                            fo=vv;
                        }
                        f.set(obj,fo);
                        break;
                    }
                }
            }
            if (pObj==null)return (T)obj;
            all.removeAll(founded);
            for(int i:all){
                Field f=fields[i];
                f.setAccessible(true);
                f.set(obj,f.get(pObj));
            }
            return (T)obj;
        }catch (Exception e){
            throw new YAMLException("你这个类有毒",e);
        }
    }
    private static <T,M> Map ConvertMapObject(String pMapName, TagGenerics pTagG, Class<M> pMapClass, Map<String,Object> pNmap, T pT){
        try {
            Class[] Types;
            Types=pTagG==null?null:pTagG.T();
            Class K=Object.class;
            Class V=Object.class;
            if(Types!=null){
                K=Types[0];
                V=Types[1];
            }
            Map newMap = (Map)pMapClass.newInstance();
            final Constructor Cons;
            if (K!=null&&Number.class.isAssignableFrom(K)) {
                Cons= K.getClass().getConstructor(String.class);
            }else{
                Cons=null;
            }
            for (Map.Entry<String, Object> me:pNmap.entrySet()) {
                final String key=me.getKey();
                String tag=null;
                final Object v=me.getValue();
                Object vv;
                if(v instanceof CommentedValue){
                    tag = pMapName + "." + key;
                    vv=((CommentedValue)v).getValue();
                    ((SerializableYamlObject)(pT)).getComments().put(tag, ((CommentedValue)(v)).getComments());
                    if(vv instanceof CommentedSection) {
                        Map mv = ((CommentedSection) (((CommentedValue) v).getValue())).values();
                        if (V!=null&&SerializableYamlObject.class.isAssignableFrom(V)) {
                            vv = saveToObject(mv,null,V);
                        } else {
                            vv = ConvertMapObject(tag, null,((CommentedSection) vv).values().getClass(), (Map<String, Object>) (mv), pT);
                        }
                    }
                }else if(v instanceof CommentedSection){
                    Map mv = ((CommentedSection) v).values();
                    if (SerializableYamlObject.class.isAssignableFrom(V)) {
                        vv = saveToObject(mv,null,V.getClass());
                    } else {
                        vv = ConvertMapObject(tag, null,V.getClass(), (Map<String, Object>) (mv), pT);
                    }
                }else{
                    vv=v;
                }
                if (Map.class.isAssignableFrom(V)) {
                    if(Cons==null) {
                        newMap.put(key, ConvertMapObject(tag,null,V.getClass(),pNmap,pT));
                    }else{
                        newMap.put(Cons.newInstance(key),ConvertMapObject(tag,null,V.getClass(),pNmap,pT));
                    }
                }else if(V!=null&&Collection.class.isAssignableFrom(V)) {
                    if (Cons == null) {
                        newMap.put(key, ConvertCollectionObject(V.getClass(),null, (Collection<Object>) v));
                    } else {
                        newMap.put(Cons.newInstance(key), ConvertCollectionObject(V.getClass(),null, (Collection<Object>) v));
                    }
                } else {
                    if(Cons==null) {
                        newMap.put(key, vv);
                    }else{
                        newMap.put(Cons.newInstance(key),vv);
                    }
                }
            }
            return newMap;
        }catch(Exception e){
            try {
                e.printStackTrace();
                return (Map)pMapClass.newInstance();
            }catch(Exception ee){throw new YAMLException("MAP CONVERT ERROR",ee);}
        }
    }
    private static Collection ConvertCollectionObject(Class pCollectionClass, TagGenerics pTagG, Collection<Object> pCollection){
        try{
            Class[] Types;
            Types=pTagG==null?null:pTagG.T();
            Collection newCollection=(Collection)pCollectionClass.newInstance();
            boolean VMap=false;
            boolean VCollection=false;
            boolean VYML=false;
            if(Types!=null) {
                if(Map.class.isAssignableFrom(Types[0])){
                    VMap = true;
                } else if (Collection.class.isAssignableFrom(Types[0])) {
                    VCollection = true;
                } else if (SerializableYamlObject.class.isAssignableFrom(Types[0])) {
                    VYML = true;
                }
            }
            for (Object no:pCollection){
                Class TT=Types==null?no.getClass():(Class<? extends Object>)Types[0];
                if(VMap){
                    no=ConvertMapObject(null,null,TT,(Map<String, Object>) no,null);
                }else if(VCollection){
                    no=ConvertCollectionObject(TT,pTagG,(Collection<Object>) no);
                }else if(VYML){
                    no=saveToObject((Map<?,?>)no,null,TT);
                }
                newCollection.add(no);
            }
            return newCollection;
        }catch (Exception e){
            try{
                return (Collection)pCollectionClass.newInstance();
            }catch (Exception ee){return null;}
        }
    }
    public static  <T extends SerializableYamlObject> void loadObject(CommentedSection pConfig, T pObj, Class<T> pClass) throws YAMLException{
        pConfig.clear();
        try{
            Field fs[]=pClass.getDeclaredFields();
            boolean empty=pObj.getComments().isEmpty();
            for(Field f : fs){
                f.setAccessible(true);
                Object o=f.get(pObj);
                String[] comments=null;
                ArrayList<String> commentsList;
                int mod=f.getModifiers();
                if(Modifier.isFinal(mod)||Modifier.isStatic(mod)||Modifier.isTransient(mod)){
                    continue;
                }
                String tag;
                Tag oc=f.getAnnotation(Tag.class);
                tag=((oc==null)?f.getName():(oc.name().isEmpty()?f.getName():oc.name()));
                if(((empty)||((commentsList=pObj.getComments().get(f.getName()))==null))){
                    comments=oc!=null?oc.comments().clone():comments;
                }else{
                    comments=new String[commentsList.size()];
                    comments=commentsList.toArray(comments);
                }
                if(o instanceof SerializableYamlObject){
                    pConfig.getOrCreateSection(tag,comments).loadObject((SerializableYamlObject)o,(Class<SerializableYamlObject>)o.getClass());
                }else if(o instanceof Map){
                    ArrayList<String> tags=new ArrayList<>();
                    tags.add(tag);
                    loadMap(pConfig.createSection(tag,comments),(Map<?,?>)o,o.getClass(),tags,pObj);
                }else if(o instanceof Collection){
                    //pConfig.getOrCreateSection(tag,comments);
                    ArrayList<Object> pConfList=new ArrayList<>();
                    loadList(pConfList,(Collection)o,pObj);
                    pConfig.set(tag,pConfList,comments);
                }else{
                    pConfig.set(tag,o,comments);
                }
            }
        }catch(Exception e){
            throw new YAMLException("你的类有毛病",e);
        }
    }
    private static <T extends SerializableYamlObject> void loadMap(CommentedSection pConfig,Map<?,?> pMap,Class pMapClass,ArrayList<String> pTag,T pYml){
        try {
            Type[] GT = pMapClass.getClass().getGenericInterfaces();
            Type K = GT[0];
            Type V = GT[1];
            boolean N=K instanceof Number;
            ArrayList<String> nt=(ArrayList<String>) pTag.clone();
            nt.add("");
            for (Map.Entry<Object, Object> e : ((Map<Object, Object>) pMap).entrySet()) {
                nt.set(nt.size()-1,N?String.valueOf(e.getKey()):e.getKey().toString());
                Object value=e.getValue();
                ArrayList<String> commits=pYml.get(nt);
                String[] commits2=commits==null?null:commits.toArray(new String[commits.size()]);
                if(value instanceof Map){
                    loadMap(pConfig.createSection(N?String.valueOf(e.getKey()):(String) e.getKey(),commits2),(Map<?,?>)value,V.getClass(),nt,pYml);
                }else if(value instanceof Collection){
                    ArrayList<Object> listObj=new ArrayList<>();
                    loadList(listObj,(Collection) value,pYml);
                    pConfig.set(N?String.valueOf(e.getKey()):(String) e.getKey(),listObj,commits2);
                }else if(value instanceof SerializableYamlObject){
                    loadObject(pConfig.createSection(N?String.valueOf(e.getKey()):(String)e.getKey(),commits2),(SerializableYamlObject)value,(Class<SerializableYamlObject>)value.getClass());
                    //pConfig.createSection(N?String.valueOf(e.getKey()):(String)e.getKey(),commits.toArray(new String[commits.size()]));
                }else{
                    pConfig.set(N?String.valueOf(e.getKey()):(String) e.getKey(),e.getValue());
                }

            }
        }catch (Exception e){
            throw new YAMLException("无法加载map",e);
        }
    }
    private static <T extends SerializableYamlObject> void loadList(List pConfig,Collection pList,T pYml){
        //Type[] GT = pList.getClass().getGenericInterfaces();
        //Type V = GT[0];
        for(Object value:pList){
            if(value instanceof Map||value instanceof SerializableYamlObject){
                pConfig.add(toMap(value));
            }else if(value instanceof Collection){
                ArrayList<Object> objList=new ArrayList<Object>();
                loadList(objList,(Collection)value,pYml);
                pConfig.add(objList);
            }else{
                pConfig.add(value);
            }
        }
    }
    private static <T extends SerializableYamlObject> Map<?,?> toMap(Object pV){
        HashMap<Object,Object> newMap=new HashMap<>();
        if(pV instanceof Map){
            Map<Object,Object> npv=(Map<Object,Object>)pV;
            Type[] GT = pV.getClass().getGenericInterfaces();
            Type K = GT[0];
            Type V = GT[1];
            if(K instanceof Number||K instanceof CharSequence){
                if(V instanceof SerializableYamlObject){
                    npv.forEach((k,v)-> newMap.put(k,toMap(v)));
                }else{
                    return npv;
                }
            }else{
                return Collections.EMPTY_MAP;
            }
        }else if(pV instanceof SerializableYamlObject){
            try{
                Field fs[]=pV.getClass().getDeclaredFields();
                for(Field f : fs){
                    f.setAccessible(true);
                    Object o=f.get(pV);
                    int mod=f.getModifiers();
                    if(Modifier.isFinal(mod)||Modifier.isStatic(mod)||Modifier.isTransient(mod)){
                        break;
                    }
                    String tag;
                    Tag oc=f.getAnnotation(Tag.class);
                    tag=((oc==null)?f.getName():(oc.name().isEmpty()?f.getName():oc.name()));
                    if(o instanceof SerializableYamlObject||o instanceof Map){
                        newMap.put(tag,toMap(o));
                    } else{
                        newMap.put(tag,o);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                throw new YAMLException("你的类有毛病");
            }
        }
        return newMap;
    }

}
