package com.segment.analytics.android.integrations.taplytics;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.taplytics.sdk.Taplytics;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.segment.analytics.internal.Utils.transform;

/**
 * Created by williamjohnson on 4/19/16.
 */
public class TaplyticsIntegration extends Integration<Taplytics> {
    public static final Factory FACTORY = new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
            return new TaplyticsIntegration(analytics, settings);
        }

        @Override
        public String key() {
            return TAPLYTICS_KEY;
        }
    };

    private static final String TAPLYTICS_KEY = "Taplytics";
    private static final String TAPLYTICS_KEY_API_KEY = "apiKey";
    private static final String TAPLYTICS_OPTION_LIVE_UPDATE = "liveUpdate";
    private static final String TAPLYTICS_OPTION_SHAKE_MENU = "shakeMenu";
    private static final String TAPLYTICS_OPTION_TURN_MENU = "turnMenu";
    private static final String TAPLYTICS_OPTION_SESSION_MINUTES = "sessionMinutes";
    private static final String TAPLYTICS_OPTION_DELAYED_START = "delayedStartTaplytics";

     static final int DISABLED = 0;
     static final int ENABLED = 1;
     static final int DEFAULT = 2;


    private static final Map<String, String> MAPPER;

    static {
        Map<String, String> mapper = new LinkedHashMap<>();
        mapper.put("firstName", "firstname");
        mapper.put("lastName", "lastname");
        mapper.put("userId", "user_id");
        MAPPER = Collections.unmodifiableMap(mapper);
    }

    private final Logger logger;

    int liveUpdate;
    int shakeMenu;
    int turnMenu;
    int sessionMinutes;

    TaplyticsIntegration(Analytics analytics, ValueMap settings) {
        logger = analytics.logger(TAPLYTICS_KEY);
        String apiKey = settings.getString(TAPLYTICS_KEY_API_KEY);
        liveUpdate = settings.getInt(TAPLYTICS_OPTION_LIVE_UPDATE, DEFAULT);
        shakeMenu = settings.getInt(TAPLYTICS_OPTION_SHAKE_MENU, DEFAULT);
        turnMenu = settings.getInt(TAPLYTICS_OPTION_TURN_MENU, DEFAULT);
        sessionMinutes = settings.getInt(TAPLYTICS_OPTION_SESSION_MINUTES, 10);
        HashMap<String, Object> options = new HashMap<>();
        if (liveUpdate != DEFAULT) {
            options.put(TAPLYTICS_OPTION_LIVE_UPDATE, liveUpdate != DISABLED);
        }
        if (shakeMenu != DEFAULT) {
            options.put(TAPLYTICS_OPTION_SHAKE_MENU, shakeMenu != DISABLED);
        }
        if (turnMenu != DEFAULT) {
            options.put(TAPLYTICS_OPTION_TURN_MENU, turnMenu != DISABLED);
        }
        options.put(TAPLYTICS_OPTION_SESSION_MINUTES, sessionMinutes);
        options.put(TAPLYTICS_OPTION_DELAYED_START, true);
        Taplytics.startTaplytics(analytics.getApplication(), apiKey, options);
        logger.verbose("Taplytics.startTaplytics(analytics.getApplication(), %s, %s)", apiKey, options);
    }

    @Override
    public void track(TrackPayload track) {
        String event = track.event();
        event(event, track.properties());
    }

    @Override
    public void identify(IdentifyPayload identify) {
        super.identify(identify);
        JSONObject traits = new ValueMap(transform(identify.traits(), MAPPER)).toJsonObject();
        Taplytics.setUserAttributes(traits);
        logger.verbose("Taplytics.setUserAttributes(%s)", traits);
    }

    private void event(String name, Properties properties) {
        JSONObject propertiesJSON = properties.toJsonObject();
        int revenue = (int) properties.revenue();
        if (revenue != 0) {
            Taplytics.logRevenue(name, revenue, propertiesJSON);
            logger.verbose("Taplytics.logRevenue(%s, %s, %s)", name, revenue, propertiesJSON);
            return;
        }
        Taplytics.logEvent(name, properties.value(), propertiesJSON);
        logger.verbose("Taplytics.logEvent(%s, %s, %s)", name, properties.value(), propertiesJSON);
    }
};