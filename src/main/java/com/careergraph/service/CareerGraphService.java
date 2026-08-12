package com.careergraph.service;

import java.util.List;

import com.careergraph.dao.CareerGraphDAO;

public class CareerGraphService {

    private final CareerGraphDAO dao;

    public CareerGraphService() {
        this.dao = new CareerGraphDAO();
    }
    
    public void seedData() {
        dao.seedData();
    }

    public List<String> findJobsBySkills(List<String> skills) {
        return dao.findJobsBySkills(skills);
    }
}