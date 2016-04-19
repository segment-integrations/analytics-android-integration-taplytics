package com.segment.analytics.android.integrations.taplytics;

import com.segment.analytics.Analytics;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Integration;
import com.taplytics.sdk.Taplytics;

/**
 * Created by williamjohnson on 4/19/16.
 */
public class TaplyticsIntegration extends Integration<Taplytics> {
    public static final Factory FACTORY = new Factory() {
        @Override public Integration<?> create(ValueMap settings, Analytics analytics) {
            return new TaplyticsIntegration(analytics, settings);
        }

        @Override public String key() {
            return TAPLYTICS_KEY;
        }
    };
    private static final String TAPLYTICS_KEY = "Taplytics";
    public TaplyticsIntegration(Analytics analytics, ValueMap settings){}
};
