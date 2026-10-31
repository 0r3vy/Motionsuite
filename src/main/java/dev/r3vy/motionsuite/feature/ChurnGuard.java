package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.ConfigSnapshot;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class ChurnGuard {
    public static final int MAX_DEPTH = 3;

    private static final int LIMIT = 4096;
    private static final int HOLD_TICKS = 1200;
    private static final int SYNC_PER_TICK = 256;
    private static final int SYNC_FLAGS = Block.NOTIFY_ALL | Block.FORCE_STATE;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Long2ObjectLinkedOpenHashMap<Held> held = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectLinkedOpenHashMap<BlockState> pending = new Long2ObjectLinkedOpenHashMap<>();
    private final long[] syncKeys = new long[SYNC_PER_TICK];
    private final BlockState[] syncStates = new BlockState[SYNC_PER_TICK];

    private ClientWorld world;
    private long now;

    public synchronized boolean frozen(long pos) {
        Held entry = held.get(pos);
        return entry != null && entry.hard;
    }

    public synchronized int nearDepth(long pos) {
        int best = Integer.MAX_VALUE;
        for (Direction direction : DIRECTIONS) {
            Held entry = held.get(BlockPos.offset(pos, direction));
            if (entry != null && entry.hard && entry.depth < best) {
                best = entry.depth;
            }
        }
        return best;
    }

    public synchronized void hold(long pos, BlockState state, int depth, boolean hard) {
        Held old = held.remove(pos);
        if (old == null && held.size() >= LIMIT) {
            evictEldest();
        }
        boolean keepHard = old != null && old.hard;
        int nextDepth = keepHard ? Math.min(old.depth, depth) : depth;
        held.put(pos, new Held(state, nextDepth, hard || keepHard, now + HOLD_TICKS));
    }

    public synchronized void thaw(long pos) {
        held.remove(pos);
        pending.remove(pos);
    }

    public synchronized boolean isEmpty() {
        return held.isEmpty() && pending.isEmpty();
    }

    public synchronized void clear() {
        held.clear();
        pending.clear();
    }

    public void tick(MinecraftClient client, ConfigSnapshot config) {
        ClientWorld current = client.world;
        int batch;
        synchronized (this) {
            if (current != world) {
                held.clear();
                pending.clear();
                world = current;
                now = 0;
            }
            if (current == null) {
                return;
            }
            now++;
            boolean culling = config.farm().blockRenderCull();
            ObjectBidirectionalIterator<Long2ObjectMap.Entry<Held>> heldIterator =
                    held.long2ObjectEntrySet().iterator();
            while (heldIterator.hasNext()) {
                Long2ObjectMap.Entry<Held> entry = heldIterator.next();
                if (culling && entry.getValue().expires > now) {
                    break;
                }
                pending.put(entry.getLongKey(), entry.getValue().state);
                heldIterator.remove();
            }
            batch = 0;
            ObjectBidirectionalIterator<Long2ObjectMap.Entry<BlockState>> pendingIterator =
                    pending.long2ObjectEntrySet().iterator();
            while (pendingIterator.hasNext() && batch < SYNC_PER_TICK) {
                Long2ObjectMap.Entry<BlockState> entry = pendingIterator.next();
                syncKeys[batch] = entry.getLongKey();
                syncStates[batch] = entry.getValue();
                batch++;
                pendingIterator.remove();
            }
        }
        for (int index = 0; index < batch; index++) {
            BlockState state = syncStates[index];
            syncStates[index] = null;
            if (state.isOf(Blocks.MOVING_PISTON)) {
                continue;
            }
            BlockPos pos = BlockPos.fromLong(syncKeys[index]);
            if (current.getBlockState(pos) != state) {
                current.setBlockState(pos, state, SYNC_FLAGS);
            }
        }
    }

    private void evictEldest() {
        if (!held.isEmpty()) {
            Long2ObjectMap.Entry<Held> eldest = held.long2ObjectEntrySet().iterator().next();
            pending.put(eldest.getLongKey(), eldest.getValue().state);
            held.remove(eldest.getLongKey());
        }
    }

    private static final class Held {
        private final BlockState state;
        private final int depth;
        private final boolean hard;
        private final long expires;

        private Held(BlockState state, int depth, boolean hard, long expires) {
            this.state = state;
            this.depth = depth;
            this.hard = hard;
            this.expires = expires;
        }
    }
}
