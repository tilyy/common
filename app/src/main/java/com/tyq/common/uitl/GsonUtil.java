package com.tyq.common.uitl;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import timber.log.Timber;

/**
 * @author zhengyongfa
 * @date 2021/11/25 15:16
 * @description 简述类的作用
 */
public abstract class GsonUtil {

    public static Gson mGson;

    static {
        config();
    }

    /**
     *
     */
    private static void config() {
        mGson = new GsonBuilder()
                .setLenient()// json宽松
                .enableComplexMapKeySerialization()//支持Map的key为复杂对象的形式
                .serializeNulls()//智能null
//                .setPrettyPrinting()// 格式化json
                .disableHtmlEscaping() //默认是GSON把HTML 转义的
                .create();
    }

    /**
     * @param <T>
     * @param jsonStr json字符串
     * @param cls     需要转换成的类
     * @return
     * @Title: getServerBean
     * @Description: 将一个json字符串转换成对象
     */
    public static <T> T fromJson(String jsonStr, @NonNull Class<T> cls) {
        if (mGson == null) {
            config();
        }
        T obj = null;
        try {
            if (jsonStr == null || jsonStr.isEmpty()) {
                return null;
            }
            obj = mGson.fromJson(jsonStr, cls);
        } catch (Exception e) {
            Timber.tag("GsonUtil").e(e);
        }
        return obj;
    }

    public static <T> T fromJson(String jsonStr, @NonNull TypeToken<T> typeToken) {
        if (mGson == null) {
            config();
        }
        T obj = null;
        try {
            if (jsonStr == null || jsonStr.isEmpty()) {
                return null;
            }
            obj = mGson.fromJson(jsonStr, typeToken.getType());
        } catch (Exception e) {
            Timber.tag("GsonUtil").e(e);
        }
        return obj;
    }

    /**
     * @param <T>
     * @param jsonStr json字符串
     * @return
     * @Title: getServerBean
     * @Description: 将一个json字符串转换成对象
     */
    public static <T> List<T> fromJsonArray(String jsonStr, @NonNull Class<T> cls) {
        if (mGson == null) {
            config();
        }
        List<T> obj = null;
        try {
            if (jsonStr == null || jsonStr.isEmpty()) {
                return null;
            }
            Type type = new TypeToken<List<T>>() {
            }.getType();
            obj = mGson.fromJson(jsonStr, type);
        } catch (Exception e) {
            Timber.tag("GsonUtil").e(e);
        }
        return obj;
    }

    /**
     * @param obj
     * @return
     * @Title: getStringFromJsonObject
     * @Description: 将一个object序列化为json字符串
     */
    public static String toJson(Object obj) {
        if (mGson == null) {
            config();
        }
        String jsonStr = "";
        try {
            if (obj == null) {
                return null;
            }
            jsonStr = mGson.toJson(obj);
        } catch (Exception e) {
            Timber.tag("GsonUtil").e(e);
        }
        return jsonStr;
    }

    /**
     * 将一个string转化成jsonObject对象
     *
     * @param resultStr string值
     * @return jsonObject对象
     */
    public static JsonObject toJsonObject(String resultStr) {
        return JsonParser.parseString(resultStr).getAsJsonObject();
    }
}