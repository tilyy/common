package com.tyq.common.desede;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.tyq.common.uitl.GsonUtil;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 对接 接口参数 加密解密工具类
 *
 * @author tyq
 *
 * <p>
 * //Base64.DEFAULT:这个参数是默认，使用默认的方法来加密,转换后的字符串带有换行符
 * //Base64.NO_PADDING:这个参数是略去加密字符串最后的"="
 * //Base64.NO_WRAP:表示转换后的字符串去掉所有的换行符（设置后CRLF就没用了）
 * //Base64.CRLF:就是Win风格的换行符,意思就是使用CRLF这一对作为一行的结尾而不是Unix风格的LF,表明转换后的字符串是带有换行符的
 * //Base64.URL_SAFE:加密时不使用对URL和文件名有特殊意义的字符来作为加密字符，具体就是以-和_取代+和/
 * //Base64.NO_CLOSE:告诉Base64OutputStream它不应关闭它正在包装的输出流当它本身是封闭的。
 */
public abstract class DesEdeUtil {

    private static final String TAG = "DesEdeUtil";

    private static final int ENCRYPT_MODE = Cipher.ENCRYPT_MODE;
    private static final int DECRYPT_MODE = Cipher.DECRYPT_MODE;
    private static final String ALGORITHM = "DESede";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final static String APP_ID = "taxiG";
    private final static String KEY = "dGF4aV9nMjAyMzAxMDlkeXNnN2oxNmNn";

    private static Cipher encryptCipher;
    private static Cipher decryptCipher;

    static {
        config(KEY);
    }

    @SuppressLint({"NewApi"})
    private static void config(String key) {
        try {
            encryptCipher = Cipher.getInstance(ALGORITHM);
            decryptCipher = Cipher.getInstance(ALGORITHM);

            byte[] material = Arrays.copyOf(Base64.decode(key.getBytes(UTF8), Base64.NO_WRAP), 24);

            DESedeKeySpec keySpec = new DESedeKeySpec(material);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            Key secKey = keyFactory.generateSecret(keySpec);
            if (secKey == null) {
                throw new NullPointerException("secKey is null");
            }

            encryptCipher.init(ENCRYPT_MODE, secKey, new SecureRandom());
            decryptCipher.init(DECRYPT_MODE, secKey, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            Timber.tag(TAG).e(e);
        } catch (NoSuchPaddingException e) {
            Timber.tag(TAG).e(e);
        } catch (InvalidKeySpecException e) {
            Timber.tag(TAG).e(e);
        } catch (InvalidKeyException e) {
            Timber.tag(TAG).e(e);
        } catch (NullPointerException e) {
            Timber.tag(TAG).e(e);
        }
    }

    /**
     * 加密方法
     *
     * @param data 需要加密的数据
     * @return 加密后的值
     */
    @SuppressLint("NewApi")
    public synchronized static String encrypt(byte[] data) {
        if (encryptCipher == null) {
            config(KEY);
        }
        if (data == null) {
            return null;
        }
        byte[] encData = null;
        try {
            encData = encryptCipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (encData == null) {
            return null;
        }
        return new String(Base64.encode(encData, Base64.NO_WRAP), UTF8);
    }

    /**
     * 加密方法
     *
     * @param data 需要加密的数据
     * @return 加密后的值
     */
    public static String encrypt(String data) {
        return encrypt(data.getBytes(UTF8));
    }

    /**
     * 解密方法
     *
     * @param data 需要解密的数据
     * @return 解密后的值
     */
    public static String decrypt(String data) {
        return decrypt(data.getBytes(UTF8));
    }

    /**
     * 解密方法
     *
     * @param data 需要解密的数据
     * @return 解密后的值
     */
    @SuppressLint("NewApi")
    public synchronized static String decrypt(byte[] data) throws RuntimeException {
        if (decryptCipher == null) {
            config(KEY);
        }
        if (data == null || data.length == 0) {
            return null;
        }
        byte[] decData = null;
        try {
            decData = decryptCipher.doFinal(Base64.decode(data, Base64.NO_WRAP));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (decData == null || decData.length == 0) {
            return null;
        }
        return new String(decData, UTF8);
    }

    /**
     * 设置url
     *
     * @param url url
     * @return url
     */
    public static String encryptUrl(String url) {
        return url + "?app_id=" + APP_ID;
    }

    /**
     * 生成加密参数body对象
     *
     * @param paraMap paraMap
     * @return RequestBody
     */
    public synchronized static RequestBody createRequestBody(Map<String, Object> paraMap) {
        paraMap.put("timeStamp", System.currentTimeMillis());

        String jsonStr = DesEdeUtil.encrypt(GsonUtil.toJson(paraMap));

        return RequestBody.create(MediaType.parse("text/plain"), jsonStr);
    }
}