package cc.commons.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import cc.commons.util.extra.CList;
import cc.commons.util.interfaces.IFilter;

public class FieldUtil {

    private static final String REFLACT_OP_ERROR = "反射操作异常";
    private static final String NO_SUCH_FIELD = "未找到该类型的值域";
    private static final String LINE_BREAK = System.getProperty("line.separator", "\n");

    private static void fieldNotEmpty(String[] pFieldNames) {
        if (pFieldNames == null || pFieldNames.length == 0)
            throw new IllegalArgumentException("至少需要一个值域名");
    }

    public static CList<Field> getAllField(Class<?> pClazz) {
        CList<Field> tFields = new CList<>();
        while (pClazz != null) {
            for (Field sField : pClazz.getDeclaredFields()) {
                tFields.add(sField);
            }
            pClazz = pClazz.getSuperclass();
        }
        return tFields;
    }

    // --------====| 打印方法 |====--------

    /**
     * 打印值域
     * 
     * @param pClazz
     *            类
     * @param pDeclared
     *            是否只打印该类定义的值域而不打印父类的值域
     */
    public static void printField(Class<?> pClazz, boolean pDeclared) {
        do {
            for (Field sField : pClazz.getDeclaredFields()) {
                System.out.println(sField);
            }
        } while (!pDeclared && (pClazz = pClazz.getSuperclass()) != null);
    }

    // --------====| 检查方法 |====--------

    /**
     * 检查值域是否存在
     * 
     * @param pClazz
     *            类
     * @param pFieldName
     *            值域名字
     * @return 是否存在
     */
    public static boolean isFieldExist(Class<?> pClazz, String pFieldName) {
        return FieldUtil.isFieldExist(pClazz, pFieldName, false);
    }

    /**
     * 检查值域是否存在
     * 
     * @param pClazz
     *            类
     * @param pFieldName
     *            值域名字
     * @return 是否存在
     */
    public static boolean isDeclaredFieldExist(Class<?> pClazz, String pFieldName) {
        return FieldUtil.isFieldExist(pClazz, pFieldName, true);
    }

    /**
     * 检查值域是否存在
     * 
     * @param pClazz
     *            类
     * @param pFieldName
     *            值域名字
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @return 是否存在
     */
    public static boolean isFieldExist(Class<?> pClazz, String pFieldName, boolean pDeclared) {
        do {
            for (Field sField : pClazz.getDeclaredFields()) {
                if (sField.getName().equals(pFieldName)) {
                    return true;
                }

            }
        } while (!pDeclared && (pClazz = pClazz.getSuperclass()) != null);

        return false;
    }

    /**
     * 检查值域是否存在
     * 
     * @param pClazz
     *            类
     * @param pFilter
     *            值域过滤器
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @return 是否存在
     */
    public static boolean isFieldExist(Class<?> pClazz, IFilter<Field> pFilter, boolean pDeclared) {
        do {
            for (Field sField : pClazz.getDeclaredFields()) {
                if (pFilter.accept(sField))
                    return true;
            }
        } while (!pDeclared && (pClazz = pClazz.getSuperclass()) != null);

        return false;
    }

    /**
     * 检查值域是否存在
     * 
     * @param pClazz
     *            类
     * @param pFilter
     *            值域过滤器
     * @return 是否存在
     */
    public static boolean isFieldExist(Class<?> pClazz, IFilter<Field> pFilter) {
        return FieldUtil.isFieldExist(pClazz, pFilter, false);
    }

    /**
     * 检查值域是否存在
     * 
     * @param pClazz
     *            类
     * @param pFilter
     *            值域过滤器
     * @return 是否存在
     */
    public static boolean isDeclaredFieldExist(Class<?> pClazz, IFilter<Field> pFilter) {
        return FieldUtil.isFieldExist(pClazz, pFilter, true);
    }

    // --------====| 获取值域方法 |====--------

    /**
     * 获取值域
     * 
     * @param pClazz
     *            类
     * @param pFieldName
     *            值域名
     * @return 匹配名字的值域,包括父类
     * @throws IllegalStateException
     *             没有匹配到任何值域
     */
    public static Field getField(Class<?> pClazz, String pFieldName) {
        return FieldUtil.getField(pClazz, pFieldName, false);
    }

