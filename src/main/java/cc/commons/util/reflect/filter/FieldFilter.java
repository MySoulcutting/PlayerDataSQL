package cc.commons.util.reflect.filter;

import java.lang.reflect.Field;
import java.util.ArrayList;

import cc.commons.util.CollUtil;
import cc.commons.util.tools.CacheGettor;

public class FieldFilter extends BaseFilter<Field,FieldFilter>{

    /** 值域的类型 */
    public Class<?> mType=null;
    /** 值域可能的类型,必须是其中一个 */
    public final CacheGettor<ArrayList<Class<?>>> mPossType=CacheGettor.create(()->new ArrayList<>());
    /** 值域类型短名 */
    public String mTypeSimpleName=null;
    /** 值域可能的类型短名,必须是其中一个 */
    public final CacheGettor<ArrayList<String>> mPossTypeSimpleName=CacheGettor.create(()->new ArrayList<>());

    public static FieldFilter c(){
        return new FieldFilter().noGeneratedModifer();
    }

    /**
     * 使用指定的值域名字创建过滤器
     * 
     * @param pName
     * @return
     */
    public static FieldFilter n(String pName){
        return FieldFilter.c().setName(pName);
    }

    /**
     * 使用可能的值域名字创建过滤器
     * 
     * @param pPossName
     * @return
     */
    public static FieldFilter pn(String...pPossName){
        return FieldFilter.c().addPossName(pPossName);
    }

    /**
     * 使用指定的值域类型创建过滤器
     * 
     * @param pType
     *            值域类型
     * @return
     */
    public static FieldFilter t(Class<?> pType){
        return FieldFilter.c().setType(pType);
    }

    /**
     * 使用可能的值域类型创建过滤器
     * 
     * @param pPossType
     *            可能的值域类型,必须为其中一个
     * @return
     */
    public static FieldFilter pt(Class<?>...pPossType){
        return FieldFilter.c().addPossType(pPossType);
    }

    /**
     * 设置过滤器的值域类型
     * 
     * @param pType
     *            类型
     * @return
     */
    public FieldFilter setType(Class<?> pType){
        this.mType=pType;
        return this;
    }

    /**
     * 为过滤器增加可能的值域类型
     * <p>
     * 注意,如果设置了此值,那么需要获取的值域必须为其中的任一类型
     * </p>
     * 
     * @param pType
     *            可能的值域类型
     * @return
     */
    public FieldFilter addPossType(Class<?>...pType){
        if(pType.length>0){
            CollUtil.addEles(this.mPossType.get(),pType);
        }
        return this;
    }

    public FieldFilter setTypeSimpleName(String pName){
        this.mTypeSimpleName=pName;
        return this;
    }

    /**
     * 为过滤器增加可能的值域类型短名
     * <p>
     * 注意,如果设置了此值,那么需要获取的值域必须为其中的任一类型
     * </p>
     * 
     * @param pName
     *            可能的值域类型短名
     * @return
     */
    public FieldFilter addPossTypeSimpleName(String...pName){
        if(pName.length>0){
            CollUtil.addEles(this.mPossTypeSimpleName.get(),pName);
        }
        return this;
    }

    @Override
    protected String getTargetName(Field pObj){
        return pObj.getName();
    }

    @Override
    protected int getTargetModifer(Field pObj){
        return pObj.getModifiers();
    }

    @Override
    public boolean accept(Field pObj){
        if(this.mType!=null&&this.mType!=pObj.getType())
            return false;

        if(this.mPossType.cached()&&!this.mPossType.get().contains(pObj.getType()))
            return false;

        if(this.mTypeSimpleName!=null&&!this.mTypeSimpleName.equals(pObj.getType().getSimpleName()))
            return false;

        if(this.mPossTypeSimpleName.cached()&&!this.mPossTypeSimpleName.get().contains(pObj.getType().getSimpleName()))
            return false;

        return super.accept(pObj);
    }

}
