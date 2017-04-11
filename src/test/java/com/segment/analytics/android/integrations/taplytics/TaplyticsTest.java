package com.segment.analytics.android.integrations.taplytics;

import android.app.Application;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import com.taplytics.sdk.Taplytics;
import java.util.HashMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(Taplytics.class) public class TaplyticsTest {

  @Rule public PowerMockRule rule = new PowerMockRule();
  @Mock Application context;
  Logger logger;
  @Mock Analytics analytics;
  TaplyticsIntegration integration;
  @Mock Taplytics taplytics;

  @Before public void setUp() {
    initMocks(this);
    mockStatic(Taplytics.class);
    logger = Logger.with(Analytics.LogLevel.DEBUG);
    when(analytics.logger("Taplytics")).thenReturn(logger);
    when(analytics.getApplication()).thenReturn(context);
    integration = new TaplyticsIntegration(analytics, new ValueMap().putValue("apiKey", "foo"));
    mockStatic(Taplytics.class); // reset the mock for verification.
  }

  @Test public void initializeWithDefaultArguments() {
    ValueMap settings = new ValueMap() //
        .putValue("apiKey", "foo");
    TaplyticsIntegration.FACTORY.create(settings, analytics);
    verifyStatic();

    HashMap<String, Object> options = new HashMap<>();
    options.put("sessionMinutes", 10);
    options.put("delayedStartTaplytics", true);
    Taplytics.startTaplytics(context, "foo", options);
  }

  @Test public void initialize() {
    ValueMap settings = new ValueMap() //
        .putValue("apiKey", "foo") //
        .putValue("sessionMinutes", 20)
        .putValue("liveUpdate_v2", "true") //
        .putValue("shakeMenu_v2", "false")
        .putValue("turnMenu_v2", "default");
    TaplyticsIntegration.FACTORY.create(settings, analytics);
    verifyStatic();

    HashMap<String, Object> options = new HashMap<>();
    options.put("liveUpdate", true);
    options.put("shakeMenu", false);
    options.put("sessionMinutes", 20);
    options.put("delayedStartTaplytics", true);
    Taplytics.startTaplytics(context, "foo", options);
  }

  @Test public void track() {
    integration.track(new TrackPayloadBuilder().event("foo").build());

    verifyStatic();
    Taplytics.logEvent(eq("foo"), eq(0.0), jsonEq(new JSONObject()));
  }

  @Test public void trackWithValue() {
    integration.track(
        new TrackPayloadBuilder().event("foo").properties(new Properties().putValue(20.0)).build());

    Properties expected = new Properties().putValue(20.0);

    verifyStatic();
    Taplytics.logEvent(eq("foo"), eq(20.0), jsonEq(expected.toJsonObject()));
  }

  @Test public void trackWithRevenue() {
    integration.track(new TrackPayloadBuilder().event("foo")
        .properties(new Properties().putValue(20.0).putRevenue(1000.0))
        .build());

    Properties expected = new Properties().putValue(20.0).putRevenue(1000.0);
    verifyStatic();
    Taplytics.logRevenue(eq("foo"), eq(1000), jsonEq(expected.toJsonObject()));
  }

  @Test public void identify() throws JSONException {
    Traits traits = createTraits("foo") //
        .putValue("anonymousId", "foobar")
        .putValue("firstName", "Kylo")
        .putValue("lastName", "Ren");

    integration.identify(new IdentifyPayloadBuilder().traits(traits).build());

    JSONObject attributes = new JSONObject();
    attributes.put("user_id", "foo");
    attributes.put("anonymousId", "foobar");
    attributes.put("firstname", "Kylo");
    attributes.put("lastname", "Ren");

    verifyStatic();
    Taplytics.setUserAttributes(jsonEq(attributes));
  }

  public static JSONObject jsonEq(JSONObject expected) {
    return argThat(new JSONObjectMatcher(expected));
  }

  private static class JSONObjectMatcher extends TypeSafeMatcher<JSONObject> {
    private final JSONObject expected;

    private JSONObjectMatcher(JSONObject expected) {
      this.expected = expected;
    }

    @Override public boolean matchesSafely(JSONObject jsonObject) {
      // todo: this relies on having the same order
      return expected.toString().equals(jsonObject.toString());
    }

    @Override public void describeTo(Description description) {
      description.appendText(expected.toString());
    }
  }
}
