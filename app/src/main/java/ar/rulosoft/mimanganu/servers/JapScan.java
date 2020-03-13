package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.ServerFilter;
import ar.rulosoft.navegadores.Navigator;

import static ar.rulosoft.mimanganu.utils.PostProcess.FLAG_PPL90;

/**
 * Created by xtj-9182 on 21.02.2017.
 */
class JapScan extends ServerBase {

    private static final String HOST = "https://www.japscan.co";
    private static final int[] idxs = {0, 8896, 1, 2, 3, 259, 4, 5, 6, 7, 4295, 9, 120587, 8715, 203, 4235, 13, 14, 17, 22, 215, 25, 115420, 30, 163, 4195, 8739, 37, 120616, 115432, 120684, 115379, 120563, 8755, 55, 187, 4219};
    private static final String[] values = {"/", "g", "b", "1", "0", "s", "5", "2", "4", "3", "w", "7", "o", "h", "v", "z", "c", "9", "d", "6", "q", "a", "r", "8", "t", "x", "i", "e", "k", "m", "l", "p", "n", "j", "f", "u", "y"};
    private static HashMap<String, String> dicc = new HashMap<>();
    private static String currentScript = "";
    private static String mwScript = "";
    private String[] letterFilter = new String[]{"All", "0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private String[] pageFilter = new String[]{"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120"};


    JapScan(Context context) {
        super(context);
        setFlag(R.drawable.flag_fr);
        setIcon(R.drawable.japscan);
        setServerName("JapScan");
        setServerID(JAPSCAN);
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        return null;
    }

    @Override
    public ArrayList<Manga> search(String search) throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        Navigator nav = getNavigatorAndFlushParameters();
        nav.addPost("search", URLEncoder.encode(search, "UTF-8"));
        String source = nav.post(HOST + "/search/");
        if (source.length() > 2) {
            JSONArray jsonArray = new JSONArray(source);
            JSONObject item;
            for (int i = 0; i < jsonArray.length(); i++) {
                item = (JSONObject) jsonArray.get(i);
                mangas.add(new Manga(getServerID(), item.getString("name"), item.getString("url"), false));
            }
        }
        return mangas;

    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        loadMangaInformation(manga, forceReload);
    }

    @Override
    public synchronized void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String source = getNavigatorAndFlushParameters().get(HOST + manga.getPath());

            // Cover Image
            // JapScan has no cover images ...
            manga.setImages(HOST + getFirstMatchDefault("<div class=\"m-2\">[\\s\\S]+?src=\"([^\"]+)", source, ""));

            // Summary
            manga.setSynopsis(getFirstMatchDefault("Synopsis:</div>[\\s\\S]+?<p[^>]+>([^<]+)", source, context.getString(R.string.nodisponible)));

            // Status
            manga.setFinished(getFirstMatchDefault("Statut:</span>([^<]+)", source, "").contains("Terminé"));

            // Author
            manga.setAuthor(getFirstMatchDefault("Auteur\\(s\\):</span>([^<]+)", source, context.getString(R.string.nodisponible)).trim());

            // Genres
            manga.setGenre(getFirstMatchDefault("Type\\(s\\):</span>([^<]+)", source, context.getString(R.string.nodisponible)));

