package com.segment.analytics.android.integrations.taplytics;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.taplytics.sdk.Taplytics;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

import static com.segment.analytics.internal.Utils.isNullOrEmpty;
import static com.segment.analytics.internal.Utils.transform;

public class TaplyticsIntegration extends Integration<Taplytics> {
  public static final Factory FACTORY =
      new Factory() {
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

  static final Map<String, String> MAPPER;

  static {
    Map<String, String> mapper = new LinkedHashMap<>();
    mapper.put("firstName", "firstname");
    mapper.put("lastName", "lastname");
    mapper.put("userId", "user_id");
    MAPPER = Collections.unmodifiableMap(mapper);
  }

  final Logger logger;

  TaplyticsIntegration(Analytics analytics, ValueMap settings) {
    logger = analytics.logger(TAPLYTICS_KEY);
    String apiKey = settings.getString("apiKey");

    HashMap<String, Object> options = new HashMap<>();
    // v1 of these settings were simply booleans and unable to represent a default value.
    // v2 uses strings, one of "true", "false" and "default" to represent the 3 options.
    // the v2 suffix was added to prevent older versions from breaking in the wild
    // (which were expecting booleans and used hardcoded defaults)
    putDefaultBooleans(settings, "liveUpdate_v2", options, "liveUpdate");
    putDefaultBooleans(settings, "shakeMenu_v2", options, "shakeMenu");
    putDefaultBooleans(settings, "turnMenu_v2", options, "turnMenu");
    int sessionMinutes = settings.getInt("sessionMinutes", 10);
    options.put("sessionMinutes", sessionMinutes);
    options.put("delayedStartTaplytics", true);
    Taplytics.startTaplytics(analytics.getApplication(), apiKey, options);
    logger.verbose("Taplytics.startTaplytics(analytics.getApplication(), %s, %s)", apiKey, options);
  }

  /**
   * Copy an a boolean from {@code settings[settingsKey]} to {@code options[optionsKey]}. Copies
   * {@code true} for {@code "true"}, {@code false} for {@code "false"} and nothing for {@code
   * "default"}.
   *
   * @param settings Settings dictionary sent by Segment CDN.
   * @param settingsKey Settings key.
   * @param options Options dictionary used by Taplytics.
   * @param optionsKey Options key.
   */
  private static void putDefaultBooleans(
      ValueMap settings, String settingsKey, Map<String, Object> options, String optionsKey) {
    String val = settings.getString(settingsKey);
    if (isNullOrEmpty(val)) {
      return;
    }
    switch (val) {
      case "true":
        options.put(optionsKey, true);
        break;
      case "false":
        options.put(optionsKey, false);
        break;
      default:
        break;
    }
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
