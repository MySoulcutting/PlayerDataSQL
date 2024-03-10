package cc.commons.commentedyaml;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Map;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.GenericProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import cc.commons.commentedyaml.serialize.convert.DataConvert;

public class CommentedConstructor extends Constructor{

    /** GenericProperty的genType值域,用于数据类型转换 */
    protected static Field field_GenericProperty_genType;

    static{
        try{
            field_GenericProperty_genType=GenericProperty.class.getDeclaredField("genType");
            field_GenericProperty_genType.setAccessible(true);
        }catch(NoSuchFieldException|SecurityException exp){
            throw new IllegalStateException(exp.getLocalizedMessage(),exp);
        }

    }

    /** 当前被使用的数据转换类实例 */
    protected LinkedHashSet<DataConvert> mDataConvert=new LinkedHashSet<>();

    public CommentedConstructor(){
        this.yamlConstructors.put(Tag.MAP,new ConstructCustomObject());
    }

    /**
     * 获取正在使用的数据转换类实例
     * <p>
     * 数据转换类实例的顺序十分重要<br>
     * key为null为默认的数据转换类实例
     * </p>
     * 
     * @return 数据转换类实例
     */
    public LinkedHashSet<DataConvert> getDataConvert(){
        return this.mDataConvert;
    }

    public class ConstructCustomObject extends ConstructYamlMap{

        @Override
        public Object construct(Node pNode){
            if(pNode.isTwoStepsConstruction()){
                throw new YAMLException("Unexpected referential mapping structure. Node: "+pNode);
            }

            Map<?,?> tRawData=(Map<?,?>)super.construct(pNode);
            Object tObj=tRawData.get(CommentedRepresenter.SerializableMark);
            if(tObj==null){
                return tRawData;
            }

            try{
                String tTypeStr=String.valueOf(tObj);
                Class<?> tClazz=Class.forName(tTypeStr);
                java.lang.reflect.Constructor<?> tConstruct=tClazz.getDeclaredConstructor();
                tConstruct.setAccessible(true);
                tObj=tConstruct.newInstance();
                for(Property sProp : CommentedConstructor.this.getPropertyUtils().getProperties(tClazz)){
                    if(!sProp.isWritable()||!tRawData.containsKey(sProp.getName()))
                        continue;

                    Object tValue=DataConvert.convertData(
                            tRawData.get(sProp.getName()),
                            sProp.getType(),
                            (Type)field_GenericProperty_genType.get(sProp));
                    sProp.set(tObj,tValue);
                }

                return tObj;
            }catch(Throwable exp){
                throw new YAMLException("Could not deserialize object",exp);
            }

        }

        @Override
        public void construct2ndStep(Node node,Object object){
            throw new YAMLException("Unexpected referential mapping structure. Node: "+node);
        }
    }

}
