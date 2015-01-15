package MSR605;

/**
 * Created by bryan_000 on 10/25/2014.
 */
public class test implements MSR605EventListener {
    @Override
    public void MSR605EventFire(MSR605Event event) {
        System.out.println("EVENT TYPE: " + event.getType().toString());
    }

    public test() {

    }
}
