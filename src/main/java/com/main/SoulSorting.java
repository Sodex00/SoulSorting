package com.main;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.type.Observer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SoulSorting extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SoulSorting v1.0 активирован!");
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        if (!(event.getDestination().getHolder() instanceof Hopper)) return;

        Hopper hopper = (Hopper) event.getDestination().getHolder();
        Inventory sourceInventory = event.getSource();
        Inventory hopperInventory = hopper.getInventory();
        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) {
            return;
        }

        if (isAllowedByFilters(item, hopper.getBlock())) {
            return;
        }

        event.setCancelled(true);

        for (ItemStack sourceItem : sourceInventory.getContents()) {
            if (sourceItem == null || sourceItem.getType().isAir() || sourceItem.getAmount() <= 0) continue;

            if (isAllowedByFilters(sourceItem, hopper.getBlock())) {
                ItemStack toMove = sourceItem.clone();
                toMove.setAmount(1); 
                if (hopperInventory.addItem(toMove).isEmpty()) {
                    sourceItem.setAmount(sourceItem.getAmount() - 1);
                    break;
                } else {
                    break;
                }
            } 
        }
    }

    private boolean isAllowedByFilters(ItemStack item, Block hopperBlock) {
        boolean hasFilters = false;

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relative = hopperBlock.getRelative(face);

            if (relative.getBlockData() instanceof Observer) {
                Observer observer = (Observer) relative.getBlockData();
                Block filterBlock = relative.getRelative(observer.getFacing());

                if (filterBlock.getState() instanceof org.bukkit.block.Chest) {
                    hasFilters = true;
                    Inventory filterInventory = ((org.bukkit.block.Chest) filterBlock.getState()).getInventory();

                    if (isItemInFilter(item, filterInventory)) {
                        return true;
                    }
                }
            }
        }

        return !hasFilters;
    }

    private boolean isItemInFilter(ItemStack item, Inventory filterInventory) {
        if (item == null) return false;
        for (ItemStack filterItem : filterInventory.getContents()) {
            if (filterItem != null && filterItem.getType() == item.getType()) {
                return true;
            }
        }
        return false;
    }
}