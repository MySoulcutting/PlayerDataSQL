package cc.bukkitPlugin.commons.extra;

/**
 * 一个无视大小的字符串
 */
public final class NCString implements Cloneable,Comparable<NCString>{

    public final String mOrign;
    public final String mLowerCase;

    public static NCString c(String pOrign){
        return new NCString(pOrign);
    }
    
    public NCString(String pOrign){
        this.mOrign=pOrign;
        this.mLowerCase=pOrign==null?null:pOrign.toLowerCase();
    }

    @Override
    public String toString(){
        return String.valueOf(this.mOrign);
    }

    @Override
    public boolean equals(Object pObj){
        return (pObj instanceof NCString)?this.compareTo((NCString)pObj)==0:false;
    }

    @Override
    public int hashCode(){
        return this.mLowerCase==null?0:this.mLowerCase.hashCode();
    }

    @Override
    protected NCString clone(){
        try{
            return (NCString)super.clone();
        }catch(CloneNotSupportedException exp){
            return new NCString(this.mOrign);
        }
    }

    @Override
    public int compareTo(NCString pOther){
        return this.compareTo(pOther.mLowerCase);
    }

    /**
     * 与小写的字符串进行比较
     * 
     * @param pLowerCase
     *            小写的字符 串
     * @return 比较值
     */
    protected int compareTo(String pLowerCase){
        if(this.mLowerCase==null&&pLowerCase==null)
            return 0;
        if(this.mLowerCase==null)
            return -1;
        if(pLowerCase==null)
            return 1;
        return this.mLowerCase.compareTo(pLowerCase);
    }

}
