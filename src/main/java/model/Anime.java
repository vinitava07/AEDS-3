package model;

import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class Anime {
    public String name;
    public String type;
    public int episodes;//            for (int j = 0; j < types.size(); j++) {
//
//                for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {
//                    recordPointer = animeRaf.getFilePointer();
//                    animeRaf.read(bytes, 0, 4);
//                    isValid = isValidRecord(bytes[0]);
//                    animeLength = getRecordLength(bytes, isValid);
//                    animeID = animeRaf.readInt();
//                    if (isValid) {
//                        if (anime.type)
//
//                    }
//
//
//                }
//            }

    public String studio;
    public String tags;
    public float rating;
    public Timestamp release_year;

    public Anime() {
        name = "";
        type = "";
        episodes = 0;
        studio = "";
        tags = "";
        rating = 0.0f;
        release_year = this.longToTimestamp(0);
    }

    public Anime(String name, String type, int episodes, String studio, String tags,
            float rating, Timestamp release_year) {
        this.name = name;
        this.type = type;
        this.episodes = episodes;
        this.studio = studio;
        this.tags = tags;
        this.rating = rating;
        this.release_year = release_year;
    }

    public void printAttributes() {
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
        System.out.println("Episodes: " + episodes);
        System.out.println("Studio: " + studio);
        System.out.println("Tags: " + tags);
        System.out.println("Rating: " + rating);
        System.out.println("Release Date: " + release_year.toInstant());
        System.out.println();
    }

    public void parseAnime(String arq) {

        String[] animeInfo;
        animeInfo = arq.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        this.name = parseAnimeString("name", animeInfo);
        this.type = parseAnimeString("type", animeInfo);
        this.episodes = parseAnimeInt("episode", animeInfo);
        this.studio = parseAnimeString("studio", animeInfo);
        this.tags = parseAnimeString("tags", animeInfo);
        this.rating = parseAnimeFloat("rating", animeInfo);
        this.release_year = parseAnimeTimestamp("release_year", animeInfo);
    }

    public int parseAnimeInt(String attribute, String[] animeInfo) {
        int value = -1;
        if (!animeInfo[2].equals("")) {
            value = Integer.valueOf(animeInfo[2].replace(".0", ""));
        }
        return value;
    }

    public float parseAnimeFloat(String attribute, String[] animeInfo) {
        float number = -1;
        if (!animeInfo[5].equals(""))
            number = Float.valueOf(animeInfo[5]);

        return number;
    }

    public String parseAnimeString(String attribute, String[] animeInfo) {
        String value;
        switch (attribute) {
            case "name":
                value = animeInfo[0];
                break;
            case "type":
                value = animeInfo[1];
                break;
            case "studio":
                value = animeInfo[3];
                break;
            case "tags":
                value = animeInfo[4].replaceAll("\"", "").replaceAll(",,", ",");
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    public Timestamp parseAnimeTimestamp(String attribute, String[] animeInfo) {
        Timestamp timestamp;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            Date parsedDate = dateFormat.parse(animeInfo[6].replace(".0", ""));
            timestamp = new java.sql.Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            timestamp = new Timestamp(0);
        }
        return timestamp;
    }

    public Timestamp longToTimestamp(long time) {

        Timestamp timestamp;

        try {
            timestamp = new Timestamp(time);
        } catch (Exception e) {
            timestamp = new Timestamp(0);
        }
        return timestamp;

    }

    public int getByteLength() {//animelen = id + anime
        /*
         * length = id size(int 4) + (utf 2 extra bytes) + name size + type (fixed size of 5) 
         * + episodes size(int 4) + (utf 2 extra bytes) + studio size + (utf 2 extra bytes)  + tags size 
         * + rating size(float 4) + release_year size(Timestamp/long 8)
         */
        return 4 + (2 + this.name.getBytes().length) + 5 + 4 + (2 + this.studio.getBytes().length)
        + (2 + this.tags.getBytes().length) + 4 + 8;
    }
}