package com.stopandbreathe

object ReminderConfig {
    
    // User facing config
    const val INTERVAL_MINUTES = 15L
    const val COUNTDOWN_SECONDS = 20L

    const val MESSAGE_MIN_TEXT_SP = 16f
    const val MESSAGE_MAX_TEXT_SP = 26f
    const val DISMISS_TEXT_SP = 13f

    val MESSAGES = listOf(
        "Breathing in, I am aware of my in-breath.\nBreathing out, I am aware of my out-breath.",
        "Breathing in, I calm my body.\nBreathing out, I feel ease.",
        "Breathing in, I smile to my body.\nBreathing out, I release the tension in my body. ",
        "Breathing in, I am aware of the pain in me.\nBreathing out, I embrace the pain in me.",
        "Breathing in, I am aware of my feelings.\nBreathing out, I calm my feelings.",
        "Breathing in, I dwell in the present moment.\nBreathing out, I know it is a wonderful moment.",
        "Breathing in, I have arrived.\nBreathing out, I am home.",
        "Breathing in, I acknowledge I'm gonna die one day.\nBreathing out, I am alive here and now.",
    )

    // CONSTANTS for internal implementation
    const val INTERVAL_MILLIS = INTERVAL_MINUTES * 60_000L
    const val COUNTDOWN_MILLIS = COUNTDOWN_SECONDS * 1_000L

    const val INTERVAL_CHECK_MILLIS = 1_000L
    const val COUNTDOWN_CHECK_MILLIS = 200L
    const val UI_REFRESH_MILLIS = 500L
    const val OVERLAY_RETRY_MILLIS = 5_000L

