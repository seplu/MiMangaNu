package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by jtx on 09.05.2016.
 */
public class NineManga extends ServerBase {
    private static String HOST = "http://ninemanga.com";

    private static String[] genre = new String[]{
            "Everything", "4 Koma", "4-Koma", "Action", "Adult", "Adventure", "Anime", "Award Winning",
            "Bara", "Comedy", "Cooking", "Demons", "Doujinshi", "Drama", "Ecchi", "Fantasy", "Gender Bender",
            "Harem", "Historical", "Horror", "Josei", "Live Action", "Magic", "Manhua", "Manhwa",
            "Martial Arts", "Matsumoto To...", "Mature", "Mecha", "Medical", "Military", "Music",
            "Mystery", "N/A", "None", "One Shot", "Oneshot", "Psychological", "Reverse Harem",
            "Romance", "Romance Shoujo", "School Life", "Sci Fi", "Sci-Fi", "Seinen", "Shoujo", "Shoujo Ai",
            "Shoujo-Ai", "Shoujoai", "Shounen", "Shounen Ai", "Shounen-Ai", "Shounenai", "Slice Of Life",
            "Smut", "Sports", "Staff Pick", "Super Power", "Supernatural", "Suspense", "Tragedy",
            "Vampire", "Webtoon", "Webtoons", "Yaoi", "Yuri", "[No Chapters]"
    };
    private static String[] genreV = new String[]{
            "index.html", "4 Koma.html", "4-Koma.html", "Action.html", "Adult.html", "Adventure.html", "Anime.html", "Award Winning.html",
            "Bara.html", "Comedy.html", "Cooking.html", "Demons.html", "Doujinshi.html", "Drama.html", "Ecchi.html", "Fantasy.html", "Gender Bender.html",
            "Harem.html", "Historical.html", "Horror.html", "Josei.html", "Live Action.html", "Magic.html", "Manhua.html", "Manhwa.html",
            "Martial Arts.html", "Matsumoto Tomokicomedy.html", "Mature.html", "Mecha.html", "Medical.html", "Military.html", "Music.html",
            "Mystery.html", "N/A.html", "None.html", "One Shot.html", "Oneshot.html", "Psychological.html", "Reverse Harem.html",
            "Romance.html", "Romance Shoujo.html", "School Life.html", "Sci Fi.html", "Sci-Fi.html", "Seinen.html", "Shoujo.html", "Shoujo Ai.html",
            "Shoujo-Ai.html", "Shoujoai.html", "Shounen.html", "Shounen Ai.html", "Shounen-Ai.html", "Shounenai.html", "Slice Of Life.html",
            "Smut.html", "Sports.html", "Staff Pick.html", "Super Power.html", "Supernatural.html", "Suspense.html", "Tragedy.html",
            "Vampire.html", "Webtoon.html", "Webtoons.html", "Yaoi.html", "Yuri.html", "[No Chapters]"
    };

    private static String[] order = {
            "/category/", "/list/New-Update/", "/list/Hot-Book/", "/list/New-Book/"
    };

    private static String[] orderV = {"Manga Directory", "Latest Releases", "Popular Manga", "New Manga"};

    public NineManga() {
        this.setFlag(R.drawable.flag_en);
        this.setIcon(R.drawable.ninemanga);
        this.setServerName("NineManga");
        setServerID(ServerBase.NINEMANGA);
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        String source = getNavigatorAndFlushParameters().get(
                HOST + "/search/?wd=" + URLEncoder.encode(term, "UTF-8"));
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern pattern = Pattern.compile("bookname\" href=\"(/manga/[^\"]+)\">(.+?)<");
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(2), HOST + matcher.group(1), false);
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters() == null || manga.getChapters().size() == 0 || forceReload)
            loadMangaInformation(manga, forceReload);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String source = getNavigatorAndFlushParameters().get(manga.getPath() + "?waring=1");
        // Front
        manga.setImages(getFirstMatchDefault("Manga\" src=\"(.+?)\"", source, ""));
        // Summary
        String summary = getFirstMatchDefault("<p itemprop=\"description\">(.+?)</p>",
                source, defaultSynopsis).replaceAll("<.+?>", "");
        manga.setSynopsis(Util.getInstance().fromHtml(summary.replaceFirst("Summary:", "")).toString());
        // Status
        manga.setFinished(!getFirstMatchDefault("<b>Status:</b>(.+?)</a>", source, "").contains("Ongoing"));
        // Author
        manga.setAuthor(getFirstMatchDefault("author.+?\">(.+?)<", source, ""));
        // Genre
        manga.setGenre((Util.getInstance().fromHtml(getFirstMatchDefault("<li itemprop=\"genre\".+?</b>(.+?)</li>", source, "").replace("a><a", "a>, <a") + ".").toString().trim()));
        // Chapter
        Pattern p = Pattern.compile("<a class=\"chapter_list_a\" href=\"(/chapter.+?)\" title=\"(.+?)\">(.+?)</a>");
        Matcher matcher = p.matcher(source);
        ArrayList<Chapter> chapters = new ArrayList<>();
        while (matcher.find()) {
            chapters.add(0, new Chapter(matcher.group(3), HOST + matcher.group(1)));
        }
        manga.setChapters(chapters);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath().replace(".html", "-" + page + ".html");
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        if (chapter.getExtra() == null)
            setExtra(chapter);
        String[] images = chapter.getExtra().split("\\|");
        return images[page];
    }

    private void setExtra(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(
                chapter.getPath().replace(".html", "-" + chapter.getPages() + "-1.html"));
        Pattern p = Pattern.compile("<img class=\"manga_pic.+?src=\"([^\"]+)");
        Matcher matcher = p.matcher(source);
        String images = "";
        while (matcher.find()) {
            images = images + "|" + matcher.group(1);
        }
        chapter.setExtra(images);
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String source = getNavigatorAndFlushParameters().get(chapter.getPath());
        String nop = getFirstMatch(
                "\\d+/(\\d+)</option>[\\s]*</select>", source,
                "failed to get the number of pages");
        chapter.setPages(Integer.parseInt(nop));
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Pattern p = Pattern.compile("<a href=\"(/manga/[^\"]+)\"><img src=\"(.+?)\".+?alt=\"([^\"]+)\"");
        Matcher matcher = p.matcher(source);
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), HOST + matcher.group(1), false);
            manga.setImages(matcher.group(2));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public ServerFilter[] getServerFilters(Context context) {
        return new ServerFilter[]{
                new ServerFilter("Genre", genre, ServerFilter.FilterType.SINGLE),
                new ServerFilter("Order", orderV, ServerFilter.FilterType.SINGLE)
        };
    }

    @Override
    public ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
        String source = getNavigatorAndFlushParameters().get(HOST + NineManga.order[filters[1][0]] + genreV[filters[0][0]].replace("_", "_" + pageNumber));
        return getMangasFromSource(source);
    }

    @Override
    public boolean hasList() {
        return false;
    }
}

