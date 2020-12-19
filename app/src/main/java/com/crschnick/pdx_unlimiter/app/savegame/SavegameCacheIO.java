package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class SavegameCacheIO {

    public static void exportSavegameCaches(Path out) {
        TaskExecutor.getInstance().submitTask(() -> {
            try {
                FileUtils.forceMkdir(out.toFile());
                for (SavegameCache<?, ?, ?, ?> cache : SavegameCache.ALL) {
                    Path cacheDir = out.resolve(cache.getName());
                    Files.createDirectory(cacheDir);
                    exportSavegameDirectory(cache, cacheDir);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }

    private static <T, I extends SavegameInfo<T>> void exportSavegameDirectory(SavegameCache<?,?,T,I> cache, Path out) throws IOException {
        for (GameCampaign<T, I> c : cache.getCampaigns()) {
            String cName = c.getName();
            for (GameCampaignEntry<T, I> e : c.getEntries()) {
                String name = cache.getEntryName(e);
                Path fileOut = out.resolve(cache.getName() + "/" + cName + " (" + name + ")." + cache.getFileEnding());
                cache.exportSavegame(e, fileOut);
            }
        }
    }
}