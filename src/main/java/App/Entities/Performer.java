package App.Entities;

import java.util.TreeMap;

public class Performer {
    private String name;
    private TreeMap<String, Album> albums;

    public Performer(){}
    public Performer(String name){
        this.name = name;
        this.albums = new TreeMap<>();
    }
    public Performer(String name, Song song){
        this.name = name;
        this.albums = new TreeMap<>();
        addSong(song);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<String, Album> getAlbums() {
        return albums;
    }

    public void setAlbums(TreeMap<String, Album> albums) {
        this.albums = albums;
    }

    public void addSong(Song song){
        String nameAlbum = song.getNameAlbum();
        if(nameAlbum!=null&&albums.get(nameAlbum)!=null)
            albums.get(nameAlbum).addSong(song);
        else {
            albums.put(nameAlbum,new Album(song));
        }
    }

    public String describe(){
        String string = "Name: "+this.name+"\n";
        for(Album album:albums.values())
            string+=album.describe();
        return string;
    }
}

