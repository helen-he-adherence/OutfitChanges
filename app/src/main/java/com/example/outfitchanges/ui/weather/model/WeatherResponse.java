package com.example.outfitchanges.ui.weather.model;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("now")
    private NowWeather now;

    @SerializedName("hourly")
    private HourlyWeather[] hourly;

    @SerializedName("daily")
    private DailyWeather[] daily;

    @SerializedName("refer")
    private Refer refer;

    public String getCode() {
        return code;
    }

    public NowWeather getNow() {
        return now;
    }

    public HourlyWeather[] getHourly() {
        return hourly;
    }

    public DailyWeather[] getDaily() {
        return daily;
    }

    public Refer getRefer() {
        return refer;
    }

    public static class NowWeather {
        @SerializedName("obsTime")
        private String obsTime;

        @SerializedName("temp")
        private String temp;

        @SerializedName("feelsLike")
        private String feelsLike;

        @SerializedName("icon")
        private String icon;

        @SerializedName("text")
        private String text;

        @SerializedName("wind360")
        private String wind360;

        @SerializedName("windDir")
        private String windDir;

        @SerializedName("windScale")
        private String windScale;

        @SerializedName("windSpeed")
        private String windSpeed;

        @SerializedName("humidity")
        private String humidity;

        @SerializedName("precip")
        private String precip;

        @SerializedName("pressure")
        private String pressure;

        @SerializedName("vis")
        private String vis;

        @SerializedName("cloud")
        private String cloud;

        @SerializedName("dew")
        private String dew;

        public String getObsTime() {
            return obsTime;
        }

        public String getTemp() {
            return temp;
        }

        public String getFeelsLike() {
            return feelsLike;
        }

        public String getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }

        public String getWind360() {
            return wind360;
        }

        public String getWindDir() {
            return windDir;
        }

        public String getWindScale() {
            return windScale;
        }

        public String getWindSpeed() {
            return windSpeed;
        }

        public String getHumidity() {
            return humidity;
        }

        public String getPrecip() {
            return precip;
        }

        public String getPressure() {
            return pressure;
        }

        public String getVis() {
            return vis;
        }

        public String getCloud() {
            return cloud;
        }

        public String getDew() {
            return dew;
        }
    }

    public static class HourlyWeather {
        @SerializedName("fxTime")
        private String fxTime;

        @SerializedName("temp")
        private String temp;

        @SerializedName("icon")
        private String icon;

        @SerializedName("text")
        private String text;

        @SerializedName("wind360")
        private String wind360;

        @SerializedName("windDir")
        private String windDir;

        @SerializedName("windScale")
        private String windScale;

        @SerializedName("windSpeed")
        private String windSpeed;

        @SerializedName("humidity")
        private String humidity;

        @SerializedName("pop")
        private String pop;

        @SerializedName("precip")
        private String precip;

        @SerializedName("pressure")
        private String pressure;

        @SerializedName("cloud")
        private String cloud;

        @SerializedName("dew")
        private String dew;

        public String getFxTime() {
            return fxTime;
        }

        public String getTemp() {
            return temp;
        }

        public String getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }

        public String getWind360() {
            return wind360;
        }

        public String getWindDir() {
            return windDir;
        }

        public String getWindScale() {
            return windScale;
        }

        public String getWindSpeed() {
            return windSpeed;
        }

        public String getHumidity() {
            return humidity;
        }

        public String getPop() {
            return pop;
        }

        public String getPrecip() {
            return precip;
        }

        public String getPressure() {
            return pressure;
        }

        public String getCloud() {
            return cloud;
        }

        public String getDew() {
            return dew;
        }
    }

    public static class DailyWeather {
        @SerializedName("fxDate")
        private String fxDate;

        @SerializedName("tempMax")
        private String tempMax;

        @SerializedName("tempMin")
        private String tempMin;

        @SerializedName("iconDay")
        private String iconDay;

        @SerializedName("textDay")
        private String textDay;

        @SerializedName("iconNight")
        private String iconNight;

        @SerializedName("textNight")
        private String textNight;

        @SerializedName("wind360Day")
        private String wind360Day;

        @SerializedName("windDirDay")
        private String windDirDay;

        @SerializedName("windScaleDay")
        private String windScaleDay;

        @SerializedName("windSpeedDay")
        private String windSpeedDay;

        @SerializedName("wind360Night")
        private String wind360Night;

        @SerializedName("windDirNight")
        private String windDirNight;

        @SerializedName("windScaleNight")
        private String windScaleNight;

        @SerializedName("windSpeedNight")
        private String windSpeedNight;

        @SerializedName("humidity")
        private String humidity;

        @SerializedName("precip")
        private String precip;

        @SerializedName("pressure")
        private String pressure;

        @SerializedName("vis")
        private String vis;

        @SerializedName("cloud")
        private String cloud;

        @SerializedName("uvIndex")
        private String uvIndex;

        public String getFxDate() {
            return fxDate;
        }

        public String getTempMax() {
            return tempMax;
        }

        public String getTempMin() {
            return tempMin;
        }

        public String getIconDay() {
            return iconDay;
        }

        public String getTextDay() {
            return textDay;
        }

        public String getIconNight() {
            return iconNight;
        }

        public String getTextNight() {
            return textNight;
        }

        public String getWind360Day() {
            return wind360Day;
        }

        public String getWindDirDay() {
            return windDirDay;
        }

        public String getWindScaleDay() {
            return windScaleDay;
        }

        public String getWindSpeedDay() {
            return windSpeedDay;
        }

        public String getWind360Night() {
            return wind360Night;
        }

        public String getWindDirNight() {
            return windDirNight;
        }

        public String getWindScaleNight() {
            return windScaleNight;
        }

        public String getWindSpeedNight() {
            return windSpeedNight;
        }

        public String getHumidity() {
            return humidity;
        }

        public String getPrecip() {
            return precip;
        }

        public String getPressure() {
            return pressure;
        }

        public String getVis() {
            return vis;
        }

        public String getCloud() {
            return cloud;
        }

        public String getUvIndex() {
            return uvIndex;
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
