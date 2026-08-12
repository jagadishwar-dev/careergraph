package com.careergraph.config;

import com.careergraph.dao.CareerGraphDAO;

public class SeedData {
	public static void main(String[] args) {

        CareerGraphDAO dao = new CareerGraphDAO();

        dao.seedData();

        System.out.println("Seed data completed.");
    }
}