    /**
     * 获取值域
     * 
     * @param pClazz
     *            类
     * @param pFieldName
     *            值域名
     * @return 匹配名字的值域,不包括父类
     * @throws IllegalStateException
     *             没有匹配到任何值域
     */
    public static Field getDeclaredField(Class<?> pClazz, String pFieldName) {
        return FieldUtil.getField(pClazz, pFieldName, true);
    }

    /**
     * 获取值域
     * 
     * @param pClazz
     *            类
     * @param pFieldName
     *            值域名
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @return 匹配名字的值域
     * @throws IllegalStateException
     *             没有匹配到任何值域
     */
    public static Field getField(Class<?> pClazz, String pFieldName, boolean pDeclared) {
        Class<?> tClass = pClazz;
        do {
            Field[] tFields = tClass.getDeclaredFields();
            for (Field sField : tFields) {
                if (sField.getName().equals(pFieldName)) {
                    return sField;
                }
            }
        } while (!pDeclared && (tClass = tClass.getSuperclass()) != null);

        throw new IllegalStateException(NO_SUCH_FIELD + LINE_BREAK
                + "\t类: " + pClazz.getName() + LINE_BREAK
                + "\t值域: " + pFieldName, new NoSuchFieldException());
    }

    /**
     * 获取值域
     * 
     * @param pClazz
     *            类
     * @param pFilter
     *            值域过滤器
     * @return 符合的值域,非空,包括父类
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static CList<Field> getField(Class<?> pClazz, IFilter<Field> pFilter) {
        return FieldUtil.getField(pClazz, pFilter, false);
    }

    /**
     * 获取值域
     * 
     * @param pClazz
     *            类
     * @param pFilter
     *            值域过滤器
     * @return 符合的值域,非空,包括父类
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static CList<Field> getDeclaredField(Class<?> pClazz, IFilter<Field> pFilter) {
        return FieldUtil.getField(pClazz, pFilter, true);
    }

    /**
     * 获取值域
     * 
     * @param pClazz
     *            类
     * @param pFilter
     *            值域过滤器
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @return 符合的值域,非空
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static CList<Field> getField(Class<?> pClazz, IFilter<Field> pFilter, boolean pDeclared) {
        Class<?> tClass = pClazz;
        CList<Field> tFoundFields = new CList<>();
        do {
            Field[] tFields = tClass.getDeclaredFields();
            if (tFields.length == 0) return tFoundFields;
            for (Field sField : tFields) {
                if (pFilter.accept(sField)) {
                    tFoundFields.add(sField);
                }
            }
        } while (!pDeclared && (tClass = tClass.getSuperclass()) != null);

        if (!tFoundFields.isEmpty()) {
            return tFoundFields;
        }
        throw new IllegalStateException(NO_SUCH_FIELD + LINE_BREAK
                + "\t类: " + pClazz.getName() + LINE_BREAK
                + "\t值域过滤器类: " + pFilter.getClass().getName(), new NoSuchFieldException());
    }

    // --------====| 获取值域值的方法 |====--------

    /**
     * 获取静态值域的值
     * 
     * @param pField
     *            值域
     * @return 值域的值
     * @throws IllegalStateException
     *             反射操作发生异常
     */
    public static Object getStaticFieldValue(Field pField) {
        return FieldUtil.getFieldValue(pField, (Object)null);
    }

    /**
     * 获取值域的值
     * 
     * @param pField
     *            值域
     * @param pObj
     *            要取值的实例,如果方法为静态,可以为null
     * @return 值域的值
     * @throws IllegalStateException
     *             反射操作发生异常
     */
    public static Object getFieldValue(Field pField, Object pObj) {
        try {
            pField.setAccessible(true);
            return pField.get(pObj);
        } catch (IllegalArgumentException | IllegalAccessException exp) {
            throw new IllegalStateException(REFLACT_OP_ERROR, exp);
        }
    }

