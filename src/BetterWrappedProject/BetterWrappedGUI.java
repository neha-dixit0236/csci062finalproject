package BetterWrappedProject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

/**
 * BetterWrappedGUI — A JavaFX front-end for the BetterWrapped backend.
 *
 * Flow: Landing → Config → Dashboard (Tabs: Stats | Anomalies | Discover)
 *
 * All backend calls use the exact public API of the project classes:
 *   BetterWrapped, SongStatistics, Bucket, KeyValuePair, SongInfo,
 *   OutlierDetector, OutlierDay,
 *   RecommendationEngine, RecommendationLoader, RecommendationSong
 *
 * @author GUI layer — CSCI 062 Final Project
 */
public class BetterWrappedGUI extends Application {

    // ─── Application-level state ─────────────────────────────────────────────
    private Stage         primaryStage;
    private BetterWrapped wrappedEngine;

    // Paths captured from the config form
    private String historyCSVPath = "";
    private String recCSVPath     = "";

    // Which time window the user selected
    private String selectedWindow = "WEEKDAY_VS_WEEKEND";

    // Raw text from the ONE_SEMESTER panel, captured on the FX thread
    // *before* the background analysis thread starts (UI controls are FX-only)
    private String midtermRawText = "";
    private String breakRawText   = "";

    // Results collected by the analysis thread, consumed by dashboard builders
    private final Map<String, BucketSummary>            summaryMap  = new LinkedHashMap<>();
    private final List<OutlierDay>                       outlierList = new ArrayList<>();
    private final Map<String, List<RecommendationSong>> recMap      = new LinkedHashMap<>();

    // ─── JavaFX entry point ──────────────────────────────────────────────────
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Better Wrapped ✦");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        showLanding();
        stage.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SCREEN 1 — LANDING
    // ════════════════════════════════════════════════════════════════════════
    private void showLanding() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root");

        // Solid dark backdrop that scales with the window
        Rectangle backdrop = new Rectangle();
        backdrop.widthProperty().bind(root.widthProperty());
        backdrop.heightProperty().bind(root.heightProperty());
        backdrop.setFill(Color.web("#121212"));

        // Decorative glowing blobs
        Circle c1 = glowCircle(340, "#1DB954", 0.18);
        c1.setTranslateX(-280); c1.setTranslateY(-180);
        Circle c2 = glowCircle(260, "#b84ef5", 0.20);
        c2.setTranslateX(320);  c2.setTranslateY(160);
        Circle c3 = glowCircle(200, "#ff4ecd", 0.15);
        c3.setTranslateX(-60);  c3.setTranslateY(220);

        // Centre content stack
        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(520);

        Label eyebrow = styledLabel("♫  YOUR MUSIC, CONTEXTUALISED", 13, "#1DB954");
        eyebrow.getStyleClass().add("eyebrow");

        Label titleWord = styledLabel("Better", 76, "#FFFFFF");
        titleWord.setStyle("-fx-font-weight:900; -fx-font-family:'Georgia';");
        Label subtitleWord = styledLabel("Wrapped", 76, "#1DB954");
        subtitleWord.setStyle(
            "-fx-font-weight:900; -fx-font-family:'Georgia';" +
            "-fx-effect:dropshadow(gaussian,#1DB954,24,0.7,0,0);");

        HBox titleRow = new HBox(14, titleWord, subtitleWord);
        titleRow.setAlignment(Pos.CENTER);

        Label tagline = styledLabel(
            "Beyond a static slideshow — explore how your academic\nlife shapes every playlist, every season.",
            15, "#A0A0A0");
        tagline.setWrapText(true);
        tagline.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox pills = new HBox(12,
            pill("📊 Trend Analysis"),
            pill("🔍 Anomaly Detection"),
            pill("🎵 Discover Weekly"));
        pills.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Get Started →");
        startBtn.getStyleClass().addAll("btn-primary", "btn-glow");
        startBtn.setOnAction(e -> showConfig());

        content.getChildren().addAll(eyebrow, titleRow, tagline, pills, startBtn);
        root.getChildren().addAll(backdrop, c1, c2, c3, content);

