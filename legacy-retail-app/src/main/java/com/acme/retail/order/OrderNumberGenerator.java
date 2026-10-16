package com.acme.retail.order;

import com.acme.retail.common.DateUtil;

/** Order numbers look like ORD-20250115-0007. The sequence is held in memory and restarts with the server. */
public class OrderNumberGenerator {

    private static int counter = 0;

    public static synchronized String next() {
        counter++;
        return "ORD-" + DateUtil.compact() + "-" + String.format("%04d", counter);
    }
}
