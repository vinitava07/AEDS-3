import java.io.RandomAccessFile;

public class Anime {
    int rank;
    String name;
    String type;
    int episodes;
    String studio;
    String tags;
    float rating;
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
        rating = 0.0f;
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
        this.rating = rating;
        this.release_year = release_year;
        this.end_year = end_year;
        this.description = description;
        this.content_warning = content_warning;
    }

    public void printAttributes() {
        System.out.println("Rank: " + rank);
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
        System.out.println("Episodes: " + episodes);
        System.out.println("Studio: " + studio);
        System.out.println("Tags: " + tags);
        System.out.println("Rating: " + rating);
        System.out.println("Release Year: " + release_year);
        System.out.println("End Year: " + end_year);
        System.out.println("Description: " + description);
        System.out.println("Content Warning: " + content_warning);
    }

    public Anime parseAnime(String arq) {

        String animeParsedNumber[];
        animeParsedNumber = arq.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        System.out.println(animeParsedNumber.length);

        for (int i = 0; i < animeParsedNumber.length; i++) {
            System.out.println(animeParsedNumber[i]);
        }
        // this.rank = parseAnimeInt(arq, "rank", animeParsedNumber);
        // this.name = parseAnimeString(arq, 1, animeParsedNumber);
        // this.type = parseAnimeString(arq, 2, animeParsedNumber);
        // this.episodes = parseAnimeInt(arq, 3, animeParsedNumber);
        // this.studio = parseAnimeString(arq, 4, animeParsedNumber);
        // this.tags = parseAnimeString(arq, 5, animeParsedNumber);
        // this.rating = parseAnimeFloat(arq, 6, animeParsedNumber);
        // this.release_year = parseAnimeInt(arq, 7, animeParsedNumber);
        // this.end_year = parseAnimeInt(arq, 8, animeParsedNumber);
        // this.description = parseAnimeString(arq, 9, animeParsedNumber);
        // this.content_warning = parseAnimeString(arq, 10, animeParsedNumber);
        // for (int i = 0; i < animeParsedNumber.length; i++) {
        // System.out.println(animeParsedNumber[i]);
        // }
        Anime a = new Anime();
        return a;
    }

    public int parseAnimeInt(String anime, String attribute, String[] animeParsedNumber) {
        int value = -1;
        ;
        if (attribute.equals("rank")) {
            if (!animeParsedNumber[0].equals("")) {
                value = Integer.valueOf(animeParsedNumber[0]);
            }
        }
        return value;
    }

    public float parseAnimeFloat(String anime, int n, String[] animeParsedNumber) {
        int i = 0;
        float number;
        if (animeParsedNumber[n].equals("")) {
            number = -1;
        } else {
            number = Float.valueOf(animeParsedNumber[n]);
        }
        return number;
    }

    public String parseAnimeString(String anime, int n, String[] animeParsedNumber) {
        String value;
        value = animeParsedNumber[n];
        return value;
    }
}
// Rank Int
// Name String
// Type String
// Episodes Int
// Studio String
// Tags String
// rating Float
// Release_year Date?
// End_year Date?
// Description String
// Content_Warning String