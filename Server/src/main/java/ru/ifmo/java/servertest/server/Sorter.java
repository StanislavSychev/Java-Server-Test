package ru.ifmo.java.servertest.server;

import java.util.ArrayList;
import java.util.List;

public class Sorter {

    public static List<Integer> sort(List<Integer> list) {
        List<Integer> res = new ArrayList<>(list);
        int n = res.size();
        boolean swapped = true;
        while (swapped) {
            swapped = false;
            for (int i = 1; i < n; i++) {
                Integer a = res.get(i);
                Integer b = res.get(i - 1);
                if (a > b) {
                    res.set(i, b);
                    res.set(i - 1 ,a);
                    swapped = true;
                }
            }
            n--;
        }
        return res;
    }
}
