package edu.drexel.se577.grouptwo.viz.filetypes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CSVInputHandlerTest {
    public static void main(String[] args) {
        try {
            Path path = Paths.get(
                    ".\\Upload_Test_Files\\test.csv");
            byte[] data = Files.readAllBytes(path);
            new CSVInputHandler().parseFile("test.csv", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
