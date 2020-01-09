package ru.ifmo.java.servertest.server;

import java.util.ArrayList;
import java.util.List;

public class Sorter {

    public static List<Integer> sort(List<Integer> list) {
        List<Integer> res = new ArrayList<>(list);
        for (int i = 0; i < res.size(); i++) {
            for (int j = 1; j < res.size() - i; j++) {
                Integer a = res.get(i);
                Integer b = res.get(j);
                if (a > b) {
                    res.set(i, b);
                    res.set(j ,a);
                }
            }
        }
        return res;
    }
}
