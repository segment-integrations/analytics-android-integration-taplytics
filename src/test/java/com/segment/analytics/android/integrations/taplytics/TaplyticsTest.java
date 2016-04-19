package com.segment.analytics.android.integrations.taplytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.taplytics.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.AliasPayloadBuilder;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.ScreenPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import com.taplytics.sdk.Taplytics;

import java.util.Arrays;
import java.util.Collections;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest(Taplytics.class)
public class TaplyticsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    @Mock
    Application context;
    Logger logger;
    @Mock
    Analytics analytics;

    TaplyticsIntegration integration;

    @Before
    public void setUp() {
        initMocks(this);
        mockStatic(Taplytics.class);
        logger = Logger.with(Analytics.LogLevel.DEBUG);
        when(analytics.logger("Taplytics")).thenReturn(logger);
        when(analytics.getApplication()).thenReturn(context);


        integration = new TaplyticsIntegration(analytics, new ValueMap());
    }

    @Test
    public void factory() {
        ValueMap settings = new ValueMap().putValue("token", "foo");

        TaplyticsIntegration integration =
                (TaplyticsIntegration) TaplyticsIntegration.FACTORY.create(settings, analytics);

        verifyStatic();
        //Integration initialized
        //Make sure settings are set correctly
        Taplytics.startTaplytics(context, "YOUR TAPLYTICS API KEY");
    }
}