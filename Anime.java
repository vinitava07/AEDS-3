import java.io.RandomAccessFile;

public class Anime {
    int rank;
    String name;
    String type;
    int episodes;
    String studio;
    String tags;
    float Rating;
    int release_year;
    int end_year;
    String description;
    String content_warning;

    Anime() {
        rank = 0;
        name = "";
        type = "";
        episodes = 0;
        studio = "";
        tags = "";
        Rating = 0.0f;
        release_year = 0;
        end_year = 0;
        description = "";
        content_warning = "";
    }

    Anime(int rank, String name, String type, int episodes, String studio, String tags,
            float rating, int release_year, int end_year, String description, String content_warning) {
        this.rank = rank;
        this.name = name;
        this.type = type;
        this.episodes = episodes;
        this.studio = studio;
        this.tags = tags;
        this.Rating = rating;
        this.release_year = release_year;
        this.end_year = end_year;
        this.description = description;
        this.content_warning = content_warning;
    }

    public Anime parseAnime(String arq) {

        // try {
        //     RandomAccessFile raf = new RandomAccessFile(arq, "r");

        // } catch (Exception e) {
        //     // TODO: handle exception
        // }
        this.rank = parseAnimeInt(anime, 3);
        System.out.println(this.rank);
        Anime a = new Anime();
        return a;
    }

    public int parseAnimeInt(String anime, int n) {
        int i = 0;
        int number;
        String animeParsedNumber[];
        while (anime.charAt(i) == ',' || i != n) {
            i++;
        }
        animeParsedNumber = anime.split(",");
        if (animeParsedNumber[i].equals("")) {
            number = -1;
        } else {
            number = Integer.valueOf(animeParsedNumber[i]);
        }
        return number;
    }

    public String parseAnimeString(String anime, int n) {
        return "";
    }
}
// Rank Int
// Name String
// Type String
// Episodes Int
// Studio String
// Tags String
// Rating Float
// Release_year Date?
// End_year Date?
// Description String
// Content_Warning String