package com.coalqc.api;

import com.coalqc.service.DefectTracker;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        createApp(new DefectTracker()).start(7000);
    }

    public static Javalin createApp(DefectTracker tracker) {
        return Javalin.create();
    }
}
