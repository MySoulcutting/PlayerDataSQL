package cc.bukkitPlugin.commons.nmsutil.nbt;

public class NBTKey{

    /** 物品NBT 类型id */
    public static final String ItemId="id";
    /** 物品NBT 耐久 */
    public static final String ItemDamage="Damage";
    /** 物品NBT 数量 */
    public static final String ItemCount="Count";
    /** 物品NBT Tag节点 */
    public static final String ItemTag="tag";
    /** NBT显示节点 ,注意不是物品名字,对应的NBT类型的NBTTagCompound */
    public static final String ItemDisplay="display";
    /** 物品显示名字节点,存在于{@link #ItemDisplay} 节点下 ,对应的NBT类型为NBTTagString */
    public static final String ItemName="Name";
    /**
     * 物品Lore节点 ,存在于{@link #ItemDisplay} 节点下 ,对应的NBT类型为NBTTagList(NBTTagString)
     */
    public static final String ItemLore="Lore";
    /** 物品附魔节点,对应的NBT类型为NBTTagList(NBTTagCompound) */
    public static final String Enchant="ench";
    /** 物品附魔Id,对应的NBT类型为数字,存在于{@link #Enchant} */
    public static final String EnchantId="id";
    /** 物品附魔等级,对应的NBT类型为数字,存在于{@link #Enchant} */
    public static final String EnchantLevel="lvl";
    /** 物品修复花费 */
    public static final String Repait="RepairCost";
    /** 不可破坏 */
    public static final String Unbreakable="Unbreakable";

    /** 物品修饰属性容器节点 */
    public static final String Attributes="AttributeModifiers";

    public static final String AttrIdentifier="AttributeName";

    public static final String AttrName="Name";

    public static final String AttrValue="Amount";
    /** 值只有0,1,2 */
    public static final String AttrType="Operation";

    public static final String AttrUUIDHigh="UUIDMost";

    public static final String AttrUUIDLow="UUIDLeast";

}
