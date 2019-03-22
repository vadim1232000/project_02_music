package App;


import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException, InvalidDataException, UnsupportedTagException, IncorrectDirectories {
        String[] path = args;
        Cataloger cataloger = new Cataloger(path);
        cataloger.parseDirectories();
        cataloger.generateHTML(path[0],"index");
        cataloger.generateHashCodeDuplicateList(path[0],"duplicates_1");
        cataloger.generateNameDuplicateList(path[0],"duplicates_2");
    }
}
