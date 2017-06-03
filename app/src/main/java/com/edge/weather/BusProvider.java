package com.edge.weather;

import com.squareup.otto.Bus;

/**
 * Created by kim on 2017. 5. 15..
 */

public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}

