package App;


import App.Entities.Album;
import App.Entities.Performer;
import App.Entities.Song;
import com.mpatric.mp3agic.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Cataloger {

    private String[] path;
    private TreeMap<String, Performer> performers;
    private ArrayList<File> files;

    private final String HEAD = "<head>\n" +
            "<meta charset=\"utf-8\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "<title>Catalog</title>\n" +
            "</head>\n";

    private final String HTML = "<html>\n";
    private final String HTML_END = "</html>\n";
    private final String BODY = "<body style=\"font: 12pt sans-serif\">\n";
    private final String BODY_END = "</body>\n";

    private final String UL = "<ul type=\"none\">\n";
    private final String UL_END = "</ul>\n";
    private final String LI = "<li>";
    private final String LI_END = "</li>\n";

    private final String B = "<b>";
    private final String B_END = "</b>\n";

    public Cataloger(String[] path) throws  IncorrectDirectories{
        if(path.length==0)
            throw new IncorrectDirectories("Choose directory correctly");
        for(String p:path){
            if(!(new File(p).isDirectory()))
                throw new IncorrectDirectories(p+"is not directory");
        }
        this.path = path;
        this.performers = new TreeMap<>();
        this.files = new ArrayList<>();
    }

    //достает файлы mp3, считывает их теги, сохряняет в список performers -- исполнителей(сортировка по альбомам уже внутри performers)
    //работает медленно
    public void parseDirectories() throws InvalidDataException, IOException, UnsupportedTagException {
        long startTime = System.currentTimeMillis();
        for (String p : path) {
            File directory = new File(p);
            files.addAll(listFilesForFolder(directory));
        }
        for (File file : files) {
            try {
                Mp3File mp3file = new Mp3File(file);
                String artist;
                String album;
                String title;
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                    artist = (id3v2Tag.getArtist() != null) ? (id3v2Tag.getArtist()) : ("Неизвестный исполнитель");
                    title = (id3v2Tag.getTitle() != null) ? (id3v2Tag.getTitle()) : ("Неизвестное название");
                    album = (id3v2Tag.getAlbum() != null) ? (id3v2Tag.getAlbum()) : ("Неизвестный альбом");
                } else if (mp3file.hasId3v1Tag()) {
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    artist = (id3v1Tag.getArtist() != null) ? (id3v1Tag.getArtist()) : ("Неизвестный исполнитель");
                    title = (id3v1Tag.getTitle() != null) ? (id3v1Tag.getTitle()) : ("Неизвестное название");
                    album = (id3v1Tag.getAlbum() != null) ? (id3v1Tag.getAlbum()) : ("Неизвестный альбом");
                } else {
                    artist = "Неизвестный исполнитель";
                    title = "Неизвестное название";
                    album = "Неизвестный альбом";
                }
                Song song = new Song(title, file.getPath(), album, mp3file.getLengthInSeconds());
                if (performers.get(artist) != null)
                    performers.get(artist).addSong(song);
                else
                    performers.put(artist, new Performer(artist, song));
            } catch (IllegalArgumentException ex) {
                System.err.println("file " + file.getName() + "(" + file.getPath() + ")" + " is incorrect");
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }
        long finishTime = System.currentTimeMillis();
        System.out.println("---------" + (finishTime - startTime) / 1000 + "sec - parseDirectories()" + "---------");
    }

    //задание 1
// просто генерирование html со всеми песнями
    public void generateHTML(String path, String filename) {
        long startTime = System.currentTimeMillis();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path + "/" + filename + ".html", "UTF-8");
            String strHtml = HTML + HEAD + BODY;

            for (Performer performer : performers.values()) {
                strHtml += UL + LI + B + performer.getName() + B_END + UL;
                for (Album album : performer.getAlbums().values()) {
                    strHtml += LI + B + album.getName() + B_END + UL;
                    for (Song song : album.getSongList()) {
                        strHtml += LI + song.getName() + " " + Song.secondsToDuration(song.getDuration())
                                + "(<a href=\"file:///" + song.getPath() + "\">" + song.getPath() + "</a>)" + LI_END;
                    }
                    strHtml += UL_END + LI_END;
                }
                strHtml += UL_END + LI_END + UL_END;
            }
            strHtml += BODY_END + HTML_END;
            writer.println(strHtml);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        long finishTime = System.currentTimeMillis();
        System.out.println("---------" + (finishTime - startTime) / 1000 + "sec - generateHTML()" + "---------");
    }

    //задание 2
    // поиск и запись(мб пока что в html) дубликатов по хеш-коду. капелька колдовства и некравивых имене переменных.
    public void generateHashCodeDuplicateList(String path, String filename) {
        long startTime = System.currentTimeMillis();
        HashMap<File, Long> longHashMap = new HashMap<>();
        for (int i = 0; i < files.size() - 1; i++)
            longHashMap.put(files.get(i), files.get(i).length());
        List<Long> duplicates = longHashMap.values().stream()
                .filter(i -> Collections.frequency(longHashMap.values(), i) > 1)
                .distinct()
                .collect(Collectors.toList());

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path + "/" + filename + ".html", "UTF-8");

            final String[] strHtml = {HTML + HEAD + BODY};
            int k = 1;
            HashMap<File, String> fileStringHashMap = new HashMap<>();
            if (!duplicates.isEmpty()) {
                for (Long duplicate : duplicates) {
                    longHashMap.forEach((i, j) -> {
                        if (j.equals(duplicate)) {
                            try {
                                fileStringHashMap.put(i, DigestUtils.md5Hex(Files.newInputStream(Paths.get(i.getPath()))));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                List<String> duplicates1 = fileStringHashMap.values().stream()
                        .filter(i -> Collections.frequency(fileStringHashMap.values(), i) > 1)
                        .distinct()
                        .collect(Collectors.toList());
                for (String duplicate : duplicates1) {
                    strHtml[0] += UL + LI + B + "Дубликаты-" + (k++) + B_END + UL;
                    fileStringHashMap.forEach((i, j) -> {
                        if (j.equals(duplicate)) {
                            strHtml[0] += LI + "(<a href=\"file:///" + i.getPath() + "\">" + i.getPath() + "</a>)" + LI_END;
                        }
                    });
                    strHtml[0] += UL_END + LI_END + UL_END;
                }
            }
            strHtml[0] += BODY_END + HTML_END;
            writer.println(strHtml[0]);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        long finishTime = System.currentTimeMillis();
        System.out.println("---------" + (finishTime - startTime) / 1000 + "sec - generateHashCodeDuplicateList()" + "---------");
    }

    //задание 3
    // поиск и запись(мб пока что в html) дубликатов по названию, автору и альбому.
    public void generateNameDuplicateList(String path, String filename) {
        long startTime = System.currentTimeMillis();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path + "/" + filename + ".html", "UTF-8");
            String strHtml = HTML + HEAD + BODY + UL;

            for (Performer performer : performers.values()) {
                for (Album album : performer.getAlbums().values()) {
                    ArrayList<String> songsNames = new ArrayList<>();
                    album.getSongList().forEach(e -> songsNames.add(e.getName()));
                    List<Song> songs = album.getSongList().stream()
                            .filter(i -> Collections.frequency(songsNames, i.getName()) > 1)
                            .collect(Collectors.toList());
                    if (!songs.isEmpty()) {
                        strHtml += LI + B + performer.getName() + ", " + album.getName() + ", " + songs.get(0).getName() + B_END + UL;
                        for (Song song : songs) {
                            if (song.getName().equals(songs.get(0).getName()))
                                strHtml += LI + "(<a href=\"file:///" + song.getPath() + "\">" + song.getPath() + "</a>)" + LI_END;
                        }
                        songs.remove(0);
                        strHtml += UL_END + LI_END;
                    }
                }
            }

            strHtml += UL_END + BODY_END + HTML_END;
            writer.println(strHtml);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        long finishTime = System.currentTimeMillis();
        System.out.println("---------" + (finishTime - startTime) / 1000 + "sec - generateNameDuplicateList()" + "---------");
    }
    private ArrayList<File> listFilesForFolder(File folder) {
        ArrayList<File> files = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(listFilesForFolder(fileEntry));
            } else {
                if (fileEntry.getName().endsWith(".mp3"))
                    files.add(fileEntry);
            }
        }
        return files;
    }
}