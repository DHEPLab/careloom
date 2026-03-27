package edu.stanford.fsi.reap.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 对象属性名到其值的映射工具 可用来比较值发生了变化的属性 */
public class FieldValueUtil {

  /**
   * 根据对象和属性名的集合获取属性集合
   *
   * @param object 待解析的对象
   * @param fieldName 属性名的集合
   * @return 属性集合
   */
  public static Set<Field> getFieldsByField(Object object, Set<String> fieldName) {
    if (object == null || fieldName == null || fieldName.isEmpty()) {
      return new HashSet<>(0);
    }

    Set<Field> fieldsGet = new HashSet<>(fieldName.size());
    Class<?> clazz = object.getClass();
    Field[] declaredFields = clazz.getDeclaredFields();
    for (Field field : declaredFields) {
      if (fieldName.contains(field.getName())) {
        fieldsGet.add(field);
      }
    }
    return fieldsGet;
  }

  /**
   * 根据属性的名称或者别名的名称获取属性的值
   *
   * @param object 对象
   * @param fieldName 属性名
   * @return 该属性的值
   */
  public static Object getValueByField(Object object, String fieldName)
      throws IllegalAccessException {
    Class<?> clazz = object.getClass();
    Field[] declaredFields = clazz.getDeclaredFields();

    Field fieldResolve = null;
    for (Field field : declaredFields) {
      // 属性名相同
      if (field.getName().equals(fieldName)) {
        fieldResolve = field;
        break;
      }
    }
    if (fieldResolve != null) {
      fieldResolve.setAccessible(true);
      return fieldResolve.get(object);
    }
    return null;
  }

  /**
   * 获取两个对象属性的值不同的所有属性名称
   *
   * @param object1 第一个对象
   * @param object2 第二个对象
   * @param onlyCompareCommonFields 只比较相同属性
   * @return 属性的值不同的所有属性名称
   */
  public static Set<String> getDifferentValueField(
      Object object1, Object object2, boolean resolveAllField, boolean onlyCompareCommonFields)
      throws IllegalAccessException {

    Map<String, Object> fieldValuePair1 = getFieldValuePair(object1, resolveAllField);
    Set<String> keySet1 = fieldValuePair1.keySet();
    Map<String, Object> fieldValuePair2 = getFieldValuePair(object2, resolveAllField);
    Set<String> keySet2 = fieldValuePair2.keySet();

    if (keySet1.isEmpty()) {
      return keySet2;
    }

    if (keySet2.isEmpty()) {
      return keySet1;
    }

    Set<String> fieldsWithDifferentValue = new HashSet<>();

    // 只比较公共属性
    for (Map.Entry<String, Object> entry : fieldValuePair1.entrySet()) {
      String fieldName = entry.getKey();
      Object value1 = entry.getValue();

      Object value2 = fieldValuePair2.get(fieldName);

      boolean sameHashCode;
      boolean sameObject;
      if ("chw".equals(fieldName)) {
        Map<String, Object> chwField1 = getFieldValuePair(value1, true);
        Map<String, Object> chwField2 = getFieldValuePair(value2, true);
        sameHashCode = chwField1.get("id") == chwField2.get("id");
        sameObject = chwField1.get("id") == chwField2.get("id");
      } else if (value1 == null && value2 == null) {
        sameHashCode = true;
        sameObject = true;
      } else if (value1 == null || value2 == null) {
        sameHashCode = false;
        sameObject = false;
      } else {
        sameHashCode = (value1.hashCode() == value2.hashCode());
        sameObject = value1.equals(value2);
      }
      if (!(sameHashCode && sameObject)) {
        fieldsWithDifferentValue.add(fieldName);
      }
    }

    // 不相同的fields
    if (!onlyCompareCommonFields) {
      Set<String> keySet1Copy = new HashSet<>(keySet1);
      Set<String> keySet2Copy = new HashSet<>(keySet2);
      keySet1.removeAll(keySet2);
      keySet2Copy.removeAll(keySet1Copy);

      fieldsWithDifferentValue.addAll(keySet1);
      fieldsWithDifferentValue.addAll(keySet2Copy);
    }
    return fieldsWithDifferentValue;
  }

  /**
   * 获取属性及其对应值的hash值（可能有hash冲突）
   *
   * @param resolveAllField 解析所有属性
   * @return 属性--> 值hash
   */
  public static <T> Map<String, Integer> getFieldHashPair(T object, boolean resolveAllField)
      throws IllegalAccessException {

    if (object == null) {
      return new HashMap<>(0);
    }

    Map<String, Object> fieldValuePair = getFieldValuePair(object, resolveAllField);
    Map<String, Integer> fieldHashPairMap = new HashMap<>(fieldValuePair.size());

    fieldValuePair.forEach((key, value) -> fieldHashPairMap.put(key, value.hashCode()));
    return fieldHashPairMap;
  }

  /**
   * 获取属性及其对应值的映射
   *
   * @param resolveAllField 解析所有属性
   * @return 属性--> 值
   */
  public static <T> Map<String, Object> getFieldValuePair(T object, boolean resolveAllField)
      throws IllegalAccessException {

    if (object == null) {
      return new HashMap<>(0);
    }

    Class<?> clazz = object.getClass();
    Field[] declaredFields = clazz.getDeclaredFields();
    Map<String, Object> fieldHashMap = new HashMap<>(declaredFields.length);
    for (Field field : declaredFields) {
      field.setAccessible(true);
      String key = field.getName();

      // 解析所有属性
      if (resolveAllField) {
        fieldHashMap.put(key, field.get(object));
      }
    }
    return fieldHashMap;
  }
}
