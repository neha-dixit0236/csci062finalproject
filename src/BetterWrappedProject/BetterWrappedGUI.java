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
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BetterWrappedGUI — JavaFX front-end for the BetterWrapped backend.
 *
 * Flow: Landing → Config (window selection only) → Dashboard
 *
 * CSV paths are hardcoded; the BetterWrapped engine is pre-loaded on a
 * background thread while the user is on the Configuration screen.
 *
 * Changes in this revision
 * ─────────────────────────
 * 1. Hardcoded CSV paths + pre-load on "Get Started" click
 * 2. Config screen shows only the time-window toggle buttons (no file browsing)
 * 3. Feature 2 detail chart: dual-bar "Seasonal Daily Average vs Spike"
 * 4. Spotify dark-mode aesthetic preserved throughout
 *
 * @author GUI layer — CSCI 062 Final Project
 */
public class BetterWrappedGUI extends Application {

    // ═══════════════════════════════════════════════════════════════════
    // CONSTANTS — hardcoded CSV paths 
    // ═══════════════════════════════════════════════════════════════════
    private static final String HISTORY_CSV = "src/BetterWrappedProject/lastFmScrobblesDataSet.csv";
    private static final String REC_CSV     = "src/BetterWrappedProject/MasterListofSongs(Feature3).csv";

    // ─── Application state ───────────────────────────────────────────
    private Stage         primaryStage;
    private BetterWrapped wrappedEngine;          // pre-loaded in background

    private String selectedWindow  = "WEEKDAY_VS_WEEKEND";
    private String midtermRawText  = "";
    private String breakRawText    = "";

    // Results populated by the analysis thread, consumed by dashboard
    private final Map<String, BucketSummary>            summaryMap  = new LinkedHashMap<>();
    private final List<OutlierDay>                       outlierList = new ArrayList<>();
    private final Map<String, List<RecommendationSong>> recMap      = new LinkedHashMap<>();

    // Bucket list kept so showOutlierDetail can compute seasonal averages
    private List<Bucket> lastBuckets = new ArrayList<>();

    // ─── Entry point ─────────────────────────────────────────────────
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

    // ═══════════════════════════════════════════════════════════════════
    // SCREEN 1 — LANDING
    // ═══════════════════════════════════════════════════════════════════
    private void showLanding() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root");

        Rectangle backdrop = new Rectangle();
        backdrop.widthProperty().bind(root.widthProperty());
        backdrop.heightProperty().bind(root.heightProperty());
        backdrop.setFill(Color.web("#121212"));

        Circle c1 = glowCircle(340, "#1DB954", 0.18);  c1.setTranslateX(-280); c1.setTranslateY(-180);
        Circle c2 = glowCircle(260, "#b84ef5", 0.20);  c2.setTranslateX(320);  c2.setTranslateY(160);
        Circle c3 = glowCircle(200, "#ff4ecd", 0.15);  c3.setTranslateX(-60);  c3.setTranslateY(220);

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

        // Toast label — shown if CSVs are missing
        Label toastLabel = new Label("");
        toastLabel.getStyleClass().add("toast-error");
        toastLabel.setVisible(false);
        toastLabel.setManaged(false);

        Button startBtn = new Button("Get Started →");
        startBtn.getStyleClass().addAll("btn-primary", "btn-glow");
        startBtn.setOnAction(e -> {
            // ── Check that both CSV files exist before proceeding ──
            boolean histOk = new File(HISTORY_CSV).exists();
            boolean recOk  = new File(REC_CSV).exists();

            if (!histOk || !recOk) {
                String missing = (!histOk ? "'" + HISTORY_CSV + "' " : "")
                    + (!recOk  ? "'" + REC_CSV + "'" : "");
                toastLabel.setText("⚠  Missing file(s): " + missing.trim()
                    + " — place them in the correct directory.");
                toastLabel.setVisible(true);
                toastLabel.setManaged(true);
                return;
            }

            // Files exist — kick off background pre-load immediately,
            // then navigate to the config screen while it runs.
            preLoadEngine();
            showConfig();
        });

