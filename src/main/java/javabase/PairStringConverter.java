package javabase;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PairStringConverter {

    public static List<String> convert(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts = input.split(";");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int plusIndex = trimmed.indexOf('+');
            String token = plusIndex >= 0 ? trimmed.substring(0, plusIndex) : trimmed;
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    public static void main(String[] args) {
        List<String> convert1 = convert("UsernameQfzf3+UsernameQfzf3;NotHighvalueflag+NotHighvalueflag;DXPD_CZ1+DXPD_CZ1;CAERRQ+CAERRQ;ZSJZH+ZSJZH;SMRZ+SMRZ;CZQTHX+CZQTHX;SQK_SALE_LABEL3+SQK_SALE_LABEL3;KYWGM+KYWGM;MFYHQTWO+MFYHQTWO;DX_CA+DX_CA;GXSCTH+GXSCTH;Safehotelin+Safehotelin;CAXKLC+CAXKLC;FCZGP+FCZGP;SCGP+SCGP;DXPD_GJ+DXPD_GJ;Gmcaerusername+Gmcaerusername;FHSRY+FHSRY;ZHHJYL2020+ZHHJYL2020;UNGYCSYDS+UNGYCSYDS;NOCZCKG+NOCZCKG;ZHWHZLTY1+ZHWHZLTY1;TWO+TWO;ZHJHMD+ZHJHMD;ZSJ+ZSJ;flightDomPlatformOldValueLow+flightDomPlatformOldValueLow;FNC2+FNC2;YJHSRY+YJHSRY;UsernameFuyingre+UsernameFuyingre;GXHTJ+GXHTJ;TGP+TGP;DXPD_G5+DXPD_G5;UsernameDcyh+UsernameDcyh;GXGQ23+GXGQ23;ZHWHZLX+ZHWHZLX;FLD+FLD;ZHHJDFYL+ZHHJDFYL;DXPD_3U+DXPD_3U;DXPD_TV+DXPD_TV;TOUCHHN+TOUCHHN;ZHWHZL+ZHWHZL;QJDBMD+QJDBMD;FlightFamilyTravelling+FlightFamilyTravelling;CZSP1+CZSP1;market_notnew_flight_label+market_notnew_flight_label;UsernameGmlnrqUsername+UsernameGmlnrqUsername;UsernameGmbtwoUsername+UsernameGmbtwoUsername;flightnew_midvalue+flightnew_midvalue;CAXK1+CAXK1;TPL+TPL;ZHHZRQ+ZHHZRQ;ZSJCA+ZSJCA;MUQCZL+MUQCZL;twell_new_float+twell_new_float;BMD+BMD;ZHCYFX4+ZHCYFX4;flight_notnewsecnew+flight_notnewsecnew;SAF4+SAF4;YCGP+YCGP;ZHCYFX2+ZHCYFX2;DXPD_KY+DXPD_KY;HYMBWK+HYMBWK;SAF3+SAF3;DX_ZH+DX_ZH;SAF1+SAF1;jijiu0rendui+jijiu0rendui;publicusername20211014+publicusername20211014;SCSCLJ+SCSCLJ;UsernameNewchailvcard+UsernameNewchailvcard;LCHBVIP+LCHBVIP;TVSCLJ+TVSCLJ;ZHZBYL1208+ZHZBYL1208;DXPD_MF+DXPD_MF;flight_travel_user+flight_travel_user;NOTBUYMU+NOTBUYMU;UserName1350Age+UserName1350Age;DXPD_DZ+DXPD_DZ;DXPD_DR+DXPD_DR;ZHYLGM1+ZHYLGM1;FYTGJ+FYTGJ;BUYKYVIP+BUYKYVIP;MFSCLJ+MFSCLJ;YNWZF+YNWZF;MUHYJB+MUHYJB;ZYZJ+ZYZJ;DXPD_JR+DXPD_JR;DXPD_RY+DXPD_RY;BQXZ+BQXZ;notjingdui+notjingdui;GSGDF+GSGDF;flightOldUncixin+flightOldUncixin;DXPD_9C+DXPD_9C;PD_HSYG+PD_HSYG;DXPD_ZH+DXPD_ZH;UNPAY+UNPAY;UsernameGmbthrUsername+UsernameGmbthrUsername;YDL+YDL;FlightOld+FlightOld;DXPD_KN+DXPD_KN;TSA+TSA;DXPD+DXPD;SCRQ+SCRQ;Gmoneusername+Gmoneusername;UsernameGmboneUsername+UsernameGmboneUsername;UsernameLyhx+UsernameLyhx;FHSRYHEI+FHSRYHEI;DXPD_BL+DXPD_BL;UsernameTxnl+UsernameTxnl;XCD+XCD;NotBuySc+NotBuySc;DXPD_BK+DXPD_BK;DXPD_SC+DXPD_SC;MUJF+MUJF;mtxbanner+mtxbanner;ZSJ6jingdui+ZSJ6jingdui;BUYQW+BUYQW;DXPD_HO+DXPD_HO;PTGP+PTGP;DXPD_PN+DXPD_PN;FHN+FHN;market_old_flight_label+market_old_flight_label;businessusernamegmmayusername+businessusernamegmmayusername;DXPD_GY+DXPD_GY;GHWFK+GHWFK;CZAGE+CZAGE;ZHA+ZHA;MUBMD+MUBMD;noOpenflower+noOpenflower;KYHY+KYHY;ZHZBYL+ZHZBYL;FlightBusinessTravelling+FlightBusinessTravelling;UsernameXfxyh+UsernameXfxyh;DXPD_QW+DXPD_QW;DX_CZ+DX_CZ;FYWHG+FYWHG;AI3050+AI3050;ZHHMD+ZHHMD;UsernameXlxy+UsernameXlxy;SLYH1H1+SLYH1H1;valueflagIsValueless+valueflagIsValueless;student_true+student_true;FAM+FAM;DXPD_A6+DXPD_A6;");
        List<String> convert2 = convert("UsernameQfzf3+UsernameQfzf3;DXPD_CZ1+DXPD_CZ1;NotHighvalueflag+NotHighvalueflag;CAERRQ+CAERRQ;SMRZ+SMRZ;ZSJZH+ZSJZH;ZSJ6_ST+ZSJ6_ST;KYWGM+KYWGM;SQK_SALE_LABEL3+SQK_SALE_LABEL3;MFYHQTWO+MFYHQTWO;DX_CA+DX_CA;ZSJ6_BY+ZSJ6_BY;ZSJ5+ZSJ5;ZSJ6+ZSJ6;GXSCTH+GXSCTH;Safehotelin+Safehotelin;PNEW+PNEW;MFYHQONE+MFYHQONE;ZSJ6HEI+ZSJ6HEI;CAXKLC+CAXKLC;FCZGP+FCZGP;SCGP+SCGP;DXPD_GJ+DXPD_GJ;Gmcaerusername+Gmcaerusername;FHSRY+FHSRY;UNGYCSYDS+UNGYCSYDS;NOCZCKG+NOCZCKG;MFYHQTHERE+MFYHQTHERE;ZHWHZLTY1+ZHWHZLTY1;TWO+TWO;UsernameBxpz+UsernameBxpz;ZHJHMD+ZHJHMD;flightDomPlatformOldValueLow+flightDomPlatformOldValueLow;UsernameFuyingre+UsernameFuyingre;GXHTJ+GXHTJ;DXPD_G5+DXPD_G5;ZHWHZLX+ZHWHZLX;FLD+FLD;DXPD_3U+DXPD_3U;DXPD_TV+DXPD_TV;FYGYCX+FYGYCX;TOUCHHN+TOUCHHN;ZHWHZL+ZHWHZL;PFNEW+PFNEW;FlightFamilyTravelling+FlightFamilyTravelling;QJDBMD+QJDBMD;CZSP1+CZSP1;SAF1_ONE+SAF1_ONE;QZHZBYL20+QZHZBYL20;market_notnew_flight_label+market_notnew_flight_label;UsernameGmlnrqUsername+UsernameGmlnrqUsername;flightnew_midvalue+flightnew_midvalue;ZSJ6_DZ+ZSJ6_DZ;CAXK1+CAXK1;BQXZ_BY+BQXZ_BY;ZHHZRQ+ZHHZRQ;ZSJCA+ZSJCA;twell_new_float+twell_new_float;BMD+BMD;flight_notnewsecnew+flight_notnewsecnew;SAF4+SAF4;YCGP+YCGP;DXPD_KY+DXPD_KY;SAF3+SAF3;HYMBWK+HYMBWK;DX_ZH+DX_ZH;jijiu0rendui+jijiu0rendui;SAF3_ONE+SAF3_ONE;SCSCLJ+SCSCLJ;DXPD_MF+DXPD_MF;ZHZBYL1208+ZHZBYL1208;UserName1350Age+UserName1350Age;DXPD_DZ+DXPD_DZ;DXPD_DR+DXPD_DR;BUYKYVIP+BUYKYVIP;GJIPYLHN+GJIPYLHN;MUHYJB+MUHYJB;ZYZJ+ZYZJ;DXPD_JR+DXPD_JR;DXPD_RY+DXPD_RY;BQXZ+BQXZ;notjingdui+notjingdui;BQXZ_DZ+BQXZ_DZ;GSGDF+GSGDF;flightOldUncixin+flightOldUncixin;DXPD_9C+DXPD_9C;FYCX0DS+FYCX0DS;PD_HSYG+PD_HSYG;DXPD_ZH+DXPD_ZH;UNPAY+UNPAY;YDL+YDL;FlightOld+FlightOld;TSA+TSA;DXPD_KN+DXPD_KN;DXPD+DXPD;ZHNOBUY+ZHNOBUY;Gmoneusername+Gmoneusername;UsernameLyhx+UsernameLyhx;FHSRYHEI+FHSRYHEI;DXPD_BL+DXPD_BL;UsernameTxnl+UsernameTxnl;NotBuySc+NotBuySc;DXPD_BK+DXPD_BK;GJIPYLHD+GJIPYLHD;DXPD_SC+DXPD_SC;BUYQW+BUYQW;ZSJ6jingdui+ZSJ6jingdui;DXPD_HO+DXPD_HO;PTGP+PTGP;DXPD_PN+DXPD_PN;FHN+FHN;market_old_flight_label+market_old_flight_label;ZSJ6_HJHEI+ZSJ6_HJHEI;DXPD_GY+DXPD_GY;ZHA+ZHA;MUBMD+MUBMD;noOpenflower+noOpenflower;KYHY+KYHY;ZHNQH+ZHNQH;UsernameXfxyh+UsernameXfxyh;ZSJ6_HJ+ZSJ6_HJ;DXPD_QW+DXPD_QW;DX_CZ+DX_CZ;BQXZ_HJ+BQXZ_HJ;AI3050+AI3050;ZHHMD+ZHHMD;UsernameXlxy+UsernameXlxy;ZSJ6_HSTZS+ZSJ6_HSTZS;valueflagIsValueless+valueflagIsValueless;SAF4_ONE+SAF4_ONE;strongpeople+strongpeople;FAM+FAM;LSYH+LSYH;DXPD_A6+DXPD_A6;");

        Set<String> convert1Set = new LinkedHashSet<>(convert1);
        Set<String> convert2Set = new LinkedHashSet<>(convert2);

        List<String> onlyInConvert1 = new ArrayList<>(convert1Set);
        onlyInConvert1.removeAll(convert2Set);

        List<String> onlyInConvert2 = new ArrayList<>(convert2Set);
        onlyInConvert2.removeAll(convert1Set);

        System.out.println("only in convert1: " + onlyInConvert1);
        System.out.println("only in convert2: " + onlyInConvert2);

    }
}
