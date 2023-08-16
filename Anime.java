import java.io.RandomAccessFile;

public class Anime {
    String name;
    String type;
    int episodes;
    String studio;
    String tags;
    float rating;
    int release_year;

    Anime() {
        name = "";
        type = "";
        episodes = 0;
        studio = "";
        tags = "";
        rating = 0.0f;
        release_year = 0;
    }

    Anime(String name, String type, int episodes, String studio, String tags,
            float rating, int release_year) {
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
        // System.out.println("Release Year: " + release_year);
    }

    public void parseAnime(String arq) {

        String animeInfo[];
        animeInfo = arq.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        // System.out.println(animeInfo.length);

        // for (int i = 0; i < animeInfo.length; i++) {
        //     System.out.println(animeInfo[i]);
        // }

        this.name = parseAnimeString("name", animeInfo);
        this.type = parseAnimeString("type", animeInfo);
        this.episodes = parseAnimeInt("episode" , animeInfo);
        this.studio = parseAnimeString("studio", animeInfo);
        this.tags = parseAnimeString("tags", animeInfo);
        this.rating = parseAnimeFloat("rating", animeInfo);
        // this.release_year
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
        if(!animeInfo[5].equals("")) number = Float.valueOf(animeInfo[5]);
        
        return number;
    }

    public String parseAnimeString(String attribute, String[] animeInfo) {
        String value;
        switch(attribute) {
            case "name":
                value = animeInfo[0];
                break;
            case "type":
                value = animeInfo[1] ;
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
}