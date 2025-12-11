package com.example.outfitchanges.ui.weather.model;

import com.google.gson.annotations.SerializedName;

public class LocationResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("location")
    private Location[] location;

    @SerializedName("refer")
    private Refer refer;

    public String getCode() {
        return code;
    }

    public Location[] getLocation() {
        return location;
    }

    public Refer getRefer() {
        return refer;
    }

    public static class Location {
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String id;

        @SerializedName("lat")
        private String lat;

        @SerializedName("lon")
        private String lon;

        @SerializedName("adm2")
        private String adm2;

        @SerializedName("adm1")
        private String adm1;

        @SerializedName("country")
        private String country;

        @SerializedName("tz")
        private String tz;

        @SerializedName("utcOffset")
        private String utcOffset;

        @SerializedName("isDst")
        private String isDst;

        @SerializedName("type")
        private String type;

        @SerializedName("rank")
        private String rank;

        @SerializedName("fxLink")
        private String fxLink;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }

        public String getAdm2() {
            return adm2;
        }

        public String getAdm1() {
            return adm1;
        }

        public String getCountry() {
            return country;
        }

        public String getTz() {
            return tz;
        }

        public String getUtcOffset() {
            return utcOffset;
        }

        public String getIsDst() {
            return isDst;
        }

        public String getType() {
            return type;
        }

        public String getRank() {
            return rank;
        }

        public String getFxLink() {
            return fxLink;
        }
    }

    public static class Refer {
        @SerializedName("sources")
        private String[] sources;

        @SerializedName("license")
        private String[] license;

        public String[] getSources() {
            return sources;
        }

        public String[] getLicense() {
            return license;
        }
    }
}