        Scene scene = new Scene(root, 960, 660);
        attachCSS(scene);
        primaryStage.setScene(scene);
    }

    // ════════════════════════════════════════════════════════════════════════
    // SCREEN 2 — CONFIGURATION
    // ════════════════════════════════════════════════════════════════════════
    private void showConfig() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        root.setTop(headerBar("Configure Your Wrapped", false));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-dark");

        VBox form = new VBox(24);
        form.setPadding(new Insets(36, 60, 36, 60));
        form.getStyleClass().add("form-container");

        // ── Step 1: CSV paths ──────────────────────────────────────────────
        form.getChildren().add(sectionLabel("① Load Your Data"));

        Label histLabel = fieldLabel("Listening History CSV");
        TextField histField = pathField("e.g. ScrobblesForOneSemester.csv");
        Button histBrowse = browseButton();
        histBrowse.setOnAction(e -> {
            File f = csvChooser("Select Listening History CSV");
            if (f != null) { historyCSVPath = f.getAbsolutePath(); histField.setText(historyCSVPath); }
        });
        HBox histRow = new HBox(10, histField, histBrowse);
        HBox.setHgrow(histField, Priority.ALWAYS);

        Label recLabel = fieldLabel("Recommendations CSV");
        TextField recField = pathField("e.g. MasterListofSongs(Feature3).csv");
        Button recBrowse = browseButton();
        recBrowse.setOnAction(e -> {
            File f = csvChooser("Select Recommendations CSV");
            if (f != null) { recCSVPath = f.getAbsolutePath(); recField.setText(recCSVPath); }
        });
        HBox recRow = new HBox(10, recField, recBrowse);
        HBox.setHgrow(recField, Priority.ALWAYS);

        form.getChildren().addAll(histLabel, histRow, recLabel, recRow);

        // ── Step 2: Time-window radio buttons ─────────────────────────────
        form.getChildren().add(sectionLabel("② Choose Time Window"));

        ToggleGroup tg   = new ToggleGroup();
        RadioButton rbWW  = radio("Weekday vs Weekend",                         tg, "WEEKDAY_VS_WEEKEND");
        RadioButton rbSEM = radio("One Semester  (Midterms / Breaks / Normal)", tg, "ONE_SEMESTER");
        RadioButton rbFY  = radio("Full Year  (Spring / Summer / Fall)",        tg, "FULL_YEAR");
        rbWW.setSelected(true);

        // ONE_SEMESTER extra inputs — hidden until the user selects that option
        TextArea midtermArea = new TextArea();
        midtermArea.setPromptText("e.g.\n11-10\n11-24");
        midtermArea.setPrefRowCount(4);
        midtermArea.getStyleClass().add("text-area-dark");

        TextArea breakArea = new TextArea();
        breakArea.setPromptText("e.g.\n11-26 11-28\n12-15 01-02");
        breakArea.setPrefRowCount(4);
        breakArea.getStyleClass().add("text-area-dark");

        VBox semPanel = new VBox(14,
            fieldLabel("Midterm / Final Dates  (MM-DD, one per line)"),
            midtermArea,
            fieldLabel("Break Date Ranges  (start MM-DD  end MM-DD, one pair per line)"),
            breakArea);
        semPanel.getStyleClass().add("card");
        semPanel.setPadding(new Insets(20));
        semPanel.setVisible(false);
        semPanel.setManaged(false);

        // FULL_YEAR informational note
        Label fullYearNote = styledLabel(
            "ℹ  Jan–Apr = Spring  ·  May–Aug = Summer  ·  Sep–Dec = Fall",
            13, "#A0A0A0");
        fullYearNote.setVisible(false);
        fullYearNote.setManaged(false);

        tg.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw == null) return;
            selectedWindow = (String) nw.getUserData();
            boolean isSem = "ONE_SEMESTER".equals(selectedWindow);
            boolean isFY  = "FULL_YEAR".equals(selectedWindow);
            semPanel.setVisible(isSem);    semPanel.setManaged(isSem);
            fullYearNote.setVisible(isFY); fullYearNote.setManaged(isFY);
        });

        form.getChildren().addAll(rbWW, rbSEM, rbFY, semPanel, fullYearNote);

        // ── Generate button ────────────────────────────────────────────────
        Label statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");

        Button analyseBtn = new Button("✦ Generate Better Wrapped");
        analyseBtn.getStyleClass().addAll("btn-primary", "btn-glow");
        analyseBtn.setMaxWidth(Double.MAX_VALUE);
        analyseBtn.setOnAction(e -> {
            // Capture all FX-thread values NOW, before handing off to the background thread
            historyCSVPath = histField.getText().trim();
            recCSVPath     = recField.getText().trim();
            midtermRawText = midtermArea.getText();
            breakRawText   = breakArea.getText();

            if (historyCSVPath.isEmpty() || recCSVPath.isEmpty()) {
                statusLabel.setText("⚠  Please fill in both CSV paths.");
                statusLabel.setStyle("-fx-text-fill:#ff4e4e;");
                return;
            }
            statusLabel.setText("⏳  Loading & analysing — please wait…");
            statusLabel.setStyle("-fx-text-fill:#A0A0A0;");
            analyseBtn.setDisable(true);
            runAnalysis(analyseBtn, statusLabel);
        });

        form.getChildren().addAll(new Separator(), analyseBtn, statusLabel);
        scroll.setContent(form);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 960, 660);
        attachCSS(scene);
        primaryStage.setScene(scene);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ANALYSIS — runs on a background thread; UI updates via Platform.runLater
    // ════════════════════════════════════════════════════════════════════════
    private void runAnalysis(Button btn, Label statusLabel) {
        new Thread(() -> {
            try {
                // 1. Load listening history via BetterWrapped constructor
                wrappedEngine = new BetterWrapped(historyCSVPath);
                List<KeyValuePair> history = wrappedEngine.getAllHistory();
                if (history == null || history.isEmpty())
                    throw new Exception("Listening history CSV is empty or could not be read.");

                // Detect the year from the first entry
                int year = history.get(0).getTimeStamp().toLocalDateTime().getYear();

                // 2. Build date-bound lists for the chosen analysis mode
                List<Timestamp> mt = new ArrayList<>();  // midterm deadlines
                List<Timestamp> br = new ArrayList<>();  // break [start,end] pairs
                List<Timestamp> sp = new ArrayList<>();  // spring [start,end]
                List<Timestamp> su = new ArrayList<>();  // summer [start,end]
                List<Timestamp> fa = new ArrayList<>();  // fall   [start,end]

                if ("ONE_SEMESTER".equals(selectedWindow)) {
                    parseSemesterDates(year, mt, br);
                } else if ("FULL_YEAR".equals(selectedWindow)) {
                    sp.add(ts(year,  1,  1,  0,  0));  sp.add(ts(year,  4, 30, 23, 59));
                    su.add(ts(year,  5,  1,  0,  0));  su.add(ts(year,  8, 31, 23, 59));
                    fa.add(ts(year,  9,  1,  0,  0));  fa.add(ts(year, 12, 31, 23, 59));
                }

                // 3. Collect all data for the three dashboard tabs
                collectSummaries(mt, br, sp, su, fa);
                collectOutliers( mt, br, sp, su, fa);
                collectRecommendations(mt, br, sp, su, fa);

                // 4. Transition to the dashboard on the FX thread
                Platform.runLater(this::showDashboard);

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("⚠  Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill:#ff4e4e;");
                    btn.setDisable(false);
                });
            }
        }, "BetterWrapped-AnalysisThread").start();
    }

    /**
     * Parses the raw text captured from the ONE_SEMESTER panel.
     *   Midterm lines : "MM-DD"
     *   Break lines   : "MM-DD MM-DD"   (start then end, space-separated)
     */
    private void parseSemesterDates(int year,
                                    List<Timestamp> mt,
                                    List<Timestamp> br) throws Exception {
        for (String line : midtermRawText.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] p = line.split("-");
            if (p.length < 2) continue;
            LocalDateTime d = LocalDateTime.of(
                year, Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), 23, 59);
            mt.add(Timestamp.valueOf(d));
        }

        for (String line : breakRawText.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] halves = line.split("\\s+");
            if (halves.length < 2) continue;
            String[] s = halves[0].split("-");
            String[] e = halves[1].split("-");
            br.add(ts(year, Integer.parseInt(s[0].trim()), Integer.parseInt(s[1].trim()),  0,  0));
            br.add(ts(year, Integer.parseInt(e[0].trim()), Integer.parseInt(e[1].trim()), 23, 59));
        }
    }

    // ── Data collectors (all called from the analysis thread) ────────────────

    /** Fills summaryMap — used by the Stats tab. */
    private void collectSummaries(List<Timestamp> mt, List<Timestamp> br,
                                  List<Timestamp> sp, List<Timestamp> su, List<Timestamp> fa) {
        summaryMap.clear();
        for (Bucket b : buildBuckets(mt, br, sp, su, fa)) {
            SongStatistics stats = new SongStatistics(b.getPlays());
            summaryMap.put(b.getName(), new BucketSummary(
                b.getName(),
                stats.getTopArtist(),
                stats.getTopSong(),
                stats.getTopGenre(),
                b.getPlays().size(),
                buildGenreFreqMap(b.getPlays())
            ));
        }
    }

    /** Fills outlierList — used by the Anomalies tab. */
    private void collectOutliers(List<Timestamp> mt, List<Timestamp> br,
                                 List<Timestamp> sp, List<Timestamp> su, List<Timestamp> fa) {
        outlierList.clear();
        // OutlierDetector constructor: (List<Bucket>, int minPlaysPerDay)
        OutlierDetector detector = new OutlierDetector(buildBuckets(mt, br, sp, su, fa), 4);
        outlierList.addAll(detector.findOutliers());
    }

    /** Fills recMap — used by the Discover tab. */
    private void collectRecommendations(List<Timestamp> mt, List<Timestamp> br,
                                        List<Timestamp> sp, List<Timestamp> su, List<Timestamp> fa) {
        recMap.clear();
        // RecommendationLoader.loadSongs(String fileName) → List<RecommendationSong>
        List<RecommendationSong> pool = RecommendationLoader.loadSongs(recCSVPath);
        // RecommendationEngine(List<RecommendationSong>, List<KeyValuePair>)
        RecommendationEngine engine =
            new RecommendationEngine(pool, wrappedEngine.getAllHistory());
        // recommendSongs(List<Bucket>) → Map<String, List<RecommendationSong>>
        recMap.putAll(engine.recommendSongs(buildBuckets(mt, br, sp, su, fa)));
    }

    // ── Bucketing helpers — mirror BetterWrapped's private bucket methods ─────

    /** Routes to the correct bucket builder for the selected window. */
    private List<Bucket> buildBuckets(List<Timestamp> mt, List<Timestamp> br,
                                      List<Timestamp> sp, List<Timestamp> su, List<Timestamp> fa) {
        switch (selectedWindow) {
            case "ONE_SEMESTER": return bucketSemester(mt, br);
            case "FULL_YEAR":    return bucketYear(sp, su, fa);
            default:             return bucketWeekdayWeekend();
        }
    }

    /** WEEKDAY_VS_WEEKEND bucketing — mirrors BetterWrapped.bucketWeekdayWeekend(). */
    private List<Bucket> bucketWeekdayWeekend() {
        Bucket weekday = new Bucket("WEEKDAY");
        Bucket weekend = new Bucket("WEEKEND");
        for (KeyValuePair kvp : wrappedEngine.getAllHistory()) {
            DayOfWeek day = kvp.getTimeStamp().toLocalDateTime().getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) weekend.addPlay(kvp);
            else                                                        weekday.addPlay(kvp);
        }
        List<Bucket> result = new ArrayList<>();
        result.add(weekday);
        result.add(weekend);
        return result;
    }

    /**
     * ONE_SEMESTER bucketing — mirrors BetterWrapped.bucketSemester().
     * Break takes priority over midterm window.
     */
    private List<Bucket> bucketSemester(List<Timestamp> midtermDates,
                                        List<Timestamp> breakDates) {
        Bucket midterm = new Bucket("MIDTERM");
        Bucket brk     = new Bucket("BREAK");
        Bucket normal  = new Bucket("NORMAL");
        for (KeyValuePair kvp : wrappedEngine.getAllHistory()) {
            Timestamp st = kvp.getTimeStamp();
            if      (isWithinDateRange(st, breakDates))        brk.addPlay(kvp);
            else if (isWithinWindow(st, midtermDates, 5))  midterm.addPlay(kvp);
            else                                             normal.addPlay(kvp);
        }
        List<Bucket> result = new ArrayList<>();
        result.add(midterm);
        result.add(brk);
        result.add(normal);
        return result;
    }

    /** FULL_YEAR bucketing — mirrors BetterWrapped.bucketYear(). */
    private List<Bucket> bucketYear(List<Timestamp> springDates,
                                    List<Timestamp> summerDates,
                                    List<Timestamp> fallDates) {
        Bucket spring = new Bucket("SPRING");
        Bucket summer = new Bucket("SUMMER");
        Bucket fall   = new Bucket("FALL");
        for (KeyValuePair kvp : wrappedEngine.getAllHistory()) {
            Timestamp st = kvp.getTimeStamp();
            if      (isWithinDateRange(st, springDates)) spring.addPlay(kvp);
            else if (isWithinDateRange(st, summerDates)) summer.addPlay(kvp);
            else if (isWithinDateRange(st, fallDates))     fall.addPlay(kvp);
        }
        List<Bucket> result = new ArrayList<>();
        result.add(spring);
        result.add(summer);
        result.add(fall);
        return result;
    }

    /**
     * Returns true if t falls within any [start, end] pair in importantDates.
     * Mirrors BetterWrapped.isWithinDateRange.
     */
    private boolean isWithinDateRange(Timestamp t, List<Timestamp> importantDates) {
        if (importantDates == null || importantDates.size() < 2) return false;
        for (int i = 0; i + 1 < importantDates.size(); i += 2) {
            if (!t.before(importantDates.get(i)) && !t.after(importantDates.get(i + 1)))
                return true;
        }
        return false;
    }

    /**
     * Returns true if t falls within daysBefore days before any deadline.
     * Mirrors BetterWrapped.isWithinWindow.
     */
    private boolean isWithinWindow(Timestamp t, List<Timestamp> deadlines, int daysBefore) {
        if (deadlines == null || deadlines.isEmpty()) return false;
        for (Timestamp deadline : deadlines) {
            Timestamp start = Timestamp.valueOf(deadline.toLocalDateTime().minusDays(daysBefore));
            if (!t.before(start) && !t.after(deadline)) return true;
        }
        return false;
    }

    /** Counts plays per genre for a list of KeyValuePairs.
     *  Uses KeyValuePair.getSongObject() and SongInfo.getGenre(). */
    private Map<String, Integer> buildGenreFreqMap(List<KeyValuePair> plays) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (KeyValuePair kvp : plays) {
            String g = kvp.getSongObject().getGenre();   // SongInfo.getGenre()
            map.merge(g, 1, Integer::sum);
        }
        return map;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SCREEN 3 — DASHBOARD
    // ════════════════════════════════════════════════════════════════════════
    private void showDashboard() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        root.setTop(headerBar("Your Better Wrapped ✦", true));

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("dark-tab-pane");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(buildStatsTab(), buildAnomalyTab(), buildDiscoverTab());

        root.setCenter(tabs);

        Scene scene = new Scene(root, 960, 660);
        attachCSS(scene);
        primaryStage.setScene(scene);
    }

    // ── Tab 1: Listening Stats ────────────────────────────────────────────────
    private Tab buildStatsTab() {
        Tab tab = new Tab("📊  Listening Stats");

        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane-dark");

        VBox content = new VBox(32);
        content.setPadding(new Insets(32));

        for (BucketSummary bs : summaryMap.values())
            content.getChildren().add(buildBucketCard(bs));

        content.getChildren().add(buildTrendComparison());

        sp.setContent(content);
        tab.setContent(sp);
        return tab;
    }

    private VBox buildBucketCard(BucketSummary bs) {
        VBox card = new VBox(20);
        card.getStyleClass().add("bucket-card");
        card.setPadding(new Insets(28));

        Label nameLabel = styledLabel(bs.name, 22, "#1DB954");
        nameLabel.setStyle("-fx-font-weight:800; -fx-font-family:'Georgia';");

        HBox statsRow = new HBox(16,
            miniStat("🎤 TOP ARTIST",  bs.topArtist),
            miniStat("🎵 TOP SONG",    bs.topSong),
            miniStat("🎸 TOP GENRE",   bs.topGenre),
            miniStat("▶ TOTAL PLAYS",  String.valueOf(bs.totalPlays)));
        statsRow.setAlignment(Pos.CENTER_LEFT);
        for (Node n : statsRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // PieChart — top 6 genres
        PieChart pie = new PieChart();
        pie.getStyleClass().add("genre-pie");
        pie.setLegendVisible(true);
        pie.setLabelsVisible(false);
        pie.setPrefSize(280, 220);
        pie.setTitle("Genre Mix");
        bs.genreFreq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(6)
            .forEach(e -> pie.getData().add(new PieChart.Data(e.getKey(), e.getValue())));

        // BarChart — build once; do NOT call buildGenreBarChart twice
        BarChart<String, Number> bar = buildGenreBarChart(bs);

        HBox bottom = new HBox(24, pie, bar);
        HBox.setHgrow(pie, Priority.ALWAYS);
        HBox.setHgrow(bar, Priority.ALWAYS);

        card.getChildren().addAll(nameLabel, statsRow, bottom);
        return card;
    }

    private BarChart<String, Number> buildGenreBarChart(BucketSummary bs) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Genre");
        yAxis.setLabel("Plays");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Genre Breakdown");
        chart.getStyleClass().add("genre-bar");
        chart.setAnimated(false);
        chart.setPrefSize(360, 220);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        bs.genreFreq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(6)
            .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        chart.getData().add(series);
        return chart;
    }

    private VBox buildTrendComparison() {
        VBox section = new VBox(16);
        section.getStyleClass().add("bucket-card");
        section.setPadding(new Insets(28));

        Label title = styledLabel("🔀 Listening Trend Comparison", 20, "#b84ef5");
        title.setStyle("-fx-font-weight:800;");
        section.getChildren().add(title);

        List<BucketSummary> list = new ArrayList<>(summaryMap.values());
        for (int i = 0; i < list.size(); i++)
            for (int j = i + 1; j < list.size(); j++)
                section.getChildren().add(trendRow(list.get(i), list.get(j)));

        return section;
    }

    private HBox trendRow(BucketSummary a, BucketSummary b) {
        String genreLine  = a.topGenre.equals(b.topGenre)
            ? "✅ Genre stayed: "  + a.topGenre
            : "🔄 Genre: " + a.topGenre + " → " + b.topGenre;
        String artistLine = a.topArtist.equals(b.topArtist)
            ? "✅ Artist stayed: " + a.topArtist
            : "🔄 Artist: " + a.topArtist + " → " + b.topArtist;
        String songLine   = a.topSong.equals(b.topSong)
            ? "✅ Top song stayed: " + a.topSong
            : "🔄 Song: " + a.topSong + " → " + b.topSong;

        VBox info = new VBox(6,
            styledLabel(a.name + "  vs  " + b.name, 14, "#FFFFFF"),
            styledLabel(genreLine,  12, "#A0A0A0"),
            styledLabel(artistLine, 12, "#A0A0A0"),
            styledLabel(songLine,   12, "#A0A0A0"));

        HBox row = new HBox(info);
        row.setPadding(new Insets(12));
        row.getStyleClass().add("trend-row");
        return row;
    }

    // ── Tab 2: Anomaly Feed ───────────────────────────────────────────────────
    private Tab buildAnomalyTab() {
        Tab tab = new Tab("🔍  Anomaly Feed");

        SplitPane split = new SplitPane();
        split.getStyleClass().add("split-dark");

        // Left: scrollable chip list
        ScrollPane feedScroll = new ScrollPane();
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane-dark");
        VBox feed = new VBox(10);
        feed.setPadding(new Insets(20));

        // Right: detail view
        VBox detailPane = new VBox(20);
        detailPane.setPadding(new Insets(24));
        detailPane.getStyleClass().add("root");
        detailPane.getChildren().add(
            styledLabel("← Click an anomaly to see\nthe genre spike detail.", 14, "#606060"));

        if (outlierList.isEmpty()) {
            feed.getChildren().add(styledLabel("No outlier days detected.", 14, "#A0A0A0"));
        } else {
            for (OutlierDay od : outlierList) {
                Button chip = buildOutlierChip(od);
                chip.setOnAction(e -> showOutlierDetail(od, detailPane));
                feed.getChildren().add(chip);
            }
        }

        feedScroll.setContent(feed);
        split.getItems().addAll(feedScroll, detailPane);
        split.setDividerPositions(0.38);
        tab.setContent(split);
        return tab;
    }

    /**
     * Builds an outlier chip button.
     *
     * OutlierDay public API used:
     *   getDate()          → LocalDate   (the outlier date)
     *   getDayGenre()      → String      (top genre on that day — NOT getOutlierGenre)
     *   getPlayCount()     → int         (plays on that day   — NOT getOutlierCount)
     *   getBucketName()    → String      (parent bucket name)
     *   getBaselineGenre() → String      (overall bucket top genre)
     */
    private Button buildOutlierChip(OutlierDay od) {
        String label = od.getDate()
            + "  ·  " + od.getDayGenre()
            + "  ×" + od.getPlayCount()
            + "  (usual: " + od.getBaselineGenre() + "  in " + od.getBucketName() + ")";
        Button b = new Button(label);
        b.getStyleClass().add("outlier-chip");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    /**
     * Detail view for a clicked outlier.
     * OutlierDay has NO getGenreCounts() method — we show the known spike only.
     */
    private void showOutlierDetail(OutlierDay od, VBox pane) {
        pane.getChildren().clear();

        Label title = styledLabel("📅 " + od.getDate(), 18, "#ff4ecd");
        title.setStyle("-fx-font-weight:700;");

        Label sub = styledLabel(
            "Outlier genre: " + od.getDayGenre()
            + "  ·  Baseline: " + od.getBaselineGenre()
            + "  ·  Bucket: " + od.getBucketName(),
            13, "#A0A0A0");

        // Bar chart: one bar for the outlier genre (actual count), one for the baseline (0)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Genre Spike on " + od.getDate());
        chart.getStyleClass().add("genre-bar");
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Outlier genre bar — od.getPlayCount() is the total plays on that day
        series.getData().add(new XYChart.Data<>(od.getDayGenre(), od.getPlayCount()));
        // Baseline genre shown at 0 as a visual anchor (only if different)
        if (!od.getBaselineGenre().equals(od.getDayGenre())) {
            series.getData().add(new XYChart.Data<>(od.getBaselineGenre(), 0));
        }
        chart.getData().add(series);

        pane.getChildren().addAll(title, sub, chart);
    }

    // ── Tab 3: Discover Weekly ────────────────────────────────────────────────
    private Tab buildDiscoverTab() {
        Tab tab = new Tab("🎵  Discover Weekly");

        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane-dark");

        VBox content = new VBox(36);
        content.setPadding(new Insets(32));

        if (recMap.isEmpty()) {
            content.getChildren().add(
                styledLabel("No recommendations generated.", 14, "#A0A0A0"));
        } else {
            for (Map.Entry<String, List<RecommendationSong>> entry : recMap.entrySet())
                content.getChildren().add(buildRecSection(entry.getKey(), entry.getValue()));
        }

        sp.setContent(content);
        tab.setContent(sp);
        return tab;
    }

    private VBox buildRecSection(String bucketName, List<RecommendationSong> songs) {
        VBox section = new VBox(16);
        Label heading = styledLabel("▶ " + bucketName, 18, "#1DB954");
        heading.setStyle("-fx-font-weight:800;");
        section.getChildren().add(heading);

        if (songs == null || songs.isEmpty()) {
            section.getChildren().add(
                styledLabel("No recommendations available for this period.", 13, "#606060"));
            return section;
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        int cols = 3;
        for (int i = 0; i < songs.size(); i++)
            grid.add(buildSongCard(songs.get(i)), i % cols, i / cols);

        section.getChildren().add(grid);
        return section;
    }

    /**
     * Song card.
     *
     * RecommendationSong public API used:
     *   getSongName()  → String   (NOT getTitle — that method does not exist)
     *   getArtist()    → String
     *   getGenre()     → String
     */
    private VBox buildSongCard(RecommendationSong song) {
        VBox card = new VBox(6);
        card.getStyleClass().add("song-card");
        card.setPadding(new Insets(14));
        card.setMinWidth(200);

        Label icon   = styledLabel("♪", 22, "#1DB954");

        // RecommendationSong.getSongName() — correct accessor
        Label title  = styledLabel(song.getSongName(), 13, "#FFFFFF");
        title.setWrapText(true);
        title.setStyle("-fx-font-weight:600;");

        Label artist = styledLabel(song.getArtist(), 11, "#A0A0A0");

        Label genre  = new Label(song.getGenre());
        genre.getStyleClass().add("genre-pill");

        card.getChildren().addAll(icon, title, artist, genre);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SHARED UI COMPONENT FACTORIES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @param showBack  true on Dashboard (back → config screen); false on Config screen.
     */
    private HBox headerBar(String titleText, boolean showBack) {
        HBox bar = new HBox();
        bar.getStyleClass().add("header-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 28, 14, 28));

        Label logo = new Label("BW");
        logo.setStyle(
            "-fx-font-weight:900; -fx-font-family:'Georgia'; -fx-font-size:18px;" +
            "-fx-background-color:#1DB954; -fx-text-fill:#121212;" +
            "-fx-background-radius:50%; -fx-padding:4 10;");

        Label title = styledLabel(titleText, 17, "#FFFFFF");
        title.setStyle("-fx-font-weight:700;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region gap = new Region();
        gap.setMinWidth(14);

        bar.getChildren().addAll(logo, gap, title, spacer);

        if (showBack) {
            Button backBtn = new Button("← Back");
            backBtn.getStyleClass().add("btn-ghost");
            backBtn.setOnAction(e -> showConfig());
            bar.getChildren().add(backBtn);
        }
        return bar;
    }

    private Label styledLabel(String text, double size, String hex) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px; -fx-text-fill:" + hex + ";");
        return l;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:17px; -fx-font-weight:800;" +
                   "-fx-text-fill:#FFFFFF; -fx-font-family:'Georgia';");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:600; -fx-text-fill:#CCCCCC;");
        return l;
    }

    private TextField pathField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field-dark");
        tf.setPrefHeight(38);
        return tf;
    }

    private Button browseButton() {
        Button b = new Button("Browse…");
        b.getStyleClass().add("btn-secondary");
        return b;
    }

    private RadioButton radio(String label, ToggleGroup tg, String userData) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(tg);
        rb.setUserData(userData);
        rb.getStyleClass().add("radio-dark");
        return rb;
    }

    private HBox pill(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("feature-pill");
        HBox box = new HBox(l);
        box.getStyleClass().add("feature-pill-box");
        return box;
    }

    private VBox miniStat(String label, String value) {
        VBox box = new VBox(4);
        box.getStyleClass().add("mini-stat");
        box.setPadding(new Insets(14, 18, 14, 18));

        Label lbl = styledLabel(label, 10, "#808080");
        lbl.setStyle("-fx-font-weight:700; -fx-letter-spacing:1.5;");

        Label val = styledLabel(
            (value == null || value.isBlank()) ? "—" : value, 14, "#FFFFFF");
        val.setWrapText(true);

        box.getChildren().addAll(lbl, val);
        return box;
    }

    private Circle glowCircle(double radius, String hex, double opacity) {
        Circle c = new Circle(radius);
        c.setFill(Color.web(hex, opacity));
        DropShadow glow = new DropShadow(radius * 0.9, Color.web(hex, 0.5));
        c.setEffect(glow);
        return c;
    }

    private File csvChooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fc.showOpenDialog(primaryStage);
    }

    private void attachCSS(Scene scene) {
        URL css = getClass().getResource("style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    /** Shorthand for creating a Timestamp from discrete date/time parts. */
    private static Timestamp ts(int y, int mo, int d, int h, int m) {
        return Timestamp.valueOf(LocalDateTime.of(y, mo, d, h, m));
    }

    // ════════════════════════════════════════════════════════════════════════
    // INNER CLASS — BucketSummary
    // Pre-computed stats for one bucket so the FX-thread dashboard builders
    // do not need to re-run SongStatistics after the analysis thread finishes.
    // ════════════════════════════════════════════════════════════════════════
    private static final class BucketSummary {
        final String              name;
        final String              topArtist;
        final String              topSong;
        final String              topGenre;
        final int                 totalPlays;
        final Map<String, Integer> genreFreq;

        BucketSummary(String name, String topArtist, String topSong,
                      String topGenre, int totalPlays, Map<String, Integer> genreFreq) {
            this.name       = name;
            this.topArtist  = topArtist;
            this.topSong    = topSong;
            this.topGenre   = topGenre;
            this.totalPlays = totalPlays;
            this.genreFreq  = genreFreq;
        }
    }
}