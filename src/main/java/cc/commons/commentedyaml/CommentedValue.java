package cc.commons.commentedyaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 一个值包装类,用来保存注释 List值会循环嵌套包装
 * 
 * @author 聪聪
 *
 */
public class CommentedValue{

    /** 注释行匹配器 */
    protected final static Pattern COMMENT=Pattern.compile("^([ ]*)# ?(.*?)$");
    /** 值包装类的真实值,或是List的嵌套 */
    private Object mValue;
    /** 注释,请不要直接操作注释 */
    private ArrayList<String> mComments=null;

    private CommentedValue(){}

    /**
     * 构造一个包装类并用指定的值初始化
     * 
     * @param pParent
     *            该值的父节点,用于值转换时使用
     * @param pPath
     *            值标签,用于同步Node树
     * @param pValue
     *            值,非null
     */
    private CommentedValue(CommentedSection pParent,String pPath,Object pValue){
        this.setValue(pParent,pPath,pValue);
    }

    /**
     * 设置包装类的值
     * 
     * @param pParent
     *            该值的父节点,用于值转换时使用
     * @param pPath
     *            值标签,用于同步Node树
     * @param pValue
     *            值,非null
     */
    public void setValue(CommentedSection pParent,String pPath,Object pValue){
        if(pValue instanceof CommentedValue){
            CommentedValue cvValue=(CommentedValue)pValue;
            this.mValue=CommentedValue.convertValue(pParent,pPath,cvValue.mValue);
            this.setComments(cvValue.getComments());
        }else this.mValue=CommentedValue.convertValue(pParent,pPath,pValue);
    }

    /**
     * 获取包装的值
     * 
     * @return 包装的值,如果为null,表示此为占位符
     */
    public Object getValue(){
        return this.mValue;
    }

    /** 添加注释 */
    public void addComments(String...pComments){
        if(pComments==null||pComments.length==0)
            return;
        List<String> tAddComments=new ArrayList<>(this.getComments());
        for(String sLine : pComments){
            tAddComments.add(sLine);
        }
        this.setComments(tAddComments);
    }

    /**
     * 添加默认注释
     * <p>
     * 如果注释已经存在,将忽略
     * </p>
     * 
     * @param pComments
     *            评论
     */
    public void addDefaultComments(String...pComments){
        if(pComments==null||pComments.length==0)
            return;
        if(!this.hasComments()){
            this.setComments(pComments);
        }
    }

    /**
     * 设置注释
     * <p>
     * 请勿设置null来清空注释<br />
     * 如果要清空注释,请使用{@link CommentedValue#clearComments()}
     * </p>
     * /
     * 
     * @param pComments
     *            注释
     */
    public void setComments(String...pComments){
        if(pComments==null)return;
        this.setComments(Arrays.asList(pComments));
    }

    /**
     * 添加集合中的注释到指定的路径
     * <p>
     * 此函数中会对每行注释进行过滤与格式化
     * </p>
     * 
     * @param pComments
     *            注释
     */
    protected void setComments(Collection<? extends String> pComments){
        if(this.mComments==null)
            this.mComments=new ArrayList<>();
        else this.mComments.clear();
        if(pComments==null||pComments.isEmpty())
            return;
        ArrayList<String> addComments=new ArrayList<>();
        for(String sLine : pComments){ // 过滤null行,格式化注释
            if(sLine==null)
                continue;
            java.util.regex.Matcher tMatcher=COMMENT.matcher(sLine);
            if(!tMatcher.find())
                addComments.add(sLine);
            else addComments.add(tMatcher.group(2));
        }
        this.mComments.addAll(pComments);
    }

    /**
     * 获取注释,可编辑
     * 
     * @return 非null
     */
    public ArrayList<String> getComments(){
        if(this.mComments==null){
            this.mComments=new ArrayList<>();
        }
        return this.mComments;
    }

    /**
     * 节点是否有注释
     */
    public boolean hasComments(){
        return !this.getComments().isEmpty();
    }

    /**
     * 清空指定路径下的注释
     * 
     * @return 清理掉的注释,非null
     */
    public ArrayList<String> clearComments(){
        ArrayList<String> removedComments=new ArrayList<>();
        if(this.mComments!=null){
            removedComments.addAll(this.getComments());
            this.mComments.clear();
        }
        return removedComments;
    }

    /**
     * 包装一个值
     * 
     * @param pParent
     *            该值的父节点
     * @param pValue
     *            值内容,非null
     * @return 包装后的值
     */
    protected static CommentedValue wrapperValue(CommentedSection pParent,String pPath,Object pValue){
        CommentedValue cvValue=null;
        if(pValue instanceof CommentedValue){
            cvValue=(CommentedValue)pValue;
            cvValue.mValue=CommentedValue.convertValue(pParent,pPath,cvValue.mValue);
        }else{
            cvValue=new CommentedValue();
            cvValue.mValue=CommentedValue.convertValue(pParent,pPath,pValue);
        }
        return cvValue;
    }

    /**
     * 转换值,同时会调整父子节点结构
     * 
     * @param pParent
     *            父节点
     * @param pValue
     *            值,非null,非CommentedValue
     * @return 转换后的值
     */
    private static Object convertValue(CommentedSection pParent,String pPath,Object pValue){
        if(pValue==null)
            return null;
        if(pValue instanceof CommentedValue){
            throw new IllegalArgumentException("值包装类不能包装值包装类实例");
        }else if(pValue instanceof CommentedSection){
            // 此处需要对节点的从属关系进行调整
            CommentedSection cmtedSec=(CommentedSection)pValue;
            // 在路径和父节点变更时,重组节点树
            if(cmtedSec.getParent()==pParent){
                if(!cmtedSec.getName().equals(pPath)){
                    cmtedSec.mName=pPath;
                }
                return cmtedSec;
            }
            CommentedSection newCmtedSec=pParent.createSection(pPath);
            for(String childKey : cmtedSec.mChild.keySet()){
                Object cfgSecValue=cmtedSec.mChild.get(childKey);
                if(cfgSecValue==null)
                    continue;
                newCmtedSec.mChild.put(childKey,CommentedValue.wrapperValue(newCmtedSec,pPath,cfgSecValue));
            }
            return newCmtedSec;
        }
        //        else if(pValue instanceof ConfigurationSection){
        //            // 转换成CommentedSection
        //            ConfigurationSection cfgSec=(ConfigurationSection)pValue;
        //            CommentedSection newCmtedSec=pParent.createSection(pPath);
        //            for(String childKey : cfgSec.getKeys(false)){
        //                Object cfgSecValue=cfgSec.get(childKey);
        //                if(cfgSecValue==null)
        //                    continue;
        //                newCmtedSec.mChild.put(childKey,CommentedValue.wrapperValue(newCmtedSec,pPath,cfgSecValue));
        //            }
        //            return newCmtedSec;
        //        }
        return pValue;
    }

    @Override
    public String toString(){
        String str="{Comments:";
        if(this.mComments!=null)
            str+=this.mComments;
        else str+="[]";
        return str+",Value:"+this.mValue+"}";
    }

}