    // Exact Android package IDs, not names or prefix matches.
    // Store IDs: Google Play's ?id= or F-Droid's /packages/ URL.
    // Include separate store/paid builds and older installed apps.
    val MAP_APP_PACKAGES = setOf(
        // Open-source maps and outdoor navigation.
        "app.comaps", // CoMaps (standalone)
        "app.comaps.fdroid", // CoMaps (F-Droid)
        "app.comaps.google", // CoMaps (Google Play)
        "app.organicmaps", // Organic Maps
        "net.osmand", // OsmAnd
        "net.osmand.plus", // OsmAnd+ / OsmAnd~ (F-Droid)
        "net.osmand.dev", // OsmAnd development builds
        "org.navitproject.navit", // Navit
        "com.zoffcc.applications.zanavi", // ZANavi
        "mobi.maptrek", // Trekarta
        "net.cyclestreets", // CycleStreets
        "com.graphhopper.maps", // GraphHopper Maps
        "com.junjunguo.pocketmaps", // PocketMaps
        "app.fedilab.openmaps", // OpenMultiMaps
        "org.nitri.opentopo", // OpenTopoMap Viewer
        "org.osmdroid", // Open Map
        "ch.bailu.aat", // AAT Another Activity Tracker
        "de.storchp.opentracks.osmplugin", // OSM Dashboard
        "de.k3b.android.locationMapViewer", // Location Map Viewer
        "dev.samuelq.gpx", // Offline GPX

        // Road navigation and general maps.
        "com.google.android.apps.maps", // Google Maps
        "com.google.android.apps.mapslite", // Google Maps Go
        "com.google.earth", // Google Earth
        "com.waze", // Waze
        "com.here.app.maps", // HERE WeGo
        "com.tomtom.gplay.navapp", // TomTom GO
        "com.tomtom.speedcams.android.map", // TomTom / AmiGO
        "com.sygic.aura", // Sygic GPS Navigation
        "com.sygic.truck", // Sygic Truck & Caravan
        "com.generalmagic.magicearth", // Magic Earth
        "com.mapfactor.navigator", // MapFactor Navigator
        "com.mapswithme.maps.pro", // MAPS.ME
        "com.mapquest.android.ace", // MapQuest
        "com.alk.copilot.mapviewer", // CoPilot GPS
        "com.mmi.maps", // Mappls / MapmyIndia
        "com.huawei.maps.app", // Petal Maps
        "com.navfree.android.OSM.ALL", // Navmii
        "com.navitel", // Navitel
        "com.kurviger.app", // Kurviger
        "gr.talent.cruiser", // Cruiser
        "com.almica.alminav", // Alminav
        "app.offraid", // OffRaid
        "cityguide.probki.net", // CityGuide
        "ua.travelgps.navimaps", // Navi-Maps
        "net.probki.geonet", // GeoNET
        "ru.dublgis.dgismobile", // 2GIS
        "ru.yandex.yandexmaps", // Yandex Maps
        "ru.yandex.yandexnavi", // Yandex Navigator
        "com.baidu.BaiduMap", // Baidu Maps
        "com.autonavi.minimap", // Amap / Gaode Maps
        "com.nhn.android.nmap", // NAVER Maps
        "net.daum.android.map", // KakaoMap
        "com.locnall.KimGiSa", // Kakao Navi
        "com.skt.tmap.ku", // TMAP

        // Hiking, trekking, topographic maps and cycling.
        "com.alltrails.alltrails", // AllTrails
        "com.trailbehind.android.gaiagps.pro", // Gaia GPS
        "com.wikiloc.wikilocandroid", // Wikiloc
        "de.komoot.android", // Komoot
        "com.outdooractive.Outdooractive", // Outdooractive
        "com.outdooractive.alpenverein", // alpenvereinaktiv
        "com.bergfex.tour", // bergfex
        "cz.seznam.mapy", // Mapy.com / Mapy.cz
        "menion.android.locus", // Locus Map
        "menion.android.locus.pro", // Locus Map Classic
        "com.orux.oruxmaps", // OruxMaps (standalone)
        "com.orux.oruxmapsDonate", // OruxMaps GP
        "psyberia.alpinequest.free", // AlpineQuest Lite
        "psyberia.alpinequest.full", // AlpineQuest
        "com.caltopo.android", // CalTopo
        "com.Avenza", // Avenza Maps
        "com.bodunov.galileo", // Guru Maps
        "com.bodunov.GalileoPro", // Guru Maps Pro
        "uk.rdzl.topo.gps", // Topo GPS
        "uk.co.ordnancesurvey.osmaps", // OS Maps
        "ch.admin.swisstopo", // swisstopo
        "org.visorando.android", // Visorando
        "com.atlasguides.guthook", // FarOut / Guthook
        "com.waymarkedtrails.hiiker", // HiiKER
        "onxmaps.backcountry", // onX Backcountry
        "onxmaps.offroad", // onX Offroad
        "onxmaps.hunt", // onX Hunt
        "com.pinkbike.trailforks", // Trailforks
        "com.hikingproject.android", // Hiking Project
        "com.mtbproject.android", // MTB Project
        "org.railstotrails.traillink", // TrailLink
        "com.openrunner.android", // OpenRunner
        "com.ridewithgps.mobile", // Ride with GPS
        "com.toursprung.bikemap", // Bikemap
        "com.umotional.bikeapp", // Cyclers
        "org.bikecityguide", // Bike Citizens
        "org.naviki", // Naviki
        "fr.geovelo", // Geovelo
        "com.geowiki.cycle_travel_android", // cycle.travel
        "de.afischer.aftrack", // AFTrack
        "quovadis.x.mobile", // QuoVadis X Mobile
        "com.garmin.android.apps.explore", // Garmin Explore
        "com.memorymap.mm2", // Memory-Map
        "com.compegps.twonav", // TwoNav
        "com.compegps.twonavfull", // TwoNav 5
        "com.atlogis.sovietmaps.free", // Russian Topo Maps
        "com.atlogis.sovietmaps", // Russian Topo Maps Pro
        "com.atlogis.northamerica.free", // US Topo Maps
        "com.augmentra.viewranger.android", // ViewRanger (legacy)
        "com.vecturagames.android.app.gpxviewer", // GPX Viewer
        "com.vecturagames.android.app.gpxviewer.pro", // GPX Pro
        "com.mountainmaps.mountainmaps", // Mountain Maps
        "net.hikingbook.hikingbook", // Hikingbook
    )

}
