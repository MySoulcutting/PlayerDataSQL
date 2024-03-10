package cc.commons.commentedyaml.comment;

import java.util.ArrayList;

public class YamlNode{

    /** 父节点,用于构造路径 */
    private YamlNode mParent;
    /** 节点为根节点时或List的值节点时,此值才会Empty */
    public String mName="";
    /** 节点字符串形式的值 */
    public String mValueStr="";
    /** 节点类型 */
    public LineType mType=null;

    /**
     * 获取当前节点到根节点的路径
     * <p>
     * 如果根节点为null,那么返回null
     * </p>
     * 
     * @param pPathSeparator
     *            路径拼接字符
     * @return 拼接的路径或null
     */
    public ArrayList<String> getPathList(/* char pPathSeparator */){
        if(this.mParent==null){
            return null;
        }else if(this.mParent==this){
            return null;
        }else{
            YamlNode tNode=this;
            ArrayList<String> tPath=new ArrayList<>();
            while(tNode.mParent!=null){
                if(tNode==tNode.mParent)
                    return tPath;

                if(tNode.mName==null||tNode.mName.isEmpty())
                    return null;
                tPath.add(0,tNode.mName);
                tNode=tNode.mParent;
            }
            return null;
        }
    }

    public void setParent(YamlNode pNode){
        this.mParent=pNode;
    }

    public YamlNode getParent(){
        return this.mParent;
    }

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        if(this.mName==null||this.mName.isEmpty())
            this.mName="";
        return sb.append("\"").append(mName).append("\"").append(":").toString();
    }

    private static String getBlank(int pCount){
        if(pCount<=0)
            return "";
        StringBuilder sb=new StringBuilder();
        while(pCount-->0){
            sb.append(' ');
        }
        return sb.toString();
    }

}
