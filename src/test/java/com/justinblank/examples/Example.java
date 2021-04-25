package com.justinblank.examples;

public class Example {

    // There is no complex theory, I just threw out a bunch of booleans
    public static boolean exampleTest(boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean g, boolean h) {
        if (a) {
            if (b && c) {
                if (!d) {
                    if (e || f) {
                        if (g) {
                            if (h) {
                                return true;
                            }
                        }
                    }
                }
                if (g && h) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean findMediumInts(int i, int j, int k, int l, int m) {
        // It doesn't matter now, but if we ever bias the generator to prefer small numbers, requiring values not be
        // too small keeps us honest
        if (1024 < i && i < 64 * 1024) { //
            if (1024 < j && j < 64 * 1024) {
                if (1024 < k && k < 64 * 1024) {
                    if (1024 < l && l < 64 * 1024) {
                        if (1024 < m && m < 64 * 1024) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
