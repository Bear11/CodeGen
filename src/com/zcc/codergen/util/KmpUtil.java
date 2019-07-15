package com.zcc.codergen.util;

public class KmpUtil {
    /**
     * param search 查找的字符串
     * param buf 被查找的字符串
     */
    public static int search(String search, String buf) {

        int[] next = new int[search.length()];
        kmpNext(search, next);
        int i = 0;
        int j = 0;
        while (i < search.length() && j < buf.length()) {

            if (i == -1 || search.charAt(i) == buf.charAt(j)) {
                i++;
                j++;

            } else {
                i = next[i];
            }
        }
        if (i == search.length()) {
            return j - i;
        } else {
            return -1;
        }

    }

    public static void kmpNext(String buf, int next[]) {
        int m = 0;
        int n = -1;
        next[0] = -1;
        while (m < buf.length() - 1) {

            if (n == -1 || buf.charAt(m) == buf.charAt(n)) {
                m++;
                n++;
                next[m] = n;

            } else {

                n = next[n];//如果不匹配，从不断缩小前缀范围进行匹配
            }
        }
    }
}
