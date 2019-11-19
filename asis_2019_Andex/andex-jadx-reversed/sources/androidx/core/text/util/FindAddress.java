package androidx.core.text.util;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestrictTo({Scope.LIBRARY_GROUP_PREFIX})
class FindAddress {
    private static final String HOUSE_COMPONENT = "(?:one|\\d+([a-z](?=[^a-z]|$)|st|nd|rd|th)?)";
    private static final String HOUSE_END = "(?=[,\"'\t                　\n\u000b\f\r  ]|$)";
    private static final String HOUSE_POST_DELIM = ",\"'\t                　\n\u000b\f\r  ";
    private static final String HOUSE_PRE_DELIM = ":,\"'\t                　\n\u000b\f\r  ";
    private static final int MAX_ADDRESS_LINES = 5;
    private static final int MAX_ADDRESS_WORDS = 14;
    private static final int MAX_LOCATION_NAME_DISTANCE = 5;
    private static final int MIN_ADDRESS_WORDS = 4;
    private static final String NL = "\n\u000b\f\r  ";
    private static final String SP = "\t                　";
    private static final String WORD_DELIM = ",*•\t                　\n\u000b\f\r  ";
    private static final String WORD_END = "(?=[,*•\t                　\n\u000b\f\r  ]|$)";
    private static final String WS = "\t                　\n\u000b\f\r  ";
    private static final int kMaxAddressNameWordLength = 25;
    private static final Pattern sHouseNumberRe = Pattern.compile("(?:one|\\d+([a-z](?=[^a-z]|$)|st|nd|rd|th)?)(?:-(?:one|\\d+([a-z](?=[^a-z]|$)|st|nd|rd|th)?))*(?=[,\"'\t                　\n\u000b\f\r  ]|$)", 2);
    private static final Pattern sLocationNameRe = Pattern.compile("(?:alley|annex|arcade|ave[.]?|avenue|alameda|bayou|beach|bend|bluffs?|bottom|boulevard|branch|bridge|brooks?|burgs?|bypass|broadway|camino|camp|canyon|cape|causeway|centers?|circles?|cliffs?|club|common|corners?|course|courts?|coves?|creek|crescent|crest|crossing|crossroad|curve|circulo|dale|dam|divide|drives?|estates?|expressway|extensions?|falls?|ferry|fields?|flats?|fords?|forest|forges?|forks?|fort|freeway|gardens?|gateway|glens?|greens?|groves?|harbors?|haven|heights|highway|hills?|hollow|inlet|islands?|isle|junctions?|keys?|knolls?|lakes?|land|landing|lane|lights?|loaf|locks?|lodge|loop|mall|manors?|meadows?|mews|mills?|mission|motorway|mount|mountains?|neck|orchard|oval|overpass|parks?|parkways?|pass|passage|path|pike|pines?|plains?|plaza|points?|ports?|prairie|privada|radial|ramp|ranch|rapids?|rd[.]?|rest|ridges?|river|roads?|route|row|rue|run|shoals?|shores?|skyway|springs?|spurs?|squares?|station|stravenue|stream|st[.]?|streets?|summit|speedway|terrace|throughway|trace|track|trafficway|trail|tunnel|turnpike|underpass|unions?|valleys?|viaduct|views?|villages?|ville|vista|walks?|wall|ways?|wells?|xing|xrd)(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);
    private static final Pattern sStateRe = Pattern.compile("(?:(ak|alaska)|(al|alabama)|(ar|arkansas)|(as|american[\t                　]+samoa)|(az|arizona)|(ca|california)|(co|colorado)|(ct|connecticut)|(dc|district[\t                　]+of[\t                　]+columbia)|(de|delaware)|(fl|florida)|(fm|federated[\t                　]+states[\t                　]+of[\t                　]+micronesia)|(ga|georgia)|(gu|guam)|(hi|hawaii)|(ia|iowa)|(id|idaho)|(il|illinois)|(in|indiana)|(ks|kansas)|(ky|kentucky)|(la|louisiana)|(ma|massachusetts)|(md|maryland)|(me|maine)|(mh|marshall[\t                　]+islands)|(mi|michigan)|(mn|minnesota)|(mo|missouri)|(mp|northern[\t                　]+mariana[\t                　]+islands)|(ms|mississippi)|(mt|montana)|(nc|north[\t                　]+carolina)|(nd|north[\t                　]+dakota)|(ne|nebraska)|(nh|new[\t                　]+hampshire)|(nj|new[\t                　]+jersey)|(nm|new[\t                　]+mexico)|(nv|nevada)|(ny|new[\t                　]+york)|(oh|ohio)|(ok|oklahoma)|(or|oregon)|(pa|pennsylvania)|(pr|puerto[\t                　]+rico)|(pw|palau)|(ri|rhode[\t                　]+island)|(sc|south[\t                　]+carolina)|(sd|south[\t                　]+dakota)|(tn|tennessee)|(tx|texas)|(ut|utah)|(va|virginia)|(vi|virgin[\t                　]+islands)|(vt|vermont)|(wa|washington)|(wi|wisconsin)|(wv|west[\t                　]+virginia)|(wy|wyoming))(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);
    private static final ZipRange[] sStateZipCodeRanges = {new ZipRange(99, 99, -1, -1), new ZipRange(35, 36, -1, -1), new ZipRange(71, 72, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(85, 86, -1, -1), new ZipRange(90, 96, -1, -1), new ZipRange(80, 81, -1, -1), new ZipRange(6, 6, -1, -1), new ZipRange(20, 20, -1, -1), new ZipRange(19, 19, -1, -1), new ZipRange(32, 34, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(30, 31, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(50, 52, -1, -1), new ZipRange(83, 83, -1, -1), new ZipRange(60, 62, -1, -1), new ZipRange(46, 47, -1, -1), new ZipRange(66, 67, 73, -1), new ZipRange(40, 42, -1, -1), new ZipRange(70, 71, -1, -1), new ZipRange(1, 2, -1, -1), new ZipRange(20, 21, -1, -1), new ZipRange(3, 4, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(48, 49, -1, -1), new ZipRange(55, 56, -1, -1), new ZipRange(63, 65, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(38, 39, -1, -1), new ZipRange(55, 56, -1, -1), new ZipRange(27, 28, -1, -1), new ZipRange(58, 58, -1, -1), new ZipRange(68, 69, -1, -1), new ZipRange(3, 4, -1, -1), new ZipRange(7, 8, -1, -1), new ZipRange(87, 88, 86, -1), new ZipRange(88, 89, 96, -1), new ZipRange(10, 14, 0, 6), new ZipRange(43, 45, -1, -1), new ZipRange(73, 74, -1, -1), new ZipRange(97, 97, -1, -1), new ZipRange(15, 19, -1, -1), new ZipRange(6, 6, 0, 9), new ZipRange(96, 96, -1, -1), new ZipRange(2, 2, -1, -1), new ZipRange(29, 29, -1, -1), new ZipRange(57, 57, -1, -1), new ZipRange(37, 38, -1, -1), new ZipRange(75, 79, 87, 88), new ZipRange(84, 84, -1, -1), new ZipRange(22, 24, 20, -1), new ZipRange(6, 9, -1, -1), new ZipRange(5, 5, -1, -1), new ZipRange(98, 99, -1, -1), new ZipRange(53, 54, -1, -1), new ZipRange(24, 26, -1, -1), new ZipRange(82, 83, -1, -1)};
    private static final Pattern sSuffixedNumberRe = Pattern.compile("(\\d+)(st|nd|rd|th)", 2);
    private static final Pattern sWordRe = Pattern.compile("[^,*•\t                　\n\u000b\f\r  ]+(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);
    private static final Pattern sZipCodeRe = Pattern.compile("(?:\\d{5}(?:-\\d{4})?)(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);

    private static class ZipRange {
        int mException1;
        int mException2;
        int mHigh;
        int mLow;

        ZipRange(int i, int i2, int i3, int i4) {
            this.mLow = i;
            this.mHigh = i2;
            this.mException1 = i3;
            this.mException2 = i4;
        }

        /* access modifiers changed from: 0000 */
        public boolean matches(String str) {
            int parseInt = Integer.parseInt(str.substring(0, 2));
            if ((this.mLow <= parseInt && parseInt <= this.mHigh) || parseInt == this.mException1 || parseInt == this.mException2) {
                return true;
            }
            return false;
        }
    }

    private static boolean checkHouseNumber(String str) {
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (Character.isDigit(str.charAt(i2))) {
                i++;
            }
        }
        if (i > 5) {
            return false;
        }
        Matcher matcher = sSuffixedNumberRe.matcher(str);
        if (!matcher.find()) {
            return true;
        }
        int parseInt = Integer.parseInt(matcher.group(1));
        if (parseInt == 0) {
            return false;
        }
        String lowerCase = matcher.group(2).toLowerCase(Locale.getDefault());
        int i3 = parseInt % 10;
        String str2 = "th";
        if (i3 == 1) {
            if (parseInt % 100 != 11) {
                str2 = "st";
            }
            return lowerCase.equals(str2);
        } else if (i3 == 2) {
            if (parseInt % 100 != 12) {
                str2 = "nd";
            }
            return lowerCase.equals(str2);
        } else if (i3 != 3) {
            return lowerCase.equals(str2);
        } else {
            if (parseInt % 100 != 13) {
                str2 = "rd";
            }
            return lowerCase.equals(str2);
        }
    }

    @VisibleForTesting
    public static MatchResult matchHouseNumber(String str, int i) {
        if (i > 0) {
            if (HOUSE_PRE_DELIM.indexOf(str.charAt(i - 1)) == -1) {
                return null;
            }
        }
        Matcher region = sHouseNumberRe.matcher(str).region(i, str.length());
        if (region.lookingAt()) {
            MatchResult matchResult = region.toMatchResult();
            if (checkHouseNumber(matchResult.group(0))) {
                return matchResult;
            }
        }
        return null;
    }

    @VisibleForTesting
    public static MatchResult matchState(String str, int i) {
        MatchResult matchResult = null;
        if (i > 0) {
            if (WORD_DELIM.indexOf(str.charAt(i - 1)) == -1) {
                return null;
            }
        }
        Matcher region = sStateRe.matcher(str).region(i, str.length());
        if (region.lookingAt()) {
            matchResult = region.toMatchResult();
        }
        return matchResult;
    }

    private static boolean isValidZipCode(String str, MatchResult matchResult) {
        boolean z = false;
        if (matchResult == null) {
            return false;
        }
        int groupCount = matchResult.groupCount();
        while (true) {
            if (groupCount <= 0) {
                break;
            }
            int i = groupCount - 1;
            if (matchResult.group(groupCount) != null) {
                groupCount = i;
                break;
            }
            groupCount = i;
        }
        if (sZipCodeRe.matcher(str).matches() && sStateZipCodeRanges[groupCount].matches(str)) {
            z = true;
        }
        return z;
    }

    @VisibleForTesting
    public static boolean isValidZipCode(String str, String str2) {
        return isValidZipCode(str, matchState(str2, 0));
    }

    @VisibleForTesting
    public static boolean isValidZipCode(String str) {
        return sZipCodeRe.matcher(str).matches();
    }

    @VisibleForTesting
    public static boolean isValidLocationName(String str) {
        return sLocationNameRe.matcher(str).matches();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0027, code lost:
        return -r13;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int attemptMatch(java.lang.String r13, java.util.regex.MatchResult r14) {
        /*
            int r14 = r14.end()
            java.util.regex.Pattern r0 = sWordRe
            java.util.regex.Matcher r0 = r0.matcher(r13)
            r1 = -1
            r2 = 0
            r3 = 1
            java.lang.String r4 = ""
            r9 = r4
            r4 = 1
            r5 = 1
            r6 = 1
            r7 = 0
            r8 = -1
            r10 = -1
        L_0x0016:
            int r11 = r13.length()
            if (r14 >= r11) goto L_0x00d7
            boolean r11 = r0.find(r14)
            if (r11 != 0) goto L_0x0028
            int r13 = r13.length()
        L_0x0026:
            int r13 = -r13
            return r13
        L_0x0028:
            int r11 = r0.end()
            int r12 = r0.start()
            int r11 = r11 - r12
            r12 = 25
            if (r11 <= r12) goto L_0x003a
            int r13 = r0.end()
            goto L_0x0026
        L_0x003a:
            int r11 = r0.start()
            if (r14 >= r11) goto L_0x0052
            int r11 = r14 + 1
            char r14 = r13.charAt(r14)
            java.lang.String r12 = "\n\u000b\f\r  "
            int r14 = r12.indexOf(r14)
            if (r14 == r1) goto L_0x0050
            int r4 = r4 + 1
        L_0x0050:
            r14 = r11
            goto L_0x003a
        L_0x0052:
            r11 = 5
            if (r4 <= r11) goto L_0x0057
            goto L_0x00d7
        L_0x0057:
            int r5 = r5 + r3
            r12 = 14
            if (r5 <= r12) goto L_0x005e
            goto L_0x00d7
        L_0x005e:
            java.util.regex.MatchResult r12 = matchHouseNumber(r13, r14)
            if (r12 == 0) goto L_0x006e
            if (r6 == 0) goto L_0x006a
            if (r4 <= r3) goto L_0x006a
            int r13 = -r14
            return r13
        L_0x006a:
            if (r8 != r1) goto L_0x00cd
            r8 = r14
            goto L_0x00cd
        L_0x006e:
            java.lang.String r6 = r0.group(r2)
            boolean r6 = isValidLocationName(r6)
            if (r6 == 0) goto L_0x007b
            r6 = 0
            r7 = 1
            goto L_0x00cd
        L_0x007b:
            if (r5 != r11) goto L_0x0084
            if (r7 != 0) goto L_0x0084
            int r14 = r0.end()
            goto L_0x00d7
        L_0x0084:
            if (r7 == 0) goto L_0x00cc
            r6 = 4
            if (r5 <= r6) goto L_0x00cc
            java.util.regex.MatchResult r14 = matchState(r13, r14)
            if (r14 == 0) goto L_0x00cc
            java.lang.String r6 = "et"
            boolean r6 = r9.equals(r6)
            if (r6 == 0) goto L_0x00a8
            java.lang.String r6 = r14.group(r2)
            java.lang.String r9 = "al"
            boolean r6 = r6.equals(r9)
            if (r6 == 0) goto L_0x00a8
            int r14 = r14.end()
            goto L_0x00d7
        L_0x00a8:
            java.util.regex.Pattern r6 = sWordRe
            java.util.regex.Matcher r6 = r6.matcher(r13)
            int r9 = r14.end()
            boolean r9 = r6.find(r9)
            if (r9 == 0) goto L_0x00c7
            java.lang.String r9 = r6.group(r2)
            boolean r14 = isValidZipCode(r9, r14)
            if (r14 == 0) goto L_0x00cc
            int r13 = r6.end()
            return r13
        L_0x00c7:
            int r14 = r14.end()
            r10 = r14
        L_0x00cc:
            r6 = 0
        L_0x00cd:
            java.lang.String r9 = r0.group(r2)
            int r14 = r0.end()
            goto L_0x0016
        L_0x00d7:
            if (r10 <= 0) goto L_0x00da
            return r10
        L_0x00da:
            if (r8 <= 0) goto L_0x00dd
            r14 = r8
        L_0x00dd:
            int r13 = -r14
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.text.util.FindAddress.attemptMatch(java.lang.String, java.util.regex.MatchResult):int");
    }

    static String findAddress(String str) {
        Matcher matcher = sHouseNumberRe.matcher(str);
        int i = 0;
        while (matcher.find(i)) {
            if (checkHouseNumber(matcher.group(0))) {
                int start = matcher.start();
                int attemptMatch = attemptMatch(str, matcher);
                if (attemptMatch > 0) {
                    return str.substring(start, attemptMatch);
                }
                i = -attemptMatch;
            } else {
                i = matcher.end();
            }
        }
        return null;
    }

    private FindAddress() {
    }
}