            // Chapters
            Pattern pattern = Pattern.compile("<div class=\"chapters_list text-truncate\">[\\s\\S]+?href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                manga.addChapterFirst(new Chapter(matcher.group(2), matcher.group(1)));
            }
        }
    }

    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String[] parts = chapter.getExtra().split("\\|");
        return parts[0] + parts[page] + (parts[parts.length - 1].equals(FLAG_PPL90) ? FLAG_PPL90 : "");
    }

    private ArrayList<Manga> getMangasFromSource(String source) {
        Pattern pattern = Pattern.compile("\"img-fluid\" src=\"([^\"]+)[\\s\\S]+?<a[^>]+?href=\"([^\"]+)\">([^<]+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        ArrayList<Manga> mangas = new ArrayList<>();
        while (matcher.find()) {
            Manga manga = new Manga(getServerID(), matcher.group(3), matcher.group(2), HOST + matcher.group(1));
            mangas.add(manga);
        }
        return mangas;
    }

    @Override
    public synchronized ArrayList<Manga> getMangasFiltered(int[][] filters, int pageNumber) throws Exception {
       /* String extra = "";
        if (filters[0][0] != 0) {
            extra = letterFilter[filters[0][0]] + "/";
        }*/
        String source = getNavigatorAndFlushParameters().get(HOST + "/mangas/" + (pageNumber - 1 + (filters[0][0] * 10)));
        return getMangasFromSource(source);
    }

    @Override
    public synchronized void chapterInit(Chapter chapter) throws Exception {
        if (chapter.getPages() == 0) {
            String source = getNavigatorAndFlushParameters().get(HOST + chapter.getPath());
            String pages = getFirstMatch("Page (\\d+)</option>\\s*</select>", source,
                    context.getString(R.string.server_failed_loading_image));
            String extra = "";
            if (source.contains("iYFbYi_UibMqYb.js")) {
                extra = "|" + FLAG_PPL90;
            }
            ArrayList<String> imgs = getAllMatch("<option[^<]+?data-img=\"([^\"]+)\"", source);
            String cd = getFirstMatch("<script src=\"\\/zjs\\/(.+?)\\.", source, context.getString(R.string.error_downloading_image));
            if ((!cd.equals(currentScript)) && (!cd.equals(mwScript))) {
                generateDictionary(cd);
            }
            for (int i = 0; i < imgs.size(); i++) {
                imgs.set(i, imageDecode(imgs.get(i)));
            }
            chapter.setExtra("|" + TextUtils.join("|", imgs) + extra);
            chapter.setPages(Integer.parseInt(pages));
        }
    }

    @Override
    public boolean hasSearch() {
        return false;
    }

    @Override
    public boolean needRefererForImages() {
        return true;
    }

    @Override
    public ServerFilter[] getServerFilters() {
        return new ServerFilter[]{
                new ServerFilter(context.getString(R.string.flt_page), pageFilter, ServerFilter.FilterType.SINGLE),
                //  new ServerFilter(context.getString(R.string.flt_alpha), letterFilter, ServerFilter.FilterType.SINGLE),
        };
    }

    private String imageDecode(String fakeURL) throws Exception {
        int point = fakeURL.indexOf("/", 9);
        String origin = fakeURL.substring(0, point);
        String path = fakeURL.substring(point, fakeURL.lastIndexOf("."));
        String ext = fakeURL.substring(fakeURL.lastIndexOf("."));

        StringBuilder sb = new StringBuilder(origin);
        for (String s : path.split("")) {
            if (!s.isEmpty()) {
                sb.append(dicc.get(s));
            }
        }
        sb.append(ext);
        return sb.toString();
    }


    private HashMap<String, String> generateDictionary(String newDicName) throws Exception {
        String[] sources = {
                "https://www.japscan.co/lecture-en-ligne/the-promised-neverland/170/",
                "https://www.japscan.co/lecture-en-ligne/2001-night-stories/volume-1/",
                "https://www.japscan.co/lecture-en-ligne/dr-stone/142/",
                "https://www.japscan.co/lecture-en-ligne/090-eko-to-issho/2/"
        };
        String enc = "";
        String mwS = "";
        String mgS = "";
        for (String web : sources) {
            String data = getNavigatorAndFlushParameters().get(web);
            if (mwS.isEmpty()) {
                mwS = getFirstMatch("<script src=\"\\/zjs\\/(.+?)\\.", data, context.getString(R.string.error_downloading_image));
            } else if (mgS.isEmpty()) {
                mgS = getFirstMatch("<script src=\"\\/zjs\\/(.+?)\\.", data, context.getString(R.string.error_downloading_image));
            } else {
                if (!data.contains(mgS)) {
                    throw new Exception("Error creating dictionary (not valid id)");
                }
            }

            Pattern p = Pattern.compile("data-img=\"https:\\/\\/c.japscan.co(\\/.+?)\\.jpg\"");
            Matcher m = p.matcher(data);
            while (m.find()) {
                enc = enc + m.group(1);
            }
        }
        if (enc.length() == 124456) {
            for (int i = 0; i < idxs.length; i++) {
                dicc.put("" + enc.charAt(idxs[i]), values[i]);
            }
        } else {
            throw new Exception("Error creating dictionary (not valid length)");
        }
        currentScript = mgS;
        mwScript = mwS;
        return dicc;
    }

}