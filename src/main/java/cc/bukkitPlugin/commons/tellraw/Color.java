package cc.bukkitPlugin.commons.tellraw;

public enum Color{
    black('0'),
    dark_aqua('3'),
    dark_blue('1'),
    dark_gray('8'),
    dark_green('2'),
    dark_purple('5'),
    dark_red('4'),
    aqua('b'),
    blue('9'),
    gold('6'),
    gray('7'),
    green('a'),
    light_purple('d'),
    red('c'),
    white('f'),
    yellow('e');

    private final char code;
    private final String toString;

    private Color(char pChar){
        this.code=pChar;
        this.toString=new String(new char[]{'§',code});
    }

    @Override
    public String toString(){
        return this.toString;
    }

    public static boolean isColor(char c){
        return Color.getColor(c)!=null;
    }

    public static Color getColor(char c){
        c=(char)((c&0xDF)+32);
        for(Color sColor : Color.values())
            if(sColor.code==c)
                return sColor;
        return null;
    }

}
