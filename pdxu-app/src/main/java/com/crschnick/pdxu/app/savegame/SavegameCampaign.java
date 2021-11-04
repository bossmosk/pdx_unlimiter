package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.model.GameDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

public final class SavegameCampaign<T, I extends SavegameInfo<T>> extends SavegameCollection<T, I> {

    private final ObjectProperty<GameDate> date;
    private final ObjectProperty<Image> image;

    public SavegameCampaign(Instant lastPlayed, String name, UUID campaignId, GameDate date, Image image) {
        super(lastPlayed, name, campaignId);
        this.date = new SimpleObjectProperty<>(date);
        this.image = new SimpleObjectProperty<>(image);
    }

    @Override
    public void onSavegameLoad(SavegameEntry<T, I> entry) {
        if (entry == getLatestEntry()) {
            imageProperty().set(SavegameActions.createImageForEntry(entry));
            updateDate();
        }
    }

    @Override
    public void onSavegamesChange() {
        updateDate();
    }

    private void updateDate() {
        getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.naturalOrder())
                .map(s -> s.getInfo().getData().getDate())
                .ifPresent(d -> dateProperty().setValue(d));
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public SavegameEntry<T, I> getLatestEntry() {
        return entryStream().findFirst().get();
    }

    public Stream<SavegameEntry<T, I>> entryStream() {
        var list = new ArrayList<>(getSavegames());
        list.sort(Comparator.comparing(SavegameEntry::getDate));
        Collections.reverse(list);
        return list.stream();
    }

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
    }
}
