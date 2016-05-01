package com.segment.analytics.android.integrations.taplytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import com.taplytics.sdk.Taplytics;
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
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
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
  }

  @Test public void factory() {
    ValueMap settings = new ValueMap() //
        .putValue("apiKey", "foo").putValue("liveUpdate", false).putValue("sessionMinutes", 20);
    TaplyticsIntegration integration =
        (TaplyticsIntegration) TaplyticsIntegration.FACTORY.create(settings, analytics);
    verifyStatic();
    assertThat(integration.liveUpdate).isFalse();
    assertThat(integration.sessionMinutes).isEqualTo(20);
  }

  @Test public void initializeWithDefaultArguments() {
    ValueMap settings = new ValueMap() //
        .putValue("apiKey", "foo");
    TaplyticsIntegration integration =
        (TaplyticsIntegration) TaplyticsIntegration.FACTORY.create(settings, analytics);
    verifyStatic();
    //Integration initialized
    //Make sure settings are set correctly
    assertThat(integration.liveUpdate).isTrue();
    assertThat(integration.sessionMinutes).isEqualTo(10);
  }

  @Test public void activityCreate() {
    ValueMap settings = new ValueMap().putValue("turnMenu", false).putValue("sessionMinutes", 10).putValue("liveUpdate", true).putValue("shakeMenu", true);
    Activity activity = mock(Activity.class);
    Bundle bundle = mock(Bundle.class);
    integration.onActivityCreated(activity, bundle);
    verifyStatic();
    Taplytics.startTaplytics(analytics.getApplication(), "foo", settings);
  }

  @Test public void activityStart() {
    Activity activity = mock(Activity.class);
    integration.onActivityStarted(activity);
  }

  @Test public void activityResume() {
    Activity activity = mock(Activity.class);
    integration.onActivityResumed(activity);
  }

  @Test public void activityPause() {
    Activity activity = mock(Activity.class);
    integration.onActivityPaused(activity);
  }

  @Test public void activityStop() {
    Activity activity = mock(Activity.class);
    integration.onActivityStopped(activity);
  }

  @Test public void activitySaveInstance() {
    Activity activity = mock(Activity.class);
    Bundle bundle = mock(Bundle.class);
    integration.onActivitySaveInstanceState(activity, bundle);
  }

  @Test public void activityDestroy() {
    Activity activity = mock(Activity.class);
    integration.onActivityDestroyed(activity);
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