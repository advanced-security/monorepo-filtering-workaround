package com.github.advanced_security.monorepo_sample_project4;

public class Project4 {
    public static void main(String[] args) {
        System.out.println("Hello World, from Project 4");

        for (int i = 0; i <= args.length; i++) { // BAD, should be <, not <=
            System.out.println("Argument " + i + ": " + args[i]);
        }
    }
}