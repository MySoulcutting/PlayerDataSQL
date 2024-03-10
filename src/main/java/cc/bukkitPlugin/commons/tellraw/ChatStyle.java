package cc.bukkitPlugin.commons.tellraw;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatStyle implements Cloneable{

    protected ChatStyle mParentStyle=null;
    protected Color mColor=null;
    protected HashMap<Format,Boolean> mFormats=new HashMap<>();
    protected ClickEvent mClickEvent=null;
    protected HoverEvent mHoverEvent=null;

    private static final ChatStyle mRootStyle=new ChatStyle(){

        @Override
        public Color getColor(){
            return null;
        }

        @Override
        public HoverEvent getHoverEvent(){
            return null;
        }

        @Override
        public ClickEvent getClickEvent(){
            return null;
        }

        @Override
        public boolean getFormat(Format pFormat){
            return false;
        }
    };

    public ChatStyle(){}

    public ChatStyle(Color pColor){
        this.mColor=pColor;
    }

    /** 获取当前样式的颜色 */
    public Color getColor(){
        return this.mColor==null?this.getParent().getColor():this.mColor;
    }

    /** 设置当前样式的颜色 */
    public ChatStyle setColor(Color pColor){
        this.mColor=pColor;
        return this;
    }

    /** 设置当前样式的格式 */
    public ChatStyle setFormat(Format...pFormats){
        this.clearFormat();
        return this.addFormat(pFormats);
    }

    /** 获取当前样式中,某个格式是否启用 */
    public boolean getFormat(Format pFormat){
        Boolean b=this.mFormats.get(pFormat);
        return b==null?this.getParent().getFormat(pFormat):b;
    }

    /** 追加格式到当前样式 */
    public ChatStyle addFormat(Format...pFormats){
        for(Format sFormat : pFormats){
            this.mFormats.put(sFormat,Boolean.TRUE);
        }
        return this;
    }

    /** 从当前样式中移除格式,无论是否启用 */
    public ChatStyle removeFormat(Format...pFormats){
        for(Format sFormat : pFormats){
            this.mFormats.remove(sFormat);
        }
        return this;
    }

    /** 只从指定目标复制格式,颜色时间等不复制 */
    public ChatStyle copyColorAndFormat(ChatStyle pStyle){
        this.mColor=pStyle.mColor;
        this.mFormats.clear();
        this.mFormats.putAll(pStyle.mFormats);
        return this;
    }

    /** 获取所有样式,不可编辑 */
    public Map<Format,Boolean> getFormats(){
        return Collections.unmodifiableMap(this.mFormats);
    }

    /** 设置指定格式为false */
    public ChatStyle turnOffFormat(Format...pFormats){
        for(Format sFormat : Format.values()){
            this.mFormats.put(sFormat,Boolean.FALSE);
        }
        return this;
    }

    /** 将所有未设置的格式设置为false */
    public ChatStyle turnOffAllUnSetFormat(){
        for(Format sFormat : Format.values()){
            Boolean tResult=this.mFormats.get(sFormat);
            if(tResult==null){
                this.mFormats.put(sFormat,Boolean.FALSE);
            }
        }
        return this;
    }

    /** 设置所有格式为false */
    public ChatStyle turnOffAllFormat(){
        for(Format sFormat : Format.values()){
            this.mFormats.put(sFormat,Boolean.FALSE);
        }
        return this;
    }

    /** 清空格式设置 */
    public ChatStyle clearFormat(){
        this.mFormats.clear();
        return this;
    }

    /** 清空所有样式 */
    public ChatStyle clearAll(){
        this.mParentStyle=null;
        this.mHoverEvent=null;
        this.mClickEvent=null;
        this.mColor=null;
        this.mFormats.clear();
        return this;
    }

    /**
     * 将此格式中的所有值设置为默认格式<br>
     * <p>
     * 此操作将: <br>
     * 移除父样式<br>
     * 清空,HoverAction和ClickAction设置<br>
     * 设置字体颜色为白色<br>
     * 设置格式全部分false
     * </p>
     */
    public ChatStyle setDefaultStyle(){
        this.mParentStyle=null;
        this.mHoverEvent=null;
        this.mClickEvent=null;
        this.mColor=Color.white;
        for(Format sStyle : Format.values()){
            this.mFormats.put(sStyle,Boolean.FALSE);
        }
        return this;
    }

    @Override
    public ChatStyle clone(){
        ChatStyle tStyle;
        try{
            tStyle=(ChatStyle)super.clone();
        }catch(CloneNotSupportedException e){
            tStyle=new ChatStyle();
        }
        tStyle.copyFrom(this);
        return tStyle;
    }

    /** 从指定样式复制内容,并返回自己 */
    public ChatStyle copyFrom(ChatStyle pStyle){
        this.mParentStyle=pStyle.mParentStyle;
        if(pStyle.mClickEvent!=null)
            this.mClickEvent=pStyle.mClickEvent.clone();
        if(pStyle.mHoverEvent!=null)
            this.mHoverEvent=pStyle.mHoverEvent.clone();
        this.mColor=pStyle.mColor;
        this.mFormats.clear();
        this.mFormats.putAll(pStyle.mFormats);
        return this;
    }

    public boolean isEmpty(){
        return this.mFormats.isEmpty()&&this.mColor==null&&this.mClickEvent==null&&this.mHoverEvent==null&&this.mParentStyle==null;
    }

    public ClickEvent getClickEvent(){
        return this.mClickEvent==null?this.getParent().getClickEvent():this.mClickEvent;
    }

    public HoverEvent getHoverEvent(){
        return this.mHoverEvent==null?this.getParent().getHoverEvent():this.mHoverEvent;
    }

    public ChatStyle setClickEvent(ClickEvent pEvent){
        this.mClickEvent=pEvent;
        return this;
    }

    public ChatStyle setClickEvent(ClickEvent.Action pAction,String pValue){
        return this.setClickEvent(new ClickEvent(pAction,pValue));
    }

    public ChatStyle setHoverEvent(HoverEvent pEvent){
        this.mHoverEvent=pEvent;
        return this;
    }

    public ChatStyle setHoverEvent(HoverEvent.Action pAction,String pValue){
        return this.setHoverEvent(new HoverEvent(pAction,pValue));
    }

    public ChatStyle setParentStyle(ChatStyle pStyle){
        this.mParentStyle=pStyle;
        return this;
    }

    private ChatStyle getParent(){
        return this.mParentStyle==null?ChatStyle.mRootStyle:this.mParentStyle;
    }

}
