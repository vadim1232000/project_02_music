package App.Entities;

public class Song {
    private String name;
    private String path;
    private String nameAlbum;
    private long duration;

    public Song(String name, String path, String nameAlbum, long duration){
        this.name = name;
        this.path = path;
        this.nameAlbum = nameAlbum;
        this.duration = duration;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAlbum() {
        return nameAlbum;
    }

    public void setNameAlbum(String nameAlbum) {
        this.nameAlbum = nameAlbum;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
    public String describe(){
        return "\t\tName: "+this.name+"; time: "+ secondsToDuration(this.duration)+"; ("+this.path +")\n";
    }
    public static String secondsToDuration(long seconds) {
        if (seconds % 60 < 10)
            return seconds / 60 + ":0" + seconds % 60;
        else
            return seconds / 60 + ":" + seconds % 60;
    }

}
