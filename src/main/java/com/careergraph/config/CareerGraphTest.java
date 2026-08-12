package com.careergraph.config;

import java.util.List;

import com.careergraph.service.CareerGraphService;

public class CareerGraphTest {
	public static void main(String[] args) {
		CareerGraphService service = new CareerGraphService();

        List<String> skills = List.of("Java", "SQL");

        List<String> jobs = service.findJobsBySkills(skills);

        System.out.println("Matching Jobs:");

        for (String job : jobs) {
            System.out.println(job);
        }
	}
}
