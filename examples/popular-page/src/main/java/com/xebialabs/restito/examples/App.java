package com.xebialabs.restito.examples;

public class App {

    public static void main(String[] args) throws Exception {

        PageRevision revision = new WikiClient("http://en.wikipedia.org")
                .getMostRecentRevision("The X-Files", "Star Trek", "The Simpsons", "Game of Thrones");

        System.out.println("The most recent change was made to '" + revision.name +
                "' by " + revision.author + " at " + revision.date);

    }

}
