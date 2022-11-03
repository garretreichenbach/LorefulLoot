package thederpgamer.lorefulloot.data.other;

import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Segment;
import thederpgamer.lorefulloot.LorefulLoot;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class EntityHollowExecutor {

    private final static LongArrayList toRemove = new LongArrayList();
    private static final int MAX_THREADS = 64;

    public static void computeSegment(Segment segment) {
        SegmentController entity = segment.getSegmentController();
        Vector3b minPos = new Vector3b(segment.getSegmentData().getMin());
        Vector3b maxPos = new Vector3b(segment.getSegmentData().getMax());
        for(int x1 = minPos.x; x1 <= maxPos.x; x1++) {
            for(int y1 = minPos.y; y1 <= maxPos.y; y1++) {
                for(int z1 = minPos.z; z1 <= maxPos.z; z1++) {
                    long index = segment.getAbsoluteIndex((byte) x1, (byte) y1, (byte) z1);
                    if(entity.getSegmentBuffer().existsPointUnsave(index) && isSystemBlock(entity.getSegmentBuffer().getPointUnsave(index).getInfo())) {
                        //Check every adjacent block, if one of them doesn't exist continue, otherwise add to remove list
                        /*
                        boolean remove = true;
                        for(int x2 = x1 - 1; x2 <= x1 + 1; x2++) {
                            for(int y2 = y1 - 1; y2 <= y1 + 1; y2++) {
                                for(int z2 = z1 - 1; z2 <= z1 + 1; z2++) {
                                    if(x2 == x1 && y2 == y1 && z2 == z1) continue;
                                    Vector3i pos = new Vector3i(x2, y2, z2);
                                    if(!entity.getSegmentBuffer().existsPointUnsave(pos)) {
                                        remove = false;
                                        break;
                                    }
                                }
                                if(!remove) break;
                            }
                            if(!remove) break;
                        }
                        if(remove) toRemove.add(index);
                         */
                        toRemove.add(index);
                    }
                }
            }
        }
    }

    private static boolean isSystemBlock(ElementInformation info) {
        return info.systemBlock || info.isReactorChamberAny() || info.isDeprecated() || info.id == 3 || info.id == 478;
    }

    public static void compute(SegmentController entity, PlayerState playerState) throws InterruptedException, ExecutionException {
        LorefulLoot.log.log(Level.INFO, "Hollowing entity " + entity.getName() + "...");
        PlayerUtils.sendMessage(playerState, "Hollowing entity " + entity.getName() + "...");
        long start = System.currentTimeMillis();
        toRemove.clear();
        Vector3i min = new Vector3i(entity.getSegmentBuffer().getBoundingBox().min);
        Vector3i max = new Vector3i(entity.getSegmentBuffer().getBoundingBox().max);
        ObjectArrayList<Segment> segments = new ObjectArrayList<>();
        for(int x = min.x; x <= max.x; x++) {
            for(int y = min.y; y <= max.y; y++) {
                for(int z = min.z; z <= max.z; z++) {
                    final Segment segment = entity.getSegmentBuffer().get(x, y, z);
                    if(segment != null && segment.getSegmentData() != null) segments.add(segment);
                }
            }
        }
        ObjectArrayList<Callable<StarRunnable>> tasks = new ObjectArrayList<>();
        for(final Segment segment : segments) {
            tasks.add(new Callable<StarRunnable>() {
                @Override
                public StarRunnable call() throws Exception {
                    return new StarRunnable() {
                        @Override
                        public void run() {
                            try {
                                computeSegment(segment);
                            } catch(Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    };
                }
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        try {
            List<Future<StarRunnable>> results = executor.invokeAll(tasks);
            for(Future<StarRunnable> result : results) {
                result.get().run();
            }
        } finally {
            executor.shutdown();
            for(long index : toRemove) {
                try {
                    SegmentPiece piece = entity.getSegmentBuffer().getPointUnsave(index);
                    piece.setType((short) 0);
                    piece.applyToSegment(entity.isOnServer());
                } catch(Exception ignored) {}
            }
            long time = System.currentTimeMillis() - start;
            LorefulLoot.log.log(java.util.logging.Level.INFO, "Hollowed out entity " + entity.getName() + " in " + time + "ms.");
            PlayerUtils.sendMessage(playerState, "Hollowed out entity " + entity.getName() + " in " + time + "ms.");
        }
    }
}