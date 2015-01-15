package MSR605;

import java.util.EventObject;

/**
 * Created by Admin on 10/28/2014.
 */
public class MSR605Event extends EventObject {

    private MSR605.MSR_EVENT type;

    public MSR605Event(Object source, MSR605.MSR_EVENT type) {
        super(source);
        this.type = type;
    }

    public MSR605.MSR_EVENT getType() {
        return type;
    }

    public String toString() {
        return super.toString()+" Type["+type.toString()+"]";
    }


}