        content.getChildren().addAll(eyebrow, titleRow, tagline, pills, toastLabel, startBtn);
        root.getChildren().addAll(backdrop, c1, c2, c3, content);

        Scene scene = new Scene(root, 960, 660);
        attachCSS(scene);
        primaryStage.setScene(scene);
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRE-LOAD — initialises BetterWrapped engine in the background
    //            while the user is reading the config screen
    // ═══════════════════════════════════════════════════════════════════
    private void preLoadEngine() {
        new Thread(() -> {
            try {
                wrappedEngine = new BetterWrapped(HISTORY_CSV);
            } catch (Exception ex) {
                // Will be caught again gracefully when runAnalysis runs
                wrappedEngine = null;
            }
        }, "BetterWrapped-PreLoadThread").start();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SCREEN 2 — CONFIGURATION  (time-window selection only)
    // ═══════════════════════════════════════════════════════════════════
    private void showConfig() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        root.setTop(headerBar("Configure Your Wrapped", false));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-dark");

        VBox form = new VBox(32);
        form.setPadding(new Insets(48, 80, 48, 80));
        form.getStyleClass().add("form-container");

        // ── Section heading ──────────────────────────────────────────
        Label heading = sectionLabel("Choose Your Time Window");

        Label subHeading = styledLabel(
            "Select how you'd like to slice your listening history.", 14, "#808080");

        // ── Toggle-button trio ───────────────────────────────────────
        // Using ToggleButtons styled as pill-cards for a high-end feel
        ToggleGroup tg = new ToggleGroup();

        ToggleButton tbWW  = windowToggle("📅", "Weekday vs Weekend",
            "Compare your music taste\nacross the working week.", tg, "WEEKDAY_VS_WEEKEND");
        ToggleButton tbSEM = windowToggle("📚", "One Semester",
            "Split into Midterms, Breaks,\nand Normal days.", tg, "ONE_SEMESTER");
        ToggleButton tbFY  = windowToggle("🌍", "Full Year",
            "Spring · Summer · Fall\nall in one view.", tg, "FULL_YEAR");

        tbWW.setSelected(true);   // default

        HBox toggleRow = new HBox(16, tbWW, tbSEM, tbFY);
        toggleRow.setAlignment(Pos.CENTER);
        for (Node n : toggleRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── ONE_SEMESTER date panel ───────────────────────────────────
        TextArea midtermArea = new TextArea();
        midtermArea.setPromptText("One midterm deadline per line\ne.g.\n11-10\n11-24");
        midtermArea.setPrefRowCount(4);
        midtermArea.getStyleClass().add("text-area-dark");

        TextArea breakArea = new TextArea();
        breakArea.setPromptText("One break range per line  (start MM-DD  end MM-DD)\ne.g.\n11-26 11-28\n12-15 01-02");
        breakArea.setPrefRowCount(4);
        breakArea.getStyleClass().add("text-area-dark");

        VBox semPanel = new VBox(14,
            fieldLabel("Midterm / Final Dates  (MM-DD, one per line)"),
            midtermArea,
            fieldLabel("Break Date Ranges  (start MM-DD  end MM-DD, one pair per line)"),
            breakArea);
        semPanel.getStyleClass().add("card");
        semPanel.setPadding(new Insets(24));
        semPanel.setVisible(false);
        semPanel.setManaged(false);

        // ── FULL_YEAR info note ───────────────────────────────────────
        Label fyNote = styledLabel(
            "ℹ  January–April = Spring  ·  May–August = Summer  ·  September–December = Fall",
            13, "#606060");
        fyNote.setVisible(false);
        fyNote.setManaged(false);

        tg.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw == null) { tg.selectToggle(old); return; }   // prevent deselection
            selectedWindow = (String) nw.getUserData();
            boolean isSem = "ONE_SEMESTER".equals(selectedWindow);
            boolean isFY  = "FULL_YEAR".equals(selectedWindow);
            semPanel.setVisible(isSem);  semPanel.setManaged(isSem);
            fyNote.setVisible(isFY);     fyNote.setManaged(isFY);
        });

        // ── Status / Generate ─────────────────────────────────────────
        Label statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");

        Button generateBtn = new Button("✦ Generate My Better Wrapped");
        generateBtn.getStyleClass().addAll("btn-primary", "btn-glow");
        generateBtn.setMaxWidth(560);

        generateBtn.setOnAction(e -> {
            // Capture FX-thread text values before spawning the analysis thread
            midtermRawText = midtermArea.getText();
            breakRawText   = breakArea.getText();

            statusLabel.setText("⏳  Analysing your listening history — please wait…");
            statusLabel.setStyle("-fx-text-fill:#A0A0A0;");
            generateBtn.setDisable(true);

            runAnalysis(generateBtn, statusLabel);
        });

        VBox btnWrapper = new VBox(12, generateBtn, statusLabel);
        btnWrapper.setAlignment(Pos.CENTER);

        form.getChildren().addAll(heading, subHeading, toggleRow, semPanel, fyNote, btnWrapper);
        scroll.setContent(form);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 960, 660);
        attachCSS(scene);
        primaryStage.setScene(scene);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ANALYSIS THREAD
    // ═══════════════════════════════════════════════════════════════════
    private void runAnalysis(Button btn, Label statusLabel) {
        new Thread(() -> {
            try {
                // Wait briefly if pre-load hasn't finished yet (max 8 s)
                int waited = 0;
                while (wrappedEngine == null && waited < 80) {
                    Thread.sleep(100);
                    waited++;
                }
                if (wrappedEngine == null)
                    wrappedEngine = new BetterWrapped(HISTORY_CSV);   // fallback

                List<KeyValuePair> history = wrappedEngine.getAllHistory();
                if (history == null || history.isEmpty())
                    throw new Exception("History CSV is empty or could not be read.");

                int year = history.get(0).getTimeStamp().toLocalDateTime().getYear();

                List<Timestamp> mt = new ArrayList<>();
                List<Timestamp> br = new ArrayList<>();
                List<Timestamp> sp = new ArrayList<>();
                List<Timestamp> su = new ArrayList<>();
                List<Timestamp> fa = new ArrayList<>();

                if ("ONE_SEMESTER".equals(selectedWindow)) {
                    parseSemesterDates(year, mt, br);
                } else if ("FULL_YEAR".equals(selectedWindow)) {
                    sp.add(ts(year,  1,  1,  0,  0));  sp.add(ts(year,  4, 30, 23, 59));
                    su.add(ts(year,  5,  1,  0,  0));  su.add(ts(year,  8, 31, 23, 59));
                    fa.add(ts(year,  9,  1,  0,  0));  fa.add(ts(year, 12, 31, 23, 59));
                }

                lastBuckets = buildBuckets(mt, br, sp, su, fa);
                collectSummaries();
                collectOutliers();
                collectRecommendations();

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

    private void parseSemesterDates(int year, List<Timestamp> mt, List<Timestamp> br)
            throws Exception {
        for (String line : midtermRawText.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] p = line.split("-");
            if (p.length < 2) continue;
            mt.add(Timestamp.valueOf(LocalDateTime.of(
                year, Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), 23, 59)));
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

    // ── Data collectors ──────────────────────────────────────────────

    private void collectSummaries() {
        summaryMap.clear();
        for (Bucket b : lastBuckets) {
            SongStatistics stats = new SongStatistics(b.getPlays());
            summaryMap.put(b.getName(), new BucketSummary(
                b.getName(), stats.getTopArtist(), stats.getTopSong(),
                stats.getTopGenre(), b.getPlays().size(),
                buildGenreFreqMap(b.getPlays())));
        }
    }

    private void collectOutliers() {
        outlierList.clear();
        outlierList.addAll(new OutlierDetector(lastBuckets, 4).findOutliers());
    }

    private void collectRecommendations() {
        recMap.clear();
        List<RecommendationSong> pool = RecommendationLoader.loadSongs(REC_CSV);
        recMap.putAll(new RecommendationEngine(pool, wrappedEngine.getAllHistory())
            .recommendSongs(lastBuckets));
    }

    // ── Bucketing helpers ────────────────────────────────────────────

    private List<Bucket> buildBuckets(List<Timestamp> mt, List<Timestamp> br,
                                      List<Timestamp> sp, List<Timestamp> su,
                                      List<Timestamp> fa) {
        switch (selectedWindow) {
            case "ONE_SEMESTER": return bucketSemester(mt, br);
            case "FULL_YEAR":    return bucketYear(sp, su, fa);
            default:             return bucketWeekdayWeekend();
        }
    }

    private List<Bucket> bucketWeekdayWeekend() {
        Bucket weekday = new Bucket("WEEKDAY"), weekend = new Bucket("WEEKEND");
        for (KeyValuePair kvp : wrappedEngine.getAllHistory()) {
            DayOfWeek d = kvp.getTimeStamp().toLocalDateTime().getDayOfWeek();
            if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) weekend.addPlay(kvp);
            else                                                    weekday.addPlay(kvp);
        }
        return asList(weekday, weekend);
    }

    private List<Bucket> bucketSemester(List<Timestamp> midterms, List<Timestamp> breaks) {
        Bucket mid = new Bucket("MIDTERM"), brk = new Bucket("BREAK"), norm = new Bucket("NORMAL");
        for (KeyValuePair kvp : wrappedEngine.getAllHistory()) {
            Timestamp st = kvp.getTimeStamp();
            if      (isWithinDateRange(st, breaks))      brk.addPlay(kvp);
            else if (isWithinWindow(st, midterms, 5))    mid.addPlay(kvp);
            else                                         norm.addPlay(kvp);
        }
        return asList(mid, brk, norm);
    }

    private List<Bucket> bucketYear(List<Timestamp> sp, List<Timestamp> su, List<Timestamp> fa) {
        Bucket spring = new Bucket("SPRING"), summer = new Bucket("SUMMER"), fall = new Bucket("FALL");
        for (KeyValuePair kvp : wrappedEngine.getAllHistory()) {
            Timestamp st = kvp.getTimeStamp();
            if      (isWithinDateRange(st, sp)) spring.addPlay(kvp);
            else if (isWithinDateRange(st, su)) summer.addPlay(kvp);
            else if (isWithinDateRange(st, fa))   fall.addPlay(kvp);
        }
        return asList(spring, summer, fall);
    }

    private boolean isWithinDateRange(Timestamp t, List<Timestamp> dates) {
        if (dates == null || dates.size() < 2) return false;
        for (int i = 0; i + 1 < dates.size(); i += 2)
            if (!t.before(dates.get(i)) && !t.after(dates.get(i + 1))) return true;
        return false;
    }

    private boolean isWithinWindow(Timestamp t, List<Timestamp> deadlines, int daysBefore) {
        if (deadlines == null || deadlines.isEmpty()) return false;
        for (Timestamp d : deadlines) {
            Timestamp start = Timestamp.valueOf(d.toLocalDateTime().minusDays(daysBefore));
            if (!t.before(start) && !t.after(d)) return true;
        }
        return false;
    }

    private Map<String, Integer> buildGenreFreqMap(List<KeyValuePair> plays) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (KeyValuePair kvp : plays)
            m.merge(kvp.getSongObject().getGenre(), 1, Integer::sum);
        return m;
    }

    /** Returns the Bucket from lastBuckets whose name matches bucketName. */
    private Optional<Bucket> findBucket(String bucketName) {
        return lastBuckets.stream()
            .filter(b -> b.getName().equals(bucketName))
            .findFirst();
    }

    /**
     * Computes the seasonal daily average play-count for every genre present
     * in the given bucket.
     *
     * Formula: totalGenrePlays / uniqueDaysInBucket
     *
     * Returns a Map<genre, averagePlaysPerDay>.
     */
    private Map<String, Double> computeSeasonalDailyAverages(Bucket bucket) {
        List<KeyValuePair> plays = bucket.getPlays();

        // Count unique days
        Set<LocalDate> uniqueDays = plays.stream()
            .map(kvp -> kvp.getTimeStamp().toLocalDateTime().toLocalDate())
            .collect(Collectors.toSet());
        int numDays = Math.max(1, uniqueDays.size());   // guard against /0

        // Total plays per genre across the whole bucket
        Map<String, Integer> genreTotals = new LinkedHashMap<>();
        for (KeyValuePair kvp : plays)
            genreTotals.merge(kvp.getSongObject().getGenre(), 1, Integer::sum);

        // Divide each total by the number of unique days
        Map<String, Double> averages = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : genreTotals.entrySet())
            averages.put(e.getKey(), (double) e.getValue() / numDays);

        return averages;
    }

    // ═══════════════════════════════════════════════════════════════════
    // SCREEN 3 — DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
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

    // ── Tab 1: Listening Stats ────────────────────────────────────────
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
            miniStat("🎤 TOP ARTIST", bs.topArtist),
            miniStat("🎵 TOP SONG",   bs.topSong),
            miniStat("🎸 TOP GENRE",  bs.topGenre),
            miniStat("▶ TOTAL PLAYS", String.valueOf(bs.totalPlays)));
        statsRow.setAlignment(Pos.CENTER_LEFT);
        for (Node n : statsRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

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
            ? "✅ Top song: " + a.topSong
            : "🔄 Song: " + a.topSong + " → " + b.topSong;

        VBox info = new VBox(6,
            styledLabel(a.name + "  vs  " + b.name, 14, "#FFFFFF"),
            styledLabel(genreLine, 12, "#A0A0A0"),
            styledLabel(artistLine, 12, "#A0A0A0"),
            styledLabel(songLine, 12, "#A0A0A0"));

        HBox row = new HBox(info);
        row.setPadding(new Insets(12));
        row.getStyleClass().add("trend-row");
        return row;
    }

    // ── Tab 2: Anomaly Feed ───────────────────────────────────────────
    private Tab buildAnomalyTab() {
        Tab tab = new Tab("🔍  Anomaly Feed");

        SplitPane split = new SplitPane();
        split.getStyleClass().add("split-dark");

        ScrollPane feedScroll = new ScrollPane();
        feedScroll.setFitToWidth(true);
        feedScroll.getStyleClass().add("scroll-pane-dark");
        VBox feed = new VBox(10);
        feed.setPadding(new Insets(20));

        VBox detailPane = new VBox(20);
        detailPane.setPadding(new Insets(24));
        detailPane.getStyleClass().add("root");
        detailPane.getChildren().add(
            styledLabel("← Click an anomaly to see\nthe genre spike vs. normal.", 14, "#606060"));

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

    private Button buildOutlierChip(OutlierDay od) {
        String label = od.getDate()
            + "  ·  " + od.getDayGenre()
            + "  ×" + od.getPlayCount()
            + "  (usual: " + od.getBaselineGenre()
            + "  in " + od.getBucketName() + ")";
        Button b = new Button(label);
        b.getStyleClass().add("outlier-chip");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    /**
     * Feature 2 detail — side-by-side comparison chart.
     *
     * For every genre that appears either on the outlier day or in the seasonal
     * averages, we render two bars:
     *   Series "Seasonal Avg"  — the genre's average plays per day in the bucket
     *                            (muted grey, CSS class .bar-average)
     *   Series "Spike Day"     — the genre's actual play count on the outlier day
     *                            (neon green/pink, CSS class .bar-spike)
     *
     * The seasonal daily average is computed as:
     *   totalGenrePlaysInBucket / numberOfUniqueDaysInBucket
     */
    private void showOutlierDetail(OutlierDay od, VBox pane) {
        pane.getChildren().clear();

        // ── Header labels ──
        Label titleLbl = styledLabel("📅 " + od.getDate(), 18, "#ff4ecd");
        titleLbl.setStyle("-fx-font-weight:700;");

        Label subLbl = styledLabel(
            "Spike genre: " + od.getDayGenre()
            + "  ·  Baseline: " + od.getBaselineGenre()
            + "  ·  Bucket: " + od.getBucketName(),
            13, "#A0A0A0");

        // ── Retrieve the parent bucket ──
        Optional<Bucket> bucketOpt = findBucket(od.getBucketName());

        // ── Build genre sets ──
        // "Spike day" counts: we only know the outlier genre count from OutlierDay.
        // We treat every other genre on the chart as having 0 spike-day plays.
        Map<String, Integer> spikeMap = new LinkedHashMap<>();
        spikeMap.put(od.getDayGenre(), od.getPlayCount());

        // Seasonal daily averages: computed from the full bucket
        Map<String, Double> avgMap = new LinkedHashMap<>();
        if (bucketOpt.isPresent()) {
            avgMap = computeSeasonalDailyAverages(bucketOpt.get());
        } else {
            // Fallback: just show the baseline genre at 0
            avgMap.put(od.getBaselineGenre(), 0.0);
        }

        // Union of genres we want to display: top-6 from avgMap + outlier genre
        Set<String> genreSet = new LinkedHashSet<>();
        genreSet.add(od.getDayGenre());            // outlier genre first
        genreSet.add(od.getBaselineGenre());       // baseline genre second
        // add top genres from the seasonal averages (limit total to 6)
        avgMap.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(6)
            .forEach(e -> genreSet.add(e.getKey()));

        // ── Build chart ──
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("Genre");
        yAxis.setLabel("Plays");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Spike vs. Seasonal Average  —  " + od.getDate());
        chart.getStyleClass().add("spike-comparison-chart");
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setCategoryGap(18);
        chart.setBarGap(4);
        chart.setPrefHeight(320);

        // Series 1 — Seasonal Daily Average (grey)
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Seasonal Avg");

        // Series 2 — Spike Day (neon green / pink)
        XYChart.Series<String, Number> spikeSeries = new XYChart.Series<>();
        spikeSeries.setName("Spike Day");

        final Map<String, Double> finalAvgMap = avgMap;
        for (String genre : genreSet) {
            double avg   = finalAvgMap.getOrDefault(genre, 0.0);
            int    spike = spikeMap.getOrDefault(genre, 0);
            avgSeries.getData().add(new XYChart.Data<>(genre, avg));
            spikeSeries.getData().add(new XYChart.Data<>(genre, spike));
        }

        // Add series BEFORE styling nodes (nodes don't exist until after add)
        chart.getData().addAll(avgSeries, spikeSeries);

        // Apply CSS style classes to the individual bar nodes on the FX thread
        // after the chart has been laid out.
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : avgSeries.getData()) {
                if (d.getNode() != null)
                    d.getNode().getStyleClass().addAll("bar-average");
            }
            for (XYChart.Data<String, Number> d : spikeSeries.getData()) {
                if (d.getNode() != null)
                    d.getNode().getStyleClass().addAll("bar-spike");
            }
        });

        // ── Legend row ──
        HBox legend = new HBox(24,
            legendDot("#404040", "Seasonal Daily Avg"),
            legendDot("#1DB954", "Spike Day"));
        legend.setAlignment(Pos.CENTER_LEFT);

        pane.getChildren().addAll(titleLbl, subLbl, chart, legend);
    }

    // ── Tab 3: Discover Weekly ────────────────────────────────────────
    private Tab buildDiscoverTab() {
        Tab tab = new Tab("🎵  Discover Weekly");
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane-dark");

        VBox content = new VBox(36);
        content.setPadding(new Insets(32));

        if (recMap.isEmpty()) {
            content.getChildren().add(styledLabel("No recommendations generated.", 14, "#A0A0A0"));
        } else {
            for (Map.Entry<String, List<RecommendationSong>> e : recMap.entrySet())
                content.getChildren().add(buildRecSection(e.getKey(), e.getValue()));
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

    private VBox buildSongCard(RecommendationSong song) {
        VBox card = new VBox(6);
        card.getStyleClass().add("song-card");
        card.setPadding(new Insets(14));
        card.setMinWidth(200);

        Label icon   = styledLabel("♪", 22, "#1DB954");
        Label title  = styledLabel(song.getSongName(), 13, "#FFFFFF");
        title.setWrapText(true);
        title.setStyle("-fx-font-weight:600;");
        Label artist = styledLabel(song.getArtist(), 11, "#A0A0A0");
        Label genre  = new Label(song.getGenre());
        genre.getStyleClass().add("genre-pill");

        card.getChildren().addAll(icon, title, artist, genre);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════
    // SHARED UI COMPONENT FACTORIES
    // ═══════════════════════════════════════════════════════════════════

    /** @param showBack true on Dashboard; false on Config screen */
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

    /**
     * A tall toggle-button card used for time-window selection.
     * Icon + title line + subtitle description.
     */
    private ToggleButton windowToggle(String icon, String titleText,
                                      String description, ToggleGroup tg, String userData) {
        Label iconLbl = styledLabel(icon, 28, "#FFFFFF");
        Label titleLbl = styledLabel(titleText, 14, "#FFFFFF");
        titleLbl.setStyle("-fx-font-weight:700;");
        Label descLbl = styledLabel(description, 11, "#808080");
        descLbl.setWrapText(true);
        descLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox inner = new VBox(8, iconLbl, titleLbl, descLbl);
        inner.setAlignment(Pos.CENTER);

        ToggleButton tb = new ToggleButton();
        tb.setGraphic(inner);
        tb.setToggleGroup(tg);
        tb.setUserData(userData);
        tb.getStyleClass().add("window-toggle");
        tb.setMaxWidth(Double.MAX_VALUE);
        tb.setPrefHeight(140);
        return tb;
    }

    private Label styledLabel(String text, double size, String hex) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px; -fx-text-fill:" + hex + ";");
        return l;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:22px; -fx-font-weight:800;" +
                   "-fx-text-fill:#FFFFFF; -fx-font-family:'Georgia';");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:600; -fx-text-fill:#CCCCCC;");
        return l;
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
        Label val = styledLabel((value == null || value.isBlank()) ? "—" : value, 14, "#FFFFFF");
        val.setWrapText(true);
        box.getChildren().addAll(lbl, val);
        return box;
    }

    /** Small legend dot + label used in the outlier detail chart. */
    private HBox legendDot(String hexColor, String labelText) {
        Region dot = new Region();
        dot.setMinSize(12, 12);
        dot.setMaxSize(12, 12);
        dot.setStyle("-fx-background-color:" + hexColor
            + "; -fx-background-radius:50%;");
        Label lbl = styledLabel(labelText, 12, "#A0A0A0");
        HBox row = new HBox(8, dot, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Circle glowCircle(double radius, String hex, double opacity) {
        Circle c = new Circle(radius);
        c.setFill(Color.web(hex, opacity));
        c.setEffect(new DropShadow(radius * 0.9, Color.web(hex, 0.5)));
        return c;
    }

    private void attachCSS(Scene scene) {
        URL css = getClass().getResource("style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    private static Timestamp ts(int y, int mo, int d, int h, int m) {
        return Timestamp.valueOf(LocalDateTime.of(y, mo, d, h, m));
    }

    @SafeVarargs
    private static <T> List<T> asList(T... items) {
        List<T> l = new ArrayList<>();
        Collections.addAll(l, items);
        return l;
    }

    // ═══════════════════════════════════════════════════════════════════
    // INNER CLASS — BucketSummary
    // ═══════════════════════════════════════════════════════════════════
    private static final class BucketSummary {
        final String              name, topArtist, topSong, topGenre;
        final int                 totalPlays;
        final Map<String, Integer> genreFreq;

        BucketSummary(String name, String topArtist, String topSong,
                      String topGenre, int totalPlays, Map<String, Integer> genreFreq) {
            this.name = name; this.topArtist = topArtist; this.topSong = topSong;
            this.topGenre = topGenre; this.totalPlays = totalPlays; this.genreFreq = genreFreq;
        }
    }
}