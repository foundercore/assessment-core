package com.assessment.common;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import com.assessment.exception.ConfigurationException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TimeUtility {
    public static final String WIDE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static DateTime getCurrentTimeInUTC() {
        return getCurrentTime(TimeZoneConstant.TZ_UTC_p0000);
    }

    public static DateTime getCurrentTime(String timezone) {
        if (TimeZoneConstant.isValidTimeZone(timezone)){
            return new DateTime().withZone(DateTimeZone.forID(timezone));
        }
        throw new ConfigurationException("Invalid! Timezone " + timezone + " not valid.");
    }

    public static String getCurrentTimeTxtInUTC(String format) {
        DateTime dt = getCurrentTimeInUTC();
        return DateTimeFormat.forPattern(format).withZone(DateTimeZone.forID(TimeZoneConstant.TZ_UCT_p0000)).print(dt);
    }

    public static String getCurrentTimeTxt(String timezone, String format) {
        if (TimeZoneConstant.isValidTimeZone(timezone)){
            return DateTimeFormat.forPattern(format).withZone(DateTimeZone.forID(timezone)).print(getCurrentTime(timezone));
        }
        throw new ConfigurationException("Invalid! Timezone " + timezone + " not valid.");
    }

    public static DateTime convertDateTimeInUTC(DateTime dateTime){
        return convertDateTimeZone(dateTime, TimeZoneConstant.TZ_UTC_p0000);
    }

    public static DateTime convertDateTimeZone(DateTime dateTime, String timezone){
        if (TimeZoneConstant.isValidTimeZone(timezone)){
            return dateTime.withZone(DateTimeZone.forID(timezone));
        }
        throw new ConfigurationException("Invalid! Timezone " + timezone + " not valid.");
    }

    public static String convertDateTimeToTxt(DateTime dateTime, String format){
        return DateTimeFormat.forPattern(format).print(dateTime);
    }

    public static String convertDateTimeToTxt(DateTime dateTime, String format, String timezone){
        if (TimeZoneConstant.isValidTimeZone(timezone)){
            return DateTimeFormat.forPattern(format).withZone(DateTimeZone.forID(timezone)).print(dateTime);
        }
        throw new ConfigurationException("Invalid! Timezone " + timezone + " not valid.");
    }

    public static DateTime convertTxtToDateTime(String dateTxt, String format, String timezone){
        if (TimeZoneConstant.isValidTimeZone(timezone)){
            return new DateTime(DateTimeFormat.forPattern(format).withZone(DateTimeZone.forID(timezone)).parseMillis(dateTxt));
        }
        throw new ConfigurationException("Invalid! Timezone " + timezone + " not valid.");
    }

    public static DateTime convertMillisToDateTime(long millis, String timezone){
        if (TimeZoneConstant.isValidTimeZone(timezone)){
            return new DateTime(millis, DateTimeZone.forID(timezone));
        }
        throw new ConfigurationException("Invalid! Timezone " + timezone + " not valid.");
    }

    public static DateTime convertMillisToDateTimeInUTC(long millis){
        return new DateTime(millis, DateTimeZone.forID(TimeZoneConstant.TZ_UCT_p0000));
    }


    public static class TimeZoneConstant {
        /**
         * These constants taken from joda library (http://joda-time.sourceforge.net/timezones.html)
         *
         * Time Zone Constant Naming Convention
         * Prefix + '_' + timezone + '_' + suffix
         * Prefix - TZ
         * Suffix - difference w.r.t UTC
         * If timezone contains '_' in name then it should be replaced by '/'
         *
         * Suffix Naming Convention
         * Consist of 5 alphanumeric chars
         * 'm' means minus
         * 'p' means plus
         * first 2 digits represent hours
         * last 2 digits represent minutes
         *
         *  Example:- 'TZ_Etc_GMTp12_m1200' means time zone 'Etc/GMT+12' with '-12:00' difference w.r.t. UTC
         */

        public static final String TZ_Etc_GMTp12_m1200 = "Etc/GMT+12";
        public static final String TZ_Etc_GMTp11_m1100 = "Etc/GMT+11";
        public static final String TZ_Pacific_Apia_m1100 = "Pacific/Apia";
        public static final String TZ_Pacific_Midway_m1100 = "Pacific/Midway";
        public static final String TZ_Pacific_Niue_m1100 = "Pacific/Niue";
        public static final String TZ_Pacific_Pago_Pago_m1100 = "Pacific/Pago_Pago";
        public static final String TZ_Pacific_Samoa_m1100 = "Pacific/Samoa";
        public static final String TZ_US_Samoa_m1100 = "US/Samoa";
        public static final String TZ_America_Adak_m1000 = "America/Adak";
        public static final String TZ_America_Atka_m1000 = "America/Atka";
        public static final String TZ_US_Aleutian_m1000 = "US/Aleutian";
        public static final String TZ_Etc_GMTp10_m1000 = "Etc/GMT+10";
        public static final String TZ_HST_m1000 = "HST";
        public static final String TZ_Pacific_Fakaofo_m1000 = "Pacific/Fakaofo";
        public static final String TZ_Pacific_Honolulu_m1000 = "Pacific/Honolulu";
        public static final String TZ_US_Hawaii_m1000 = "US/Hawaii";
        public static final String TZ_Pacific_Johnston_m1000 = "Pacific/Johnston";
        public static final String TZ_Pacific_Rarotonga_m1000 = "Pacific/Rarotonga";
        public static final String TZ_Pacific_Tahiti_m1000 = "Pacific/Tahiti";
        public static final String TZ_Pacific_Marquesas_m0930 = "Pacific/Marquesas";
        public static final String TZ_America_Anchorage_m0900 = "America/Anchorage";
        public static final String TZ_US_Alaska_m0900 = "US/Alaska";
        public static final String TZ_America_Juneau_m0900 = "America/Juneau";
        public static final String TZ_America_Nome_m0900 = "America/Nome";
        public static final String TZ_America_Yakutat_m0900 = "America/Yakutat";
        public static final String TZ_Etc_GMTp9_m0900 = "Etc/GMT+9";
        public static final String TZ_Pacific_Gambier_m0900 = "Pacific/Gambier";
        public static final String TZ_America_Dawson_m0800 = "America/Dawson";
        public static final String TZ_America_Los_Angeles_m0800 = "America/Los_Angeles";
        public static final String TZ_US_Pacific_m0800 = "US/Pacific";
        public static final String TZ_US_PacificmNew_m0800 = "US/Pacific-New";
        public static final String TZ_America_Santa_Isabel_m0800 = "America/Santa_Isabel";
        public static final String TZ_America_Tijuana_m0800 = "America/Tijuana";
        public static final String TZ_America_Ensenada_m0800 = "America_Ensenada";
        public static final String TZ_Mexico_BajaNorte_m0800 = "Mexico_BajaNorte";
        public static final String TZ_America_Vancouver_m0800 = "America/Vancouver";
        public static final String TZ_Canada_Pacific_m0800 = "Canada_Pacific";
        public static final String TZ_America_Whitehorse_m0800 = "America/Whitehorse";
        public static final String TZ_Canada_Yukon_m0800 = "Canada_Yukon";
        public static final String TZ_Etc_GMTp8_m0800 = "Etc/GMT+8";
        public static final String TZ_PST8PDT_m0800 = "PST8PDT";
        public static final String TZ_Pacific_Pitcairn_m0800 = "Pacific/Pitcairn";
        public static final String TZ_America_Boise_m0700 = "America/Boise";
        public static final String TZ_America_Cambridge_Bay_m0700 = "America/Cambridge_Bay";
        public static final String TZ_America_Chihuahua_m0700 = "America/Chihuahua";
        public static final String TZ_America_Dawson_Creek_m0700 = "America/Dawson_Creek";
        public static final String TZ_America_Denver_m0700 = "America/Denver";
        public static final String TZ_America_Shiprock_m0700 = "America_Shiprock";
        public static final String TZ_Navajo_m0700 = "Navajo";
        public static final String TZ_US_Mountain_m0700 = "US_Mountain";
        public static final String TZ_America_Edmonton_m0700 = "America/Edmonton";
        public static final String TZ_Canada_Mountain_m0700 = "Canada_Mountain";
        public static final String TZ_America_Hermosillo_m0700 = "America/Hermosillo";
        public static final String TZ_America_Inuvik_m0700 = "America/Inuvik";
        public static final String TZ_America_Mazatlan_m0700 = "America/Mazatlan";
        public static final String TZ_Mexico_BajaSur_m0700 = "Mexico_BajaSur";
        public static final String TZ_America_Ojinaga_m0700 = "America/Ojinaga";
        public static final String TZ_America_Phoenix_m0700 = "America/Phoenix";
        public static final String TZ_US_Arizona_m0700 = "US_Arizona";
        public static final String TZ_America_Yellowknife_m0700 = "America/Yellowknife";
        public static final String TZ_Etc_GMTp7_m0700 = "Etc/GMT+7";
        public static final String TZ_MST_m0700 = "MST";
        public static final String TZ_MST7MDT_m0700 = "MST7MDT";
        public static final String TZ_America_Bahia_Banderas_m0600 = "America/Bahia_Banderas";
        public static final String TZ_America_Belize_m0600 = "America/Belize";
        public static final String TZ_America_Cancun_m0600 = "America/Cancun";
        public static final String TZ_America_Chicago_m0600 = "America/Chicago";
        public static final String TZ_US_Central_m0600 = "US_Central";
        public static final String TZ_America_Costa_Rica_m0600 = "America/Costa_Rica";
        public static final String TZ_America_El_Salvador_m0600 = "America/El_Salvador";
        public static final String TZ_America_Guatemala_m0600 = "America/Guatemala";
        public static final String TZ_America_Indiana_Knox_m0600 = "America/Indiana/Knox";
        public static final String TZ_America_Knox_IN_m0600 = "America_Knox_IN";
        public static final String TZ_US_IndianamStarke_m0600 = "US_IndianamStarke";
        public static final String TZ_America_Indiana_Tell_City_m0600 = "America/Indiana/Tell_City";
        public static final String TZ_America_Managua_m0600 = "America/Managua";
        public static final String TZ_America_Matamoros_m0600 = "America/Matamoros";
        public static final String TZ_America_Menominee_m0600 = "America/Menominee";
        public static final String TZ_America_Merida_m0600 = "America/Merida";
        public static final String TZ_America_Mexico_City_m0600 = "America/Mexico_City";
        public static final String TZ_Mexico_General_m0600 = "Mexico_General";
        public static final String TZ_America_Monterrey_m0600 = "America/Monterrey";
        public static final String TZ_America_North_Dakota_Center_m0600 = "America/North_Dakota/Center";
        public static final String TZ_America_North_Dakota_New_Salem_m0600 = "America/North_Dakota/New_Salem";
        public static final String TZ_America_Rainy_River_m0600 = "America/Rainy_River";
        public static final String TZ_America_Rankin_Inlet_m0600 = "America/Rankin_Inlet";
        public static final String TZ_America_Regina_m0600 = "America/Regina";
        public static final String TZ_Canada_EastmSaskatchewan_m0600 = "Canada_EastmSaskatchewan";
        public static final String TZ_Canada_Saskatchewan_m0600 = "Canada_Saskatchewan";
        public static final String TZ_America_Swift_Current_m0600 = "America/Swift_Current";
        public static final String TZ_America_Tegucigalpa_m0600 = "America/Tegucigalpa";
        public static final String TZ_America_Winnipeg_m0600 = "America/Winnipeg";
        public static final String TZ_Canada_Central_m0600 = "Canada_Central";
        public static final String TZ_CST6CDT_m0600 = "CST6CDT";
        public static final String TZ_Etc_GMTp6_m0600 = "Etc/GMT+6";
        public static final String TZ_Pacific_Easter_m0600 = "Pacific/Easter";
        public static final String TZ_Chile_EasterIsland_m0600 = "Chile_EasterIsland";
        public static final String TZ_Pacific_Galapagos_m0600 = "Pacific/Galapagos";
        public static final String TZ_America_Atikokan_m0500 = "America/Atikokan";
        public static final String TZ_America_Coral_Harbour_m0500 = "America_Coral_Harbour";
        public static final String TZ_America_Bogota_m0500 = "America/Bogota";
        public static final String TZ_America_Cayman_m0500 = "America/Cayman";
        public static final String TZ_America_Detroit_m0500 = "America/Detroit";
        public static final String TZ_US_Michigan_m0500 = "US_Michigan";
        public static final String TZ_America_Grand_Turk_m0500 = "America/Grand_Turk";
        public static final String TZ_America_Guayaquil_m0500 = "America/Guayaquil";
        public static final String TZ_America_Havana_m0500 = "America/Havana";
        public static final String TZ_Cuba_m0500 = "Cuba";
        public static final String TZ_America_Indiana_Indianapolis_m0500 = "America/Indiana/Indianapolis";
        public static final String TZ_America_Fort_Wayne_m0500 = "America_Fort_Wayne";
        public static final String TZ_America_Indianapolis_m0500 = "America_Indianapolis";
        public static final String TZ_US_EastmIndiana_m0500 = "US_EastmIndiana";
        public static final String TZ_America_Indiana_Marengo_m0500 = "America/Indiana/Marengo";
        public static final String TZ_America_Indiana_Petersburg_m0500 = "America/Indiana/Petersburg";
        public static final String TZ_America_Indiana_Vevay_m0500 = "America/Indiana/Vevay";
        public static final String TZ_America_Indiana_Vincennes_m0500 = "America/Indiana/Vincennes";
        public static final String TZ_America_Indiana_Winamac_m0500 = "America/Indiana/Winamac";
        public static final String TZ_America_Iqaluit_m0500 = "America/Iqaluit";
        public static final String TZ_America_Jamaica_m0500 = "America/Jamaica";
        public static final String TZ_Jamaica_m0500 = "Jamaica";
        public static final String TZ_America_Kentucky_Louisville_m0500 = "America/Kentucky/Louisville";
        public static final String TZ_America_Louisville_m0500 = "America_Louisville";
        public static final String TZ_America_Kentucky_Monticello_m0500 = "America/Kentucky/Monticello";
        public static final String TZ_America_Lima_m0500 = "America/Lima";
        public static final String TZ_America_Montreal_m0500 = "America/Montreal";
        public static final String TZ_America_Nassau_m0500 = "America/Nassau";
        public static final String TZ_America_New_York_m0500 = "America/New_York";
        public static final String TZ_US_Eastern_m0500 = "US_Eastern";
        public static final String TZ_America_Nipigon_m0500 = "America/Nipigon";
        public static final String TZ_America_Panama_m0500 = "America/Panama";
        public static final String TZ_America_Pangnirtung_m0500 = "America/Pangnirtung";
        public static final String TZ_America_PortmaumPrince_m0500 = "America/Port-au-Prince";
        public static final String TZ_America_Resolute_m0500 = "America/Resolute";
        public static final String TZ_America_Thunder_Bay_m0500 = "America/Thunder_Bay";
        public static final String TZ_America_Toronto_m0500 = "America/Toronto";
        public static final String TZ_Canada_Eastern_m0500 = "Canada_Eastern";
        public static final String TZ_EST_m0500 = "EST";
        public static final String TZ_EST5EDT_m0500 = "EST5EDT";
        public static final String TZ_Etc_GMTp5_m0500 = "Etc/GMT+5";
        public static final String TZ_America_Caracas_m0430 = "America/Caracas";
        public static final String TZ_America_Anguilla_m0400 = "America/Anguilla";
        public static final String TZ_America_Antigua_m0400 = "America/Antigua";
        public static final String TZ_America_Argentina_San_Luis_m0300 = "America/Argentina/San_Luis";
        public static final String TZ_America_Aruba_m0400 = "America/Aruba";
        public static final String TZ_America_Asuncion_m0400 = "America/Asuncion";
        public static final String TZ_America_Barbados_m0400 = "America/Barbados";
        public static final String TZ_America_BlancmSablon_m0400 = "America/Blanc-Sablon";
        public static final String TZ_America_Boa_Vista_m0400 = "America/Boa_Vista";
        public static final String TZ_America_Campo_Grande_m0400 = "America/Campo_Grande";
        public static final String TZ_America_Cuiaba_m0400 = "America/Cuiaba";
        public static final String TZ_America_Curacao_m0400 = "America/Curacao";
        public static final String TZ_America_Dominica_m0400 = "America/Dominica";
        public static final String TZ_America_Eirunepe_m0400 = "America/Eirunepe";
        public static final String TZ_America_Glace_Bay_m0400 = "America/Glace_Bay";
        public static final String TZ_America_Goose_Bay_m0400 = "America/Goose_Bay";
        public static final String TZ_America_Grenada_m0400 = "America/Grenada";
        public static final String TZ_America_Guadeloupe_m0400 = "America/Guadeloupe";
        public static final String TZ_America_Marigot_m0400 = "America_Marigot";
        public static final String TZ_America_St_Barthelemy_m0400 = "America_St_Barthelemy";
        public static final String TZ_America_Guyana_m0400 = "America/Guyana";
        public static final String TZ_America_Halifax_m0400 = "America/Halifax";
        public static final String TZ_Canada_Atlantic_m0400 = "Canada_Atlantic";
        public static final String TZ_America_La_Paz_m0400 = "America/La_Paz";
        public static final String TZ_America_Manaus_m0400 = "America/Manaus";
        public static final String TZ_Brazil_West_m0400 = "Brazil_West";
        public static final String TZ_America_Martinique_m0400 = "America/Martinique";
        public static final String TZ_America_Moncton_m0400 = "America/Moncton";
        public static final String TZ_America_Montserrat_m0400 = "America/Montserrat";
        public static final String TZ_America_Port_of_Spain_m0400 = "America/Port_of_Spain";
        public static final String TZ_America_Porto_Velho_m0400 = "America/Porto_Velho";
        public static final String TZ_America_Puerto_Rico_m0400 = "America/Puerto_Rico";
        public static final String TZ_America_Rio_Branco_m0400 = "America/Rio_Branco";
        public static final String TZ_America_Porto_Acre_m0400 = "America_Porto_Acre";
        public static final String TZ_Brazil_Acre_m0400 = "Brazil_Acre";
        public static final String TZ_America_Santiago_m0400 = "America/Santiago";
        public static final String TZ_Chile_Continental_m0400 = "Chile_Continental";
        public static final String TZ_America_Santo_Domingo_m0400 = "America/Santo_Domingo";
        public static final String TZ_America_St_Kitts_m0400 = "America/St_Kitts";
        public static final String TZ_America_St_Lucia_m0400 = "America/St_Lucia";
        public static final String TZ_America_St_Thomas_m0400 = "America/St_Thomas";
        public static final String TZ_America_Virgin_m0400 = "America_Virgin";
        public static final String TZ_America_St_Vincent_m0400 = "America/St_Vincent";
        public static final String TZ_America_Thule_m0400 = "America/Thule";
        public static final String TZ_America_Tortola_m0400 = "America/Tortola";
        public static final String TZ_Antarctica_Palmer_m0400 = "Antarctica/Palmer";
        public static final String TZ_Atlantic_Bermuda_m0400 = "Atlantic/Bermuda";
        public static final String TZ_Atlantic_Stanley_m0400 = "Atlantic/Stanley";
        public static final String TZ_Etc_GMTp4_m0400 = "Etc/GMT+4";
        public static final String TZ_America_St_Johns_m0330 = "America/St_Johns";
        public static final String TZ_Canada_Newfoundland_m0330 = "Canada_Newfoundland";
        public static final String TZ_America_Araguaina_m0300 = "America/Araguaina";
        public static final String TZ_America_Argentina_Buenos_Aires_m0300 = "America/Argentina/Buenos_Aires";
        public static final String TZ_America_Buenos_Aires_m0300 = "America_Buenos_Aires";
        public static final String TZ_America_Argentina_Catamarca_m0300 = "America/Argentina/Catamarca";
        public static final String TZ_America_Argentina_ComodRivadavia_m0300 = "America_Argentina_ComodRivadavia";
        public static final String TZ_America_Catamarca_m0300 = "America_Catamarca";
        public static final String TZ_America_Argentina_Cordoba_m0300 = "America/Argentina/Cordoba";
        public static final String TZ_America_Cordoba_m0300 = "America_Cordoba";
        public static final String TZ_America_Rosario_m0300 = "America_Rosario";
        public static final String TZ_America_Argentina_Jujuy_m0300 = "America/Argentina/Jujuy";
        public static final String TZ_America_Jujuy_m0300 = "America_Jujuy";
        public static final String TZ_America_Argentina_La_Rioja_m0300 = "America/Argentina/La_Rioja";
        public static final String TZ_America_Argentina_Mendoza_m0300 = "America/Argentina/Mendoza";
        public static final String TZ_America_Mendoza_m0300 = "America_Mendoza";
        public static final String TZ_America_Argentina_Rio_Gallegos_m0300 = "America/Argentina/Rio_Gallegos";
        public static final String TZ_America_Argentina_Salta_m0300 = "America/Argentina/Salta";
        public static final String TZ_America_Argentina_San_Juan_m0300 = "America/Argentina/San_Juan";
        public static final String TZ_America_Argentina_Tucuman_m0300 = "America/Argentina/Tucuman";
        public static final String TZ_America_Argentina_Ushuaia_m0300 = "America/Argentina/Ushuaia";
        public static final String TZ_America_Bahia_m0300 = "America/Bahia";
        public static final String TZ_America_Belem_m0300 = "America/Belem";
        public static final String TZ_America_Cayenne_m0300 = "America/Cayenne";
        public static final String TZ_America_Fortaleza_m0300 = "America/Fortaleza";
        public static final String TZ_America_Godthab_m0300 = "America/Godthab";
        public static final String TZ_America_Maceio_m0300 = "America/Maceio";
        public static final String TZ_America_Miquelon_m0300 = "America/Miquelon";
        public static final String TZ_America_Montevideo_m0300 = "America/Montevideo";
        public static final String TZ_America_Paramaribo_m0300 = "America/Paramaribo";
        public static final String TZ_America_Recife_m0300 = "America/Recife";
        public static final String TZ_America_Santarem_m0300 = "America/Santarem";
        public static final String TZ_America_Sao_Paulo_m0300 = "America/Sao_Paulo";
        public static final String TZ_Brazil_East_m0300 = "Brazil_East";
        public static final String TZ_Antarctica_Rothera_m0300 = "Antarctica/Rothera";
        public static final String TZ_Etc_GMTp3_m0300 = "Etc/GMT+3";
        public static final String TZ_America_Noronha_m0200 = "America/Noronha";
        public static final String TZ_Brazil_DeNoronha_m0200 = "Brazil_DeNoronha";
        public static final String TZ_Atlantic_South_Georgia_m0200 = "Atlantic/South_Georgia";
        public static final String TZ_Etc_GMTp2_m0200 = "Etc/GMT+2";
        public static final String TZ_America_Scoresbysund_m0100 = "America/Scoresbysund";
        public static final String TZ_Atlantic_Azores_m0100 = "Atlantic/Azores";
        public static final String TZ_Atlantic_Cape_Verde_m0100 = "Atlantic/Cape_Verde";
        public static final String TZ_Etc_GMTp1_m0100 = "Etc/GMT+1";
        public static final String TZ_Africa_Abidjan_p0000 = "Africa/Abidjan";
        public static final String TZ_Africa_Accra_p0000 = "Africa/Accra";
        public static final String TZ_Africa_Bamako_p0000 = "Africa/Bamako";
        public static final String TZ_Africa_Timbuktu_p0000 = "Africa_Timbuktu";
        public static final String TZ_Africa_Banjul_p0000 = "Africa/Banjul";
        public static final String TZ_Africa_Bissau_p0000 = "Africa/Bissau";
        public static final String TZ_Africa_Casablanca_p0000 = "Africa/Casablanca";
        public static final String TZ_Africa_Conakry_p0000 = "Africa/Conakry";
        public static final String TZ_Africa_Dakar_p0000 = "Africa/Dakar";
        public static final String TZ_Africa_El_Aaiun_p0000 = "Africa/El_Aaiun";
        public static final String TZ_Africa_Freetown_p0000 = "Africa/Freetown";
        public static final String TZ_Africa_Lome_p0000 = "Africa/Lome";
        public static final String TZ_Africa_Monrovia_p0000 = "Africa/Monrovia";
        public static final String TZ_Africa_Nouakchott_p0000 = "Africa/Nouakchott";
        public static final String TZ_Africa_Ouagadougou_p0000 = "Africa/Ouagadougou";
        public static final String TZ_Africa_Sao_Tome_p0000 = "Africa/Sao_Tome";
        public static final String TZ_America_Danmarkshavn_p0000 = "America/Danmarkshavn";
        public static final String TZ_Atlantic_Canary_p0000 = "Atlantic/Canary";
        public static final String TZ_Atlantic_Faroe_p0000 = "Atlantic/Faroe";
        public static final String TZ_Atlantic_Faeroe_p0000 = "Atlantic_Faeroe";
        public static final String TZ_Atlantic_Madeira_p0000 = "Atlantic/Madeira";
        public static final String TZ_Atlantic_Reykjavik_p0000 = "Atlantic/Reykjavik";
        public static final String TZ_Iceland_p0000 = "Iceland";
        public static final String TZ_Atlantic_St_Helena_p0000 = "Atlantic/St_Helena";
        public static final String TZ_Etc_GMT_p0000 = "Etc/GMT";
        public static final String TZ_Etc_GMTp0_p0000 = "Etc_GMTp0";
        public static final String TZ_Etc_GMTm0_p0000 = "Etc_GMTm0";
        public static final String TZ_Etc_GMT0_p0000 = "Etc_GMT0";
        public static final String TZ_Etc_Greenwich_p0000 = "Etc_Greenwich";
        public static final String TZ_GMT_p0000 = "GMT";
        public static final String TZ_GMTp0_p0000 = "GMTp0";
        public static final String TZ_GMTm0_p0000 = "GMTm0";
        public static final String TZ_GMT0_p0000 = "GMT0";
        public static final String TZ_Greenwich_p0000 = "Greenwich";
        public static final String TZ_Etc_UCT_p0000 = "Etc/UCT";
        public static final String TZ_UCT_p0000 = "UCT";
        public static final String TZ_Etc_UTC_p0000 = "Etc/UTC";
        public static final String TZ_Etc_Universal_p0000 = "Etc_Universal";
        public static final String TZ_Etc_Zulu_p0000 = "Etc_Zulu";
        public static final String TZ_Universal_p0000 = "Universal";
        public static final String TZ_Zulu_p0000 = "Zulu";
        public static final String TZ_Europe_Dublin_p0000 = "Europe/Dublin";
        public static final String TZ_Eire_p0000 = "Eire";
        public static final String TZ_Europe_Lisbon_p0000 = "Europe/Lisbon";
        public static final String TZ_Portugal_p0000 = "Portugal";
        public static final String TZ_Europe_London_p0000 = "Europe/London";
        public static final String TZ_Europe_Belfast_p0000 = "Europe_Belfast";
        public static final String TZ_Europe_Guernsey_p0000 = "Europe_Guernsey";
        public static final String TZ_Europe_Isle_of_Man_p0000 = "Europe_Isle_of_Man";
        public static final String TZ_Europe_Jersey_p0000 = "Europe_Jersey";
        public static final String TZ_GB_p0000 = "GB";
        public static final String TZ_GBmEire_p0000 = "GBmEire";
        public static final String TZ_UTC_p0000 = "UTC";
        public static final String TZ_WET_p0000 = "WET";
        public static final String TZ_Africa_Algiers_p0100 = "Africa/Algiers";
        public static final String TZ_Africa_Bangui_p0100 = "Africa/Bangui";
        public static final String TZ_Africa_Brazzaville_p0100 = "Africa/Brazzaville";
        public static final String TZ_Africa_Ceuta_p0100 = "Africa/Ceuta";
        public static final String TZ_Africa_Douala_p0100 = "Africa/Douala";
        public static final String TZ_Africa_Kinshasa_p0100 = "Africa/Kinshasa";
        public static final String TZ_Africa_Lagos_p0100 = "Africa/Lagos";
        public static final String TZ_Africa_Libreville_p0100 = "Africa/Libreville";
        public static final String TZ_Africa_Luanda_p0100 = "Africa/Luanda";
        public static final String TZ_Africa_Malabo_p0100 = "Africa/Malabo";
        public static final String TZ_Africa_Ndjamena_p0100 = "Africa/Ndjamena";
        public static final String TZ_Africa_Niamey_p0100 = "Africa/Niamey";
        public static final String TZ_Africa_PortomNovo_p0100 = "Africa/Porto-Novo";
        public static final String TZ_Africa_Tunis_p0100 = "Africa/Tunis";
        public static final String TZ_Africa_Windhoek_p0100 = "Africa/Windhoek";
        public static final String TZ_CET_p0100 = "CET";
        public static final String TZ_Etc_GMTm1_p0100 = "Etc/GMT-1";
        public static final String TZ_Europe_Amsterdam_p0100 = "Europe/Amsterdam";
        public static final String TZ_Europe_Andorra_p0100 = "Europe/Andorra";
        public static final String TZ_Europe_Belgrade_p0100 = "Europe/Belgrade";
        public static final String TZ_Europe_Ljubljana_p0100 = "Europe_Ljubljana";
        public static final String TZ_Europe_Podgorica_p0100 = "Europe_Podgorica";
        public static final String TZ_Europe_Sarajevo_p0100 = "Europe_Sarajevo";
        public static final String TZ_Europe_Skopje_p0100 = "Europe_Skopje";
        public static final String TZ_Europe_Zagreb_p0100 = "Europe_Zagreb";
        public static final String TZ_Europe_Berlin_p0100 = "Europe/Berlin";
        public static final String TZ_Europe_Brussels_p0100 = "Europe/Brussels";
        public static final String TZ_Europe_Budapest_p0100 = "Europe/Budapest";
        public static final String TZ_Europe_Copenhagen_p0100 = "Europe/Copenhagen";
        public static final String TZ_Europe_Gibraltar_p0100 = "Europe/Gibraltar";
        public static final String TZ_Europe_Luxembourg_p0100 = "Europe/Luxembourg";
        public static final String TZ_Europe_Madrid_p0100 = "Europe/Madrid";
        public static final String TZ_Europe_Malta_p0100 = "Europe/Malta";
        public static final String TZ_Europe_Monaco_p0100 = "Europe/Monaco";
        public static final String TZ_Europe_Oslo_p0100 = "Europe/Oslo";
        public static final String TZ_Arctic_Longyearbyen_p0100 = "Arctic_Longyearbyen";
        public static final String TZ_Atlantic_Jan_Mayen_p0100 = "Atlantic_Jan_Mayen";
        public static final String TZ_Europe_Paris_p0100 = "Europe/Paris";
        public static final String TZ_Europe_Prague_p0100 = "Europe/Prague";
        public static final String TZ_Europe_Bratislava_p0100 = "Europe_Bratislava";
        public static final String TZ_Europe_Rome_p0100 = "Europe/Rome";
        public static final String TZ_Europe_San_Marino_p0100 = "Europe_San_Marino";
        public static final String TZ_Europe_Vatican_p0100 = "Europe_Vatican";
        public static final String TZ_Europe_Stockholm_p0100 = "Europe/Stockholm";
        public static final String TZ_Europe_Tirane_p0100 = "Europe/Tirane";
        public static final String TZ_Europe_Vaduz_p0100 = "Europe/Vaduz";
        public static final String TZ_Europe_Vienna_p0100 = "Europe/Vienna";
        public static final String TZ_Europe_Warsaw_p0100 = "Europe/Warsaw";
        public static final String TZ_Poland_p0100 = "Poland";
        public static final String TZ_Europe_Zurich_p0100 = "Europe/Zurich";
        public static final String TZ_MET_p0100 = "MET";
        public static final String TZ_Africa_Blantyre_p0200 = "Africa/Blantyre";
        public static final String TZ_Africa_Bujumbura_p0200 = "Africa/Bujumbura";
        public static final String TZ_Africa_Cairo_p0200 = "Africa/Cairo";
        public static final String TZ_Egypt_p0200 = "Egypt";
        public static final String TZ_Africa_Gaborone_p0200 = "Africa/Gaborone";
        public static final String TZ_Africa_Harare_p0200 = "Africa/Harare";
        public static final String TZ_Africa_Johannesburg_p0200 = "Africa/Johannesburg";
        public static final String TZ_Africa_Kigali_p0200 = "Africa/Kigali";
        public static final String TZ_Africa_Lubumbashi_p0200 = "Africa/Lubumbashi";
        public static final String TZ_Africa_Lusaka_p0200 = "Africa/Lusaka";
        public static final String TZ_Africa_Maputo_p0200 = "Africa/Maputo";
        public static final String TZ_Africa_Maseru_p0200 = "Africa/Maseru";
        public static final String TZ_Africa_Mbabane_p0200 = "Africa/Mbabane";
        public static final String TZ_Africa_Tripoli_p0200 = "Africa/Tripoli";
        public static final String TZ_Libya_p0200 = "Libya";
        public static final String TZ_Asia_Amman_p0200 = "Asia/Amman";
        public static final String TZ_Asia_Beirut_p0200 = "Asia/Beirut";
        public static final String TZ_Asia_Damascus_p0200 = "Asia/Damascus";
        public static final String TZ_Asia_Gaza_p0200 = "Asia/Gaza";
        public static final String TZ_Asia_Jerusalem_p0200 = "Asia/Jerusalem";
        public static final String TZ_Asia_Tel_Aviv_p0200 = "Asia_Tel_Aviv";
        public static final String TZ_Israel_p0200 = "Israel";
        public static final String TZ_Asia_Nicosia_p0200 = "Asia/Nicosia";
        public static final String TZ_Europe_Nicosia_p0200 = "Europe_Nicosia";
        public static final String TZ_EET_p0200 = "EET";
        public static final String TZ_Etc_GMTm2_p0200 = "Etc/GMT-2";
        public static final String TZ_Europe_Athens_p0200 = "Europe/Athens";
        public static final String TZ_Europe_Bucharest_p0200 = "Europe/Bucharest";
        public static final String TZ_Europe_Chisinau_p0200 = "Europe/Chisinau";
        public static final String TZ_Europe_Tiraspol_p0200 = "Europe_Tiraspol";
        public static final String TZ_Europe_Helsinki_p0200 = "Europe/Helsinki";
        public static final String TZ_Europe_Mariehamn_p0200 = "Europe_Mariehamn";
        public static final String TZ_Europe_Istanbul_p0200 = "Europe/Istanbul";
        public static final String TZ_Asia_Istanbul_p0200 = "Asia_Istanbul";
        public static final String TZ_Turkey_p0200 = "Turkey";
        public static final String TZ_Europe_Kaliningrad_p0200 = "Europe/Kaliningrad";
        public static final String TZ_Europe_Kiev_p0200 = "Europe/Kiev";
        public static final String TZ_Europe_Minsk_p0200 = "Europe/Minsk";
        public static final String TZ_Europe_Riga_p0200 = "Europe/Riga";
        public static final String TZ_Europe_Simferopol_p0200 = "Europe/Simferopol";
        public static final String TZ_Europe_Sofia_p0200 = "Europe/Sofia";
        public static final String TZ_Europe_Tallinn_p0200 = "Europe/Tallinn";
        public static final String TZ_Europe_Uzhgorod_p0200 = "Europe/Uzhgorod";
        public static final String TZ_Europe_Vilnius_p0200 = "Europe/Vilnius";
        public static final String TZ_Europe_Zaporozhye_p0200 = "Europe/Zaporozhye";
        public static final String TZ_Africa_Addis_Ababa_p0300 = "Africa/Addis_Ababa";
        public static final String TZ_Africa_Asmara_p0300 = "Africa/Asmara";
        public static final String TZ_Africa_Asmera_p0300 = "Africa_Asmera";
        public static final String TZ_Africa_Dar_es_Salaam_p0300 = "Africa/Dar_es_Salaam";
        public static final String TZ_Africa_Djibouti_p0300 = "Africa/Djibouti";
        public static final String TZ_Africa_Kampala_p0300 = "Africa/Kampala";
        public static final String TZ_Africa_Khartoum_p0300 = "Africa/Khartoum";
        public static final String TZ_Africa_Mogadishu_p0300 = "Africa/Mogadishu";
        public static final String TZ_Africa_Nairobi_p0300 = "Africa/Nairobi";
        public static final String TZ_Antarctica_Syowa_p0300 = "Antarctica/Syowa";
        public static final String TZ_Asia_Aden_p0300 = "Asia/Aden";
        public static final String TZ_Asia_Baghdad_p0300 = "Asia/Baghdad";
        public static final String TZ_Asia_Bahrain_p0300 = "Asia/Bahrain";
        public static final String TZ_Asia_Kuwait_p0300 = "Asia/Kuwait";
        public static final String TZ_Asia_Qatar_p0300 = "Asia/Qatar";
        public static final String TZ_Asia_Riyadh_p0300 = "Asia/Riyadh";
        public static final String TZ_Etc_GMTm3_p0300 = "Etc/GMT-3";
        public static final String TZ_Europe_Moscow_p0300 = "Europe/Moscow";
        public static final String TZ_WmSU_p0300 = "WmSU";
        public static final String TZ_Europe_Samara_p0300 = "Europe/Samara";
        public static final String TZ_Europe_Volgograd_p0300 = "Europe/Volgograd";
        public static final String TZ_Indian_Antananarivo_p0300 = "Indian/Antananarivo";
        public static final String TZ_Indian_Comoro_p0300 = "Indian/Comoro";
        public static final String TZ_Indian_Mayotte_p0300 = "Indian/Mayotte";
        public static final String TZ_Asia_Tehran_p0330 = "Asia/Tehran";
        public static final String TZ_Iran_p0330 = "Iran";
        public static final String TZ_Asia_Baku_p0400 = "Asia/Baku";
        public static final String TZ_Asia_Dubai_p0400 = "Asia/Dubai";
        public static final String TZ_Asia_Muscat_p0400 = "Asia/Muscat";
        public static final String TZ_Asia_Tbilisi_p0400 = "Asia/Tbilisi";
        public static final String TZ_Asia_Yerevan_p0400 = "Asia/Yerevan";
        public static final String TZ_Etc_GMTm4_p0400 = "Etc/GMT-4";
        public static final String TZ_Indian_Mahe_p0400 = "Indian/Mahe";
        public static final String TZ_Indian_Mauritius_p0400 = "Indian/Mauritius";
        public static final String TZ_Indian_Reunion_p0400 = "Indian/Reunion";
        public static final String TZ_Asia_Kabul_p0430 = "Asia/Kabul";
        public static final String TZ_Antarctica_Mawson_p0500 = "Antarctica/Mawson";
        public static final String TZ_Asia_Aqtau_p0500 = "Asia/Aqtau";
        public static final String TZ_Asia_Aqtobe_p0500 = "Asia/Aqtobe";
        public static final String TZ_Asia_Ashgabat_p0500 = "Asia/Ashgabat";
        public static final String TZ_Asia_Ashkhabad_p0500 = "Asia_Ashkhabad";
        public static final String TZ_Asia_Dushanbe_p0500 = "Asia/Dushanbe";
        public static final String TZ_Asia_Karachi_p0500 = "Asia/Karachi";
        public static final String TZ_Asia_Oral_p0500 = "Asia/Oral";
        public static final String TZ_Asia_Samarkand_p0500 = "Asia/Samarkand";
        public static final String TZ_Asia_Tashkent_p0500 = "Asia/Tashkent";
        public static final String TZ_Asia_Yekaterinburg_p0500 = "Asia/Yekaterinburg";
        public static final String TZ_Etc_GMTm5_p0500 = "Etc/GMT-5";
        public static final String TZ_Indian_Kerguelen_p0500 = "Indian/Kerguelen";
        public static final String TZ_Indian_Maldives_p0500 = "Indian/Maldives";
        public static final String TZ_Asia_Colombo_p0530 = "Asia/Colombo";
        public static final String TZ_Asia_Kolkata_p0530 = "Asia/Kolkata";
        public static final String TZ_Asia_Calcutta_p0530 = "Asia_Calcutta";
        public static final String TZ_Asia_Kathmandu_p0545 = "Asia/Kathmandu";
        public static final String TZ_Asia_Katmandu_p0545 = "Asia_Katmandu";
        public static final String TZ_Antarctica_Vostok_p0600 = "Antarctica/Vostok";
        public static final String TZ_Asia_Almaty_p0600 = "Asia/Almaty";
        public static final String TZ_Asia_Bishkek_p0600 = "Asia/Bishkek";
        public static final String TZ_Asia_Dhaka_p0600 = "Asia/Dhaka";
        public static final String TZ_Asia_Dacca_p0600 = "Asia_Dacca";
        public static final String TZ_Asia_Novokuznetsk_p0600 = "Asia/Novokuznetsk";
        public static final String TZ_Asia_Novosibirsk_p0600 = "Asia/Novosibirsk";
        public static final String TZ_Asia_Omsk_p0600 = "Asia/Omsk";
        public static final String TZ_Asia_Qyzylorda_p0600 = "Asia/Qyzylorda";
        public static final String TZ_Asia_Thimphu_p0600 = "Asia/Thimphu";
        public static final String TZ_Asia_Thimbu_p0600 = "Asia_Thimbu";
        public static final String TZ_Etc_GMTm6_p0600 = "Etc/GMT-6";
        public static final String TZ_Indian_Chagos_p0600 = "Indian/Chagos";
        public static final String TZ_Asia_Rangoon_p0630 = "Asia/Rangoon";
        public static final String TZ_Indian_Cocos_p0630 = "Indian/Cocos";
        public static final String TZ_Antarctica_Davis_p0700 = "Antarctica/Davis";
        public static final String TZ_Asia_Bangkok_p0700 = "Asia/Bangkok";
        public static final String TZ_Asia_Ho_Chi_Minh_p0700 = "Asia/Ho_Chi_Minh";
        public static final String TZ_Asia_Saigon_p0700 = "Asia_Saigon";
        public static final String TZ_Asia_Hovd_p0700 = "Asia/Hovd";
        public static final String TZ_Asia_Jakarta_p0700 = "Asia/Jakarta";
        public static final String TZ_Asia_Krasnoyarsk_p0700 = "Asia/Krasnoyarsk";
        public static final String TZ_Asia_Phnom_Penh_p0700 = "Asia/Phnom_Penh";
        public static final String TZ_Asia_Pontianak_p0700 = "Asia/Pontianak";
        public static final String TZ_Asia_Vientiane_p0700 = "Asia/Vientiane";
        public static final String TZ_Etc_GMTm7_p0700 = "Etc/GMT-7";
        public static final String TZ_Indian_Christmas_p0700 = "Indian/Christmas";
        public static final String TZ_Antarctica_Casey_p0800 = "Antarctica/Casey";
        public static final String TZ_Asia_Brunei_p0800 = "Asia/Brunei";
        public static final String TZ_Asia_Choibalsan_p0800 = "Asia/Choibalsan";
        public static final String TZ_Asia_Chongqing_p0800 = "Asia/Chongqing";
        public static final String TZ_Asia_Chungking_p0800 = "Asia_Chungking";
        public static final String TZ_Asia_Harbin_p0800 = "Asia/Harbin";
        public static final String TZ_Asia_Hong_Kong_p0800 = "Asia/Hong_Kong";
        public static final String TZ_Hongkong_p0800 = "Hongkong";
        public static final String TZ_Asia_Irkutsk_p0800 = "Asia/Irkutsk";
        public static final String TZ_Asia_Kashgar_p0800 = "Asia/Kashgar";
        public static final String TZ_Asia_Kuala_Lumpur_p0800 = "Asia/Kuala_Lumpur";
        public static final String TZ_Asia_Kuching_p0800 = "Asia/Kuching";
        public static final String TZ_Asia_Macau_p0800 = "Asia/Macau";
        public static final String TZ_Asia_Macao_p0800 = "Asia_Macao";
        public static final String TZ_Asia_Makassar_p0800 = "Asia/Makassar";
        public static final String TZ_Asia_Ujung_Pandang_p0800 = "Asia_Ujung_Pandang";
        public static final String TZ_Asia_Manila_p0800 = "Asia/Manila";
        public static final String TZ_Asia_Shanghai_p0800 = "Asia/Shanghai";
        public static final String TZ_PRC_p0800 = "PRC";
        public static final String TZ_Asia_Singapore_p0800 = "Asia/Singapore";
        public static final String TZ_Singapore_p0800 = "Singapore";
        public static final String TZ_Asia_Taipei_p0800 = "Asia/Taipei";
        public static final String TZ_ROC_p0800 = "ROC";
        public static final String TZ_Asia_Ulaanbaatar_p0800 = "Asia/Ulaanbaatar";
        public static final String TZ_Asia_Ulan_Bator_p0800 = "Asia_Ulan_Bator";
        public static final String TZ_Asia_Urumqi_p0800 = "Asia/Urumqi";
        public static final String TZ_Australia_Perth_p0800 = "Australia/Perth";
        public static final String TZ_Australia_West_p0800 = "Australia_West";
        public static final String TZ_Etc_GMTm8_p0800 = "Etc/GMT-8";
        public static final String TZ_Australia_Eucla_p0845 = "Australia/Eucla";
        public static final String TZ_Asia_Dili_p0900 = "Asia/Dili";
        public static final String TZ_Asia_Jayapura_p0900 = "Asia/Jayapura";
        public static final String TZ_Asia_Pyongyang_p0900 = "Asia/Pyongyang";
        public static final String TZ_Asia_Seoul_p0900 = "Asia/Seoul";
        public static final String TZ_ROK_p0900 = "ROK";
        public static final String TZ_Asia_Tokyo_p0900 = "Asia/Tokyo";
        public static final String TZ_Japan_p0900 = "Japan";
        public static final String TZ_Asia_Yakutsk_p0900 = "Asia/Yakutsk";
        public static final String TZ_Etc_GMTm9_p0900 = "Etc/GMT-9";
        public static final String TZ_Pacific_Palau_p0900 = "Pacific/Palau";
        public static final String TZ_Australia_Adelaide_p0930 = "Australia/Adelaide";
        public static final String TZ_Australia_South_p0930 = "Australia_South";
        public static final String TZ_Australia_Broken_Hill_p0930 = "Australia/Broken_Hill";
        public static final String TZ_Australia_Yancowinna_p0930 = "Australia_Yancowinna";
        public static final String TZ_Australia_Darwin_p0930 = "Australia/Darwin";
        public static final String TZ_Australia_North_p0930 = "Australia_North";
        public static final String TZ_Antarctica_DumontDUrville_p1000 = "Antarctica/DumontDUrville";
        public static final String TZ_Asia_Sakhalin_p1000 = "Asia/Sakhalin";
        public static final String TZ_Asia_Vladivostok_p1000 = "Asia/Vladivostok";
        public static final String TZ_Australia_Brisbane_p1000 = "Australia/Brisbane";
        public static final String TZ_Australia_Queensland_p1000 = "Australia_Queensland";
        public static final String TZ_Australia_Currie_p1000 = "Australia/Currie";
        public static final String TZ_Australia_Hobart_p1000 = "Australia/Hobart";
        public static final String TZ_Australia_Tasmania_p1000 = "Australia_Tasmania";
        public static final String TZ_Australia_Lindeman_p1000 = "Australia/Lindeman";
        public static final String TZ_Australia_Melbourne_p1000 = "Australia/Melbourne";
        public static final String TZ_Australia_Victoria_p1000 = "Australia_Victoria";
        public static final String TZ_Australia_Sydney_p1000 = "Australia/Sydney";
        public static final String TZ_Australia_ACT_p1000 = "Australia_ACT";
        public static final String TZ_Australia_Canberra_p1000 = "Australia_Canberra";
        public static final String TZ_Australia_NSW_p1000 = "Australia_NSW";
        public static final String TZ_Etc_GMTm10_p1000 = "Etc/GMT-10";
        public static final String TZ_Pacific_Chuuk_p1000 = "Pacific/Chuuk";
        public static final String TZ_Pacific_Truk_p1000 = "Pacific_Truk";
        public static final String TZ_Pacific_Yap_p1000 = "Pacific_Yap";
        public static final String TZ_Pacific_Guam_p1000 = "Pacific/Guam";
        public static final String TZ_Pacific_Port_Moresby_p1000 = "Pacific/Port_Moresby";
        public static final String TZ_Pacific_Saipan_p1000 = "Pacific/Saipan";
        public static final String TZ_Australia_Lord_Howe_p1030 = "Australia/Lord_Howe";
        public static final String TZ_Australia_LHI_p1030 = "Australia_LHI";
        public static final String TZ_Antarctica_Macquarie_p1100 = "Antarctica/Macquarie";
        public static final String TZ_Asia_Anadyr_p1100 = "Asia/Anadyr";
        public static final String TZ_Asia_Kamchatka_p1100 = "Asia/Kamchatka";
        public static final String TZ_Asia_Magadan_p1100 = "Asia/Magadan";
        public static final String TZ_Etc_GMTm11_p1100 = "Etc/GMT-11";
        public static final String TZ_Pacific_Efate_p1100 = "Pacific/Efate";
        public static final String TZ_Pacific_Guadalcanal_p1100 = "Pacific/Guadalcanal";
        public static final String TZ_Pacific_Kosrae_p1100 = "Pacific/Kosrae";
        public static final String TZ_Pacific_Noumea_p1100 = "Pacific/Noumea";
        public static final String TZ_Pacific_Pohnpei_p1100 = "Pacific/Pohnpei";
        public static final String TZ_Pacific_Ponape_p1100 = "Pacific_Ponape";
        public static final String TZ_Pacific_Norfolk_p1130 = "Pacific/Norfolk";
        public static final String TZ_Antarctica_McMurdo_p1200 = "Antarctica/McMurdo";
        public static final String TZ_Antarctica_South_Pole_p1200 = "Antarctica_South_Pole";
        public static final String TZ_Etc_GMTm12_p1200 = "Etc/GMT-12";
        public static final String TZ_Pacific_Auckland_p1200 = "Pacific/Auckland";
        public static final String TZ_NZ_p1200 = "NZ";
        public static final String TZ_Pacific_Fiji_p1200 = "Pacific/Fiji";
        public static final String TZ_Pacific_Funafuti_p1200 = "Pacific/Funafuti";
        public static final String TZ_Pacific_Kwajalein_p1200 = "Pacific/Kwajalein";
        public static final String TZ_Kwajalein_p1200 = "Kwajalein";
        public static final String TZ_Pacific_Majuro_p1200 = "Pacific/Majuro";
        public static final String TZ_Pacific_Nauru_p1200 = "Pacific/Nauru";
        public static final String TZ_Pacific_Tarawa_p1200 = "Pacific/Tarawa";
        public static final String TZ_Pacific_Wake_p1200 = "Pacific/Wake";
        public static final String TZ_Pacific_Wallis_p1200 = "Pacific/Wallis";
        public static final String TZ_Pacific_Chatham_p1245 = "Pacific/Chatham";
        public static final String TZ_NZmCHAT_p1245 = "NZmCHAT";
        public static final String TZ_Etc_GMTm13_p1300 = "Etc/GMT-13";
        public static final String TZ_Pacific_Enderbury_p1300 = "Pacific/Enderbury";
        public static final String TZ_Pacific_Tongatapu_p1300 = "Pacific/Tongatapu";
        public static final String TZ_Etc_GMTm14_p1400 = "Etc/GMT-14";
        public static final String TZ_Pacific_Kiritimati_p1400 = "Pacific/Kiritimati";

        private static Map<String, String> timeZoneValueMap = new ConcurrentHashMap<>();

        static {
            for (Field field : TimeZoneConstant.class.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    if (field.getType().isAssignableFrom(String.class)) {
                        try {
                            timeZoneValueMap.put(String.valueOf(field.get("value")), field.getName());
                        } catch (IllegalAccessException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }

        public static boolean isValidTimeZone(String tz){
            return timeZoneValueMap.containsKey(tz);
        }
    }
}
