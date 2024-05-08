package com.lightresearch.probecamera.ausbc.utils

import android.os.Build

/*
* libcommon
* utility/helper classes for myself
*
* Copyright (c) 2014-2018 saki t_saki@serenegiant.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/   object BuildCheck {
    private fun check(value: Int): Boolean {
        return Build.VERSION.SDK_INT >= value
    }

    /**
     * Magic version number for a current development build,
     * which has not yet turned into an official release. API=10000
     * @return
     */
    val isCurrentDevelopment: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.CUR_DEVELOPMENT

    /**
     * October 2008: The original, first, version of Android.  Yay!, API>=1
     * @return
     */
    val isBase: Boolean
        get() = check(Build.VERSION_CODES.BASE)

    /**
     * February 2009: First Android update, officially called 1.1., API>=2
     * @return
     */
    val isBase11: Boolean
        get() = check(Build.VERSION_CODES.BASE_1_1)

    /**
     * May 2009: Android 1.5., API>=3
     * @return
     */
    val isCupcake: Boolean
        get() = check(Build.VERSION_CODES.CUPCAKE)

    /**
     * May 2009: Android 1.5., API>=3
     * @return
     */
    val isAndroid1_5: Boolean
        get() = check(Build.VERSION_CODES.CUPCAKE)

    /**
     * September 2009: Android 1.6., API>=4
     * @return
     */
    val isDonut: Boolean
        get() = check(Build.VERSION_CODES.DONUT)

    /**
     * September 2009: Android 1.6., API>=4
     * @return
     */
    val isAndroid1_6: Boolean
        get() = check(Build.VERSION_CODES.DONUT)

    /**
     * November 2009: Android 2.0, API>=5
     * @return
     */
    val isEclair: Boolean
        get() = check(Build.VERSION_CODES.ECLAIR)

    /**
     * November 2009: Android 2.0, API>=5
     * @return
     */
    val isAndroid2_0: Boolean
        get() = check(Build.VERSION_CODES.ECLAIR)

    /**
     * December 2009: Android 2.0.1, API>=6
     * @return
     */
    val isEclair01: Boolean
        get() = check(Build.VERSION_CODES.ECLAIR_0_1)

    /**
     * January 2010: Android 2.1, API>=7
     * @return
     */
    val isEclairMR1: Boolean
        get() = check(Build.VERSION_CODES.ECLAIR_MR1)

    /**
     * June 2010: Android 2.2, API>=8
     * @return
     */
    val isFroyo: Boolean
        get() = check(Build.VERSION_CODES.FROYO)

    /**
     * June 2010: Android 2.2, API>=8
     * @return
     */
    val isAndroid2_2: Boolean
        get() = check(Build.VERSION_CODES.FROYO)

    /**
     * November 2010: Android 2.3, API>=9
     * @return
     */
    val isGingerBread: Boolean
        get() = check(Build.VERSION_CODES.GINGERBREAD)

    /**
     * November 2010: Android 2.3, API>=9
     * @return
     */
    val isAndroid2_3: Boolean
        get() = check(Build.VERSION_CODES.GINGERBREAD)

    /**
     * February 2011: Android 2.3.3., API>=10
     * @return
     */
    val isGingerBreadMR1: Boolean
        get() = check(Build.VERSION_CODES.GINGERBREAD_MR1)

    /**
     * February 2011: Android 2.3.3., API>=10
     * @return
     */
    val isAndroid2_3_3: Boolean
        get() = check(Build.VERSION_CODES.GINGERBREAD_MR1)

    /**
     * February 2011: Android 3.0., API>=11
     * @return
     */
    val isHoneyComb: Boolean
        get() = check(Build.VERSION_CODES.HONEYCOMB)

    /**
     * February 2011: Android 3.0., API>=11
     * @return
     */
    val isAndroid3: Boolean
        get() = check(Build.VERSION_CODES.HONEYCOMB)

    /**
     * May 2011: Android 3.1., API>=12
     * @return
     */
    val isHoneyCombMR1: Boolean
        get() = check(Build.VERSION_CODES.HONEYCOMB_MR1)

    /**
     * May 2011: Android 3.1., API>=12
     * @return
     */
    val isAndroid3_1: Boolean
        get() = check(Build.VERSION_CODES.HONEYCOMB_MR1)

    /**
     * June 2011: Android 3.2., API>=13
     * @return
     */
    val isHoneyCombMR2: Boolean
        get() = check(Build.VERSION_CODES.HONEYCOMB_MR2)

    /**
     * June 2011: Android 3.2., API>=13
     * @return
     */
    val isAndroid3_2: Boolean
        get() = check(Build.VERSION_CODES.HONEYCOMB_MR2)

    /**
     * October 2011: Android 4.0., API>=14
     * @return
     */
    val isIcecreamSandwich: Boolean
        get() = check(Build.VERSION_CODES.ICE_CREAM_SANDWICH)

    /**
     * October 2011: Android 4.0., API>=14
     * @return
     */
    val isAndroid4: Boolean
        get() = check(Build.VERSION_CODES.ICE_CREAM_SANDWICH)

    /**
     * December 2011: Android 4.0.3., API>=15
     * @return
     */
    val isIcecreamSandwichMR1: Boolean
        get() = check(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)

    /**
     * December 2011: Android 4.0.3., API>=15
     * @return
     */
    val isAndroid4_0_3: Boolean
        get() = check(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)

    /**
     * June 2012: Android 4.1., API>=16
     * @return
     */
    val isJellyBean: Boolean
        get() = check(Build.VERSION_CODES.JELLY_BEAN)

    /**
     * June 2012: Android 4.1., API>=16
     * @return
     */
    val isAndroid4_1: Boolean
        get() = check(Build.VERSION_CODES.JELLY_BEAN)

    /**
     * November 2012: Android 4.2, Moar jelly beans!, API>=17
     * @return
     */
    val isJellyBeanMr1: Boolean
        get() = check(Build.VERSION_CODES.JELLY_BEAN_MR1)

    /**
     * November 2012: Android 4.2, Moar jelly beans!, API>=17
     * @return
     */
    val isAndroid4_2: Boolean
        get() = check(Build.VERSION_CODES.JELLY_BEAN_MR1)

    /**
     * July 2013: Android 4.3, the revenge of the beans., API>=18
     * @return
     */
    val isJellyBeanMR2: Boolean
        get() = check(Build.VERSION_CODES.JELLY_BEAN_MR2)

    /**
     * July 2013: Android 4.3, the revenge of the beans., API>=18
     * @return
     */
    val isAndroid4_3: Boolean
        get() = check(Build.VERSION_CODES.JELLY_BEAN_MR2)

    /**
     * October 2013: Android 4.4, KitKat, another tasty treat., API>=19
     * @return
     */
    val isKitKat: Boolean
        get() = check(Build.VERSION_CODES.KITKAT)

    /**
     * October 2013: Android 4.4, KitKat, another tasty treat., API>=19
     * @return
     */
    val isAndroid4_4: Boolean
        get() = check(Build.VERSION_CODES.KITKAT)

    /**
     * Android 4.4W: KitKat for watches, snacks on the run., API>=20
     * @return
     */
    val isKitKatWatch: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH

    /**
     * Lollipop.  A flat one with beautiful shadows.  But still tasty., API>=21
     * @return
     */
    val isL: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    /**
     * Lollipop.  A flat one with beautiful shadows.  But still tasty., API>=21
     * @return
     */
    val isLollipop: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    /**
     * Lollipop.  A flat one with beautiful shadows.  But still tasty., API>=21
     * @return
     */
    val isAndroid5: Boolean
        get() = check(Build.VERSION_CODES.LOLLIPOP)

    /**
     * Lollipop with an extra sugar coating on the outside!, API>=22
     * @return
     */
    val isLollipopMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1

    /**
     * Marshmallow.  A flat one with beautiful shadows.  But still tasty., API>=23
     * @return
     */
    val isM: Boolean
        get() = check(Build.VERSION_CODES.M)

    /**
     * Marshmallow.  A flat one with beautiful shadows.  But still tasty., API>=23
     * @return
     */
    val isMarshmallow: Boolean
        get() = check(Build.VERSION_CODES.M)

    /**
     * Marshmallow.  A flat one with beautiful shadows.  But still tasty., API>=23
     * @return
     */
    val isAndroid6: Boolean
        get() = check(Build.VERSION_CODES.M)

    /**
     * 虫歯の元, API >= 24
     * @return
     */
    val isN: Boolean
        get() = check(Build.VERSION_CODES.N)

    /**
     * 歯にくっつくやつ, API >= 24
     * @return
     */
    val isNougat: Boolean
        get() = check(Build.VERSION_CODES.N)

    /**
     * API >= 24
     * @return
     */
    val isAndroid7: Boolean
        get() = check(Build.VERSION_CODES.N)

    /**
     * API>=25
     * @return
     */
    val isNMR1: Boolean
        get() = check(Build.VERSION_CODES.N_MR1)

    /**
     * API>=25
     * @return
     */
    val isNougatMR1: Boolean
        get() = check(Build.VERSION_CODES.N_MR1)

    /**
     * おれおれぇー API>=26
     * @return
     */
    val isO: Boolean
        get() = check(Build.VERSION_CODES.O)

    /**
     * おれおれぇー API>=26
     * @return
     */
    val isOreo: Boolean
        get() = check(Build.VERSION_CODES.O)

    /**
     * おれおれぇー API>=26
     * @return
     */
    val isAndroid8: Boolean
        get() = check(Build.VERSION_CODES.O)

    /**
     * おれおれぇー API>=27
     * @return
     */
    val isOMR1: Boolean
        get() = check(Build.VERSION_CODES.O_MR1)

    /**
     * おれおれぇー MR1 API>=27
     * @return
     */
    val isOreoMR1: Boolean
        get() = check(Build.VERSION_CODES.O_MR1)

    /**
     * おっ！ぱい API>=28
     * @return
     */
    val isP: Boolean
        get() = check(Build.VERSION_CODES.P)

    /**
     * おっ！ぱい API>=28
     * @return
     */
    val isPie: Boolean
        get() = check(Build.VERSION_CODES.P)

    /**
     * おっ！ぱい API>=28
     * @return
     */
    val isAndroid9: Boolean
        get() = check(Build.VERSION_CODES.P)
}