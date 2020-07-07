analytics-android-integration-taplytics
======================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/taplytics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/taplytics)
[![Javadocs](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/taplytics.svg?label=javadoc)](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/taplytics)

Taplytics integration for [analytics-android](https://github.com/segmentio/analytics-android).

## Installation

To install the Segment-Taplytics integration, simply add this line to your gradle file:

```
compile 'com.segment.analytics.android.integrations:taplytics:+'
```

## Usage

After adding the dependency, you must register the integration with our SDK.  To do this, import 
the Taplytics integration:


```
import com.segment.analytics.android.integrations.taplytics.TaplyticsIntegration;

```

And add the following:

```
analytics = new Analytics.Builder(this, "write_key")
                .use(TaplyticsIntegration.FACTORY)
                .build();
```

## Initializing Taplytics without Segment

If you would like to initialize Taplytics yourself to leverage functionality required on 
initialization, you can use Taplytics native methods to initialize then use `SKIPINIT` to 
skip initializing through Segment.

```
Taplytics.startTaplytics(context, apiKey, options);

analytics = new Analytics.Builder(this, "write_key")
                .use(TaplyticsIntegration.SKIPINIT)
                .build();
```

Please see [our documentation](https://segment.com/docs/destinations/taplytics/) for more information.


## License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|

The MIT License (MIT)

Copyright (c) 2014 Segment, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
