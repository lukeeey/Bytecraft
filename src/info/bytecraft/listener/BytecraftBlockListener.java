package info.bytecraft.listener;

import info.bytecraft.Bytecraft;
import info.bytecraft.api.BytecraftPlayer;
import info.bytecraft.api.BytecraftPlayer.Flag;
import info.bytecraft.database.*;

import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BytecraftBlockListener implements Listener
{
    private Bytecraft plugin;

    public BytecraftBlockListener(Bytecraft plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        if (!player.getRank().canBuild()) {
            event.setCancelled(true);
            return;
        }

        if (player.hasFlag(Flag.HARDWARNED)) {
            event.setCancelled(true);
            return;
        }

        Location loc = event.getBlock().getLocation();
        try (IContext ctx = plugin.createContext()) {
            ILogDAO dbLog = ctx.getLogDAO();
            IPlayerDAO dbPlayer = ctx.getPlayerDAO();
            if (dbLog.isLegal(event.getBlock())) {
                dbPlayer.give(player, plugin.getValue(event.getBlock()));
            }
            dbLog.insertPaperLog(player, loc, event.getBlock().getType(),
                    "broke");
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        if (!player.getRank().canBuild()) {
            event.setCancelled(true);
            return;
        }

        if (player.hasFlag(Flag.HARDWARNED)) {
            event.setCancelled(true);
            return;
        }

        Location loc = event.getBlock().getLocation();
        try (IContext ctx = plugin.createContext()) {
            ILogDAO dao = ctx.getLogDAO();
            dao.insertPaperLog(player, loc, event.getBlock().getType(),
                    "placed");
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event)
    {
        if (event.getEntity() instanceof Creeper) {
            event.setCancelled(true);
            return;
        }
        else if (event.getEntityType() == EntityType.PRIMED_TNT) {
            event.setCancelled(true);
        }
    }
}
