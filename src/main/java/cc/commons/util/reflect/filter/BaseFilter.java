package cc.commons.util.reflect.filter;

import java.util.ArrayList;

import cc.commons.util.CollUtil;
import cc.commons.util.interfaces.IFilter;
import cc.commons.util.tools.CacheGettor;

public abstract class BaseFilter<T,E extends BaseFilter<T,E>> implements IFilter<T>{

    /** 值域/方法名字 */
    public String mName=null;
    /** 可能的值域/方法名字 */
    public final CacheGettor<ArrayList<String>> mPossName=CacheGettor.create(()->new ArrayList<>());
    /** 不可能的值域/方法名字 */
    public CacheGettor<ArrayList<String>> mDenyName=CacheGettor.create(()->new ArrayList<>());
    /** 可能的访问限定符,必定存在一个 */
    public int mPossModifer=0;
    /** 不允许的访问限定符 */
    public int mDenyModifer=0;
    /** 子过滤器 */
    public CacheGettor<ArrayList<IFilter<T>>> mSubFilter=CacheGettor.create(()->new ArrayList<>());

    public E setName(String pName){
        this.mName=pName;
        return (E)this;
    }
    
    public E addPossName(String...pNames){
        if(pNames.length>0){
            CollUtil.addEles(this.mPossName.get(),pNames);
        }
        return (E)this;
    }

    public E denyName(String...pNames){
        if(pNames.length>0){
            CollUtil.addEles(this.mDenyName.get(),pNames);
        }
        return (E)this;
    }
    
    public E addPossModifer(int...pModifers){
        this.mPossModifer=this.addModifer(this.mPossModifer,pModifers);
        return (E)this;
    }

    public E addDeniedModifer(int...pModifers){
        this.mDenyModifer=this.addModifer(this.mDenyModifer,pModifers);
        return (E)this;
    }

    protected int addModifer(int pTarget,int...pModifers){
        for(int sModifer : pModifers){
            pTarget|=sModifer;
        }
        return pTarget;
    }

    public E noSyntheticModifer(){
        this.addDeniedModifer(0x00001000);
        return (E)this;
    }

    public E noBridgeModifer(){
        this.addDeniedModifer(0x00000040);
        return (E)this;
    }

    // 静态构造函数默认调用
    public E noGeneratedModifer(){
        this.noBridgeModifer();
        return this.noSyntheticModifer();
    }

    /**
     * 添加子过滤器
     * 
     * @param pFilter
     *            过滤器
     * @return
     */
    public E addFilter(IFilter<T> pFilter){
        this.mSubFilter.get().add(pFilter);
        return (E)this;
    }

    protected abstract String getTargetName(T pObj);

    protected abstract int getTargetModifer(T pObj);

    @Override
    public boolean accept(T pObj){
        if(this.mName!=null&&!this.mName.equals(getTargetName(pObj)))
            return false;

        if(this.mPossName.cached()&&!this.mPossName.get().contains(getTargetName(pObj)))
            return false;

        if(this.mDenyName.cached()&&this.mDenyName.get().contains(getTargetName(pObj)))
            return false;
        
        if(this.mPossModifer!=0&&(this.mPossModifer&getTargetModifer(pObj))==0)
            return false;

        if(this.mDenyModifer!=0&&(this.mDenyModifer&getTargetModifer(pObj))!=0)
            return false;

        if(this.mSubFilter.cached()) for(IFilter<T> sFilter : this.mSubFilter.get()){
            if(!sFilter.accept(pObj)) return false;
        }

        return true;
    }

}
