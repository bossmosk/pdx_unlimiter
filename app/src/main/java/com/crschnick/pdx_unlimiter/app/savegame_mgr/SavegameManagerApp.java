package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.Main;
import com.crschnick.pdx_unlimiter.app.installation.Eu4App;
import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SavegameManagerApp extends Application {

    private static Tooltip tooltip(String text) {

        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.ZERO);
        return t;
    }

    private static HBox createRulerLabel(Eu4Campaign.Entry.Ruler ruler, boolean isRuler) {
        HBox box = new HBox();
        box.setSpacing(0);
        box.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(box, tooltip((isRuler ? "Ruler: " : "Heir: ") + ruler.getName()));
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        if (isRuler) {
            var i = Eu4ImageLoader.loadInterfaceImage("tab_domestic_court.dds");
            i.setViewport(new Rectangle2D(8, 10, 30, 30));
            box.getChildren().add(i);
        } else {
            box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("monarch_heir_crown_icon.dds"));
        }

        Label adm = new Label(" " + ruler.getAdm());
        adm.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        box.getChildren().add(adm);
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_administrative_in_text.dds"));


        Label dip = new Label("/ " + ruler.getDip());
        dip.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        box.getChildren().add(dip);
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_diplomatic_in_text.dds"));

        Label mil = new Label("/ " + ruler.getMil());
        mil.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        box.getChildren().add(mil);
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_military_in_text.dds"));
        box.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        return box;
    }

    private GridPane createCampaignEntryNode(Eu4Campaign.Entry e) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setMaxHeight(120);
        grid.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");

        Label name = new Label(e.getDate().toString());
        name.setAlignment(Pos.CENTER);
        name.setPadding(new Insets(5, 5, 5, 5));
        name.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        grid.add(name, 0, 0, 3, 1);

        Label date = new Label( e.getDate().toString());
        date.setPrefHeight(200);
        date.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");

        grid.add(date, 0, 1);
        grid.add(createRulerLabel(e.getRuler(), true), 0, 2);
        if (e.getHeir().isPresent()) {
            grid.add(createRulerLabel(e.getHeir().get(), false), 0, 3);
        }


        Label version = new Label("v" + e.getVersion().toString());
        version.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        grid.add(version, 0, 4);


        Tooltip t = new Tooltip("Test");
        t.setShowDelay(Duration.ZERO);
        int wars = 0;
        for (Eu4Campaign.Entry.War war : e.getWars()) {
            if (wars >= 3) {
                break;
            }
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", war.getEnemies(), "Fighting in the " + war.getTitle() + " against ", ""), 1 + wars, 1);
            wars++;
        }
        grid.add(createDiplomacyRow("icon_alliance.dds", e.getAllies(), "Allies: ", "None"), 1, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_royalmarriage.dds", e.getMarriages(), "Royal marriages: ", "None"), 2, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_guaranting.dds", e.getGuarantees(), "Guarantees: ", "None"), 3, 2);
        grid.add(createDiplomacyRow("icon_vassal.dds", e.getVassals(), "Vassals: ", "None"), 1, 3);
        grid.add(createDiplomacyRow("icon_vassal.dds", e.getJuniorPartners(), "Personal union junior partners: ", "none"), 2, 3);
        grid.add(createDiplomacyRow("subject_tributary_icon.dds", e.getTributaryJuniors(), "Tributaries: ", "None"), 3, 3);
        grid.add(createDiplomacyRow("icon_march.dds", e.getMarches(), "Marches: ", "None"), 1, 4);
        grid.add(createDiplomacyRow("icon_truce.dds", e.getTruces().keySet(), "Truces: ", "None"), 2, 4);
        if (e.getSeniorPartner().isPresent()) {
            grid.add(createDiplomacyRow("icon_alliance.dds", Set.of(e.getSeniorPartner().get()), "Under personal union with ", "no country"), 4, 4);
        }
        return grid;
    }

    private String getCountryTooltip(Set<String> tags) {
        StringBuilder b = new StringBuilder();
        for (String s : tags) {
            b.append(Installation.EU4.get().getCountryName(s));
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private Node createDiplomacyRow(String icon, Set<String> tags, String tooltipStart, String none) {
        HBox box = new HBox();
        box.setSpacing(3);
        box.setAlignment(Pos.CENTER_LEFT);
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage(icon));
        for (String tag : tags) {
            ImageView n = Eu4ImageLoader.loadFlagImage(tag, 20);
            box.getChildren().add(n);
        }
        box.setStyle("-fx-background-color: #777777; -fx-border-color: #666666; -fx-border-width: 3px;");
        box.setPadding(new Insets(0, 5, 0, 0));
        box.setMaxHeight(40);
        Tooltip.install(box, tooltip(tooltipStart + (tags.size() > 0 ? getCountryTooltip(tags) : none)));
        return box;
    }

    private void openCampaign(Eu4Campaign c) {
        SavegameCache.EU4_CACHE.load(c);
        layout.setCenter(createSavegameList(c));
    }

    private Button createCampaignButton(Eu4Campaign c) {
        ImageView w = Eu4ImageLoader.loadFlagImage(c.getTag(), 35);

        Pane countryColor = new Pane();
        countryColor.setStyle("-fx-background-color: black;");
        countryColor.setPrefSize(12,12);
        Button btn = new Button("", w);
        btn.setText("Name\nasd");
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                openCampaign(c);
            }
        });
        btn.setPadding(new Insets(5, 5, 5, 5));
        btn.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        btn.setBorder(Border.EMPTY);
        return btn;
    }

    private Node createSavegameList(Eu4Campaign c) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        for (Eu4Campaign.Entry e : c.getSavegames()) {
            GridPane button = createCampaignEntryNode(e);
            button.prefWidthProperty().bind(grid.widthProperty());
            button.getProperties().put("entry", e);
            grid.getChildren().add(button);
        }

        for (Node entryNode : grid.getChildren()) {
            entryNode.setOnMouseClicked((m) -> {
                entryNode.setStyle("-fx-background-color: #666666; -fx-border-color: #44bb44; -fx-border-width: 3px;");
                SavegameManagerApp.this.selectedSave = Optional.of((Eu4Campaign.Entry) entryNode.getProperties().get("entry"));
                for (Node other : grid.getChildren()) {
                    if (other.equals(entryNode)) {
                        continue;
                    }

                    launchButton.ifPresent((b) -> b.setText("Start " + ((Eu4Campaign.Entry) entryNode.getProperties().get("entry")).getCurrentTag()));
                    other.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");
                }
            });
        }

        ScrollPane pane = new ScrollPane(grid);
        pane.setFitToWidth(true);
        pane.setStyle("-fx-focus-color: transparent;");
        return pane;
    }

    private Node createCampaignList(List<Eu4Campaign> data) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        for (Eu4Campaign d : data) {
            Button button = createCampaignButton(d);
            button.setMinWidth(200);
            grid.getChildren().add(button);
        }
        ScrollPane pane = new ScrollPane(grid);
        pane.setMinViewportWidth(200);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return pane;
    }


    private Node createStatusBar(Eu4Installation i, Optional<PdxApp> app) {
        BorderPane pane = new BorderPane();
        if (app.isPresent()) {
            pane.setStyle("-fx-border-color: #339933; -fx-background-color: #33aa33;");
        } else {
            pane.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;");
        }

        Label text = new Label(i.getName(), new ImageView(Eu4ImageLoader.loadImage(Installation.EU4.get().getPath().resolve("launcher-assets").resolve("icon.png"))));
        text.setAlignment(Pos.BOTTOM_CENTER);
        text.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setLeft(text);

        Label status = new Label("Status: " + (app.isPresent() ? "Running" : "Stopped"));
        status.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setCenter(status);

        if (app.isPresent()) {
            Button b = new Button("Kill");
            b.setOnMouseClicked((m) -> {
                app.get().kill();
            });
            b.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;-fx-text-fill: white; -fx-font-size: 18px;");
            pane.setRight(b);
        } else {
            Button b = new Button("Start" + (selectedSave.isPresent() ? " " + selectedSave.get().getCurrentTag() : ""));
            b.setOnMouseClicked((m) -> {

            });
            launchButton = Optional.of(b);
            b.setStyle("-fx-border-color: #339933; -fx-background-radius: 0; -fx-border-radius: 0; -fx-background-color: #33aa33;-fx-text-fill: white; -fx-font-size: 18px;");
            pane.setRight(b);
        }
        return pane;
    }

    private void createStatusThread(BorderPane layout) {
        Thread t = new Thread(() -> {
            Platform.runLater(() -> layout.setBottom(createStatusBar(Installation.EU4.get(), Optional.empty())));

            Optional<PdxApp> oldApp = Optional.empty();
            while (true) {

                if (!oldApp.equals(PdxApp.getActiveApp())) {
                    if (PdxApp.getActiveApp().isPresent()) {
                        Platform.runLater(() -> layout.setBottom(createStatusBar(Installation.EU4.get(), PdxApp.getActiveApp())));
                    } else {
                        Optional<PdxApp> finalOldApp = oldApp;
                        Platform.runLater(() -> layout.setBottom(createStatusBar(Installation.EU4.get(), Optional.empty())));
                    }
                    oldApp = PdxApp.getActiveApp();
                 }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private BorderPane layout;

    private Optional<Button> launchButton = Optional.empty();

    private Optional<Eu4Campaign.Entry> selectedSave = Optional.empty();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(Eu4ImageLoader.loadImage(Installation.EU4.get().getPath().resolve("launcher-assets").resolve("icon.png")));

        layout = new BorderPane();

        createStatusThread(layout);

        layout.setLeft(createCampaignList(SavegameCache.EU4_CACHE.getCampaigns()));

        primaryStage.setTitle("Pdx Unlimiter");
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