    /**
     * 获取静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @return 值域的值
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static Object getStaticFieldValue(Class<?> pClazz, String pFieldName) {
        return FieldUtil.getFieldValue(FieldUtil.getField(pClazz, pFieldName, false), (Object)null);
    }

    /**
     * 获取静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @return 值域的值
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static Object getStaticDeclaredFieldValue(Class<?> pClazz, String pFieldName) {
        return FieldUtil.getFieldValue(FieldUtil.getField(pClazz, pFieldName, true), (Object)null);
    }

    /**
     * 获取静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @return 值域的值
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static Object getStaticFieldValue(Class<?> pClazz, String pFieldName, boolean pDeclared) {
        return FieldUtil.getFieldValue(FieldUtil.getField(pClazz, pFieldName, pDeclared), (Object)null);
    }

    /**
     * 获取值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pObj
     *            要取值的实例,如果方法为静态,可以为null
     * @return 值域的值
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static Object getFieldValue(Class<?> pClazz, String pFieldName, Object pObj) {
        return FieldUtil.getFieldValue(FieldUtil.getField(pClazz, pFieldName, false), pObj);
    }

    /**
     * 获取值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pObj
     *            要取值的实例,如果方法为静态,可以为null
     * @return 值域的值
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static Object getDeclaredFieldValue(Class<?> pClazz, String pFieldName, Object pObj) {
        return FieldUtil.getFieldValue(FieldUtil.getField(pClazz, pFieldName, true), pObj);
    }

    /**
     * 获取值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @param pObj
     *            要取值的实例,如果方法为静态,可以为null
     * @return 值域的值
     * @throws IllegalStateException
     *             没有符合条件的值域
     */
    public static Object getFieldValue(Class<?> pClazz, String pFieldName, boolean pDeclared, Object pObj) {
        return FieldUtil.getFieldValue(FieldUtil.getField(pClazz, pFieldName, pDeclared), pObj);
    }

    /**
     * 获取静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFilter
     *            值域过滤器
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @return 符合的值域的值,非空
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static <T> CList<T> getStaticFieldValue(Class<?> pClazz, IFilter<Field> pFilter, boolean pDeclared) {
        return FieldUtil.getFieldValue(pClazz, pFilter, pDeclared, (Object)null);
    }

    /**
     * 获取值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFilter
     *            值域过滤器
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @param pObj
     *            要取值的实例,如果方法为静态,可以为null
     * @return 符合的值域的值,非空
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static CList getFieldValue(Class<?> pClazz, IFilter<Field> pFilter, boolean pDeclared, Object pObj) {
        CList tFieldValues = new CList<>();
        for (Field sField : FieldUtil.getField(pClazz, pFilter, pDeclared)) {
            tFieldValues.add(FieldUtil.getFieldValue(sField, pObj));
        }
        return tFieldValues;
    }

    // --------====| 设置值域值的方法 |====--------

    /**
     * 设置静态值域的值
     * 
     * @param pField
     *            值域
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常
     */
    public static void setStaticFieldValue(Field pField, Object pValue) {
        FieldUtil.setFieldValue(pField, (Object)null, pValue);
    }

    /**
     * 设置值域的值
     * 
     * @param pField
     *            值域
     * @param pObj
     *            要设置值的实例,如果值域为静态,可以为null
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常
     */
    public static void setFieldValue(Field pField, Object pObj, Object pValue) {
        try {
            pField.setAccessible(true);
            pField.set(pObj, pValue);
        } catch (IllegalArgumentException | IllegalAccessException exp) {
            throw new IllegalStateException(REFLACT_OP_ERROR, exp);
        }
    }

    /**
     * 设置静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static void setStaticFieldValue(Class<?> pClazz, String pFieldName, Object pValue) {
        FieldUtil.setFieldValue(pClazz, pFieldName, false, (Object)null, pValue);
    }

    /**
     * 设置静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static void setStaticDeclaredFieldValue(Class<?> pClazz, String pFieldName, Object pValue) {
        FieldUtil.setFieldValue(pClazz, pFieldName, true, (Object)null, pValue);
    }

    /**
     * 设置静态值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static void setStaticFieldValue(Class<?> pClazz, String pFieldName, boolean pDeclared, Object pValue) {
        FieldUtil.setFieldValue(pClazz, pFieldName, pDeclared, (Object)null, pValue);
    }

    /**
     * 设置值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pObj
     *            要设置值的实例,如果值域为静态,可以为null
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static void setFieldValue(Class<?> pClazz, String pFieldName, Object pObj, Object pValue) {
        FieldUtil.setFieldValue(FieldUtil.getField(pClazz, pFieldName, false), pObj, pValue);
    }

    /**
     * 设置值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pObj
     *            要设置值的实例,如果值域为静态,可以为null
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static void setDeclaredFieldValue(Class<?> pClazz, String pFieldName, Object pObj, Object pValue) {
        FieldUtil.setFieldValue(FieldUtil.getField(pClazz, pFieldName, true), pObj, pValue);
    }

    /**
     * 设置值域的值
     * 
     * @param pClazz
     *            类,用于获取值域
     * @param pFieldName
     *            值域名
     * @param pDeclared
     *            是否只检索该类定义的值域而不检索父类的值域
     * @param pObj
     *            要设置值的实例,如果值域为静态,可以为null
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常,或没有符合条件的值域
     */
    public static void setFieldValue(Class<?> pClazz, String pFieldName, boolean pDeclared, Object pObj, Object pValue) {
        FieldUtil.setFieldValue(FieldUtil.getField(pClazz, pFieldName, pDeclared), pObj, pValue);
    }

