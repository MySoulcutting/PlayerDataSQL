package cc.commons.util.reflect.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import cc.commons.util.CollUtil;
import cc.commons.util.tools.CacheGettor;

public class MethodFilter extends BaseFilter<Method,MethodFilter>{

    /** 方法返回类型 */
    public Class<?> mReturnType=null;
    /** 方法的可能返回类型,必须是其中一个 */
    public final CacheGettor<ArrayList<Class<?>>> mPossReturnTypes=CacheGettor.create(()->new ArrayList<>());
    /** 方法不可能的方法类型 */
    public final CacheGettor<ArrayList<Class<?>>> mDeniedReturnType=CacheGettor.create(()->new ArrayList<>());
    /** 方法参数类型 */
    public Class<?>[] mParamTypes=null;
    /** 方法的参数个数,<0不检查 */
    public int mParamAmount=-1;

    /**
     * 创建一个空的过滤器
     * 
     * @return
     */
    public static MethodFilter c(){
        return new MethodFilter().noGeneratedModifer();
    }

    /**
     * 使用指定的方法名字创建过滤器
     * 
     * @param pName
     *            方法名字
     * @return
     */
    public static MethodFilter n(String pName){
        return MethodFilter.c().setName(pName);
    }

    /**
     * 使用可能的方法名字创建过滤器
     * <p>
     * 注意如果设置了此值,方法类型必须为其中之一才会匹配
     * </p>
     * 
     * @param pPossNames
     *            方法名字
     * @return
     */
    public static MethodFilter pn(String...pPossNames){
        return MethodFilter.c().addPossName(pPossNames);
    }

    /**
     * 使用指定的返回类型创建过滤器
     * 
     * @param pType
     *            返回类型
     * @return
     */
    public static MethodFilter rt(Class<?> pType){
        return MethodFilter.c().setReturnType(pType);
    }

    /**
     * 使用指定的返回类型和参数类型创建过滤器
     * 
     * @param pType
     *            返回类型
     * @param pParamType
     *            参数类型
     * @return
     */
    public static MethodFilter rpt(Class<?> pType,Class<?>...pParamType){
        return MethodFilter.c().setReturnType(pType).setParamType(pParamType);
    }

    /**
     * 使用可能的返回类型创建过滤器
     * <p>
     * 注意如果设置了此值,方法类型必须为其中之一才会匹配
     * </p>
     * 
     * @param pTypes
     *            返回类型
     * @return
     */
    public static MethodFilter prt(Class<?>...pTypes){
        return MethodFilter.c().possReturnType(pTypes);
    }

    /**
     * 设置方法的返回类型
     * 
     * @param pType
     *            返回类型
     * @return
     */
    public MethodFilter setReturnType(Class<?> pType){
        this.mReturnType=pType;
        return this;
    }

    /**
     * 设置方法的可能返回类型
     * <p>
     * 注意如果设置了此值,方法类型必须为其中之一才会匹配
     * </p>
     * 
     * @param pTypes
     * @return
     */
    public MethodFilter possReturnType(Class<?>...pTypes){
        if(pTypes.length>0){
            CollUtil.addEles(this.mPossReturnTypes.get(),pTypes);
        }
        return this;
    }

    /**
     * 设置方法不可嫩的返回类型
     * 
     * @param pTypes
     *            不可能的返回类型
     * @return
     */
    public MethodFilter denyReturnType(Class<?>...pTypes){
        if(pTypes.length>0){
            CollUtil.addEles(this.mDeniedReturnType.get(),pTypes);
        }
        return this;
    }

    /**
     * 设置方法无无参方法
     * <p>
     * 通过设置方法参数个数为0来实现
     * </p>
     * 
     * @return
     */
    public MethodFilter noParam(){
        return this.setParamAmount(0);
    }

    /**
     * 设置方法的参数个数
     * 
     * @param pAmount
     *            参数个数
     * @return
     */
    public MethodFilter setParamAmount(int pAmount){
        this.mParamAmount=pAmount;
        return this;
    }

    /**
     * 设置方法参数类型
     * 
     * @param pTypes
     *            参数类型
     * @return
     */
    public MethodFilter setParamType(Class<?>...pTypes){
        this.mParamTypes=pTypes;
        return this;
    }

    /**
     * 不匹配名字为toString的方法
     * 
     * @return
     */
    public MethodFilter denyToString(){
        return this.denyName("toString");
    }

    @Override
    protected String getTargetName(Method pObj){
        return pObj.getName();
    }

    @Override
    protected int getTargetModifer(Method pObj){
        return pObj.getModifiers();
    }

    @Override
    public boolean accept(Method pObj){
        if(this.mReturnType!=null&&this.mReturnType!=pObj.getReturnType())
            return false;

        if(this.mPossReturnTypes.cached()&&!this.mPossReturnTypes.get().contains(pObj.getReturnType()))
            return false;

        if(this.mDeniedReturnType.cached()&&this.mDeniedReturnType.get().contains(pObj.getReturnType()))
            return false;

        if(this.mParamAmount>=0&&this.mParamAmount!=pObj.getParameterCount())
            return false;

        if(this.mParamTypes!=null&&!Arrays.equals(this.mParamTypes,pObj.getParameterTypes()))
            return false;

        return super.accept(pObj);
    }

}
