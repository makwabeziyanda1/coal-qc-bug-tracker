package com.coalqc.service;

public class DefectNotFoundException extends RuntimeException {

    public DefectNotFoundException(int id) {
        super("No defect found with id " + id);
    }
}
