package org.example.rpc.server.common;

import com.alibaba.dubbo.common.utils.ReflectUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Convert {


    private static String DATE_FORMAT;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object compatibleTypeConvert(Object value, Class<?> type) {
        if(value == null || type == null || type.isAssignableFrom(value.getClass())) {
            return value;
        }
        if(value instanceof String) {
            String string = (String) value;
            if (char.class.equals(type) || Character.class.equals(type)) {
                if(string.length() != 1) {
                    throw new IllegalArgumentException(String.format("CAN NOT convert String(%s) to char!" +
                            " when convert String to char, the String MUST only 1 char.", string));
                }
                return string.charAt(0);
            } else if(type.isEnum()) {
                return Enum.valueOf((Class<Enum>)type, string);
            } else if(type == BigInteger.class) {
                return new BigInteger(string);
            } else if(type == BigDecimal.class) {
                return new BigDecimal(string);
            } else if(type == Short.class || type == short.class) {
                return new Short(string);
            } else if(type == Integer.class || type == int.class) {
                return new Integer(string);
            } else if(type == Long.class || type == long.class) {
                return new Long(string);
            } else if(type == Double.class || type == double.class) {
                return new Double(string);
            } else if(type == Float.class || type == float.class) {
                return new Float(string);
            }  else if(type == Byte.class || type == byte.class) {
                return new Byte(string);
            } else if(type == Boolean.class || type == boolean.class) {
                return new Boolean(string);
            } else if(type == Date.class) {
                try {
                    return new SimpleDateFormat(DATE_FORMAT).parse((String) value);
                } catch (ParseException e) {
                    throw new IllegalStateException("Failed to parse date " + value + " by format " + DATE_FORMAT + ", cause: " + e.getMessage(), e);
                }
            } else if (type == Class.class) {
                try {
                    return ReflectUtils.name2class((String)value);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return value;
    }


}
