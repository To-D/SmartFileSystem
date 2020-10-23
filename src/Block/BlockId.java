package Block;

import interfaces.Id;

public class BlockId implements Id {
    private int id; // 1,2,3,…

    public BlockId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
