# 加密

+ 不可逆加密 
    + MD5
    + SHA256
+ 可逆加密
    + 对称加密
        + DES
        + AES
    + 非对称加密
        + RSA

## MD5

```java
public class MD5 {
    private static final String MD5_ALGORITHM = "MD5";

    public static String encrypt(String data) throws Exception {
        // 获取MD5算法实例
        MessageDigest messageDigest = MessageDigest.getInstance(MD5_ALGORITHM);
        // 计算散列值
        byte[] digest = messageDigest.digest(data.getBytes());
        Formatter formatter = new Formatter();
        // 补齐前导0，并格式化
        for (byte b : digest) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static void main(String[] args) throws Exception {
        String data = "Hello World";
        String encryptedData = encrypt(data);
        System.out.println("加密后的数据：" + encryptedData);
    }
}
```

## SHA256

```java
public class SHA256 {
    private static final String SHA_256_ALGORITHM = "SHA-256";

    public static String encrypt(String data) throws Exception {
        //获取SHA-256算法实例
        MessageDigest messageDigest = MessageDigest.getInstance(SHA_256_ALGORITHM);
        //计算散列值
        byte[] digest = messageDigest.digest(data.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        //将byte数组转换为15进制字符串
        for (byte b : digest) {
            stringBuilder.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws Exception {
        String data = "Hello World";
        String encryptedData = encrypt(data);
        System.out.println("加密后的数据：" + encryptedData);
    }
}
```

## DES

```java
public class DES {

    private static final String DES_ALGORITHM = "DES";

    /**
     * DES加密
     */
    public static String encrypt(String data, String key) throws Exception {
        // 根据密钥生成密钥规范
        KeySpec keySpec = new DESKeySpec(key.getBytes());
        // 根据密钥规范生成密钥工厂
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(DES_ALGORITHM);
        // 根据密钥工厂和密钥规范生成密钥
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        // 根据加密算法获取加密器
        Cipher cipher = Cipher.getInstance(DES_ALGORITHM);
        // 初始化加密器，设置加密模式和密钥
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        // 加密数据
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        // 对加密后的数据进行Base64编码
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * DES解密
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        // 根据密钥生成密钥规范
        KeySpec keySpec = new DESKeySpec(key.getBytes());
        // 根据密钥规范生成密钥工厂
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(DES_ALGORITHM);
        // 根据密钥工厂和密钥规范生成密钥
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        // 对加密后的数据进行Base64解码
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        // 根据加密算法获取解密器
        Cipher cipher = Cipher.getInstance(DES_ALGORITHM);
        // 初始化解密器，设置解密模式和密钥
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        // 解密数据
        byte[] decryptedData = cipher.doFinal(decodedData);
        // 将解密后的数据转换为字符串
        return new String(decryptedData);
    }

    public static void main(String[] args) throws Exception {
        String data = "Hello World";
        String key = "12345678";

        String encryptedData = encrypt(data, key);
        System.out.println("加密后的数据：" + encryptedData);

        String decryptedData = decrypt(encryptedData, key);
        System.out.println("解密后的数据：" + decryptedData);
    }
}
```

## AES

```java
public class AES {

    private static final String AES_ALGORITHM = "AES";
    // AES加密模式为CBC，填充方式为PKCS5Padding
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    // AES密钥为16位
    private static final String AES_KEY = "1234567890123456";
    // AES初始化向量为16位
    private static final String AES_IV = "abcdefghijklmnop";

    /**
     * AES加密
     */
    public static String encrypt(String data) throws Exception {
        // 将AES密钥转换为SecretKeySpec对象
        SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), AES_ALGORITHM);
        // 将AES初始化向量转换为IvParameterSpec对象
        IvParameterSpec ivParameterSpec = new IvParameterSpec(AES_IV.getBytes());
        // 根据加密算法获取加密器
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        // 初始化加密器，设置加密模式、密钥和初始化向量
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        // 加密数据
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        // 对加密后的数据使用Base64编码
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * AES解密
     */
    public static String decrypt(String encryptedData) throws Exception {
        // 将AES密钥转换为SecretKeySpec对象
        SecretKeySpec secretKeySpec = new SecretKeySpec(AES_KEY.getBytes(), AES_ALGORITHM);
        // 将AES初始化向量转换为IvParameterSpec对象
        IvParameterSpec ivParameterSpec = new IvParameterSpec(AES_IV.getBytes());
        // 根据加密算法获取解密器
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        // 初始化解密器，设置解密模式、密钥和初始化向量
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        // 对加密后的数据使用Base64解码
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        // 解密数据
        byte[] decryptedData = cipher.doFinal(decodedData);
        // 返回解密后的数据
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        String data = "Hello World";

        String encryptedData = encrypt(data);
        System.out.println("加密后的数据：" + encryptedData);

        String decryptedData = decrypt(encryptedData);
        System.out.println("解密后的数据：" + decryptedData);
    }
}
```

## RSA

```java
public class RSA {
    private static final String RSA_ALGORITHM = "RSA";

    /**
     * 生成RSA密钥对
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(2048); // 密钥大小为2048位
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 使用公钥加密数据
     */
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 使用私钥解密数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String data = "Hello World";

        String encryptedData = encrypt(data, publicKey);
        System.out.println("加密后的数据：" + encryptedData);

        String decryptedData = decrypt(encryptedData, privateKey);
        System.out.println("解密后的数据：" + decryptedData);
    }
}
```
