package cc.bukkitPlugin.commons.tellraw;

public enum Format{
    obfuscated('k',"obfuscated"),
    bold('l',"bold"),
    strikethrough('m',"strikethrough"),
    underline('n',"underlined"),
    italic('o',"italic"),
    reset('r',null);

    public static final char FORMAT_CHAR='§';
    private final char code;
    private final String mLabel;
    private final String toString;

    private Format(char pChar,String pLabel){
        this.code=pChar;
        this.mLabel=pLabel;
        this.toString=new String(new char[]{'§',code});
    }

    @Override
    public String toString(){
        return this.toString;
    }

    public static boolean isStyle(char c){
        return Format.getStyle(c)!=null;
    }

    public static Format getStyle(char c){
        c=(char)((c&0xDF)+32);
        for(Format sStyle : Format.values())
            if(sStyle.code==c)
                return sStyle;
        return null;
    }

    /**
     * 如果为null代表这个是不是Json使用的节点
     */
    public String getJsonLabel(){
        return this.mLabel;
    }

}
