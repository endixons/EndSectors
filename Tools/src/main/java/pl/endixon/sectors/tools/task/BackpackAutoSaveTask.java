package pl.endixon.sectors.tools.task;

import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import pl.endixon.sectors.tools.EndSectorsToolsPlugin;
import pl.endixon.sectors.tools.backpack.BackpackService;
import pl.endixon.sectors.tools.user.profile.cache.ProfileBackpackCache;
import pl.endixon.sectors.tools.utils.LoggerUtil;

@RequiredArgsConstructor
public class BackpackAutoSaveTask extends BukkitRunnable {

    private final BackpackService backpackService;

    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        int savedCount = 0;

        try {
            for (var backpack : ProfileBackpackCache.getValues()) {
                if (backpack == null) continue;

                this.backpackService.saveBackpack(backpack);
                savedCount++;
            }

            if (savedCount > 0) {
                final long end = System.currentTimeMillis();
                LoggerUtil.info("[Backpack-AutoSave] Zsynchronizowano {} plecaków w {}ms.", savedCount, (end - start));
            }
        } catch (Exception e) {
            LoggerUtil.error("[Backpack-AutoSave] Krytyczny błąd podczas auto-zapisu!", e);
        }
    }
}