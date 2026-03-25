package videogoose.lorefulloot;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.common.util.linAlg.Vector3i;
public class Test {
    public static void test(StellarSystem system) {
        Vector3i local = system.getLocalPos(new Vector3i());
    }
}