    /**
     * 设置静态final值域的值
     * 
     * @param pField
     *            值域
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常
     */
    public static void setFinalFieldValue(Field pField, Object pValue) {
        FieldUtil.setFinalFieldValue(pField, (Object)null, pValue);
    }

    /**
     * 设置final值域的值
     * 
     * @param pField
     *            值域
     * @param pObj
     *            要设置值的实例,如果值域为静态,可以为null
     * @param pValue
     *            要设置成的值
     * @throws IllegalStateException
     *             反射操作发生异常
     */
    public static void setFinalFieldValue(Field pField, Object pObj, Object pValue) {
        try {
            pField.setAccessible(true);
            boolean tIsFinal = Modifier.isFinal(pField.getModifiers());
            int tOriginModifer = pField.getModifiers();
            if (tIsFinal) {
                if (FFC == null) findFinalModifer();
                FFC.beforeCall(pField);
            }
            pField.set(pObj, pValue);
            if (tIsFinal) {
                FFC.afterCall(pField);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException exp) {
            throw new IllegalStateException(REFLACT_OP_ERROR, exp);
        }
    }

    private static FinalFieldCall FFC = null;
    private final static String mTest = "";

    private abstract static class FinalFieldCall {

        public abstract void beforeCall(Field pField);

        public abstract void afterCall(Field pField);
    }

    private synchronized static void findFinalModifer() throws NoSuchFieldException {
        if (FFC != null) return;

        Field tModifierF = null;
        for (String sFName : new String[]{"modifiers", "accessFlags"}) {
            if (FieldUtil.isDeclaredFieldExist(Field.class, "modifiers")) {
                tModifierF = FieldUtil.getDeclaredField(Field.class, sFName);
                break;
            }
        }

        Field tField = FieldUtil.getDeclaredField(FieldUtil.class, "mTest");
        if (tModifierF == null) {
            CList<Field> tFields = FieldUtil.getField(Field.class, (pField) -> pField.getType() == int.class);
            for (Field sField : tFields) {
                int tOrignV = (int)FieldUtil.getFieldValue(sField, tField);
                if ((tOrignV & 0xffff) == tField.getModifiers()) { //android
                    tModifierF = sField;
                    break;
                }
            }
        }

        if (tModifierF != null) {
            final Field tModifierField = tModifierF;
            FFC = new FinalFieldCall() {

                @Override
                public void beforeCall(Field pField) {
                    FieldUtil.setFieldValue(tModifierField, pField, (pField.getModifiers() & (~Modifier.FINAL)));
                }

                @Override
                public void afterCall(Field pField) {
                    FieldUtil.setFieldValue(tModifierField, pField, (pField.getModifiers() | (Modifier.FINAL)));
                }
            };
            return;
        } else {
            // android 5.1
            if (FieldUtil.isDeclaredFieldExist(Field.class, "artField")) {
                final Field field_ArtField = FieldUtil.getField(Field.class, "artField");
                Class<?> tClazz_ArtField = field_ArtField.getType();
                final Field field_ArtField_accessFlags = FieldUtil.getDeclaredField(tClazz_ArtField, "accessFlags");

                FFC = new FinalFieldCall() {

                    @Override
                    public void beforeCall(Field pField) {
                        Object tObj_ArtField = FieldUtil.getFieldValue(field_ArtField, pField);
                        FieldUtil.setFieldValue(field_ArtField_accessFlags, tObj_ArtField, (pField.getModifiers() & (~Modifier.FINAL)));
                    }

                    @Override
                    public void afterCall(Field pField) {
                        Object tObj_ArtField = FieldUtil.getFieldValue(field_ArtField, pField);
                        FieldUtil.setFieldValue(field_ArtField_accessFlags, tObj_ArtField, (pField.getModifiers() | (Modifier.FINAL)));
                    }
                };
                return;
            }

        }
        throw new NoSuchFieldException("No accessFlag find!");
    }
}
