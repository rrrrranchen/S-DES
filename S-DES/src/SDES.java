import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

public class SDES {
    // 密钥扩展置换
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    private static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    // 初始置换盒
    private static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    // 最终置换盒
    private static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};

    // S盒
    private static final int[][] SBOX1 = {
            {1, 0, 3, 2},
            {3, 2, 1, 0},
            {0, 2, 1, 3},
            {3, 1, 0, 2}
    };
    private static final int[][] SBOX2 = {
            {0, 1, 2, 3},
            {2, 3, 1, 0},
            {3, 0, 1, 2},
            {2, 1, 0, 3}
    };

    // 8位到4位的置换盒
    private static final int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};

    // 轮函数的置换盒
    private static final int[] P_BOX = {2, 4, 3, 1};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("choose: ");
        String style = scanner.nextLine();
        //普通二进制加解密
        if(Objects.equals(style, "1")){
            System.out.print("Enter 8-bit plaintext (1s and 0s): ");
            String plaintext = scanner.nextLine();

            System.out.print("Enter 10-bit key (1s and 0s): ");
            String key = scanner.nextLine();
            String encrypted = encrypt(plaintext, key);
            String decrypted = decrypt(encrypted, key);

            System.out.println("Plaintext: " + plaintext);
            System.out.println("Encrypted: " + encrypted);
            System.out.println("Decrypted: " + decrypted);
            scanner.close();
        }
        //字符串加解密
        else if(Objects.equals(style, "2")){
            System.out.print("输入明文: ");
            String plaintext = scanner.nextLine();
            System.out.print("Enter 10-bit key (1s and 0s): ");
            String key = scanner.nextLine();
            String []encrypted;
            String []decrypted = new String[plaintext.length()];
            encrypted=charToBinaryStringArray(plaintext);
            for(int i=0;i<plaintext.length();i++){
                encrypted[i]=encrypt(encrypted[i],key);
                decrypted[i]=decrypt(encrypted[i],key);
            }
            String encrypted1=binaryStringArrayToString(encrypted);
            String decrypted1=binaryStringArrayToString(decrypted);
            System.out.println("Plaintext: " + plaintext);
            System.out.println("Encrypted: " + encrypted1);
            System.out.println("Decrypted: " + decrypted1);
        }
        //暴力破解
        else{
            int x=0;
            String []r1=new String[100];
            String []r2=new String[100];
            while(true){
            System.out.print("输入密文: ");
                String ciphertext = scanner.nextLine();
            if(Objects.equals(ciphertext, "-1")){
                break;
            }
            r1[x]=ciphertext;
            System.out.print("输入明文: ");
            String plaintext = scanner.nextLine();
            r2[x]=plaintext;
            x++;

            }
            String [] Ciphertext =new String[x];
            String [] Plaintext =new String[x];
            for(int i=0;i<x;i++){
                Ciphertext[i]=r1[i];
                Plaintext[i]=r2[i];
            }
            //计算时间功能
            Instant start = Instant.now();
            String []key= bruteForceDecrypt(Ciphertext, Plaintext);
            Instant end = Instant.now();
            System.out.println("Execution time: " + Duration.between(start, end).toMillis() + " milliseconds.");
            for (String s : key) {
                if (s != null) {
                    System.out.println("密钥： " + s);
                }
            }
        }
    }


    public static String[] bruteForceDecrypt(String[] ciphertext, String[] plaintext) {
        String[] K = new String[1000];
        List<String> foundKeys = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        for (int y = 0; y < ciphertext.length; y++) {
            final int index = y; // final or effectively final variable for the lambda expression

            Future<Void> future = executor.submit(() -> {
                for (int i = 0; i < (1 << 10); i++) { // 10-bit key
                    String key = Integer.toBinaryString(i);
                    if (key.length() < 10) {
                        key = String.format("%10s", key).replace(' ', '0');
                    }
                    String decrypted = decrypt(ciphertext[index], key);

                    if (decrypted.equals(plaintext[index])) {
                        synchronized (foundKeys) {
                            foundKeys.add(key);
                        }
                    }
                }
                return null; // Callable must return something
            });

            futures.add(future);
        }

        // 等待所有任务完成
        for (Future<Void> future : futures) {
            try {
                future.get(); // 获取结果，确保任务完成
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常
            }
        }

        // 将找到的密钥转换为数组
        String[] Key = processStrings(foundKeys.toArray(new String[0]), ciphertext.length);

        executor.shutdown(); // 关闭线程池
        return Key;
    }
    //加密算法函数
    public static String encrypt(String plaintext, String key) {
        // 将明文和密钥转换为整数数组
        int[] ipResult = initialPermutation(binaryStringToIntArray(plaintext), IP);
        int[] left = new int[4];
        int[] right = new int[4];

        // 将结果分成左右两半
        System.arraycopy(ipResult, 0, left, 0, 4);
        System.arraycopy(ipResult, 4, right, 0, 4);

        int[] k = binaryStringToIntArray(key);
        System.out.println("k: " + Arrays.toString(k));
        // 确保密钥有效
        if (k.length != 10) {
            throw new IllegalArgumentException("密钥必须为 10 位长。");
        }

        k = permute(k, P10);
        int[] k1 = keyGeneration(k, 1);
        int[] k2 = keyGeneration(k1, 2);

        k1 = permute(k1, P8);
        k2 = permute(k2, P8);

        // 处理第一轮
        int[] F1 = f(right, k1);
        int[] R1 = xor(left, F1);

        // 准备第二轮
        int[] L2 = right;
        int[] R2 = R1;

        // 处理第二轮
        int[] F2 = f(R1, k2);
        R2 = xor(L2, F2);

        // 合并结果
        int[] miwen = new int[8];
        System.arraycopy(R2, 0, miwen, 0, 4);
        System.arraycopy(R1, 0, miwen, 4, 4); // 注意：这里使用的是 R1 而不是 L2

        // 最终置换
        miwen = permute(miwen, IP_INV);
        return intArrayToString(miwen);
    }

    //解密算法函数
    public static String decrypt(String ciphertext, String key) {

        // 将明文和密钥转换为整数数组
        int[] ipResult = initialPermutation(binaryStringToIntArray(ciphertext), IP);
        int[] left = new int[4];
        int[] right = new int[4];

        // 将结果分成左右两半
        System.arraycopy(ipResult, 0, left, 0, 4);
        System.arraycopy(ipResult, 4, right, 0, 4);

        int[] k = binaryStringToIntArray(key);
        System.out.println("k: " + Arrays.toString(k));
        // 确保密钥有效
        if (k.length != 10) {
            throw new IllegalArgumentException("密钥必须为 10 位长。");
        }

        k = permute(k, P10);
        int[] k1 = keyGeneration(k, 1);
        int[] k2 = keyGeneration(k1, 2);


        k1 = permute(k1, P8);
        k2 = permute(k2, P8);

        // 处理第一轮
        int[] F1 = f(right, k2);
        int[] R1 = xor(left, F1);

        // 准备第二轮
        int[] L2 = right;
        int[] R2 = R1;

        // 处理第二轮
        int[] F2 = f(R1, k1);
        R2 = xor(L2, F2);

        // 合并结果
        int[] miwen = new int[8];
        System.arraycopy(R2, 0, miwen, 0, 4);
        System.arraycopy(R1, 0, miwen, 4, 4); // 注意：这里使用的是 R1 而不是 L2

        // 最终置换
        miwen = permute(miwen, IP_INV);
        return intArrayToString(miwen);
    }
    //初始置换
    private static int[] initialPermutation(int[] data, int[] permute) {
        int[] result = new int[8];
        for (int i = 0; i < permute.length; i++) {
            result[i] = data[permute[i] - 1];
        }
        return result;
    }


    //左移函数
    private static int[] leftShift(int[] data, int shift) {
        int[] result = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[(i + shift) % data.length];
        }
        return result;
    }

    //f函数
    private static int[] f(int[] data, int[] subkey) {
        int[] epResult = permute(data, EP);
        int[] xorResult = xor(epResult, subkey);
        int midPoint = xorResult.length / 2;
        int[] firstHalf = new int[midPoint];
        int[] secondHalf = new int[midPoint];
        System.arraycopy(xorResult, 0, firstHalf, 0, midPoint);
        System.arraycopy(xorResult, midPoint, secondHalf, 0, midPoint);
        int[] result1;
        result1= sbox(firstHalf,SBOX1);
        System.out.println("sbox1: " + Arrays.toString(result1));
        int[] result2;
        result2= sbox(secondHalf,SBOX2);
        System.out.println("sbox2: " + Arrays.toString(result2));
        int[] result=new int[4];
        result[0]=result1[0];
        result[1]=result1[1];
        result[2]=result2[0];
        result[3]=result2[1];
        result = permute(result,P_BOX);
        return result;
    }

    //从sbox表中找出对应的置换数
    private static int[] sbox(int[] arr, int[][] sbox) {
        int first = 0, second = 0; // first表示横坐标，second表示纵坐标
        int size = arr.length;

        int num1 = arr[0];     // 0
        int num2 = arr[3];

        if (num1 == 0 && num2 == 0)
            first = 0;
        else if (num1 == 0 && num2 == 1)
            first = 1;
        else if (num1 == 1 && num2 == 0)
            first = 2;
        else if (num1 == 1 && num2 == 1)
            first = 3;

        int num3 = arr[1];     // 1
        int num4 = arr[2];

        if (num3 == 0 && num4 == 0)
            second = 0;
        else if (num3 == 0 && num4 == 1)
            second = 1;
        else if (num3 == 1 && num4 == 0)
            second = 2;
        else if (num3 == 1 && num4 == 1)
            second = 3;

        int num = sbox[first][second]; // 利用横纵坐标在置换表中确定置换后的十进制整数
        int[] output = new int[2];
        output[0] = (num & 2) >> 1; // 提取二进制的第一位
        output[1] = num & 1; // 提取二进制的第二位
        return output;
    }

    //异或操作
    private static int[] xor(int[] data1, int[] data2) {
        int[] result = new int[data1.length];
        for (int i = 0; i < data1.length; i++) {
            result[i] = data1[i] ^ data2[i];
        }
        return result;
    }

    //根据轮次得出子密钥
    private static int[] keyGeneration(int[] key, int round) {


        // 创建左右部分
        int[] left = new int[5];
        int[] right = new int[5];
        System.arraycopy(key, 0, left, 0, 5);
        System.arraycopy(key, 5, right, 0, 5);

        // 根据轮次进行左移
        left = leftShift(left, round == 1 ? 1 : 2);
        right = leftShift(right, round == 1 ? 1 : 2);

        // 将两个部分合并
        int[] shiftedKey = new int[10];
        System.arraycopy(left, 0, shiftedKey, 0, 5);
        System.arraycopy(right, 0, shiftedKey, 5, 5);
        return shiftedKey;
    }


    //置换函数
    private static int[] permute(int[] data, int[] permute) {
        int[] result = new int[permute.length];
        for (int i = 0; i < permute.length; i++) {
            result[i] = data[permute[i] - 1];
        }
        System.out.println("k: " + Arrays.toString(result));
        return result;
    }

    //将字符串转化为整数数组
    private static int[] binaryStringToIntArray(String binaryString) {
        int[] result = new int[binaryString.length()];
        for (int i = 0; i < binaryString.length(); i++) {
            result[i] = binaryString.charAt(i) - '0';
        }
        return result;
    }

    //将整数数组转化成字符串
    public static String intArrayToString(int[] array) {
        if (array == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(""); // 在元素之间不添加任何字符
            }
        }
        return sb.toString();
    }

    //将字符串转化为二进制八位字符串组
    public static String[] charToBinaryStringArray(String input) {
        String[] binaryStrings = new String[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int decimalValue = (int) c;
            StringBuilder binaryBuilder = new StringBuilder();
            // 转换为8位二进制并填充到字符串构建器中
            for (int j = 7; j >= 0; j--) {
                binaryBuilder.append((decimalValue >> j) & 1);
            }
            binaryStrings[i] = binaryBuilder.toString();
        }
        return binaryStrings;
    }

    //将二进制八位字符串组转化为字符串
    public static String binaryStringArrayToString(String[] binaryStrings) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String binaryString : binaryStrings) {
            // 将8位二进制字符串转换为整数
            int decimalValue = binaryStringToDecimal(binaryString);
            // 将整数转换为字符并添加到字符串构建器中
            stringBuilder.append((char) decimalValue);
        }

        return stringBuilder.toString();
    }

    //将字符串转化为整数
    public static int binaryStringToDecimal(String binaryString) {
        int decimalValue = 0;
        for (int i = 0; i < binaryString.length(); i++) {
            int bitValue = binaryString.charAt(i) - '0';
            decimalValue = decimalValue * 2 + bitValue;
        }
        return decimalValue;
    }

    //删除字符串组中出现次数少于y的字符串，并去重
    public static String[] processStrings(String[] input, int y) {
        // 使用HashMap统计每个字符串的出现次数
        HashMap<String, Integer> frequencyMap = new HashMap<>();

        for (String str : input) {
            if (str != null) {
                frequencyMap.put(str, frequencyMap.getOrDefault(str, 0) + 1);
            }
        }

        // 使用HashSet存储满足条件的字符串
        HashSet<String> resultSet = new HashSet<>();

        for (String str : input) {
            if (frequencyMap.getOrDefault(str, 0) >= y) {
                resultSet.add(str);
            }
        }

        // 创建最终的结果数组
        String[] result = new String[resultSet.size()];
        int index = 0;

        for (String str : resultSet) {
            result[index++] = str;
        }

        return result;
    }
}

